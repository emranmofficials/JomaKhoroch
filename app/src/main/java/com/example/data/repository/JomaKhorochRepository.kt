package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.DatabaseSeeder
import com.example.data.local.JomaKhorochDatabase
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.BackupHistoryEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.LedgerTransactionEntity
import com.example.data.local.entity.NoteAttachmentEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.NotebookEntity
import com.example.data.local.entity.PersonalLoanEntity
import com.example.data.local.entity.ShopEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TransactionItemEntity
import com.example.domain.accounting.AccountingEngine
import com.example.domain.accounting.LedgerEntryRecord
import com.example.domain.accounting.LedgerTransactionType
import com.example.domain.accounting.LoanDirection
import com.example.domain.accounting.LoanPrincipalRecord
import com.example.domain.accounting.ShopOpeningRecord
import com.example.domain.accounting.ValidationResult
import kotlinx.coroutines.flow.Flow

data class PurchaseLineItemInput(
    val productName: String,
    val quantityMillis: Long,
    val unit: String,
    val unitPricePoisha: Long,
    val lineTotalPoisha: Long
)

class JomaKhorochRepository(val db: JomaKhorochDatabase) {

    // Reactive Flows
    fun observeActiveNotebooks(): Flow<List<NotebookEntity>> = db.notebookDao().observeActiveNotebooks()
    fun observeAllNotebooks(): Flow<List<NotebookEntity>> = db.notebookDao().observeAllNotebooks()
    fun observeCategories(notebookId: Long): Flow<List<CategoryEntity>> = db.categoryDao().observeCategories(notebookId)
    fun observeShops(notebookId: Long): Flow<List<ShopEntity>> = db.shopDao().observeShops(notebookId)
    fun observeLoans(notebookId: Long): Flow<List<PersonalLoanEntity>> = db.loanDao().observeLoans(notebookId)
    fun observeTransactions(notebookId: Long): Flow<List<LedgerTransactionEntity>> =
        db.transactionDao().observeActiveTransactions(notebookId)
    fun observeAllActiveTransactions(): Flow<List<LedgerTransactionEntity>> =
        db.transactionDao().observeAllActiveTransactions()
    fun observeItemsForTransaction(txId: Long): Flow<List<TransactionItemEntity>> =
        db.transactionDao().observeItemsForTransaction(txId)
    fun observeBudgetsForMonth(notebookId: Long, yearMonth: String): Flow<List<BudgetEntity>> =
        db.budgetDao().observeBudgetsForMonth(notebookId, yearMonth)
    fun observeAllBudgetsForBook(notebookId: Long): Flow<List<BudgetEntity>> =
        db.budgetDao().observeAllBudgetsForBook(notebookId)
    fun observeNotes(notebookId: Long): Flow<List<NoteEntity>> = db.diaryDao().observeNotes(notebookId)
    fun observeAttachments(): Flow<List<NoteAttachmentEntity>> = db.diaryDao().observeAllAttachments()
    fun observeTasks(notebookId: Long): Flow<List<TaskEntity>> = db.diaryDao().observeTasks(notebookId)
    fun observeAuditLogs(notebookId: Long): Flow<List<AuditLogEntity>> = db.systemDao().observeAuditLogs(notebookId)
    fun observeBackupHistory(): Flow<List<BackupHistoryEntity>> = db.systemDao().observeBackupHistory()

    suspend fun ensureInitialized(): Long = DatabaseSeeder.ensureSeeded(db)

    // Account Books (Module 05)
    suspend fun createNotebook(
        name: String,
        type: String,
        iconKey: String,
        openingBalancePoisha: Long
    ): Result<Long> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("REQUIRED_FIELD"))
        if (AccountingEngine.validateNonNegativeAmount(openingBalancePoisha) is ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException("AMOUNT_CANNOT_BE_NEGATIVE"))
        }
        val id = db.withTransaction {
            val bookId = db.notebookDao().insert(
                NotebookEntity(
                    name = name.trim(),
                    type = type,
                    iconKey = iconKey,
                    openingBalancePoisha = openingBalancePoisha
                )
            )
            db.categoryDao().insertAll(DatabaseSeeder.defaultCategoriesForBook(bookId))
            bookId
        }
        return Result.success(id)
    }

    suspend fun updateNotebookOpeningBalance(notebookId: Long, openingBalancePoisha: Long): Result<Unit> {
        if (openingBalancePoisha < 0L) return Result.failure(IllegalArgumentException("AMOUNT_CANNOT_BE_NEGATIVE"))
        val existing = db.notebookDao().getNotebookById(notebookId)
            ?: return Result.failure(IllegalStateException("BOOK_NOT_FOUND"))
        db.withTransaction {
            db.notebookDao().update(existing.copy(openingBalancePoisha = openingBalancePoisha))
            db.systemDao().insertAuditLog(
                AuditLogEntity(
                    notebookId = notebookId,
                    entityType = "BOOK",
                    entityId = notebookId,
                    action = "EDIT",
                    previousSnapshot = "opening=${existing.openingBalancePoisha}",
                    newSnapshot = "opening=$openingBalancePoisha",
                    reason = "Opening balance updated"
                )
            )
        }
        return Result.success(Unit)
    }

    suspend fun archiveNotebook(notebookId: Long): Result<Long> {
        val activeCount = db.notebookDao().countActiveNotebooks()
        if (activeCount <= 1) return Result.failure(IllegalStateException("CANNOT_DELETE_LAST_BOOK"))
        return db.withTransaction {
            val book = db.notebookDao().getNotebookById(notebookId)
                ?: return@withTransaction Result.failure(IllegalStateException("BOOK_NOT_FOUND"))
            db.notebookDao().setArchived(notebookId, true)
            db.systemDao().insertAuditLog(
                AuditLogEntity(
                    notebookId = notebookId,
                    entityType = "BOOK",
                    entityId = notebookId,
                    action = "ARCHIVE",
                    previousSnapshot = book.name,
                    newSnapshot = "ARCHIVED",
                    reason = "Archived by user"
                )
            )
            val nextActive = db.notebookDao().getAllNotebooksOnce().first { !it.isArchived }.id
            Result.success(nextActive)
        }
    }

    suspend fun permanentlyDeleteNotebook(notebookId: Long): Result<Long> {
        val activeCount = db.notebookDao().countActiveNotebooks()
        val book = db.notebookDao().getNotebookById(notebookId)
            ?: return Result.failure(IllegalStateException("BOOK_NOT_FOUND"))
        if (!book.isArchived && activeCount <= 1) {
            return Result.failure(IllegalStateException("CANNOT_DELETE_LAST_BOOK"))
        }
        return db.withTransaction {
            db.notebookDao().deleteById(notebookId)
            val nextActive = db.notebookDao().getAllNotebooksOnce().first { !it.isArchived }.id
            Result.success(nextActive)
        }
    }

    // Categories (Module 04)
    suspend fun addCustomCategory(
        notebookId: Long,
        nameBn: String,
        nameEn: String,
        type: String,
        colorHex: String = "#176B45"
    ): Result<Long> {
        val trimmedBn = nameBn.trim().ifEmpty { nameEn.trim() }
        val trimmedEn = nameEn.trim().ifEmpty { nameBn.trim() }
        if (trimmedBn.isEmpty()) return Result.failure(IllegalArgumentException("REQUIRED_FIELD"))
        val id = db.categoryDao().insert(
            CategoryEntity(
                notebookId = notebookId,
                nameBn = trimmedBn,
                nameEn = trimmedEn,
                type = type,
                iconKey = if (type == "INCOME") "payments" else "label",
                colorHex = colorHex,
                isDefault = false
            )
        )
        return Result.success(id)
    }

    suspend fun deleteCategory(categoryId: Long) {
        db.categoryDao().softDelete(categoryId)
    }

    // Income & Expense (Module 04)
    suspend fun recordIncomeOrExpense(
        notebookId: Long,
        type: LedgerTransactionType,
        amountPoisha: Long,
        categoryId: Long?,
        paymentMethod: String,
        note: String,
        transactionDate: Long
    ): Result<Long> {
        require(type == LedgerTransactionType.INCOME || type == LedgerTransactionType.EXPENSE)
        if (AccountingEngine.validatePositiveAmount(amountPoisha) is ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException("AMOUNT_MUST_BE_POSITIVE"))
        }
        val id = db.transactionDao().insert(
            LedgerTransactionEntity(
                notebookId = notebookId,
                type = type.name,
                totalAmountPoisha = amountPoisha,
                paidAmountPoisha = amountPoisha,
                dueAmountPoisha = 0L,
                categoryId = categoryId,
                paymentMethod = paymentMethod,
                note = note.trim(),
                transactionDate = transactionDate
            )
        )
        return Result.success(id)
    }

    // Shop Due Management (Module 02)
    suspend fun saveShop(shop: ShopEntity): Result<Long> {
        if (shop.name.isBlank()) return Result.failure(IllegalArgumentException("REQUIRED_FIELD"))
        if (shop.openingBalancePoisha < 0L) {
            return Result.failure(IllegalArgumentException("AMOUNT_CANNOT_BE_NEGATIVE"))
        }
        val clean = shop.copy(name = shop.name.trim())
        return if (clean.id > 0L) {
            db.shopDao().update(clean)
            Result.success(clean.id)
        } else {
            Result.success(db.shopDao().insert(clean))
        }
    }

    suspend fun recordShopPurchase(
        notebookId: Long,
        shopId: Long,
        totalAmountPoisha: Long,
        paidAmountPoisha: Long,
        categoryId: Long?,
        paymentMethod: String,
        note: String,
        transactionDate: Long,
        items: List<PurchaseLineItemInput> = emptyList()
    ): Result<Long> {
        val computedTotal = if (items.isNotEmpty()) items.sumOf { it.lineTotalPoisha } else totalAmountPoisha
        val validation = AccountingEngine.validateShopPurchase(computedTotal, paidAmountPoisha)
        if (validation is ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException(validation.errorCode))
        }
        val dueAmount = computedTotal - paidAmountPoisha
        val txId = db.withTransaction {
            val insertedTxId = db.transactionDao().insert(
                LedgerTransactionEntity(
                    notebookId = notebookId,
                    type = LedgerTransactionType.SHOP_PURCHASE.name,
                    totalAmountPoisha = computedTotal,
                    paidAmountPoisha = paidAmountPoisha,
                    dueAmountPoisha = dueAmount,
                    categoryId = categoryId,
                    shopId = shopId,
                    paymentMethod = paymentMethod,
                    note = note.trim(),
                    transactionDate = transactionDate
                )
            )
            if (items.isNotEmpty()) {
                db.transactionDao().insertItems(
                    items.map {
                        TransactionItemEntity(
                            transactionId = insertedTxId,
                            productName = it.productName.trim(),
                            quantityMillis = it.quantityMillis,
                            unit = it.unit.trim().ifEmpty { "pcs" },
                            unitPricePoisha = it.unitPricePoisha,
                            lineTotalPoisha = it.lineTotalPoisha
                        )
                    }
                )
            }
            insertedTxId
        }
        return Result.success(txId)
    }

    suspend fun recordShopDuePayment(
        notebookId: Long,
        shopId: Long,
        paymentPoisha: Long,
        paymentMethod: String,
        note: String,
        transactionDate: Long
    ): Result<Long> {
        return db.withTransaction {
            val shop = db.shopDao().getShopById(shopId)
                ?: return@withTransaction Result.failure(IllegalStateException("SHOP_NOT_FOUND"))
            val entries = db.transactionDao().getActiveTransactionsByBookOnce(notebookId).map { it.toLedgerRecord() }
            val currentDue = AccountingEngine.computeShopBalance(
                ShopOpeningRecord(shop.id, shop.openingBalancePoisha),
                entries
            ).outstandingDuePoisha
            val validation = AccountingEngine.validateSettlement(paymentPoisha, currentDue)
            if (validation is ValidationResult.Invalid) {
                return@withTransaction Result.failure(IllegalArgumentException(validation.errorCode))
            }
            val txId = db.transactionDao().insert(
                LedgerTransactionEntity(
                    notebookId = notebookId,
                    type = LedgerTransactionType.SHOP_DUE_PAYMENT.name,
                    totalAmountPoisha = paymentPoisha,
                    paidAmountPoisha = paymentPoisha,
                    dueAmountPoisha = 0L,
                    shopId = shopId,
                    paymentMethod = paymentMethod,
                    note = note.trim(),
                    transactionDate = transactionDate
                )
            )
            Result.success(txId)
        }
    }

    // Personal Lending & Borrowing (Module 03 — strictly separated from shops)
    suspend fun createPersonalLoan(
        notebookId: Long,
        direction: LoanDirection,
        personName: String,
        phone: String,
        principalPoisha: Long,
        loanDate: Long,
        dueDate: Long?,
        description: String,
        paymentMethod: String
    ): Result<Long> {
        if (personName.isBlank()) return Result.failure(IllegalArgumentException("REQUIRED_FIELD"))
        if (AccountingEngine.validatePositiveAmount(principalPoisha) is ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException("AMOUNT_MUST_BE_POSITIVE"))
        }
        val txType = if (direction == LoanDirection.GIVEN) {
            LedgerTransactionType.LENDING_GIVEN
        } else {
            LedgerTransactionType.BORROWING_TAKEN
        }
        val loanId = db.withTransaction {
            val createdLoanId = db.loanDao().insert(
                PersonalLoanEntity(
                    notebookId = notebookId,
                    direction = direction.name,
                    personName = personName.trim(),
                    phone = phone.trim(),
                    principalPoisha = principalPoisha,
                    loanDate = loanDate,
                    dueDate = dueDate,
                    description = description.trim(),
                    isSettled = false
                )
            )
            db.transactionDao().insert(
                LedgerTransactionEntity(
                    notebookId = notebookId,
                    type = txType.name,
                    totalAmountPoisha = principalPoisha,
                    paidAmountPoisha = principalPoisha,
                    dueAmountPoisha = 0L,
                    loanId = createdLoanId,
                    paymentMethod = paymentMethod,
                    note = description.trim().ifEmpty { personName.trim() },
                    transactionDate = loanDate
                )
            )
            createdLoanId
        }
        return Result.success(loanId)
    }

    suspend fun recordLoanRepayment(
        notebookId: Long,
        loanId: Long,
        paymentPoisha: Long,
        paymentMethod: String,
        note: String,
        transactionDate: Long
    ): Result<Long> {
        return db.withTransaction {
            val loan = db.loanDao().getLoanById(loanId)
                ?: return@withTransaction Result.failure(IllegalStateException("LOAN_NOT_FOUND"))
            val direction = LoanDirection.valueOf(loan.direction)
            val entries = db.transactionDao().getActiveTransactionsByBookOnce(notebookId).map { it.toLedgerRecord() }
            val currentRemaining = AccountingEngine.computeLoanBalance(
                LoanPrincipalRecord(loan.id, direction, loan.principalPoisha),
                entries
            ).remainingPoisha
            val validation = AccountingEngine.validateSettlement(paymentPoisha, currentRemaining)
            if (validation is ValidationResult.Invalid) {
                return@withTransaction Result.failure(IllegalArgumentException(validation.errorCode))
            }
            val repayType = if (direction == LoanDirection.GIVEN) {
                LedgerTransactionType.LENDING_REPAYMENT
            } else {
                LedgerTransactionType.BORROWING_REPAYMENT
            }
            val txId = db.transactionDao().insert(
                LedgerTransactionEntity(
                    notebookId = notebookId,
                    type = repayType.name,
                    totalAmountPoisha = paymentPoisha,
                    paidAmountPoisha = paymentPoisha,
                    dueAmountPoisha = 0L,
                    loanId = loanId,
                    paymentMethod = paymentMethod,
                    note = note.trim(),
                    transactionDate = transactionDate
                )
            )
            if (currentRemaining - paymentPoisha == 0L) {
                db.loanDao().updateSettled(loanId, true)
            }
            Result.success(txId)
        }
    }

    // Transaction Correction & Soft Delete with Audit Trail
    suspend fun updateTransactionWithAudit(
        txId: Long,
        newTotalPoisha: Long,
        newPaidPoisha: Long,
        newNote: String,
        newCategoryId: Long?,
        newPaymentMethod: String,
        newDate: Long,
        reason: String
    ): Result<Unit> {
        return db.withTransaction {
            val existing = db.transactionDao().getById(txId)
                ?: return@withTransaction Result.failure(IllegalStateException("TX_NOT_FOUND"))
            val txType = LedgerTransactionType.valueOf(existing.type)
            val validation = if (txType == LedgerTransactionType.SHOP_PURCHASE) {
                AccountingEngine.validateShopPurchase(newTotalPoisha, newPaidPoisha)
            } else {
                AccountingEngine.validatePositiveAmount(newTotalPoisha)
            }
            if (validation is ValidationResult.Invalid) {
                return@withTransaction Result.failure(IllegalArgumentException(validation.errorCode))
            }
            val effectivePaid = if (txType == LedgerTransactionType.SHOP_PURCHASE) newPaidPoisha else newTotalPoisha
            val effectiveDue = if (txType == LedgerTransactionType.SHOP_PURCHASE) newTotalPoisha - newPaidPoisha else 0L
            val updated = existing.copy(
                totalAmountPoisha = newTotalPoisha,
                paidAmountPoisha = effectivePaid,
                dueAmountPoisha = effectiveDue,
                note = newNote.trim(),
                categoryId = newCategoryId,
                paymentMethod = newPaymentMethod,
                transactionDate = newDate,
                updatedAt = System.currentTimeMillis()
            )
            db.transactionDao().update(updated)
            existing.loanId?.let { loanId ->
                val loan = db.loanDao().getLoanById(loanId)
                if (loan != null) {
                    val direction = LoanDirection.valueOf(loan.direction)
                    val updatedLoan = if (txType == LedgerTransactionType.LENDING_GIVEN ||
                        txType == LedgerTransactionType.BORROWING_TAKEN
                    ) {
                        loan.copy(principalPoisha = newTotalPoisha)
                    } else {
                        loan
                    }
                    val entries = db.transactionDao()
                        .getActiveTransactionsByBookOnce(existing.notebookId)
                        .map { it.toLedgerRecord() }
                    val balance = AccountingEngine.computeLoanBalance(
                        LoanPrincipalRecord(updatedLoan.id, direction, updatedLoan.principalPoisha),
                        entries
                    )
                    db.loanDao().update(updatedLoan.copy(isSettled = balance.isSettled))
                }
            }
            db.systemDao().insertAuditLog(
                AuditLogEntity(
                    notebookId = existing.notebookId,
                    entityType = "TRANSACTION",
                    entityId = existing.id,
                    action = "EDIT",
                    previousSnapshot = "type=${existing.type},total=${existing.totalAmountPoisha},paid=${existing.paidAmountPoisha},note=${existing.note}",
                    newSnapshot = "type=${updated.type},total=${updated.totalAmountPoisha},paid=${updated.paidAmountPoisha},note=${updated.note}",
                    reason = reason.trim().ifEmpty { "Manual correction" }
                )
            )
            Result.success(Unit)
        }
    }

    suspend fun softDeleteTransactionWithAudit(txId: Long, reason: String = "Deleted by user"): Result<Unit> {
        return db.withTransaction {
            val existing = db.transactionDao().getById(txId)
                ?: return@withTransaction Result.failure(IllegalStateException("TX_NOT_FOUND"))
            val now = System.currentTimeMillis()
            db.transactionDao().softDelete(txId, now)
            // Keep PersonalLoanEntity in sync when a principal or repayment transaction is deleted
            existing.loanId?.let { loanId ->
                val loan = db.loanDao().getLoanById(loanId)
                if (loan != null) {
                    when (existing.type) {
                        LedgerTransactionType.LENDING_GIVEN.name,
                        LedgerTransactionType.BORROWING_TAKEN.name -> {
                            db.loanDao().update(loan.copy(principalPoisha = 0L, isSettled = true))
                        }
                        LedgerTransactionType.LENDING_REPAYMENT.name,
                        LedgerTransactionType.BORROWING_REPAYMENT.name -> {
                            db.loanDao().updateSettled(loanId, false)
                        }
                    }
                }
            }
            db.systemDao().insertAuditLog(
                AuditLogEntity(
                    notebookId = existing.notebookId,
                    entityType = "TRANSACTION",
                    entityId = existing.id,
                    action = "DELETE",
                    previousSnapshot = "type=${existing.type},total=${existing.totalAmountPoisha},paid=${existing.paidAmountPoisha},note=${existing.note}",
                    newSnapshot = "DELETED",
                    reason = reason
                )
            )
            Result.success(Unit)
        }
    }

    // Budgets (Module 08)
    suspend fun upsertBudget(
        notebookId: Long,
        yearMonth: String,
        categoryId: Long?,
        limitPoisha: Long
    ): Result<Long> {
        if (AccountingEngine.validatePositiveAmount(limitPoisha) is ValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException("AMOUNT_MUST_BE_POSITIVE"))
        }
        val existing = db.budgetDao().findBudget(notebookId, yearMonth, categoryId)
        val id = if (existing != null) {
            db.budgetDao().update(existing.copy(limitPoisha = limitPoisha, updatedAt = System.currentTimeMillis()))
            existing.id
        } else {
            db.budgetDao().insert(
                BudgetEntity(
                    notebookId = notebookId,
                    yearMonth = yearMonth,
                    categoryId = categoryId,
                    limitPoisha = limitPoisha
                )
            )
        }
        return Result.success(id)
    }

    suspend fun deleteBudget(budgetId: Long) {
        db.budgetDao().deleteById(budgetId)
    }

    // Digital Pocket Diary (Module 06)
    suspend fun saveNote(note: NoteEntity): Long {
        val updated = note.copy(updatedAt = System.currentTimeMillis())
        return if (updated.id > 0L) {
            db.diaryDao().updateNote(updated)
            updated.id
        } else {
            db.diaryDao().insertNote(updated)
        }
    }

    suspend fun togglePinNote(note: NoteEntity) {
        db.diaryDao().updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNote(noteId: Long) {
        db.diaryDao().deleteNoteById(noteId)
    }

    suspend fun addNoteAttachment(noteId: Long, filePath: String, caption: String = ""): Long {
        return db.diaryDao().insertAttachment(
            NoteAttachmentEntity(noteId = noteId, filePath = filePath, caption = caption)
        )
    }

    suspend fun deleteNoteAttachment(attachmentId: Long) {
        db.diaryDao().deleteAttachmentById(attachmentId)
    }

    // Tasks & Reminders (Module 07)
    suspend fun saveTask(task: TaskEntity): Long {
        return if (task.id > 0L) {
            db.diaryDao().updateTask(task)
            task.id
        } else {
            db.diaryDao().insertTask(task)
        }
    }

    suspend fun setTaskCompleted(taskId: Long, completed: Boolean) {
        db.diaryDao().setTaskCompleted(taskId, completed)
    }

    suspend fun deleteTask(taskId: Long) {
        db.diaryDao().deleteTaskById(taskId)
    }

    suspend fun recordBackupHistory(entry: BackupHistoryEntity) {
        db.systemDao().insertBackupHistory(entry)
    }
}

fun LedgerTransactionEntity.toLedgerRecord(): LedgerEntryRecord = LedgerEntryRecord(
    id = id,
    notebookId = notebookId,
    type = LedgerTransactionType.valueOf(type),
    totalAmountPoisha = totalAmountPoisha,
    paidAmountPoisha = paidAmountPoisha,
    dueAmountPoisha = dueAmountPoisha,
    categoryId = categoryId,
    shopId = shopId,
    loanId = loanId,
    transactionDate = transactionDate,
    isDeleted = isDeleted
)
