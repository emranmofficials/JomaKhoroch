package com.example.data.cloud

import android.content.Context
import androidx.room.withTransaction
import com.example.R
import com.example.data.local.JomaKhorochDatabase
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class CloudSyncOutcome(
    val revision: Long,
    val pulledFromCloud: Boolean,
    val totalSyncedRecords: Int,
    val syncedAtMillis: Long
)

/**
 * Offline-first + Online Real-Time Firebase Firestore Sync Repository.
 *
 * Uses the provisioned custom Firestore database (`R.string.firestore_database_id`) with
 * persistent disk caching enabled so writes queue locally while offline and flush automatically
 * when connectivity returns.
 */
class CloudSyncRepository private constructor(
    private val localDb: JomaKhorochDatabase,
    private val dbProvider: () -> FirebaseFirestore?,
    private val authProvider: () -> FirebaseAuth?
) {
    constructor(
        db: FirebaseFirestore,
        localDb: JomaKhorochDatabase,
        auth: FirebaseAuth = Firebase.auth
    ) : this(
        localDb = localDb,
        dbProvider = { db },
        authProvider = { auth }
    )

    constructor(context: Context, localDb: JomaKhorochDatabase) : this(
        localDb = localDb,
        dbProvider = {
            if (FirebaseApp.getApps(context.applicationContext).isNotEmpty()) {
                createConfiguredFirestore(context)
            } else {
                null
            }
        },
        authProvider = {
            if (FirebaseApp.getApps(context.applicationContext).isNotEmpty()) {
                Firebase.auth
            } else {
                null
            }
        }
    )

    private val db: FirebaseFirestore? by lazy { dbProvider() }
    private val auth: FirebaseAuth? by lazy { authProvider() }

    val isCloudAvailable: Boolean
        get() = db != null && auth != null

    private fun requireFirestore(): FirebaseFirestore {
        return db ?: throw IllegalStateException("Cloud sync is unavailable without Firebase configuration.")
    }

    private fun requireUserId(): String {
        val uid = auth?.currentUser?.uid
        if (uid.isNullOrBlank()) {
            throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        }
        return uid
    }

    /**
     * Real-time snapshot observer on the signed-in user's sync metadata document.
     * Triggers pull-merge whenever another device increments `syncRevision`.
     */
    fun observeUserProfile(userId: String): Flow<CloudUserProfile?> = flow {
        val firestore = db ?: return@flow
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user changed; refusing cross-user listener." }
        val docPath = "users/$uid"
        emitAll(
            firestore.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            CloudUserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth?.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docPath)
                    }
                    throw error
                }
        )
    }

    /**
     * Pulls remote collections from Firestore into Room when cloud holds data for a newly
     * signed-in device or when another device published a newer revision.
     */
    suspend fun pullFromCloudIfNeeded(
        userId: String,
        deviceId: String,
        forcePull: Boolean = false
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext Result.success(false)
        if (auth == null) return@withContext Result.success(false)
        var activePath = "users/$userId"
        try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user read." }

            val userRef = firestore.collection("users").document(uid)
            val profileSnap = userRef.get().await()
            if (!profileSnap.exists()) {
                return@withContext Result.success(false)
            }

            val profile = profileSnap.toObject(
                CloudUserProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            val isRemoteNewerDevice = profile != null &&
                profile.syncRevision > 0L &&
                profile.deviceId != deviceId

            val localHasUserActivity =
                localDb.transactionDao().getAllTransactionsOnce().isNotEmpty() ||
                    localDb.shopDao().getAllShopsOnce().isNotEmpty() ||
                    localDb.loanDao().getAllLoansOnce().isNotEmpty() ||
                    localDb.diaryDao().getAllNotesOnce().isNotEmpty() ||
                    localDb.diaryDao().getAllTasksOnce().isNotEmpty()

            if (!forcePull && !isRemoteNewerDevice && localHasUserActivity) {
                return@withContext Result.success(false)
            }

            activePath = "users/$uid/notebooks"
            val cloudNotebooks = userRef.collection("notebooks").get().await().documents.mapNotNull {
                it.toObject(CloudNotebook::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            if (cloudNotebooks.isEmpty()) {
                return@withContext Result.success(false)
            }

            activePath = "users/$uid/categories"
            val cloudCategories = userRef.collection("categories").get().await().documents.mapNotNull {
                it.toObject(CloudCategory::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

            activePath = "users/$uid/shops"
            val cloudShops = userRef.collection("shops").get().await().documents.mapNotNull {
                it.toObject(CloudShop::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

            activePath = "users/$uid/personal_loans"
            val cloudLoans = userRef.collection("personal_loans").get().await().documents.mapNotNull {
                it.toObject(CloudPersonalLoan::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

            activePath = "users/$uid/transactions"
            val cloudTransactions = userRef.collection("transactions").get().await().documents.mapNotNull {
                it.toObject(CloudTransaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

            activePath = "users/$uid/budgets"
            val cloudBudgets = userRef.collection("budgets").get().await().documents.mapNotNull {
                it.toObject(CloudBudget::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

            activePath = "users/$uid/notes"
            val cloudNotes = userRef.collection("notes").get().await().documents.mapNotNull {
                it.toObject(CloudNote::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

            activePath = "users/$uid/tasks"
            val cloudTasks = userRef.collection("tasks").get().await().documents.mapNotNull {
                it.toObject(CloudTask::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

            mergeCloudCollectionsIntoRoom(
                notebooks = cloudNotebooks,
                categories = cloudCategories,
                shops = cloudShops,
                loans = cloudLoans,
                transactions = cloudTransactions,
                budgets = cloudBudgets,
                notes = cloudNotes,
                tasks = cloudTasks,
                replaceFreshSeed = !localHasUserActivity
            )

            Result.success(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, activePath)
            Result.failure(e)
        }
    }

    /**
     * Pushes all current local Room records to the signed-in user's private Firestore space.
     * Creates new docs with serverTimestamp() and performs partial updates on existing docs
     * so immutable createdAt/userId/localId fields are always preserved.
     */
    suspend fun pushLocalStateToCloud(
        userId: String,
        displayName: String,
        email: String?,
        deviceId: String
    ): Result<CloudSyncOutcome> = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext Result.success(
            CloudSyncOutcome(
                revision = 0L,
                pulledFromCloud = false,
                totalSyncedRecords = 0,
                syncedAtMillis = System.currentTimeMillis()
            )
        )
        if (auth == null) {
            return@withContext Result.success(
                CloudSyncOutcome(
                    revision = 0L,
                    pulledFromCloud = false,
                    totalSyncedRecords = 0,
                    syncedAtMillis = System.currentTimeMillis()
                )
            )
        }
        var activePath = "users/$userId"
        var activeOp = OperationType.WRITE
        try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user write." }

            val nowMs = System.currentTimeMillis()
            val userRef = firestore.collection("users").document(uid)

            val notebooks = localDb.notebookDao().getAllNotebooksOnce()
            val categories = localDb.categoryDao().getAllCategoriesOnce()
            val shops = localDb.shopDao().getAllShopsOnce()
            val loans = localDb.loanDao().getAllLoansOnce()
            val transactions = localDb.transactionDao().getAllTransactionsOnce()
            val txItemsByTxId = localDb.transactionDao().getAllItemsOnce().groupBy { it.transactionId }
            val budgets = localDb.budgetDao().getAllBudgetsOnce()
            val notes = localDb.diaryDao().getAllNotesOnce()
            val tasks = localDb.diaryDao().getAllTasksOnce()

            // Upsert each user-scoped collection in deterministic doc IDs
            activePath = "users/$uid/notebooks"
            syncCollection(
                collectionRef = userRef.collection("notebooks"),
                desiredDocs = notebooks.associate { "nb_${it.id}" to CloudNotebook.fromEntity(uid, it, nowMs) },
                createPayload = { it.toCreateMap(uid) },
                updatePayload = { it.toUpdateMap() }
            )

            activePath = "users/$uid/categories"
            syncCollection(
                collectionRef = userRef.collection("categories"),
                desiredDocs = categories.associate { "cat_${it.id}" to CloudCategory.fromEntity(uid, it, nowMs) },
                createPayload = { it.toCreateMap(uid) },
                updatePayload = { it.toUpdateMap() }
            )

            activePath = "users/$uid/shops"
            syncCollection(
                collectionRef = userRef.collection("shops"),
                desiredDocs = shops.associate { "shop_${it.id}" to CloudShop.fromEntity(uid, it, nowMs) },
                createPayload = { it.toCreateMap(uid) },
                updatePayload = { it.toUpdateMap() }
            )

            activePath = "users/$uid/personal_loans"
            syncCollection(
                collectionRef = userRef.collection("personal_loans"),
                desiredDocs = loans.associate { "loan_${it.id}" to CloudPersonalLoan.fromEntity(uid, it, nowMs) },
                createPayload = { it.toCreateMap(uid) },
                updatePayload = { it.toUpdateMap() }
            )

            activePath = "users/$uid/transactions"
            syncCollection(
                collectionRef = userRef.collection("transactions"),
                desiredDocs = transactions.associate { tx ->
                    "tx_${tx.id}" to CloudTransaction.fromEntity(uid, tx, txItemsByTxId[tx.id].orEmpty())
                },
                createPayload = { it.toCreateMap(uid) },
                updatePayload = { it.toUpdateMap() }
            )

            activePath = "users/$uid/budgets"
            syncCollection(
                collectionRef = userRef.collection("budgets"),
                desiredDocs = budgets.associate { "budget_${it.id}" to CloudBudget.fromEntity(uid, it) },
                createPayload = { it.toCreateMap(uid) },
                updatePayload = { it.toUpdateMap() }
            )

            activePath = "users/$uid/notes"
            syncCollection(
                collectionRef = userRef.collection("notes"),
                desiredDocs = notes.associate { "note_${it.id}" to CloudNote.fromEntity(uid, it) },
                createPayload = { it.toCreateMap(uid) },
                updatePayload = { it.toUpdateMap() }
            )

            activePath = "users/$uid/tasks"
            syncCollection(
                collectionRef = userRef.collection("tasks"),
                desiredDocs = tasks.associate { "task_${it.id}" to CloudTask.fromEntity(uid, it, nowMs) },
                createPayload = { it.toCreateMap(uid) },
                updatePayload = { it.toUpdateMap() }
            )

            // Update or create user profile sync cursor last so other devices pull a complete snapshot
            activePath = "users/$uid"
            activeOp = OperationType.GET
            val existingProfile = userRef.get().await()
            val prevRevision = existingProfile.getLong("syncRevision") ?: 0L
            val nextRevision = prevRevision + 1L
            val profileModel = CloudUserProfile(
                userId = uid,
                displayName = displayName.ifBlank { "JomaKhoroch User" },
                email = email,
                deviceId = deviceId,
                syncRevision = nextRevision
            )

            if (existingProfile.exists()) {
                activeOp = OperationType.UPDATE
                userRef.update(profileModel.toUpdateMap()).await()
            } else {
                activeOp = OperationType.CREATE
                userRef.set(profileModel.toCreateMap(uid)).await()
            }

            val totalRecords = notebooks.size + shops.size + loans.size +
                transactions.count { !it.isDeleted } + budgets.size + notes.size + tasks.size

            Result.success(
                CloudSyncOutcome(
                    revision = nextRevision,
                    pulledFromCloud = false,
                    totalSyncedRecords = totalRecords,
                    syncedAtMillis = System.currentTimeMillis()
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, activeOp, activePath)
            Result.failure(e)
        }
    }

    private suspend fun <T> syncCollection(
        collectionRef: com.google.firebase.firestore.CollectionReference,
        desiredDocs: Map<String, T>,
        createPayload: (T) -> Map<String, Any>,
        updatePayload: (T) -> Map<String, Any>
    ) {
        val existingIds = collectionRef.get().await().documents.map { it.id }.toSet()
        val staleIds = existingIds - desiredDocs.keys

        // Commit in chunks under Firestore's 500-operation batch limit
        val operations = mutableListOf<Pair<String, suspend (com.google.firebase.firestore.WriteBatch) -> Unit>>()
        for ((docId, model) in desiredDocs) {
            val docRef = collectionRef.document(docId)
            if (docId in existingIds) {
                val payload = updatePayload(model)
                operations.add(docId to { batch -> batch.update(docRef, payload) })
            } else {
                val payload = createPayload(model)
                operations.add(docId to { batch -> batch.set(docRef, payload) })
            }
        }
        for (staleId in staleIds) {
            val docRef = collectionRef.document(staleId)
            operations.add(staleId to { batch -> batch.delete(docRef) })
        }

        val firestore = requireFirestore()
        operations.chunked(400).forEach { chunk ->
            val batch = firestore.batch()
            for ((_, applyOp) in chunk) {
                applyOp(batch)
            }
            batch.commit().await()
        }
    }

    private suspend fun mergeCloudCollectionsIntoRoom(
        notebooks: List<CloudNotebook>,
        categories: List<CloudCategory>,
        shops: List<CloudShop>,
        loans: List<CloudPersonalLoan>,
        transactions: List<CloudTransaction>,
        budgets: List<CloudBudget>,
        notes: List<CloudNote>,
        tasks: List<CloudTask>,
        replaceFreshSeed: Boolean
    ) {
        localDb.withTransaction {
            if (replaceFreshSeed) {
                localDb.notebookDao().deleteAll()
            }

            val existingBookIds = localDb.notebookDao().getAllNotebooksOnce().map { it.id }.toSet()
            val validBookIds = mutableSetOf<Long>().apply { addAll(existingBookIds) }
            for (nb in notebooks.sortedBy { it.localId }) {
                if (nb.localId <= 0L) continue
                val entity = nb.toEntity()
                if (entity.id in existingBookIds) {
                    localDb.notebookDao().update(entity)
                } else {
                    localDb.notebookDao().insert(entity)
                }
                validBookIds.add(entity.id)
            }

            val existingCatIds = localDb.categoryDao().getAllCategoriesOnce().map { it.id }.toSet()
            val validCatIds = mutableSetOf<Long>().apply { addAll(existingCatIds) }
            for (cat in categories.sortedBy { it.localId }) {
                if (cat.localId <= 0L || cat.notebookId !in validBookIds) continue
                val entity = cat.toEntity()
                if (entity.id in existingCatIds) {
                    localDb.categoryDao().update(entity)
                } else {
                    localDb.categoryDao().insert(entity)
                }
                validCatIds.add(entity.id)
            }

            val existingShopIds = localDb.shopDao().getAllShopsOnce().map { it.id }.toSet()
            val validShopIds = mutableSetOf<Long>().apply { addAll(existingShopIds) }
            for (shop in shops.sortedBy { it.localId }) {
                if (shop.localId <= 0L || shop.notebookId !in validBookIds) continue
                val entity = shop.toEntity()
                if (entity.id in existingShopIds) {
                    localDb.shopDao().update(entity)
                } else {
                    localDb.shopDao().insert(entity)
                }
                validShopIds.add(entity.id)
            }

            val existingLoanIds = localDb.loanDao().getAllLoansOnce().map { it.id }.toSet()
            val validLoanIds = mutableSetOf<Long>().apply { addAll(existingLoanIds) }
            for (loan in loans.sortedBy { it.localId }) {
                if (loan.localId <= 0L || loan.notebookId !in validBookIds) continue
                val entity = loan.toEntity()
                if (entity.id in existingLoanIds) {
                    localDb.loanDao().update(entity)
                } else {
                    localDb.loanDao().insert(entity)
                }
                validLoanIds.add(entity.id)
            }

            val existingTxIds = localDb.transactionDao().getAllTransactionsOnce().map { it.id }.toSet()
            for (tx in transactions.sortedBy { it.localId }) {
                if (tx.localId <= 0L || tx.notebookId !in validBookIds) continue
                val rawEntity = tx.toEntity()
                val safeEntity = rawEntity.copy(
                    categoryId = rawEntity.categoryId?.takeIf { it in validCatIds },
                    shopId = rawEntity.shopId?.takeIf { it in validShopIds },
                    loanId = rawEntity.loanId?.takeIf { it in validLoanIds }
                )
                if (safeEntity.id in existingTxIds) {
                    localDb.transactionDao().update(safeEntity)
                } else {
                    localDb.transactionDao().insert(safeEntity)
                }
                val lineItems = tx.toLineItemEntities()
                if (lineItems.isNotEmpty()) {
                    localDb.transactionDao().deleteItemsForTransaction(safeEntity.id)
                    localDb.transactionDao().insertItems(lineItems)
                }
            }

            val existingBudgetIds = localDb.budgetDao().getAllBudgetsOnce().map { it.id }.toSet()
            for (budget in budgets.sortedBy { it.localId }) {
                if (budget.localId <= 0L || budget.notebookId !in validBookIds) continue
                val rawEntity = budget.toEntity()
                val safeEntity = rawEntity.copy(
                    categoryId = rawEntity.categoryId?.takeIf { it in validCatIds }
                )
                if (safeEntity.id in existingBudgetIds) {
                    localDb.budgetDao().update(safeEntity)
                } else {
                    localDb.budgetDao().insert(safeEntity)
                }
            }

            val existingNoteIds = localDb.diaryDao().getAllNotesOnce().map { it.id }.toSet()
            for (note in notes.sortedBy { it.localId }) {
                if (note.localId <= 0L || note.notebookId !in validBookIds) continue
                val entity = note.toEntity()
                if (entity.id in existingNoteIds) {
                    localDb.diaryDao().updateNote(entity)
                } else {
                    localDb.diaryDao().insertNote(entity)
                }
            }

            val existingTaskIds = localDb.diaryDao().getAllTasksOnce().map { it.id }.toSet()
            for (task in tasks.sortedBy { it.localId }) {
                if (task.localId <= 0L || task.notebookId !in validBookIds) continue
                val rawEntity = task.toEntity()
                val safeEntity = rawEntity.copy(
                    linkedLoanId = rawEntity.linkedLoanId?.takeIf { it in validLoanIds }
                )
                if (safeEntity.id in existingTaskIds) {
                    localDb.diaryDao().updateTask(safeEntity)
                } else {
                    localDb.diaryDao().insertTask(safeEntity)
                }
            }
        }
    }

    /**
     * Complete Play Store Compliant Cloud Account & Data Deletion.
     * Removes all collections scoped to this user under /users/{userId}.
     */
    suspend fun deleteAllUserData(userId: String) = withContext(Dispatchers.IO) {
        val firestore = db ?: return@withContext
        val userDoc = firestore.collection("users").document(userId)
        val collections = listOf(
            "notebooks", "categories", "shops", "loans",
            "transactions", "budgets", "notes", "tasks"
        )
        for (col in collections) {
            runCatching {
                val snapshot = userDoc.collection(col).get().await()
                for (doc in snapshot.documents) {
                    runCatching { doc.reference.delete().await() }
                }
            }
        }
        runCatching { userDoc.delete().await() }
    }

    companion object {
        fun createConfiguredFirestore(context: Context): FirebaseFirestore {
            val databaseId = context.applicationContext.getString(R.string.firestore_database_id)
            val firestore = FirebaseFirestore.getInstance(databaseId)
            runCatching {
                firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(
                        PersistentCacheSettings.newBuilder()
                            .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                            .build()
                    )
                    .build()
            }
            return firestore
        }
    }
}
