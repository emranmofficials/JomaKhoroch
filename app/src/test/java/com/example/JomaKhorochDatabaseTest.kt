package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.JomaKhorochDatabase
import com.example.data.local.entity.ShopEntity
import com.example.data.repository.JomaKhorochRepository
import com.example.data.repository.PurchaseLineItemInput
import com.example.data.repository.toLedgerRecord
import com.example.domain.accounting.AccountingEngine
import com.example.domain.accounting.LedgerTransactionType
import com.example.domain.accounting.LoanDirection
import com.example.domain.accounting.LoanPrincipalRecord
import com.example.domain.accounting.ShopOpeningRecord
import com.example.infrastructure.EncryptedBackupManager
import com.example.infrastructure.PinSecurityManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class JomaKhorochDatabaseTest {

    private lateinit var db: JomaKhorochDatabase
    private lateinit var repo: JomaKhorochRepository
    private lateinit var backupManager: EncryptedBackupManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, JomaKhorochDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = JomaKhorochRepository(db)
        backupManager = EncryptedBackupManager(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `seeding creates 3 default books with zero fake transactions`() = runTest {
        val defaultBookId = repo.ensureInitialized()
        val books = repo.observeActiveNotebooks().first()
        val txs = repo.observeTransactions(defaultBookId).first()

        assertEquals(3, books.size)
        assertTrue(txs.isEmpty())
    }

    @Test
    fun `end to end shop workflow and multi-notebook isolation`() = runTest {
        repo.ensureInitialized()
        val books = repo.observeActiveNotebooks().first()
        val personalId = books[0].id
        val businessId = books[2].id

        // Add salary income in Personal book
        repo.recordIncomeOrExpense(
            notebookId = personalId,
            type = LedgerTransactionType.INCOME,
            amountPoisha = 1_000_000L, // ৳10,000
            categoryId = null,
            paymentMethod = "CASH",
            note = "Salary",
            transactionDate = 1000L
        ).getOrThrow()

        // Create Rahim Store in Personal book and add itemized credit purchase of ৳1,000 (paid ৳0)
        val shopId = repo.saveShop(
            ShopEntity(notebookId = personalId, name = "Rahim Store", ownerName = "Rahim")
        ).getOrThrow()

        val items = listOf(
            PurchaseLineItemInput("Rice", 5000L, "kg", 8_000L, 40_000L), // ৳400
            PurchaseLineItemInput("Oil", 3000L, "L", 20_000L, 60_000L) // ৳600
        )
        val purchaseTxId = repo.recordShopPurchase(
            notebookId = personalId,
            shopId = shopId,
            totalAmountPoisha = 100_000L,
            paidAmountPoisha = 0L,
            categoryId = null,
            paymentMethod = "CASH",
            note = "Grocery Shopping",
            transactionDate = 2000L,
            items = items
        ).getOrThrow()

        // Make partial payment of ৳400
        repo.recordShopDuePayment(
            notebookId = personalId,
            shopId = shopId,
            paymentPoisha = 40_000L,
            paymentMethod = "CASH",
            note = "Partial due payment",
            transactionDate = 3000L
        ).getOrThrow()

        val personalTxs = repo.observeTransactions(personalId).first()
        val businessTxs = repo.observeTransactions(businessId).first()
        val savedItems = repo.observeItemsForTransaction(purchaseTxId).first()

        // Verify strict notebook isolation
        assertEquals(3, personalTxs.size)
        assertTrue(businessTxs.isEmpty())
        assertEquals(2, savedItems.size)

        // Verify exact accounting state
        val summary = AccountingEngine.computeBookSummary(
            openingBalancePoisha = 0L,
            shops = listOf(ShopOpeningRecord(shopId, 0L)),
            loans = emptyList(),
            entries = personalTxs.map { it.toLedgerRecord() }
        )
        assertEquals(1_000_000L, summary.totalIncomePoisha)
        assertEquals(100_000L, summary.totalExpensePoisha) // Expense NOT double counted
        assertEquals(60_000L, summary.totalShopPayablePoisha) // ৳600 remaining due
        assertEquals(960_000L, summary.currentCashBalancePoisha) // ৳10,000 - ৳400
    }

    @Test
    fun `encrypted backup and restore preserves full database and rejects wrong passphrase`() = runTest {
        val bookId = repo.ensureInitialized()
        repo.createPersonalLoan(
            notebookId = bookId,
            direction = LoanDirection.GIVEN,
            personName = "Karim",
            phone = "01700000000",
            principalPoisha = 200_000L,
            loanDate = 1000L,
            dueDate = null,
            description = "Emergency loan",
            paymentMethod = "CASH"
        ).getOrThrow()

        val export = backupManager.createEncryptedBackup("secret1234").getOrThrow()

        // Wrong passphrase must fail cleanly
        val wrongResult = backupManager.inspectEncryptedBackup(export.encryptedBytes, "wrongpass")
        assertTrue(wrongResult.isFailure)

        // Wipe database completely
        db.notebookDao().deleteAll()
        assertTrue(repo.observeActiveNotebooks().first().isEmpty())

        // Inspect & Restore with correct passphrase
        val preview = backupManager.inspectEncryptedBackup(export.encryptedBytes, "secret1234").getOrThrow()
        assertEquals(3, preview.notebookCount)
        assertEquals(1, preview.loanCount)

        backupManager.restoreFromPreview(preview, "test.jkbackup").getOrThrow()
        val restoredLoans = repo.observeLoans(bookId).first()
        assertEquals(1, restoredLoans.size)
        assertEquals("Karim", restoredLoans.first().personName)

        // Verify restored loan balance
        val restoredTxs = repo.observeTransactions(bookId).first().map { it.toLedgerRecord() }
        val loanBalance = AccountingEngine.computeLoanBalance(
            LoanPrincipalRecord(restoredLoans.first().id, LoanDirection.GIVEN, restoredLoans.first().principalPoisha),
            restoredTxs
        )
        assertEquals(200_000L, loanBalance.remainingPoisha)
    }

    @Test
    fun `transaction edit creates immutable audit log and PIN hashing verifies correctly`() = runTest {
        val bookId = repo.ensureInitialized()
        val txId = repo.recordIncomeOrExpense(
            notebookId = bookId,
            type = LedgerTransactionType.EXPENSE,
            amountPoisha = 50_000L,
            categoryId = null,
            paymentMethod = "CASH",
            note = "Bus fare",
            transactionDate = 1000L
        ).getOrThrow()

        repo.updateTransactionWithAudit(
            txId = txId,
            newTotalPoisha = 55_000L,
            newPaidPoisha = 55_000L,
            newNote = "Bus fare corrected",
            newCategoryId = null,
            newPaymentMethod = "CASH",
            newDate = 1000L,
            reason = "Typo fix"
        ).getOrThrow()

        val logs = repo.observeAuditLogs(bookId).first()
        assertEquals(1, logs.size)
        assertEquals("EDIT", logs.first().action)
        assertEquals("Typo fix", logs.first().reason)

        // Verify salted PBKDF2 PIN hashing
        val hashed = PinSecurityManager.hashPin("2580")
        assertTrue(PinSecurityManager.verifyPin("2580", hashed.saltBase64, hashed.hashBase64))
        assertFalse(PinSecurityManager.verifyPin("0000", hashed.saltBase64, hashed.hashBase64))
    }
}
