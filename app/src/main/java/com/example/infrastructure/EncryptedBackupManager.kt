package com.example.infrastructure

import android.content.Context
import androidx.room.withTransaction
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class BackupExportResult(
    val encryptedBytes: ByteArray,
    val recordCount: Int,
    val checksumSha256: String,
    val schemaVersion: Int
)

data class BackupPreview(
    val schemaVersion: Int,
    val exportedAt: Long,
    val notebookCount: Int,
    val transactionCount: Int,
    val shopCount: Int,
    val loanCount: Int,
    val noteCount: Int,
    val taskCount: Int,
    val checksumSha256: String,
    val payloadJson: JSONObject
)

/**
 * 100% offline AES-256-GCM encrypted backup & restore manager with SHA-256 integrity verification
 * and schema version checks.
 */
class EncryptedBackupManager(private val db: JomaKhorochDatabase) {

    companion object {
        private val MAGIC_HEADER = "JKBACKUP_V1\n".toByteArray(Charsets.UTF_8)
        private const val SALT_LEN = 16
        private const val IV_LEN = 12
        private const val GCM_TAG_BITS = 128
        private const val PBKDF2_ITERATIONS = 120_000
    }

    suspend fun createEncryptedBackup(passphrase: String): Result<BackupExportResult> =
        withContext(Dispatchers.IO) {
            if (passphrase.length < 4) {
                return@withContext Result.failure(IllegalArgumentException("PASSPHRASE_TOO_SHORT"))
            }
            runCatching {
                val (payloadJson, recordCount) = buildDatabaseSnapshotJson()
                val canonicalData = payloadJson.toString()
                val checksum = sha256Hex(canonicalData.toByteArray(Charsets.UTF_8))

                val envelope = JSONObject().apply {
                    put("app", "JomaKhoroch")
                    put("schemaVersion", JomaKhorochDatabase.SCHEMA_VERSION)
                    put("exportedAt", System.currentTimeMillis())
                    put("recordCount", recordCount)
                    put("checksumSha256", checksum)
                    put("dataJson", canonicalData)
                }

                val encryptedBytes = encryptBytes(
                    envelope.toString().toByteArray(Charsets.UTF_8),
                    passphrase
                )

                BackupExportResult(
                    encryptedBytes = encryptedBytes,
                    recordCount = recordCount,
                    checksumSha256 = checksum,
                    schemaVersion = JomaKhorochDatabase.SCHEMA_VERSION
                )
            }
        }

    suspend fun saveLocalSnapshot(context: Context, passphrase: String): Result<File> =
        withContext(Dispatchers.IO) {
            val export = createEncryptedBackup(passphrase).getOrElse { return@withContext Result.failure(it) }
            runCatching {
                val backupDir = File(context.filesDir, "local_backups").apply { mkdirs() }
                val fileName = "jomakhoroch_snapshot_${System.currentTimeMillis()}.jkbackup"
                val outFile = File(backupDir, fileName)
                require(outFile.canonicalPath.startsWith(backupDir.canonicalPath + File.separator)) {
                    "Invalid backup path"
                }
                outFile.writeBytes(export.encryptedBytes)
                db.systemDao().insertBackupHistory(
                    BackupHistoryEntity(
                        fileName = fileName,
                        backupType = "LOCAL_SNAPSHOT",
                        schemaVersion = export.schemaVersion,
                        recordCount = export.recordCount,
                        checksumSha256 = export.checksumSha256
                    )
                )
                outFile
            }
        }

    suspend fun inspectEncryptedBackup(
        encryptedBytes: ByteArray,
        passphrase: String
    ): Result<BackupPreview> = withContext(Dispatchers.IO) {
        if (passphrase.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("PASSPHRASE_REQUIRED"))
        }
        runCatching {
            val plainBytes = decryptBytes(encryptedBytes, passphrase)
            val envelope = JSONObject(String(plainBytes, Charsets.UTF_8))
            require(envelope.optString("app") == "JomaKhoroch") { "INVALID_BACKUP_FORMAT" }
            val schemaVersion = envelope.getInt("schemaVersion")
            require(schemaVersion <= JomaKhorochDatabase.SCHEMA_VERSION) { "UNSUPPORTED_SCHEMA_VERSION" }

            val canonicalData = if (envelope.has("dataJson")) {
                envelope.getString("dataJson")
            } else {
                envelope.getJSONObject("data").toString()
            }
            val expectedChecksum = envelope.getString("checksumSha256")
            val actualChecksum = sha256Hex(canonicalData.toByteArray(Charsets.UTF_8))
            require(expectedChecksum == actualChecksum) { "CHECKSUM_MISMATCH" }
            val dataObj = JSONObject(canonicalData)

            BackupPreview(
                schemaVersion = schemaVersion,
                exportedAt = envelope.optLong("exportedAt", 0L),
                notebookCount = dataObj.optJSONArray("notebooks")?.length() ?: 0,
                transactionCount = dataObj.optJSONArray("transactions")?.length() ?: 0,
                shopCount = dataObj.optJSONArray("shops")?.length() ?: 0,
                loanCount = dataObj.optJSONArray("personal_loans")?.length() ?: 0,
                noteCount = dataObj.optJSONArray("notes")?.length() ?: 0,
                taskCount = dataObj.optJSONArray("tasks")?.length() ?: 0,
                checksumSha256 = actualChecksum,
                payloadJson = dataObj
            )
        }
    }

    suspend fun restoreFromPreview(preview: BackupPreview, fileName: String): Result<Long> =
        withContext(Dispatchers.IO) {
            runCatching {
                val data = preview.payloadJson
                val notebooks = parseNotebooks(data.optJSONArray("notebooks"))
                require(notebooks.isNotEmpty()) { "EMPTY_BACKUP_NO_BOOKS" }
                val categories = parseCategories(data.optJSONArray("categories"))
                val shops = parseShops(data.optJSONArray("shops"))
                val loans = parseLoans(data.optJSONArray("personal_loans"))
                val transactions = parseTransactions(data.optJSONArray("transactions"))
                val items = parseTransactionItems(data.optJSONArray("transaction_items"))
                val budgets = parseBudgets(data.optJSONArray("budgets"))
                val notes = parseNotes(data.optJSONArray("notes"))
                val attachments = parseAttachments(data.optJSONArray("note_attachments"))
                val tasks = parseTasks(data.optJSONArray("tasks"))
                val auditLogs = parseAuditLogs(data.optJSONArray("audit_logs"))

                db.withTransaction {
                    // Cascade clears all child tables cleanly
                    db.notebookDao().deleteAll()
                    db.systemDao().deleteAllAuditLogs()

                    notebooks.forEach { db.notebookDao().insert(it) }
                    if (categories.isNotEmpty()) db.categoryDao().insertAll(categories)
                    shops.forEach { db.shopDao().insert(it) }
                    loans.forEach { db.loanDao().insert(it) }
                    transactions.forEach { db.transactionDao().insert(it) }
                    if (items.isNotEmpty()) db.transactionDao().insertItems(items)
                    budgets.forEach { db.budgetDao().insert(it) }
                    notes.forEach { db.diaryDao().insertNote(it) }
                    attachments.forEach { db.diaryDao().insertAttachment(it) }
                    tasks.forEach { db.diaryDao().insertTask(it) }
                    auditLogs.forEach { db.systemDao().insertAuditLog(it) }

                    val totalCount = notebooks.size + shops.size + loans.size +
                        transactions.size + budgets.size + notes.size + tasks.size
                    db.systemDao().insertBackupHistory(
                        BackupHistoryEntity(
                            fileName = fileName,
                            backupType = "RESTORE",
                            schemaVersion = preview.schemaVersion,
                            recordCount = totalCount,
                            checksumSha256 = preview.checksumSha256
                        )
                    )
                }
                notebooks.firstOrNull { !it.isArchived }?.id ?: notebooks.first().id
            }
        }

    private suspend fun buildDatabaseSnapshotJson(): Pair<JSONObject, Int> {
        val notebooks = db.notebookDao().getAllNotebooksOnce()
        val categories = db.categoryDao().getAllCategoriesOnce()
        val shops = db.shopDao().getAllShopsOnce()
        val loans = db.loanDao().getAllLoansOnce()
        val transactions = db.transactionDao().getAllTransactionsOnce()
        val items = db.transactionDao().getAllItemsOnce()
        val budgets = db.budgetDao().getAllBudgetsOnce()
        val notes = db.diaryDao().getAllNotesOnce()
        val attachments = db.diaryDao().getAllAttachmentsOnce()
        val tasks = db.diaryDao().getAllTasksOnce()
        val audits = db.systemDao().getAllAuditLogsOnce()

        val root = JSONObject().apply {
            put("notebooks", JSONArray().apply {
                notebooks.forEach { n ->
                    put(JSONObject().apply {
                        put("id", n.id)
                        put("name", n.name)
                        put("type", n.type)
                        put("iconKey", n.iconKey)
                        put("openingBalancePoisha", n.openingBalancePoisha)
                        put("isArchived", n.isArchived)
                        put("createdAt", n.createdAt)
                    })
                }
            })
            put("categories", JSONArray().apply {
                categories.forEach { c ->
                    put(JSONObject().apply {
                        put("id", c.id)
                        put("notebookId", c.notebookId)
                        put("nameBn", c.nameBn)
                        put("nameEn", c.nameEn)
                        put("type", c.type)
                        put("iconKey", c.iconKey)
                        put("colorHex", c.colorHex)
                        put("isDefault", c.isDefault)
                        put("isActive", c.isActive)
                    })
                }
            })
            put("shops", JSONArray().apply {
                shops.forEach { s ->
                    put(JSONObject().apply {
                        put("id", s.id)
                        put("notebookId", s.notebookId)
                        put("name", s.name)
                        put("ownerName", s.ownerName)
                        put("phone", s.phone)
                        put("address", s.address)
                        put("shopCategory", s.shopCategory)
                        put("openingBalancePoisha", s.openingBalancePoisha)
                        put("notes", s.notes)
                        put("isArchived", s.isArchived)
                        put("createdAt", s.createdAt)
                    })
                }
            })
            put("personal_loans", JSONArray().apply {
                loans.forEach { l ->
                    put(JSONObject().apply {
                        put("id", l.id)
                        put("notebookId", l.notebookId)
                        put("direction", l.direction)
                        put("personName", l.personName)
                        put("phone", l.phone)
                        put("principalPoisha", l.principalPoisha)
                        put("loanDate", l.loanDate)
                        put("dueDate", l.dueDate ?: JSONObject.NULL)
                        put("description", l.description)
                        put("isSettled", l.isSettled)
                        put("createdAt", l.createdAt)
                    })
                }
            })
            put("transactions", JSONArray().apply {
                transactions.forEach { t ->
                    put(JSONObject().apply {
                        put("id", t.id)
                        put("notebookId", t.notebookId)
                        put("type", t.type)
                        put("totalAmountPoisha", t.totalAmountPoisha)
                        put("paidAmountPoisha", t.paidAmountPoisha)
                        put("dueAmountPoisha", t.dueAmountPoisha)
                        put("categoryId", t.categoryId ?: JSONObject.NULL)
                        put("shopId", t.shopId ?: JSONObject.NULL)
                        put("loanId", t.loanId ?: JSONObject.NULL)
                        put("paymentMethod", t.paymentMethod)
                        put("note", t.note)
                        put("transactionDate", t.transactionDate)
                        put("isDeleted", t.isDeleted)
                        put("createdAt", t.createdAt)
                        put("updatedAt", t.updatedAt)
                    })
                }
            })
            put("transaction_items", JSONArray().apply {
                items.forEach { i ->
                    put(JSONObject().apply {
                        put("id", i.id)
                        put("transactionId", i.transactionId)
                        put("productName", i.productName)
                        put("quantityMillis", i.quantityMillis)
                        put("unit", i.unit)
                        put("unitPricePoisha", i.unitPricePoisha)
                        put("lineTotalPoisha", i.lineTotalPoisha)
                    })
                }
            })
            put("budgets", JSONArray().apply {
                budgets.forEach { b ->
                    put(JSONObject().apply {
                        put("id", b.id)
                        put("notebookId", b.notebookId)
                        put("yearMonth", b.yearMonth)
                        put("categoryId", b.categoryId ?: JSONObject.NULL)
                        put("limitPoisha", b.limitPoisha)
                        put("updatedAt", b.updatedAt)
                    })
                }
            })
            put("notes", JSONArray().apply {
                notes.forEach { n ->
                    put(JSONObject().apply {
                        put("id", n.id)
                        put("notebookId", n.notebookId)
                        put("title", n.title)
                        put("content", n.content)
                        put("category", n.category)
                        put("checklistJson", n.checklistJson)
                        put("contactPhone", n.contactPhone)
                        put("contactAddress", n.contactAddress)
                        put("isPinned", n.isPinned)
                        put("createdAt", n.createdAt)
                        put("updatedAt", n.updatedAt)
                    })
                }
            })
            put("note_attachments", JSONArray().apply {
                attachments.forEach { a ->
                    put(JSONObject().apply {
                        put("id", a.id)
                        put("noteId", a.noteId)
                        put("filePath", a.filePath)
                        put("caption", a.caption)
                        put("createdAt", a.createdAt)
                    })
                }
            })
            put("tasks", JSONArray().apply {
                tasks.forEach { t ->
                    put(JSONObject().apply {
                        put("id", t.id)
                        put("notebookId", t.notebookId)
                        put("title", t.title)
                        put("description", t.description)
                        put("taskType", t.taskType)
                        put("dueAt", t.dueAt ?: JSONObject.NULL)
                        put("reminderAt", t.reminderAt ?: JSONObject.NULL)
                        put("isCompleted", t.isCompleted)
                        put("linkedLoanId", t.linkedLoanId ?: JSONObject.NULL)
                        put("createdAt", t.createdAt)
                    })
                }
            })
            put("audit_logs", JSONArray().apply {
                audits.forEach { a ->
                    put(JSONObject().apply {
                        put("id", a.id)
                        put("notebookId", a.notebookId)
                        put("entityType", a.entityType)
                        put("entityId", a.entityId)
                        put("action", a.action)
                        put("previousSnapshot", a.previousSnapshot)
                        put("newSnapshot", a.newSnapshot)
                        put("reason", a.reason)
                        put("createdAt", a.createdAt)
                    })
                }
            })
        }

        val count = notebooks.size + shops.size + loans.size + transactions.size +
            budgets.size + notes.size + tasks.size
        return root to count
    }

    private fun encryptBytes(plain: ByteArray, passphrase: String): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LEN).also { random.nextBytes(it) }
        val iv = ByteArray(IV_LEN).also { random.nextBytes(it) }
        val key = deriveAesKey(passphrase, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        val ciphertext = cipher.doFinal(plain)
        return MAGIC_HEADER + salt + iv + ciphertext
    }

    private fun decryptBytes(encrypted: ByteArray, passphrase: String): ByteArray {
        val minSize = MAGIC_HEADER.size + SALT_LEN + IV_LEN + 16
        require(encrypted.size >= minSize) { "CORRUPTED_BACKUP_FILE" }
        val header = encrypted.copyOfRange(0, MAGIC_HEADER.size)
        require(header.contentEquals(MAGIC_HEADER)) { "INVALID_BACKUP_HEADER" }
        val saltStart = MAGIC_HEADER.size
        val ivStart = saltStart + SALT_LEN
        val cipherStart = ivStart + IV_LEN

        val salt = encrypted.copyOfRange(saltStart, ivStart)
        val iv = encrypted.copyOfRange(ivStart, cipherStart)
        val ciphertext = encrypted.copyOfRange(cipherStart, encrypted.size)
        val key = deriveAesKey(passphrase, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun deriveAesKey(passphrase: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val bytes = try {
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
        return SecretKeySpec(bytes, "AES")
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun JSONObject.optLongOrNull(key: String): Long? =
        if (isNull(key) || !has(key)) null else optLong(key)

    private fun parseNotebooks(arr: JSONArray?): List<NotebookEntity> {
        if (arr == null) return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            NotebookEntity(
                id = o.getLong("id"),
                name = o.getString("name"),
                type = o.optString("type", "CUSTOM"),
                iconKey = o.optString("iconKey", "wallet"),
                openingBalancePoisha = o.optLong("openingBalancePoisha", 0L),
                isArchived = o.optBoolean("isArchived", false),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }

    private fun parseCategories(arr: JSONArray?): List<CategoryEntity> {
        if (arr == null) return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            CategoryEntity(
                id = o.getLong("id"),
                notebookId = o.getLong("notebookId"),
                nameBn = o.getString("nameBn"),
                nameEn = o.getString("nameEn"),
                type = o.getString("type"),
                iconKey = o.optString("iconKey", "category"),
                colorHex = o.optString("colorHex", "#176B45"),
                isDefault = o.optBoolean("isDefault", false),
                isActive = o.optBoolean("isActive", true)
            )
        }
    }

    private fun parseShops(arr: JSONArray?): List<ShopEntity> {
        if (arr == null) return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            ShopEntity(
                id = o.getLong("id"),
                notebookId = o.getLong("notebookId"),
                name = o.getString("name"),
                ownerName = o.optString("ownerName", ""),
                phone = o.optString("phone", ""),
                address = o.optString("address", ""),
                shopCategory = o.optString("shopCategory", ""),
                openingBalancePoisha = o.optLong("openingBalancePoisha", 0L),
                notes = o.optString("notes", ""),
                isArchived = o.optBoolean("isArchived", false),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }

    private fun parseLoans(arr: JSONArray?): List<PersonalLoanEntity> {
        if (arr == null) return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            PersonalLoanEntity(
                id = o.getLong("id"),
                notebookId = o.getLong("notebookId"),
                direction = o.getString("direction"),
                personName = o.getString("personName"),
                phone = o.optString("phone", ""),
                principalPoisha = o.getLong("principalPoisha"),
                loanDate = o.getLong("loanDate"),
                dueDate = o.optLongOrNull("dueDate"),
                description = o.optString("description", ""),
                isSettled = o.optBoolean("isSettled", false),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }

    private fun parseTransactions(arr: JSONArray?): List<LedgerTransactionEntity> {
        if (arr == null) return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            LedgerTransactionEntity(
                id = o.getLong("id"),
                notebookId = o.getLong("notebookId"),
                type = o.getString("type"),
                totalAmountPoisha = o.getLong("totalAmountPoisha"),
                paidAmountPoisha = o.getLong("paidAmountPoisha"),
                dueAmountPoisha = o.getLong("dueAmountPoisha"),
                categoryId = o.optLongOrNull("categoryId"),
                shopId = o.optLongOrNull("shopId"),
                loanId = o.optLongOrNull("loanId"),
                paymentMethod = o.optString("paymentMethod", "CASH"),
                note = o.optString("note", ""),
                transactionDate = o.getLong("transactionDate"),
                isDeleted = o.optBoolean("isDeleted", false),
                createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
            )
        }
    }

    private fun parseTransactionItems(arr: JSONArray?): List<TransactionItemEntity> {
        if (arr == null) return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            TransactionItemEntity(
                id = o.getLong("id"),
                transactionId = o.getLong("transactionId"),
                productName = o.getString("productName"),
                quantityMillis = o.getLong("quantityMillis"),
                unit = o.optString("unit", "pcs"),
                unitPricePoisha = o.getLong("unitPricePoisha"),
                lineTotalPoisha = o.getLong("lineTotalPoisha")
            )
        }
    }

    private fun parseBudgets(arr: JSONArray?): List<BudgetEntity> {
        if (arr == null) return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            BudgetEntity(
                id = o.getLong("id"),
                notebookId = o.getLong("notebookId"),
                yearMonth = o.getString("yearMonth"),
                categoryId = o.optLongOrNull("categoryId"),
                limitPoisha = o.getLong("limitPoisha"),
                updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
            )
        }
    }

    private fun parseNotes(arr: JSONArray?): List<NoteEntity> {
        if (arr == null) return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            NoteEntity(
                id = o.getLong("id"),
                notebookId = o.getLong("notebookId"),
                title = o.getString("title"),
                content = o.optString("content", ""),
                category = o.optString("category", "PERSONAL"),
                checklistJson = o.optString("checklistJson", "[]"),
                contactPhone = o.optString("contactPhone", ""),
                contactAddress = o.optString("contactAddress", ""),
                isPinned = o.optBoolean("isPinned", false),
                createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
            )
        }
    }

    private fun parseAttachments(arr: JSONArray?): List<NoteAttachmentEntity> {
        if (arr == null) return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            NoteAttachmentEntity(
                id = o.getLong("id"),
                noteId = o.getLong("noteId"),
                filePath = o.getString("filePath"),
                caption = o.optString("caption", ""),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }

    private fun parseTasks(arr: JSONArray?): List<TaskEntity> {
        if (arr == null) return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            TaskEntity(
                id = o.getLong("id"),
                notebookId = o.getLong("notebookId"),
                title = o.getString("title"),
                description = o.optString("description", ""),
                taskType = o.optString("taskType", "DAILY"),
                dueAt = o.optLongOrNull("dueAt"),
                reminderAt = o.optLongOrNull("reminderAt"),
                isCompleted = o.optBoolean("isCompleted", false),
                linkedLoanId = o.optLongOrNull("linkedLoanId"),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }

    private fun parseAuditLogs(arr: JSONArray?): List<AuditLogEntity> {
        if (arr == null) return emptyList()
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            AuditLogEntity(
                id = o.getLong("id"),
                notebookId = o.getLong("notebookId"),
                entityType = o.getString("entityType"),
                entityId = o.getLong("entityId"),
                action = o.getString("action"),
                previousSnapshot = o.optString("previousSnapshot", ""),
                newSnapshot = o.optString("newSnapshot", ""),
                reason = o.optString("reason", ""),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}
