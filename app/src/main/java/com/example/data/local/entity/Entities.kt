package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "notebooks")
data class NotebookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val type: String, // PERSONAL, FAMILY, BUSINESS, CUSTOM
    val iconKey: String = "wallet",
    val openingBalancePoisha: Long = 0L,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = NotebookEntity::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("notebookId")]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val notebookId: Long,
    val nameBn: String,
    val nameEn: String,
    val type: String, // INCOME, EXPENSE
    val iconKey: String = "category",
    val colorHex: String = "#176B45",
    val isDefault: Boolean = false,
    val isActive: Boolean = true
)

@Entity(
    tableName = "shops",
    foreignKeys = [
        ForeignKey(
            entity = NotebookEntity::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("notebookId")]
)
data class ShopEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val notebookId: Long,
    val name: String,
    val ownerName: String = "",
    val phone: String = "",
    val address: String = "",
    val shopCategory: String = "",
    val openingBalancePoisha: Long = 0L,
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "personal_loans",
    foreignKeys = [
        ForeignKey(
            entity = NotebookEntity::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("notebookId"), Index("direction")]
)
data class PersonalLoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val notebookId: Long,
    val direction: String, // GIVEN, BORROWED
    val personName: String,
    val phone: String = "",
    val principalPoisha: Long,
    val loanDate: Long,
    val dueDate: Long? = null,
    val description: String = "",
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = NotebookEntity::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ShopEntity::class,
            parentColumns = ["id"],
            childColumns = ["shopId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonalLoanEntity::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("notebookId"),
        Index("categoryId"),
        Index("shopId"),
        Index("loanId"),
        Index("transactionDate"),
        Index("type")
    ]
)
data class LedgerTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val notebookId: Long,
    val type: String, // LedgerTransactionType.name
    val totalAmountPoisha: Long,
    val paidAmountPoisha: Long,
    val dueAmountPoisha: Long,
    val categoryId: Long? = null,
    val shopId: Long? = null,
    val loanId: Long? = null,
    val paymentMethod: String = "CASH", // CASH, MOBILE_BANKING, BANK
    val note: String = "",
    val transactionDate: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = LedgerTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId")]
)
data class TransactionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val transactionId: Long,
    val productName: String,
    val quantityMillis: Long, // integer x1000 (e.g. 500 = 0.5)
    val unit: String = "pcs",
    val unitPricePoisha: Long,
    val lineTotalPoisha: Long
)

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = NotebookEntity::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("notebookId"),
        Index("categoryId"),
        Index(value = ["notebookId", "yearMonth", "categoryId"], unique = true)
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val notebookId: Long,
    val yearMonth: String, // YYYY-MM
    val categoryId: Long? = null, // null = overall monthly budget
    val limitPoisha: Long,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = NotebookEntity::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("notebookId"), Index("category"), Index("isPinned")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val notebookId: Long,
    val title: String,
    val content: String = "",
    val category: String = "PERSONAL", // PERSONAL, SHOPPING_LIST, PHONE_ADDRESS, IMPORTANT
    val checklistJson: String = "[]",
    val contactPhone: String = "",
    val contactAddress: String = "",
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "note_attachments",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class NoteAttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val noteId: Long,
    val filePath: String,
    val caption: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = NotebookEntity::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("notebookId"), Index("isCompleted"), Index("dueAt")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val notebookId: Long,
    val title: String,
    val description: String = "",
    val taskType: String = "DAILY", // DAILY, BILL, LOAN_REPAYMENT, CUSTOM
    val dueAt: Long? = null,
    val reminderAt: Long? = null,
    val isCompleted: Boolean = false,
    val linkedLoanId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "backup_history")
data class BackupHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fileName: String,
    val backupType: String, // ENCRYPTED_EXPORT, LOCAL_SNAPSHOT, RESTORE
    val schemaVersion: Int,
    val recordCount: Int,
    val checksumSha256: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "audit_logs",
    indices = [Index("notebookId"), Index("entityType"), Index("entityId")]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val notebookId: Long,
    val entityType: String, // TRANSACTION, SHOP, LOAN, BOOK
    val entityId: Long,
    val action: String, // EDIT, DELETE, ARCHIVE, RESTORE
    val previousSnapshot: String,
    val newSnapshot: String,
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
