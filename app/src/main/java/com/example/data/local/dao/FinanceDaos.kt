package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookDao {
    @Query("SELECT * FROM notebooks WHERE isArchived = 0 ORDER BY id ASC")
    fun observeActiveNotebooks(): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks ORDER BY isArchived ASC, id ASC")
    fun observeAllNotebooks(): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks ORDER BY id ASC")
    suspend fun getAllNotebooksOnce(): List<NotebookEntity>

    @Query("SELECT * FROM notebooks WHERE id = :id LIMIT 1")
    suspend fun getNotebookById(id: Long): NotebookEntity?

    @Query("SELECT COUNT(*) FROM notebooks WHERE isArchived = 0")
    suspend fun countActiveNotebooks(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notebook: NotebookEntity): Long

    @Update
    suspend fun update(notebook: NotebookEntity)

    @Query("UPDATE notebooks SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)

    @Query("DELETE FROM notebooks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notebooks")
    suspend fun deleteAll()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE notebookId = :notebookId AND isActive = 1 ORDER BY isDefault DESC, id ASC")
    fun observeCategories(notebookId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getAllCategoriesOnce(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("UPDATE categories SET isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)
}

@Dao
interface ShopDao {
    @Query("SELECT * FROM shops WHERE notebookId = :notebookId AND isArchived = 0 ORDER BY name COLLATE NOCASE ASC")
    fun observeShops(notebookId: Long): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops ORDER BY id ASC")
    suspend fun getAllShopsOnce(): List<ShopEntity>

    @Query("SELECT * FROM shops WHERE id = :id LIMIT 1")
    suspend fun getShopById(id: Long): ShopEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shop: ShopEntity): Long

    @Update
    suspend fun update(shop: ShopEntity)

    @Query("UPDATE shops SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)
}

@Dao
interface LoanDao {
    @Query("SELECT * FROM personal_loans WHERE notebookId = :notebookId ORDER BY isSettled ASC, loanDate DESC")
    fun observeLoans(notebookId: Long): Flow<List<PersonalLoanEntity>>

    @Query("SELECT * FROM personal_loans ORDER BY id ASC")
    suspend fun getAllLoansOnce(): List<PersonalLoanEntity>

    @Query("SELECT * FROM personal_loans WHERE id = :id LIMIT 1")
    suspend fun getLoanById(id: Long): PersonalLoanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loan: PersonalLoanEntity): Long

    @Update
    suspend fun update(loan: PersonalLoanEntity)

    @Query("UPDATE personal_loans SET isSettled = :settled WHERE id = :loanId")
    suspend fun updateSettled(loanId: Long, settled: Boolean)
}

@Dao
interface TransactionDao {
    @Query(
        "SELECT * FROM transactions WHERE notebookId = :notebookId AND isDeleted = 0 " +
            "ORDER BY transactionDate DESC, id DESC"
    )
    fun observeActiveTransactions(notebookId: Long): Flow<List<LedgerTransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY transactionDate DESC, id DESC")
    fun observeAllActiveTransactions(): Flow<List<LedgerTransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY id ASC")
    suspend fun getAllTransactionsOnce(): List<LedgerTransactionEntity>

    @Query("SELECT * FROM transactions WHERE notebookId = :notebookId AND isDeleted = 0")
    suspend fun getActiveTransactionsByBookOnce(notebookId: Long): List<LedgerTransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): LedgerTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: LedgerTransactionEntity): Long

    @Update
    suspend fun update(transaction: LedgerTransactionEntity)

    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: Long, updatedAt: Long)

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId ORDER BY id ASC")
    fun observeItemsForTransaction(transactionId: Long): Flow<List<TransactionItemEntity>>

    @Query("SELECT * FROM transaction_items ORDER BY id ASC")
    suspend fun getAllItemsOnce(): List<TransactionItemEntity>

    @Query("SELECT * FROM transaction_items WHERE transactionId IN (:txIds)")
    suspend fun getItemsForTransactionsOnce(txIds: List<Long>): List<TransactionItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<TransactionItemEntity>)

    @Query("DELETE FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun deleteItemsForTransaction(transactionId: Long)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE notebookId = :notebookId AND yearMonth = :yearMonth ORDER BY categoryId ASC")
    fun observeBudgetsForMonth(notebookId: Long, yearMonth: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE notebookId = :notebookId ORDER BY yearMonth DESC")
    fun observeAllBudgetsForBook(notebookId: Long): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets ORDER BY id ASC")
    suspend fun getAllBudgetsOnce(): List<BudgetEntity>

    @Query(
        "SELECT * FROM budgets WHERE notebookId = :notebookId AND yearMonth = :yearMonth AND " +
            "((:categoryId IS NULL AND categoryId IS NULL) OR categoryId = :categoryId) LIMIT 1"
    )
    suspend fun findBudget(notebookId: Long, yearMonth: String, categoryId: Long?): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface DiaryDao {
    @Query("SELECT * FROM notes WHERE notebookId = :notebookId ORDER BY isPinned DESC, updatedAt DESC")
    fun observeNotes(notebookId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY id ASC")
    suspend fun getAllNotesOnce(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("SELECT * FROM note_attachments ORDER BY id ASC")
    fun observeAllAttachments(): Flow<List<NoteAttachmentEntity>>

    @Query("SELECT * FROM note_attachments ORDER BY id ASC")
    suspend fun getAllAttachmentsOnce(): List<NoteAttachmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: NoteAttachmentEntity): Long

    @Query("DELETE FROM note_attachments WHERE id = :id")
    suspend fun deleteAttachmentById(id: Long)

    @Query("SELECT * FROM tasks WHERE notebookId = :notebookId ORDER BY isCompleted ASC, COALESCE(dueAt, 9223372036854775807) ASC, id DESC")
    fun observeTasks(notebookId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY id ASC")
    suspend fun getAllTasksOnce(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, completed: Boolean)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}

@Dao
interface SystemDao {
    @Query("SELECT * FROM backup_history ORDER BY createdAt DESC LIMIT 30")
    fun observeBackupHistory(): Flow<List<BackupHistoryEntity>>

    @Query("SELECT * FROM backup_history ORDER BY id ASC")
    suspend fun getAllBackupHistoryOnce(): List<BackupHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackupHistory(item: BackupHistoryEntity): Long

    @Query("SELECT * FROM audit_logs WHERE notebookId = :notebookId ORDER BY createdAt DESC LIMIT 100")
    fun observeAuditLogs(notebookId: Long): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs ORDER BY id ASC")
    suspend fun getAllAuditLogsOnce(): List<AuditLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    @Query("DELETE FROM audit_logs")
    suspend fun deleteAllAuditLogs()

    @Query("DELETE FROM backup_history")
    suspend fun deleteAllBackupHistory()
}
