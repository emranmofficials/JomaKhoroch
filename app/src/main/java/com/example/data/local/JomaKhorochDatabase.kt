package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.DiaryDao
import com.example.data.local.dao.LoanDao
import com.example.data.local.dao.NotebookDao
import com.example.data.local.dao.ShopDao
import com.example.data.local.dao.SystemDao
import com.example.data.local.dao.TransactionDao
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

@Database(
    entities = [
        NotebookEntity::class,
        CategoryEntity::class,
        ShopEntity::class,
        PersonalLoanEntity::class,
        LedgerTransactionEntity::class,
        TransactionItemEntity::class,
        BudgetEntity::class,
        NoteEntity::class,
        NoteAttachmentEntity::class,
        TaskEntity::class,
        BackupHistoryEntity::class,
        AuditLogEntity::class
    ],
    version = JomaKhorochDatabase.SCHEMA_VERSION,
    exportSchema = false
)
abstract class JomaKhorochDatabase : RoomDatabase() {
    abstract fun notebookDao(): NotebookDao
    abstract fun categoryDao(): CategoryDao
    abstract fun shopDao(): ShopDao
    abstract fun loanDao(): LoanDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun diaryDao(): DiaryDao
    abstract fun systemDao(): SystemDao

    companion object {
        const val SCHEMA_VERSION = 1
        const val DB_NAME = "jomakhoroch_offline.db"

        @Volatile
        private var INSTANCE: JomaKhorochDatabase? = null

        fun getInstance(context: Context): JomaKhorochDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    JomaKhorochDatabase::class.java,
                    DB_NAME
                ).build().also { INSTANCE = it }
            }
        }
    }
}

/**
 * Seeds default account books and Bangladesh-relevant categories on first launch.
 * Never inserts fake financial transactions.
 */
object DatabaseSeeder {
    fun defaultCategoriesForBook(notebookId: Long): List<CategoryEntity> = listOf(
        // Income categories
        CategoryEntity(notebookId = notebookId, nameBn = "বেতন", nameEn = "Salary", type = "INCOME", iconKey = "payments", colorHex = "#176B45", isDefault = true),
        CategoryEntity(notebookId = notebookId, nameBn = "ব্যবসার আয়", nameEn = "Business Income", type = "INCOME", iconKey = "store", colorHex = "#21845A", isDefault = true),
        CategoryEntity(notebookId = notebookId, nameBn = "দৈনিক আয়", nameEn = "Daily Income", type = "INCOME", iconKey = "today", colorHex = "#126E82", isDefault = true),
        CategoryEntity(notebookId = notebookId, nameBn = "ভাড়া আয়", nameEn = "Rent", type = "INCOME", iconKey = "home", colorHex = "#2E7D32", isDefault = true),
        CategoryEntity(notebookId = notebookId, nameBn = "অন্যান্য আয়", nameEn = "Other Income", type = "INCOME", iconKey = "add_circle", colorHex = "#3D8B68", isDefault = true),
        // Expense categories
        CategoryEntity(notebookId = notebookId, nameBn = "বাজার / মুদি", nameEn = "Grocery", type = "EXPENSE", iconKey = "shopping_basket", colorHex = "#C63D3D", isDefault = true),
        CategoryEntity(notebookId = notebookId, nameBn = "খাবার", nameEn = "Food", type = "EXPENSE", iconKey = "restaurant", colorHex = "#D8572A", isDefault = true),
        CategoryEntity(notebookId = notebookId, nameBn = "যাতায়াত", nameEn = "Transportation", type = "EXPENSE", iconKey = "directions_bus", colorHex = "#3E6B89", isDefault = true),
        CategoryEntity(notebookId = notebookId, nameBn = "চিকিৎসা", nameEn = "Medical", type = "EXPENSE", iconKey = "medical_services", colorHex = "#C23B5A", isDefault = true),
        CategoryEntity(notebookId = notebookId, nameBn = "শিক্ষা", nameEn = "Education", type = "EXPENSE", iconKey = "school", colorHex = "#5D5398", isDefault = true),
        CategoryEntity(notebookId = notebookId, nameBn = "বাড়ি ভাড়া", nameEn = "House Rent", type = "EXPENSE", iconKey = "apartment", colorHex = "#8C5A2B", isDefault = true),
        CategoryEntity(notebookId = notebookId, nameBn = "বিদ্যুৎ বিল", nameEn = "Electricity", type = "EXPENSE", iconKey = "bolt", colorHex = "#C98A1A", isDefault = true),
        CategoryEntity(notebookId = notebookId, nameBn = "গ্যাস বিল", nameEn = "Gas", type = "EXPENSE", iconKey = "local_fire_department", colorHex = "#CC6B1E", isDefault = true),
        CategoryEntity(notebookId = notebookId, nameBn = "পোশাক", nameEn = "Clothing", type = "EXPENSE", iconKey = "checkroom", colorHex = "#9B4F78", isDefault = true),
        CategoryEntity(notebookId = notebookId, nameBn = "বিনোদন", nameEn = "Entertainment", type = "EXPENSE", iconKey = "movie", colorHex = "#2D7A78", isDefault = true),
        CategoryEntity(notebookId = notebookId, nameBn = "অন্যান্য খরচ", nameEn = "Other Expenses", type = "EXPENSE", iconKey = "receipt", colorHex = "#63706A", isDefault = true)
    )

    suspend fun ensureSeeded(db: JomaKhorochDatabase): Long {
        val existing = db.notebookDao().getAllNotebooksOnce()
        if (existing.isNotEmpty()) {
            return existing.firstOrNull { !it.isArchived }?.id ?: existing.first().id
        }
        return db.withTransaction {
            val personalId = db.notebookDao().insert(
                NotebookEntity(name = "ব্যক্তিগত খাতা", type = "PERSONAL", iconKey = "person")
            )
            val familyId = db.notebookDao().insert(
                NotebookEntity(name = "পারিবারিক খাতা", type = "FAMILY", iconKey = "family")
            )
            val businessId = db.notebookDao().insert(
                NotebookEntity(name = "ব্যবসার খাতা", type = "BUSINESS", iconKey = "business")
            )
            db.categoryDao().insertAll(defaultCategoriesForBook(personalId))
            db.categoryDao().insertAll(defaultCategoriesForBook(familyId))
            db.categoryDao().insertAll(defaultCategoriesForBook(businessId))
            personalId
        }
    }
}
