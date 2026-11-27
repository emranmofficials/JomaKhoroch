package com.example

import com.example.core.localization.NumeralStyle
import com.example.core.util.CurrencyFormatter
import com.example.domain.accounting.AccountingEngine
import com.example.domain.accounting.BudgetStatus
import com.example.domain.accounting.LedgerEntryRecord
import com.example.domain.accounting.LedgerTransactionType
import com.example.domain.accounting.LoanDirection
import com.example.domain.accounting.LoanPrincipalRecord
import com.example.domain.accounting.ShopOpeningRecord
import com.example.domain.accounting.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountingEngineTest {

    @Test
    fun `example 1 - shop credit purchase increases expense and shop payable without decreasing cash`() {
        val shop = ShopOpeningRecord(shopId = 1L, openingBalancePoisha = 0L)
        val entries = listOf(
            LedgerEntryRecord(
                id = 1L,
                notebookId = 1L,
                type = LedgerTransactionType.SHOP_PURCHASE,
                totalAmountPoisha = 100_000L, // ৳1,000
                paidAmountPoisha = 0L,
                dueAmountPoisha = 100_000L,
                categoryId = null,
                shopId = 1L,
                loanId = null,
                transactionDate = 1000L
            )
        )
        val summary = AccountingEngine.computeBookSummary(
            openingBalancePoisha = 500_000L, // ৳5,000 initial cash
            shops = listOf(shop),
            loans = emptyList(),
            entries = entries
        )

        assertEquals(100_000L, summary.totalExpensePoisha)
        assertEquals(100_000L, summary.totalShopPayablePoisha)
        assertEquals(500_000L, summary.currentCashBalancePoisha) // Cash unchanged
    }

    @Test
    fun `example 2 - partial shop due payment reduces due and cash without double counting expense`() {
        val shop = ShopOpeningRecord(shopId = 1L, openingBalancePoisha = 0L)
        val entries = listOf(
            LedgerEntryRecord(
                id = 1L,
                notebookId = 1L,
                type = LedgerTransactionType.SHOP_PURCHASE,
                totalAmountPoisha = 100_000L, // ৳1,000
                paidAmountPoisha = 0L,
                dueAmountPoisha = 100_000L,
                categoryId = null,
                shopId = 1L,
                loanId = null,
                transactionDate = 1000L
            ),
            LedgerEntryRecord(
                id = 2L,
                notebookId = 1L,
                type = LedgerTransactionType.SHOP_DUE_PAYMENT,
                totalAmountPoisha = 40_000L, // ৳400
                paidAmountPoisha = 40_000L,
                dueAmountPoisha = 0L,
                categoryId = null,
                shopId = 1L,
                loanId = null,
                transactionDate = 2000L
            )
        )

        val summary = AccountingEngine.computeBookSummary(
            openingBalancePoisha = 500_000L,
            shops = listOf(shop),
            loans = emptyList(),
            entries = entries
        )

        assertEquals(60_000L, summary.totalShopPayablePoisha) // ৳600 remaining due
        assertEquals(460_000L, summary.currentCashBalancePoisha) // ৳5,000 - ৳400 = ৳4,600
        assertEquals(100_000L, summary.totalExpensePoisha) // Still ৳1,000 — NO double counting!
    }

    @Test
    fun `example 3 - cash purchase increases expense and decreases cash with zero payable`() {
        val shop = ShopOpeningRecord(shopId = 1L, openingBalancePoisha = 0L)
        val entries = listOf(
            LedgerEntryRecord(
                id = 1L,
                notebookId = 1L,
                type = LedgerTransactionType.SHOP_PURCHASE,
                totalAmountPoisha = 50_000L, // ৳500
                paidAmountPoisha = 50_000L,
                dueAmountPoisha = 0L,
                categoryId = null,
                shopId = 1L,
                loanId = null,
                transactionDate = 1000L
            )
        )

        val summary = AccountingEngine.computeBookSummary(
            openingBalancePoisha = 200_000L,
            shops = listOf(shop),
            loans = emptyList(),
            entries = entries
        )

        assertEquals(50_000L, summary.totalExpensePoisha)
        assertEquals(150_000L, summary.currentCashBalancePoisha)
        assertEquals(0L, summary.totalShopPayablePoisha)
    }

    @Test
    fun `example 4 - lending money decreases cash and increases receivable without increasing expense`() {
        val loan = LoanPrincipalRecord(loanId = 10L, direction = LoanDirection.GIVEN, principalPoisha = 200_000L)
        val entries = listOf(
            LedgerEntryRecord(
                id = 1L,
                notebookId = 1L,
                type = LedgerTransactionType.LENDING_GIVEN,
                totalAmountPoisha = 200_000L, // ৳2,000
                paidAmountPoisha = 200_000L,
                dueAmountPoisha = 0L,
                categoryId = null,
                shopId = null,
                loanId = 10L,
                transactionDate = 1000L
            )
        )

        val summary = AccountingEngine.computeBookSummary(
            openingBalancePoisha = 500_000L,
            shops = emptyList(),
            loans = listOf(loan),
            entries = entries
        )

        assertEquals(300_000L, summary.currentCashBalancePoisha)
        assertEquals(200_000L, summary.totalReceivablePoisha)
        assertEquals(0L, summary.totalExpensePoisha)
    }

    @Test
    fun `example 5 - borrowing money increases cash and payable without increasing income`() {
        val loan = LoanPrincipalRecord(loanId = 20L, direction = LoanDirection.BORROWED, principalPoisha = 300_000L)
        val entries = listOf(
            LedgerEntryRecord(
                id = 1L,
                notebookId = 1L,
                type = LedgerTransactionType.BORROWING_TAKEN,
                totalAmountPoisha = 300_000L, // ৳3,000
                paidAmountPoisha = 300_000L,
                dueAmountPoisha = 0L,
                categoryId = null,
                shopId = null,
                loanId = 20L,
                transactionDate = 1000L
            )
        )

        val summary = AccountingEngine.computeBookSummary(
            openingBalancePoisha = 100_000L,
            shops = emptyList(),
            loans = listOf(loan),
            entries = entries
        )

        assertEquals(400_000L, summary.currentCashBalancePoisha)
        assertEquals(300_000L, summary.totalPersonalPayablePoisha)
        assertEquals(0L, summary.totalIncomePoisha)
        assertEquals(0L, summary.totalShopPayablePoisha)
    }

    @Test
    fun `validation rejects negative amounts and overpayments`() {
        assertTrue(AccountingEngine.validatePositiveAmount(0L) is ValidationResult.Invalid)
        assertTrue(AccountingEngine.validatePositiveAmount(-100L) is ValidationResult.Invalid)
        assertTrue(AccountingEngine.validateShopPurchase(50_000L, 60_000L) is ValidationResult.Invalid)
        assertTrue(AccountingEngine.validateSettlement(70_000L, 60_000L) is ValidationResult.Invalid)
        assertEquals(ValidationResult.Valid, AccountingEngine.validateSettlement(60_000L, 60_000L))
    }

    @Test
    fun `budget status reflects healthy warning and exceeded thresholds`() {
        val healthy = AccountingEngine.computeBudgetProgress(100_000L, 50_000L)
        val warning = AccountingEngine.computeBudgetProgress(100_000L, 85_000L)
        val exceeded = AccountingEngine.computeBudgetProgress(100_000L, 120_000L)

        assertEquals(BudgetStatus.HEALTHY, healthy.status)
        assertEquals(BudgetStatus.WARNING, warning.status)
        assertEquals(BudgetStatus.EXCEEDED, exceeded.status)
        assertEquals(-20_000L, exceeded.remainingPoisha)
    }

    @Test
    fun `currency parser and formatter use integer poisha with Bangla and English numerals`() {
        assertEquals(50_050L, CurrencyFormatter.parseTakaToPoisha("500.50"))
        assertEquals(50_050L, CurrencyFormatter.parseTakaToPoisha("৫০০.৫০"))
        assertNull(CurrencyFormatter.parseTakaToPoisha("-100"))
        assertNull(CurrencyFormatter.parseTakaToPoisha("abc"))

        assertEquals("৳১,২৫,০০০", CurrencyFormatter.formatPoisha(12_500_000L, NumeralStyle.BANGLA))
        assertEquals("৳1,25,000.50", CurrencyFormatter.formatPoisha(12_500_050L, NumeralStyle.ENGLISH))

        // 0.5 kg * ৳120/kg = ৳60 (6000 poisha) using pure integer math
        val qtyMillis = CurrencyFormatter.parseQuantityToMillis("0.5")!!
        val lineTotal = CurrencyFormatter.calculateItemTotalPoisha(qtyMillis, 12_000L)
        assertEquals(6_000L, lineTotal)
    }
}
