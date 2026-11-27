package com.example.presentation.state

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.BuildConfig
import com.example.core.localization.AppLanguage
import com.example.core.localization.NumeralStyle
import com.example.core.util.DateUtils
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
import com.example.data.preferences.AppPreferences
import com.example.data.repository.PurchaseLineItemInput
import com.example.data.repository.toLedgerRecord
import com.example.domain.accounting.AccountingEngine
import com.example.domain.accounting.FinancialSummary
import com.example.domain.accounting.LedgerTransactionType
import com.example.domain.accounting.LoanBalanceSummary
import com.example.domain.accounting.LoanDirection
import com.example.domain.accounting.LoanPrincipalRecord
import com.example.domain.accounting.ShopBalanceSummary
import com.example.domain.accounting.ShopOpeningRecord
import com.example.infrastructure.AppReleaseInfo
import com.example.infrastructure.AppUpdateChecker
import com.example.infrastructure.AttachmentStorage
import com.example.infrastructure.BackupPreview
import com.example.infrastructure.PinSecurityManager
import com.example.infrastructure.ReminderScheduler
import com.example.infrastructure.UpdateCheckResult
import com.example.ui.theme.ThemeMode
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE_PENDING,
    ERROR
}

data class CloudSyncUiState(
    val isOnline: Boolean = true,
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val lastSyncedAtMillis: Long? = null,
    val syncError: String? = null,
    val userDisplayName: String = "",
    val userEmail: String? = null,
    val syncedRecordCount: Int = 0
)

data class AppUpdateUiState(
    val currentVersion: String = BuildConfig.VERSION_NAME,
    val configuredRepo: String = BuildConfig.GITHUB_RELEASE_REPO,
    val isChecking: Boolean = false,
    val availableRelease: AppReleaseInfo? = null,
    val lastCheckedAtMillis: Long? = null,
    val statusMessage: String? = null,
    val isUpToDate: Boolean = false
)

enum class NotificationAlertType {
    APP_UPDATE,
    BUDGET_EXCEEDED,
    BUDGET_WARNING,
    LOAN_DUE,
    TASK_REMINDER,
    SHOP_DUE
}

data class InAppNotificationItem(
    val id: String,
    val type: NotificationAlertType,
    val title: String,
    val subtitle: String,
    val timestampMillis: Long,
    val isUrgent: Boolean
)

data class ShopWithBalance(
    val shop: ShopEntity,
    val balance: ShopBalanceSummary
)

data class LoanWithBalance(
    val loan: PersonalLoanEntity,
    val balance: LoanBalanceSummary
)

data class BookSummaryRow(
    val notebook: NotebookEntity,
    val incomePoisha: Long,
    val expensePoisha: Long,
    val cashBalancePoisha: Long,
    val transactionCount: Int = 0,
    val lastTransactionDate: Long? = null
)

data class JomaKhorochUiState(
    val isReady: Boolean = false,
    val splashCompleted: Boolean = false,
    val preferences: AppPreferences = AppPreferences(),
    val isUnlocked: Boolean = true,
    val activeNotebook: NotebookEntity? = null,
    val activeNotebooks: List<NotebookEntity> = emptyList(),
    val allNotebooks: List<NotebookEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val shops: List<ShopWithBalance> = emptyList(),
    val lendingLoans: List<LoanWithBalance> = emptyList(),
    val borrowingLoans: List<LoanWithBalance> = emptyList(),
    val transactions: List<LedgerTransactionEntity> = emptyList(),
    val financialSummary: FinancialSummary = FinancialSummary(0, 0, 0, 0, 0, 0, 0),
    val currentMonthIncomePoisha: Long = 0L,
    val currentMonthExpensePoisha: Long = 0L,
    val selectedBudgetMonth: String = DateUtils.currentYearMonth(),
    val budgets: List<BudgetEntity> = emptyList(),
    val allBudgetsForBook: List<BudgetEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val attachmentsByNoteId: Map<Long, List<NoteAttachmentEntity>> = emptyMap(),
    val tasks: List<TaskEntity> = emptyList(),
    val auditLogs: List<AuditLogEntity> = emptyList(),
    val backupHistory: List<BackupHistoryEntity> = emptyList(),
    val allBookSummaries: List<BookSummaryRow> = emptyList(),
    val notifications: List<InAppNotificationItem> = emptyList(),
    val cloudSync: CloudSyncUiState = CloudSyncUiState(),
    val appUpdate: AppUpdateUiState = AppUpdateUiState(),
    val snackbarMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class JomaKhorochViewModel(
    private val container: AppContainer,
    private val currentUserId: String,
    private val userDisplayName: String,
    private val userEmail: String?
) : ViewModel() {

    private val repo = container.repository
    private val prefsRepo = container.preferences
    private val backupManager = container.backupManager
    private val cloudSyncRepo = container.cloudSyncRepository
    private val networkMonitor = container.networkMonitor
    private val deviceId = container.deviceId

    private val isSeeded = MutableStateFlow(false)
    private val splashCompletedState = MutableStateFlow(true)
    private val isUnlockedState = MutableStateFlow(false)
    private val selectedBudgetMonth = MutableStateFlow(DateUtils.currentYearMonth())
    private val snackbarMessage = MutableStateFlow<String?>(null)
    private val cloudSyncState = MutableStateFlow(
        CloudSyncUiState(
            isOnline = networkMonitor.isOnline.value,
            syncStatus = if (networkMonitor.isOnline.value) SyncStatus.IDLE else SyncStatus.OFFLINE_PENDING,
            userDisplayName = userDisplayName.ifBlank { userEmail?.substringBefore("@").orEmpty() },
            userEmail = userEmail
        )
    )
    private val appUpdateState = MutableStateFlow(AppUpdateUiState())

    private var profileObserverJob: Job? = null
    private var syncDebounceJob: Job? = null
    private var activeSyncJob: Job? = null
    private var updateCheckJob: Job? = null
    private var lastSeenCloudRevision: Long = 0L
    private var hasPendingLocalChanges: Boolean = false
    private var hasAutoCheckedUpdateThisSession: Boolean = false
    private var signingOut: Boolean = false

    private data class BookScopedData(
        val categories: List<CategoryEntity> = emptyList(),
        val shops: List<ShopEntity> = emptyList(),
        val loans: List<PersonalLoanEntity> = emptyList(),
        val transactions: List<LedgerTransactionEntity> = emptyList(),
        val budgets: List<BudgetEntity> = emptyList(),
        val allBudgets: List<BudgetEntity> = emptyList(),
        val notes: List<NoteEntity> = emptyList(),
        val attachments: List<NoteAttachmentEntity> = emptyList(),
        val tasks: List<TaskEntity> = emptyList(),
        val auditLogs: List<AuditLogEntity> = emptyList()
    )

    init {
        viewModelScope.launch {
            val defaultBookId = repo.ensureInitialized()
            val allBooks = repo.db.notebookDao().getAllNotebooksOnce()
            val initialPrefs = prefsRepo.preferencesFlow.first()
            isUnlockedState.value = !initialPrefs.isPinEnabled
            if (allBooks.none { it.id == initialPrefs.activeNotebookId && !it.isArchived }) {
                prefsRepo.setActiveNotebookId(defaultBookId)
            }
            if (!initialPrefs.onboardingCompleted) {
                val hasExistingData = initialPrefs.welcomeBannerDismissed ||
                    initialPrefs.isPinEnabled ||
                    repo.db.transactionDao().getAllTransactionsOnce().isNotEmpty()
                if (hasExistingData) {
                    prefsRepo.setOnboardingCompleted(true)
                }
            }
            isSeeded.value = true

            startNetworkObserver()
            startProfileObserver()
            runInitialCloudSync()
            checkForAppUpdate(silent = true)
        }
    }

    private fun startNetworkObserver() {
        viewModelScope.launch {
            var wasOnline = networkMonitor.isOnline.value
            networkMonitor.isOnline.collect { online ->
                cloudSyncState.update { state ->
                    val nextStatus = when {
                        !online && (hasPendingLocalChanges || state.syncStatus == SyncStatus.SYNCING) ->
                            SyncStatus.OFFLINE_PENDING
                        !online && state.syncStatus == SyncStatus.IDLE ->
                            SyncStatus.OFFLINE_PENDING
                        else -> state.syncStatus
                    }
                    state.copy(isOnline = online, syncStatus = nextStatus)
                }
                if (online && !wasOnline && !signingOut) {
                    val status = cloudSyncState.value.syncStatus
                    if (hasPendingLocalChanges || status == SyncStatus.OFFLINE_PENDING || status == SyncStatus.ERROR) {
                        triggerCloudSync(pullFirst = true)
                    }
                    if (!hasAutoCheckedUpdateThisSession) {
                        checkForAppUpdate(silent = true)
                    }
                }
                wasOnline = online
            }
        }
    }

    private fun startProfileObserver() {
        profileObserverJob?.cancel()
        if (!cloudSyncRepo.isCloudAvailable) return
        profileObserverJob = viewModelScope.launch {
            try {
                cloudSyncRepo.observeUserProfile(currentUserId).collect { profile ->
                    if (signingOut || profile == null) return@collect
                    if (profile.syncRevision > lastSeenCloudRevision) {
                        val shouldPullRemote = profile.deviceId.isNotBlank() && profile.deviceId != deviceId
                        lastSeenCloudRevision = profile.syncRevision
                        if (shouldPullRemote) {
                            pullRemoteAndRefreshActiveBook(forcePull = true)
                        }
                    }
                }
            } catch (e: CancellationException) {
                val firestoreError = generateSequence<Throwable>(e) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null) throw e
                if (!signingOut) {
                    cloudSyncState.update {
                        it.copy(
                            syncStatus = SyncStatus.ERROR,
                            syncError = firestoreError.localizedMessage ?: "Cloud sync permission error"
                        )
                    }
                }
            } catch (e: Exception) {
                if (!signingOut) {
                    cloudSyncState.update {
                        it.copy(
                            syncStatus = if (networkMonitor.isOnline.value) SyncStatus.ERROR else SyncStatus.OFFLINE_PENDING,
                            syncError = e.localizedMessage
                        )
                    }
                }
            }
        }
    }

    private fun runInitialCloudSync() {
        if (signingOut) return
        if (!cloudSyncRepo.isCloudAvailable) {
            cloudSyncState.update {
                it.copy(syncStatus = SyncStatus.IDLE, syncError = null)
            }
            return
        }
        if (!networkMonitor.isOnline.value) {
            hasPendingLocalChanges = true
            cloudSyncState.update {
                it.copy(isOnline = false, syncStatus = SyncStatus.OFFLINE_PENDING, syncError = null)
            }
            return
        }
        triggerCloudSync(pullFirst = true)
    }

    private suspend fun pullRemoteAndRefreshActiveBook(forcePull: Boolean): Boolean {
        val result = cloudSyncRepo.pullFromCloudIfNeeded(
            userId = currentUserId,
            deviceId = deviceId,
            forcePull = forcePull
        )
        val pulled = result.getOrElse { err ->
            cloudSyncState.update {
                it.copy(
                    syncStatus = if (networkMonitor.isOnline.value) SyncStatus.ERROR else SyncStatus.OFFLINE_PENDING,
                    syncError = err.localizedMessage
                )
            }
            return false
        }
        if (pulled) {
            val books = repo.db.notebookDao().getAllNotebooksOnce().filter { !it.isArchived }
            val currentActive = prefsRepo.preferencesFlow.first().activeNotebookId
            if (books.isNotEmpty() && books.none { it.id == currentActive }) {
                prefsRepo.setActiveNotebookId(books.first().id)
            }
        }
        return pulled
    }

    private fun scheduleCloudSync() {
        if (signingOut) return
        hasPendingLocalChanges = true
        if (!networkMonitor.isOnline.value) {
            cloudSyncState.update {
                it.copy(isOnline = false, syncStatus = SyncStatus.OFFLINE_PENDING, syncError = null)
            }
            return
        }
        syncDebounceJob?.cancel()
        syncDebounceJob = viewModelScope.launch {
            delay(1_200L)
            triggerCloudSync(pullFirst = false)
        }
    }

    private fun triggerCloudSync(
        pullFirst: Boolean,
        successMsg: String? = null
    ) {
        if (signingOut) return
        if (!cloudSyncRepo.isCloudAvailable) {
            cloudSyncState.update {
                it.copy(syncStatus = SyncStatus.IDLE, syncError = null)
            }
            return
        }
        if (activeSyncJob?.isActive == true) return
        activeSyncJob = viewModelScope.launch {
            cloudSyncState.update { it.copy(syncStatus = SyncStatus.SYNCING, syncError = null) }
            try {
                if (pullFirst) {
                    pullRemoteAndRefreshActiveBook(forcePull = false)
                }
                val pushResult = cloudSyncRepo.pushLocalStateToCloud(
                    userId = currentUserId,
                    displayName = userDisplayName.ifBlank { userEmail?.substringBefore("@") ?: "JomaKhoroch User" },
                    email = userEmail,
                    deviceId = deviceId
                )
                pushResult.onSuccess { outcome ->
                    hasPendingLocalChanges = false
                    lastSeenCloudRevision = maxOf(lastSeenCloudRevision, outcome.revision)
                    cloudSyncState.update {
                        it.copy(
                            isOnline = networkMonitor.isOnline.value,
                            syncStatus = if (networkMonitor.isOnline.value) SyncStatus.SYNCED else SyncStatus.OFFLINE_PENDING,
                            lastSyncedAtMillis = outcome.syncedAtMillis,
                            syncError = null,
                            syncedRecordCount = outcome.totalSyncedRecords
                        )
                    }
                    if (successMsg != null) {
                        showMessage(successMsg)
                    }
                }.onFailure { err ->
                    hasPendingLocalChanges = true
                    val onlineNow = networkMonitor.isOnline.value
                    cloudSyncState.update {
                        it.copy(
                            isOnline = onlineNow,
                            syncStatus = if (onlineNow) SyncStatus.ERROR else SyncStatus.OFFLINE_PENDING,
                            syncError = if (onlineNow) (err.localizedMessage ?: "Cloud sync failed") else null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            }
        }
    }

    fun syncNow(successMsg: String? = null, offlineMsg: String? = null) {
        if (signingOut) return
        if (!networkMonitor.isOnline.value) {
            hasPendingLocalChanges = true
            cloudSyncState.update {
                it.copy(isOnline = false, syncStatus = SyncStatus.OFFLINE_PENDING, syncError = null)
            }
            if (offlineMsg != null) showMessage(offlineMsg)
            return
        }
        syncDebounceJob?.cancel()
        triggerCloudSync(pullFirst = true, successMsg = successMsg)
    }

    fun prepareSignOut() {
        signingOut = true
        profileObserverJob?.cancel()
        syncDebounceJob?.cancel()
        activeSyncJob?.cancel()
        updateCheckJob?.cancel()
    }

    suspend fun deleteAccountAndCloudData() {
        prepareSignOut()
        runCatching { cloudSyncRepo.deleteAllUserData(currentUserId) }
    }

    override fun onCleared() {
        prepareSignOut()
        super.onCleared()
    }

    fun markSplashCompleted() {
        splashCompletedState.value = true
    }

    private val bookScopedFlow: Flow<BookScopedData> = combine(
        prefsRepo.preferencesFlow,
        selectedBudgetMonth
    ) { prefs, month -> prefs.activeNotebookId to month }
        .flatMapLatest { (bookId, month) ->
            val partA = combine(
                repo.observeCategories(bookId),
                repo.observeShops(bookId),
                repo.observeLoans(bookId),
                repo.observeTransactions(bookId),
                repo.observeBudgetsForMonth(bookId, month)
            ) { cats, shops, loans, txs, budgets ->
                BookScopedData(
                    categories = cats,
                    shops = shops,
                    loans = loans,
                    transactions = txs,
                    budgets = budgets
                )
            }
            val partB = combine(
                repo.observeAllBudgetsForBook(bookId),
                repo.observeNotes(bookId),
                repo.observeAttachments(),
                repo.observeTasks(bookId),
                repo.observeAuditLogs(bookId)
            ) { allBudgets, notes, atts, tasks, audits ->
                BookScopedData(
                    allBudgets = allBudgets,
                    notes = notes,
                    attachments = atts,
                    tasks = tasks,
                    auditLogs = audits
                )
            }
            combine(partA, partB) { a, b ->
                a.copy(
                    allBudgets = b.allBudgets,
                    notes = b.notes,
                    attachments = b.attachments,
                    tasks = b.tasks,
                    auditLogs = b.auditLogs
                )
            }
        }

    private data class StartupAndServiceFlags(
        val seeded: Boolean,
        val splashCompleted: Boolean,
        val cloudSync: CloudSyncUiState,
        val appUpdate: AppUpdateUiState
    )

    private val startupFlagsFlow = combine(
        isSeeded,
        splashCompletedState,
        cloudSyncState,
        appUpdateState
    ) { seeded, splashDone, cloudSync, appUpdate ->
        StartupAndServiceFlags(seeded, splashDone, cloudSync, appUpdate)
    }

    private val coreMetaFlow = combine(
        startupFlagsFlow,
        prefsRepo.preferencesFlow,
        isUnlockedState,
        selectedBudgetMonth,
        snackbarMessage
    ) { flags, prefs, unlocked, budgetMonth, snack ->
        val effectiveRepo = prefs.updateRepoOverride.ifBlank { BuildConfig.GITHUB_RELEASE_REPO }.trim()
        CoreMeta(
            seeded = flags.seeded,
            splashCompleted = flags.splashCompleted,
            prefs = prefs,
            unlocked = unlocked,
            budgetMonth = budgetMonth,
            snack = snack,
            cloudSync = flags.cloudSync,
            appUpdate = flags.appUpdate.copy(configuredRepo = effectiveRepo)
        )
    }

    private data class CoreMeta(
        val seeded: Boolean,
        val splashCompleted: Boolean,
        val prefs: AppPreferences,
        val unlocked: Boolean,
        val budgetMonth: String,
        val snack: String?,
        val cloudSync: CloudSyncUiState,
        val appUpdate: AppUpdateUiState
    )

    val uiState: StateFlow<JomaKhorochUiState> = combine(
        coreMetaFlow,
        repo.observeAllNotebooks(),
        repo.observeAllActiveTransactions(),
        repo.observeBackupHistory(),
        bookScopedFlow
    ) { meta, allNotebooks, allActiveTxs, backupHistory, scoped ->
        val activeNotebooks = allNotebooks.filter { !it.isArchived }
        val activeBook = activeNotebooks.firstOrNull { it.id == meta.prefs.activeNotebookId }
            ?: activeNotebooks.firstOrNull()
        val ledgerRecords = scoped.transactions.map { it.toLedgerRecord() }

        val shopsWithBalance = scoped.shops.map { shop ->
            val bal = AccountingEngine.computeShopBalance(
                ShopOpeningRecord(shop.id, shop.openingBalancePoisha),
                ledgerRecords
            )
            ShopWithBalance(shop, bal)
        }

        val allLoansWithBalance = scoped.loans.map { loan ->
            val dir = LoanDirection.valueOf(loan.direction)
            val bal = AccountingEngine.computeLoanBalance(
                LoanPrincipalRecord(loan.id, dir, loan.principalPoisha),
                ledgerRecords
            )
            LoanWithBalance(loan, bal)
        }

        val summary = AccountingEngine.computeBookSummary(
            openingBalancePoisha = activeBook?.openingBalancePoisha ?: 0L,
            shops = scoped.shops.map { ShopOpeningRecord(it.id, it.openingBalancePoisha) },
            loans = scoped.loans.map { LoanPrincipalRecord(it.id, LoanDirection.valueOf(it.direction), it.principalPoisha) },
            entries = ledgerRecords
        )

        val (monthStart, monthEnd) = DateUtils.monthRange(DateUtils.currentYearMonth())
        val (monthInc, monthExp) = AccountingEngine.computePeriodIncomeExpense(
            ledgerRecords,
            monthStart,
            monthEnd
        )

        val txByBook = allActiveTxs.groupBy { it.notebookId }
        val bookSummaryRows = activeNotebooks.map { book ->
            val bookTxs = txByBook[book.id].orEmpty()
            val entries = bookTxs.map { it.toLedgerRecord() }
            val bookSum = AccountingEngine.computeBookSummary(
                openingBalancePoisha = book.openingBalancePoisha,
                shops = emptyList(),
                loans = emptyList(),
                entries = entries
            )
            BookSummaryRow(
                notebook = book,
                incomePoisha = bookSum.totalIncomePoisha,
                expensePoisha = bookSum.totalExpensePoisha,
                cashBalancePoisha = bookSum.currentCashBalancePoisha,
                transactionCount = bookTxs.size,
                lastTransactionDate = bookTxs.maxOfOrNull { it.transactionDate }
            )
        }

        val inAppAlerts = buildInAppNotifications(
            prefs = meta.prefs,
            appUpdate = meta.appUpdate,
            categories = scoped.categories,
            allBudgets = scoped.allBudgets,
            transactions = scoped.transactions,
            loans = allLoansWithBalance,
            shops = shopsWithBalance,
            tasks = scoped.tasks
        )

        JomaKhorochUiState(
            isReady = meta.seeded && activeBook != null,
            splashCompleted = meta.splashCompleted,
            preferences = meta.prefs,
            isUnlocked = !meta.prefs.isPinEnabled || meta.unlocked,
            activeNotebook = activeBook,
            activeNotebooks = activeNotebooks,
            allNotebooks = allNotebooks,
            categories = scoped.categories,
            shops = shopsWithBalance,
            lendingLoans = allLoansWithBalance.filter { it.loan.direction == LoanDirection.GIVEN.name },
            borrowingLoans = allLoansWithBalance.filter { it.loan.direction == LoanDirection.BORROWED.name },
            transactions = scoped.transactions,
            financialSummary = summary,
            currentMonthIncomePoisha = monthInc,
            currentMonthExpensePoisha = monthExp,
            selectedBudgetMonth = meta.budgetMonth,
            budgets = scoped.budgets,
            allBudgetsForBook = scoped.allBudgets,
            notes = scoped.notes,
            attachmentsByNoteId = scoped.attachments.groupBy { it.noteId },
            tasks = scoped.tasks,
            auditLogs = scoped.auditLogs,
            backupHistory = backupHistory,
            allBookSummaries = bookSummaryRows,
            notifications = inAppAlerts,
            cloudSync = meta.cloudSync.copy(
                userDisplayName = meta.prefs.profileNameOverride.ifBlank { meta.cloudSync.userDisplayName }
            ),
            appUpdate = meta.appUpdate,
            snackbarMessage = meta.snack
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = JomaKhorochUiState()
    )

    fun observeTransactionItems(txId: Long): Flow<List<TransactionItemEntity>> =
        repo.observeItemsForTransaction(txId)

    fun showMessage(msg: String) {
        snackbarMessage.value = msg
    }

    fun clearMessage() {
        snackbarMessage.value = null
    }

    // Session & Preferences
    fun selectNotebook(notebookId: Long) = viewModelScope.launch {
        prefsRepo.setActiveNotebookId(notebookId)
    }

    fun setLanguage(language: AppLanguage) = viewModelScope.launch {
        prefsRepo.setLanguage(language)
    }

    fun setNumeralStyle(style: NumeralStyle) = viewModelScope.launch {
        prefsRepo.setNumeralStyle(style)
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        prefsRepo.setThemeMode(mode)
    }

    fun toggleHideBalance() = viewModelScope.launch {
        prefsRepo.setHideBalance(!uiState.value.preferences.hideBalance)
    }

    fun dismissWelcomeBanner() = viewModelScope.launch {
        prefsRepo.dismissWelcomeBanner()
    }

    fun setSelectedBudgetMonth(yearMonth: String) {
        selectedBudgetMonth.value = yearMonth
    }

    fun updateUserProfile(displayName: String, businessName: String, savedMsg: String) = viewModelScope.launch {
        prefsRepo.setProfileDetails(displayName, businessName)
        showMessage(savedMsg)
    }

    fun updateProfilePhoto(context: Context, uri: Uri?, savedMsg: String, errorMsg: String) = viewModelScope.launch {
        val previousPath = uiState.value.preferences.profilePhotoPath
        if (uri == null) {
            if (previousPath.isNotBlank()) {
                AttachmentStorage.deleteInternalAttachment(context, previousPath)
            }
            prefsRepo.setProfilePhotoPath("")
            showMessage(savedMsg)
            return@launch
        }
        val copied = AttachmentStorage.copyPickedImageToInternal(context, uri)
        copied.onSuccess { newPath ->
            if (previousPath.isNotBlank() && previousPath != newPath) {
                AttachmentStorage.deleteInternalAttachment(context, previousPath)
            }
            prefsRepo.setProfilePhotoPath(newPath)
            showMessage(savedMsg)
        }.onFailure {
            showMessage(errorMsg)
        }
    }

    // Security & PIN Lock
    fun verifyAndUnlock(pin: String): Boolean {
        val prefs = uiState.value.preferences
        val ok = PinSecurityManager.verifyPin(pin, prefs.pinSalt, prefs.pinHash)
        if (ok) isUnlockedState.value = true
        return ok
    }

    fun unlockViaBiometric() {
        isUnlockedState.value = true
    }

    fun lockNow() {
        if (uiState.value.preferences.isPinEnabled) {
            isUnlockedState.value = false
        }
    }

    fun enablePin(pin: String, successMsg: String) = viewModelScope.launch {
        val hashed = PinSecurityManager.hashPin(pin)
        prefsRepo.savePinCredentials(hashed.saltBase64, hashed.hashBase64)
        isUnlockedState.value = true
        showMessage(successMsg)
    }

    fun disablePin(successMsg: String) = viewModelScope.launch {
        prefsRepo.clearPinCredentials()
        isUnlockedState.value = true
        showMessage(successMsg)
    }

    fun setBiometricEnabled(enabled: Boolean) = viewModelScope.launch {
        prefsRepo.setBiometricEnabled(enabled)
    }

    // Notification System Preferences & Triggers
    fun setNotificationsEnabled(context: Context, enabled: Boolean) = viewModelScope.launch {
        prefsRepo.setNotificationsEnabled(enabled)
        val prefs = prefsRepo.preferencesFlow.first()
        val isBn = prefs.language == AppLanguage.BANGLA
        if (!enabled) {
            ReminderScheduler.cancelDailyReminder(context)
        } else if (prefs.dailyReminderEnabled) {
            ReminderScheduler.scheduleDailyReminder(context, prefs.dailyReminderHour, prefs.dailyReminderMinute, isBn)
        }
    }

    fun setBudgetAlertsEnabled(enabled: Boolean) = viewModelScope.launch {
        prefsRepo.setBudgetAlertsEnabled(enabled)
    }

    fun setLoanDueAlertsEnabled(context: Context, enabled: Boolean) = viewModelScope.launch {
        prefsRepo.setLoanDueAlertsEnabled(enabled)
        val prefs = prefsRepo.preferencesFlow.first()
        val isBn = prefs.language == AppLanguage.BANGLA
        val now = System.currentTimeMillis()
        repo.db.loanDao().getAllLoansOnce().forEach { loan ->
            if (enabled && prefs.notificationsEnabled && !loan.isSettled && (loan.dueDate ?: 0L) > now) {
                loan.dueDate?.let { due ->
                    ReminderScheduler.scheduleLoanDueReminder(
                        context = context,
                        loanId = loan.id,
                        personName = loan.personName,
                        isLending = loan.direction == LoanDirection.GIVEN.name,
                        dueDateMillis = due,
                        isBangla = isBn
                    )
                }
            } else {
                ReminderScheduler.cancelLoanDueReminder(context, loan.id)
            }
        }
    }

    fun setDailyReminder(
        context: Context,
        enabled: Boolean,
        hour: Int? = null,
        minute: Int? = null
    ) = viewModelScope.launch {
        prefsRepo.setDailyReminder(enabled, hour, minute)
        val prefs = prefsRepo.preferencesFlow.first()
        val isBn = prefs.language == AppLanguage.BANGLA
        if (enabled && prefs.notificationsEnabled) {
            ReminderScheduler.scheduleDailyReminder(
                context = context,
                hour = hour ?: prefs.dailyReminderHour,
                minute = minute ?: prefs.dailyReminderMinute,
                isBangla = isBn
            )
        } else {
            ReminderScheduler.cancelDailyReminder(context)
        }
    }

    fun sendTestNotification(context: Context, sentMsg: String, deniedMsg: String) {
        val isBn = uiState.value.preferences.language == AppLanguage.BANGLA
        val ok = ReminderScheduler.sendTestNotification(context, isBn)
        showMessage(if (ok) sentMsg else deniedMsg)
    }

    // App Update & Release Notifications
    fun setUpdateAlertsEnabled(enabled: Boolean) = viewModelScope.launch {
        prefsRepo.setUpdateAlertsEnabled(enabled)
    }

    fun dismissUpdateBanner(versionName: String) = viewModelScope.launch {
        prefsRepo.setDismissedUpdateVersion(versionName)
    }

    fun setUpdateRepoOverride(rawInput: String, savedMsg: String, invalidMsg: String) = viewModelScope.launch {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            prefsRepo.setUpdateRepoOverride("")
            appUpdateState.update { it.copy(statusMessage = null, availableRelease = null, isUpToDate = false) }
            showMessage(savedMsg)
            return@launch
        }
        val normalized = AppUpdateChecker.normalizeGitHubRepo(trimmed)
        if (normalized == null) {
            showMessage(invalidMsg)
            return@launch
        }
        prefsRepo.setUpdateRepoOverride(normalized)
        showMessage(savedMsg)
        checkForAppUpdate(silent = false)
    }

    fun checkForAppUpdate(silent: Boolean = false) {
        if (signingOut) return
        if (updateCheckJob?.isActive == true) return
        updateCheckJob = viewModelScope.launch {
            val prefs = prefsRepo.preferencesFlow.first()
            val isBn = prefs.language == AppLanguage.BANGLA
            val effectiveRepo = prefs.updateRepoOverride.ifBlank { BuildConfig.GITHUB_RELEASE_REPO }.trim()

            if (effectiveRepo.isBlank()) {
                appUpdateState.update {
                    it.copy(
                        isChecking = false,
                        statusMessage = if (silent) null else if (isBn) {
                            "আপডেট চেক করতে GitHub রিপোজিটরি (owner/repo) যুক্ত করুন"
                        } else {
                            "Set your GitHub repository (owner/repo) to check for releases"
                        }
                    )
                }
                if (!silent) {
                    showMessage(
                        if (isBn) "আগে GitHub রিপোজিটরি (owner/repo) সেট করুন"
                        else "Please configure your GitHub repository (owner/repo) first"
                    )
                }
                return@launch
            }

            if (!networkMonitor.isOnline.value) {
                val msg = if (isBn) "অফলাইনে আছেন — ইন্টারনেট চালু করে আপডেট চেক করুন"
                else "You are offline — connect to the internet to check for updates"
                appUpdateState.update { it.copy(isChecking = false, statusMessage = if (silent) it.statusMessage else msg) }
                if (!silent) showMessage(msg)
                return@launch
            }

            appUpdateState.update { it.copy(isChecking = true, statusMessage = null) }
            val result = AppUpdateChecker.checkLatestGitHubRelease(
                repoInput = effectiveRepo,
                currentVersion = BuildConfig.VERSION_NAME
            )
            val checkedAt = System.currentTimeMillis()
            hasAutoCheckedUpdateThisSession = true

            when (result) {
                is UpdateCheckResult.UpdateAvailable -> {
                    val release = result.release
                    val foundMsg = if (isBn) "নতুন ভার্সন v${release.versionName} পাওয়া গেছে!"
                    else "New version v${release.versionName} is available!"
                    appUpdateState.update {
                        it.copy(
                            isChecking = false,
                            availableRelease = release,
                            lastCheckedAtMillis = checkedAt,
                            statusMessage = foundMsg,
                            isUpToDate = false
                        )
                    }
                    if (!silent) showMessage(foundMsg)

                    if (prefs.notificationsEnabled &&
                        prefs.updateAlertsEnabled &&
                        prefs.lastNotifiedUpdateVersion != release.versionName
                    ) {
                        val posted = ReminderScheduler.notifyAppUpdateAvailable(
                            context = container.appContext,
                            versionName = release.versionName,
                            releaseNotes = release.releaseNotes,
                            isBangla = isBn
                        )
                        if (posted) {
                            prefsRepo.setLastNotifiedUpdateVersion(release.versionName)
                        }
                    }
                }
                is UpdateCheckResult.UpToDate -> {
                    val upToDateMsg = if (isBn) "আপনি সর্বশেষ ভার্সন (v${BuildConfig.VERSION_NAME}) ব্যবহার করছেন"
                    else "You are on the latest version (v${BuildConfig.VERSION_NAME})"
                    appUpdateState.update {
                        it.copy(
                            isChecking = false,
                            availableRelease = null,
                            lastCheckedAtMillis = checkedAt,
                            statusMessage = upToDateMsg,
                            isUpToDate = true
                        )
                    }
                    if (!silent) showMessage(upToDateMsg)
                }
                UpdateCheckResult.RepoNotConfigured -> {
                    val msg = if (isBn) "সঠিক GitHub রিপোজিটরি (owner/repo) দিন"
                    else "Please enter a valid GitHub repository (owner/repo)"
                    appUpdateState.update { it.copy(isChecking = false, statusMessage = if (silent) null else msg) }
                    if (!silent) showMessage(msg)
                }
                UpdateCheckResult.Offline -> {
                    val msg = if (isBn) "ইন্টারনেট সংযোগ নেই" else "No internet connection"
                    appUpdateState.update { it.copy(isChecking = false, statusMessage = if (silent) null else msg) }
                    if (!silent) showMessage(msg)
                }
                is UpdateCheckResult.Error -> {
                    val msg = when (result.message) {
                        "NO_RELEASE_FOUND" -> if (isBn) "এখনো কোনো GitHub রিলিজ প্রকাশিত হয়নি" else "No published GitHub release found yet"
                        "RATE_LIMITED" -> if (isBn) "অনেকবার চেষ্টা হয়েছে, কিছুক্ষণ পর আবার দেখুন" else "GitHub API limit reached, try again shortly"
                        else -> if (isBn) "আপডেট যাচাই করা যায়নি — পরে চেষ্টা করুন" else "Could not check for updates right now"
                    }
                    appUpdateState.update {
                        it.copy(
                            isChecking = false,
                            lastCheckedAtMillis = checkedAt,
                            statusMessage = if (silent) it.statusMessage else msg
                        )
                    }
                    if (!silent) showMessage(msg)
                }
            }
        }
    }

    private fun buildInAppNotifications(
        prefs: AppPreferences,
        appUpdate: AppUpdateUiState,
        categories: List<CategoryEntity>,
        allBudgets: List<BudgetEntity>,
        transactions: List<LedgerTransactionEntity>,
        loans: List<LoanWithBalance>,
        shops: List<ShopWithBalance>,
        tasks: List<TaskEntity>
    ): List<InAppNotificationItem> {
        val isBn = prefs.language == AppLanguage.BANGLA
        val numerals = prefs.numeralStyle
        val now = System.currentTimeMillis()
        val threeDaysMs = 3L * 24L * 60L * 60L * 1000L
        val items = mutableListOf<InAppNotificationItem>()

        // 0. App Update Release Notification (highest priority when newer version exists)
        appUpdate.availableRelease?.let { release ->
            items.add(
                InAppNotificationItem(
                    id = "app_update_${release.versionName}",
                    type = NotificationAlertType.APP_UPDATE,
                    title = if (isBn) "নতুন আপডেট পাওয়া গেছে: v${release.versionName}" else "New App Update Available: v${release.versionName}",
                    subtitle = if (isBn) {
                        "বর্তমান ভার্সন v${appUpdate.currentVersion} • ডাউনলোড ও আপডেট করতে ট্যাপ করুন"
                    } else {
                        "Current v${appUpdate.currentVersion} • Tap to view changes & update"
                    },
                    timestampMillis = appUpdate.lastCheckedAtMillis ?: now,
                    isUrgent = true
                )
            )
        }

        // 1. Budget Warnings & Exceeded Alerts (current month)
        if (prefs.budgetAlertsEnabled) {
            val currentMonth = DateUtils.currentYearMonth(now)
            val (monthStart, monthEnd) = DateUtils.monthRange(currentMonth)
            val monthExpenses = transactions.filter {
                !it.isDeleted &&
                    it.transactionDate in monthStart..monthEnd &&
                    (it.type == LedgerTransactionType.EXPENSE.name || it.type == LedgerTransactionType.SHOP_PURCHASE.name)
            }
            val catsById = categories.associateBy { it.id }
            allBudgets.filter { it.yearMonth == currentMonth && it.limitPoisha > 0L }.forEach { budget ->
                val spent = if (budget.categoryId == null) {
                    monthExpenses.sumOf { it.totalAmountPoisha }
                } else {
                    monthExpenses.filter { it.categoryId == budget.categoryId }.sumOf { it.totalAmountPoisha }
                }
                val ratio = spent.toDouble() / budget.limitPoisha.toDouble()
                if (ratio >= 0.80) {
                    val catName = budget.categoryId?.let { id ->
                        val cat = catsById[id]
                        if (isBn) cat?.nameBn else cat?.nameEn
                    } ?: if (isBn) "মাসিক মূল বাজেট" else "Monthly Budget"
                    val pct = com.example.core.util.CurrencyFormatter.toLocalizedDigits("${(ratio * 100).toInt()}%", numerals)
                    val spentStr = com.example.core.util.CurrencyFormatter.formatPoisha(spent, numerals)
                    val limitStr = com.example.core.util.CurrencyFormatter.formatPoisha(budget.limitPoisha, numerals)
                    val exceeded = ratio >= 1.0
                    items.add(
                        InAppNotificationItem(
                            id = "budget_${budget.id}",
                            type = if (exceeded) NotificationAlertType.BUDGET_EXCEEDED else NotificationAlertType.BUDGET_WARNING,
                            title = if (exceeded) {
                                if (isBn) "$catName অতিক্রম করেছে ($pct)" else "$catName exceeded ($pct)"
                            } else {
                                if (isBn) "$catName সতর্কতা ($pct)" else "$catName warning ($pct)"
                            },
                            subtitle = if (isBn) "খরচ: $spentStr / সীমা: $limitStr" else "Spent $spentStr of $limitStr",
                            timestampMillis = budget.updatedAt,
                            isUrgent = exceeded
                        )
                    )
                }
            }
        }

        // 2. Loan Due-Date Alerts (overdue or due within 3 days)
        if (prefs.loanDueAlertsEnabled) {
            loans.filter { !it.balance.isSettled && it.loan.dueDate != null }.forEach { loanWrap ->
                val due = loanWrap.loan.dueDate ?: return@forEach
                if (due <= now + threeDaysMs) {
                    val overdue = due < DateUtils.startOfDay(now)
                    val isLending = loanWrap.loan.direction == LoanDirection.GIVEN.name
                    val remStr = com.example.core.util.CurrencyFormatter.formatPoisha(loanWrap.balance.remainingPoisha, numerals)
                    val dateStr = DateUtils.formatDate(due, prefs.language, numerals)
                    items.add(
                        InAppNotificationItem(
                            id = "loan_${loanWrap.loan.id}",
                            type = NotificationAlertType.LOAN_DUE,
                            title = if (isBn) {
                                if (isLending) "পাওনা আদায়: ${loanWrap.loan.personName}" else "ধার পরিশোধ: ${loanWrap.loan.personName}"
                            } else {
                                if (isLending) "Receivable due: ${loanWrap.loan.personName}" else "Loan due: ${loanWrap.loan.personName}"
                            },
                            subtitle = if (isBn) {
                                val tag = if (overdue) "সময় অতিক্রান্ত" else "তারিখ: $dateStr"
                                "বাকি $remStr • $tag"
                            } else {
                                val tag = if (overdue) "Overdue ($dateStr)" else "Due $dateStr"
                                "$remStr remaining • $tag"
                            },
                            timestampMillis = due,
                            isUrgent = overdue
                        )
                    )
                }
            }
        }

        // 3. Pending Tasks & Scheduled Reminders
        tasks.filter { !it.isCompleted }.forEach { task ->
            val targetTime = task.reminderAt ?: task.dueAt ?: task.createdAt
            val overdue = (task.dueAt ?: Long.MAX_VALUE) < now
            val dateStr = (task.reminderAt ?: task.dueAt)?.let { DateUtils.formatDateTime(it, prefs.language, numerals) }
            items.add(
                InAppNotificationItem(
                    id = "task_${task.id}",
                    type = NotificationAlertType.TASK_REMINDER,
                    title = task.title,
                    subtitle = dateStr ?: (task.description.ifBlank {
                        if (isBn) "বকেয়া কাজ / রিমাইন্ডার" else "Pending task reminder"
                    }),
                    timestampMillis = targetTime,
                    isUrgent = overdue
                )
            )
        }

        // 4. Shop Outstanding Dues Summary (when due > 0)
        val dueShops = shops.filter { it.balance.outstandingDuePoisha > 0L }
        if (dueShops.isNotEmpty()) {
            val totalShopDue = dueShops.sumOf { it.balance.outstandingDuePoisha }
            val countStr = com.example.core.util.CurrencyFormatter.toLocalizedDigits(dueShops.size.toString(), numerals)
            val dueStr = com.example.core.util.CurrencyFormatter.formatPoisha(totalShopDue, numerals)
            items.add(
                InAppNotificationItem(
                    id = "shop_dues_summary",
                    type = NotificationAlertType.SHOP_DUE,
                    title = if (isBn) "দোকানের মোট বকেয়া: $dueStr" else "Shop Dues Outstanding: $dueStr",
                    subtitle = if (isBn) "$countStr টি দোকানে বাকি পরিশোধের অপেক্ষায়" else "$countStr shop(s) with pending credit due",
                    timestampMillis = now,
                    isUrgent = false
                )
            )
        }

        return items.sortedWith(compareByDescending<InAppNotificationItem> { it.isUrgent }.thenByDescending { it.timestampMillis })
    }

    private suspend fun checkAndSendBudgetAlertNotification(bookId: Long, categoryId: Long?) {
        val prefs = prefsRepo.preferencesFlow.first()
        if (!prefs.notificationsEnabled || !prefs.budgetAlertsEnabled) return
        val isBn = prefs.language == AppLanguage.BANGLA
        val numerals = prefs.numeralStyle
        val currentMonth = DateUtils.currentYearMonth()
        val (monthStart, monthEnd) = DateUtils.monthRange(currentMonth)

        val budgets = repo.db.budgetDao().getAllBudgetsOnce()
            .filter { it.notebookId == bookId && it.yearMonth == currentMonth && it.limitPoisha > 0L }
        if (budgets.isEmpty()) return

        val monthExpenses = repo.db.transactionDao().getAllTransactionsOnce().filter {
            !it.isDeleted &&
                it.notebookId == bookId &&
                it.transactionDate in monthStart..monthEnd &&
                (it.type == LedgerTransactionType.EXPENSE.name || it.type == LedgerTransactionType.SHOP_PURCHASE.name)
        }
        val catsById = repo.db.categoryDao().getAllCategoriesOnce().associateBy { it.id }

        budgets.filter { it.categoryId == null || it.categoryId == categoryId }.forEach { budget ->
            val spent = if (budget.categoryId == null) {
                monthExpenses.sumOf { it.totalAmountPoisha }
            } else {
                monthExpenses.filter { it.categoryId == budget.categoryId }.sumOf { it.totalAmountPoisha }
            }
            val ratio = spent.toDouble() / budget.limitPoisha.toDouble()
            if (ratio >= 0.80) {
                val catName = budget.categoryId?.let { id ->
                    val cat = catsById[id]
                    if (isBn) cat?.nameBn else cat?.nameEn
                } ?: if (isBn) "মাসিক বাজেট" else "Monthly Budget"
                val pct = com.example.core.util.CurrencyFormatter.toLocalizedDigits("${(ratio * 100).toInt()}%", numerals)
                val spentStr = com.example.core.util.CurrencyFormatter.formatPoisha(spent, numerals)
                val limitStr = com.example.core.util.CurrencyFormatter.formatPoisha(budget.limitPoisha, numerals)
                val title = if (ratio >= 1.0) {
                    if (isBn) "$catName অতিক্রম করেছে ($pct)" else "$catName Exceeded ($pct)"
                } else {
                    if (isBn) "$catName সতর্কতা ($pct)" else "$catName Warning ($pct)"
                }
                val body = if (isBn) {
                    "এই মাসে মোট খরচ $spentStr (নির্ধারিত সীমা $limitStr)।"
                } else {
                    "Spent $spentStr out of $limitStr this month."
                }
                ReminderScheduler.notifyBudgetAlert(container.appContext, budget.id, title, body)
            }
        }
    }

    // Account Books
    fun createNotebook(
        name: String,
        type: String,
        iconKey: String,
        openingPoisha: Long,
        successMsg: String,
        onDone: () -> Unit = {}
    ) = viewModelScope.launch {
        repo.createNotebook(name, type, iconKey, openingPoisha)
            .onSuccess { newId ->
                prefsRepo.setActiveNotebookId(newId)
                scheduleCloudSync()
                showMessage(successMsg)
                onDone()
            }
            .onFailure { showMessage(it.message ?: "Error") }
    }

    fun updateOpeningBalance(openingPoisha: Long, successMsg: String) = viewModelScope.launch {
        val bookId = uiState.value.activeNotebook?.id ?: return@launch
        repo.updateNotebookOpeningBalance(bookId, openingPoisha)
            .onSuccess {
                scheduleCloudSync()
                showMessage(successMsg)
            }
            .onFailure { showMessage(it.message ?: "Error") }
    }

    fun archiveNotebook(bookId: Long, successMsg: String, lastBookError: String) = viewModelScope.launch {
        repo.archiveNotebook(bookId)
            .onSuccess { nextActive ->
                if (uiState.value.activeNotebook?.id == bookId) {
                    prefsRepo.setActiveNotebookId(nextActive)
                }
                scheduleCloudSync()
                showMessage(successMsg)
            }
            .onFailure {
                showMessage(if (it.message == "CANNOT_DELETE_LAST_BOOK") lastBookError else it.message ?: "Error")
            }
    }

    fun deleteNotebookPermanently(bookId: Long, successMsg: String, lastBookError: String) = viewModelScope.launch {
        repo.permanentlyDeleteNotebook(bookId)
            .onSuccess { nextActive ->
                if (uiState.value.activeNotebook?.id == bookId) {
                    prefsRepo.setActiveNotebookId(nextActive)
                }
                scheduleCloudSync()
                showMessage(successMsg)
            }
            .onFailure {
                showMessage(if (it.message == "CANNOT_DELETE_LAST_BOOK") lastBookError else it.message ?: "Error")
            }
    }

    // Categories
    fun addCategory(nameBn: String, nameEn: String, type: String, successMsg: String) = viewModelScope.launch {
        val bookId = uiState.value.activeNotebook?.id ?: return@launch
        repo.addCustomCategory(bookId, nameBn, nameEn, type)
            .onSuccess {
                scheduleCloudSync()
                showMessage(successMsg)
            }
            .onFailure { showMessage(it.message ?: "Error") }
    }

    fun deleteCategory(categoryId: Long, successMsg: String) = viewModelScope.launch {
        repo.deleteCategory(categoryId)
        scheduleCloudSync()
        showMessage(successMsg)
    }

    // Income / Expense
    fun addIncomeOrExpense(
        type: LedgerTransactionType,
        amountPoisha: Long,
        categoryId: Long?,
        paymentMethod: String,
        note: String,
        dateMillis: Long,
        successMsg: String,
        onDone: () -> Unit
    ) = viewModelScope.launch {
        val bookId = uiState.value.activeNotebook?.id ?: return@launch
        repo.recordIncomeOrExpense(bookId, type, amountPoisha, categoryId, paymentMethod, note, dateMillis)
            .onSuccess {
                if (type == LedgerTransactionType.EXPENSE) {
                    checkAndSendBudgetAlertNotification(bookId, categoryId)
                }
                scheduleCloudSync()
                showMessage(successMsg)
                onDone()
            }
            .onFailure { showMessage(it.message ?: "Error") }
    }

    // Shops
    fun saveShop(
        existingId: Long = 0L,
        name: String,
        ownerName: String,
        phone: String,
        address: String,
        shopCategory: String,
        openingPoisha: Long,
        notes: String,
        successMsg: String,
        onDone: () -> Unit
    ) = viewModelScope.launch {
        val bookId = uiState.value.activeNotebook?.id ?: return@launch
        repo.saveShop(
            ShopEntity(
                id = existingId,
                notebookId = bookId,
                name = name,
                ownerName = ownerName,
                phone = phone,
                address = address,
                shopCategory = shopCategory,
                openingBalancePoisha = openingPoisha,
                notes = notes
            )
        ).onSuccess {
            scheduleCloudSync()
            showMessage(successMsg)
            onDone()
        }.onFailure { showMessage(it.message ?: "Error") }
    }

    fun addShopPurchase(
        shopId: Long,
        totalPoisha: Long,
        paidPoisha: Long,
        categoryId: Long?,
        paymentMethod: String,
        note: String,
        dateMillis: Long,
        items: List<PurchaseLineItemInput>,
        successMsg: String,
        onDone: () -> Unit
    ) = viewModelScope.launch {
        val bookId = uiState.value.activeNotebook?.id ?: return@launch
        repo.recordShopPurchase(
            notebookId = bookId,
            shopId = shopId,
            totalAmountPoisha = totalPoisha,
            paidAmountPoisha = paidPoisha,
            categoryId = categoryId,
            paymentMethod = paymentMethod,
            note = note,
            transactionDate = dateMillis,
            items = items
        ).onSuccess {
            checkAndSendBudgetAlertNotification(bookId, categoryId)
            scheduleCloudSync()
            showMessage(successMsg)
            onDone()
        }.onFailure { showMessage(it.message ?: "Error") }
    }

    fun recordShopPayment(
        shopId: Long,
        paymentPoisha: Long,
        paymentMethod: String,
        note: String,
        dateMillis: Long,
        successMsg: String,
        onDone: () -> Unit
    ) = viewModelScope.launch {
        val bookId = uiState.value.activeNotebook?.id ?: return@launch
        repo.recordShopDuePayment(bookId, shopId, paymentPoisha, paymentMethod, note, dateMillis)
            .onSuccess {
                scheduleCloudSync()
                showMessage(successMsg)
                onDone()
            }
            .onFailure { showMessage(it.message ?: "Error") }
    }

    // Personal Lending & Borrowing
    fun addPersonalLoan(
        direction: LoanDirection,
        personName: String,
        phone: String,
        principalPoisha: Long,
        loanDate: Long,
        dueDate: Long?,
        description: String,
        paymentMethod: String,
        successMsg: String,
        onDone: () -> Unit
    ) = viewModelScope.launch {
        val bookId = uiState.value.activeNotebook?.id ?: return@launch
        repo.createPersonalLoan(
            notebookId = bookId,
            direction = direction,
            personName = personName,
            phone = phone,
            principalPoisha = principalPoisha,
            loanDate = loanDate,
            dueDate = dueDate,
            description = description,
            paymentMethod = paymentMethod
        ).onSuccess { loanId ->
            val prefs = prefsRepo.preferencesFlow.first()
            if (dueDate != null && prefs.notificationsEnabled && prefs.loanDueAlertsEnabled) {
                ReminderScheduler.scheduleLoanDueReminder(
                    context = container.appContext,
                    loanId = loanId,
                    personName = personName.trim(),
                    isLending = direction == LoanDirection.GIVEN,
                    dueDateMillis = dueDate,
                    isBangla = prefs.language == AppLanguage.BANGLA
                )
            }
            scheduleCloudSync()
            showMessage(successMsg)
            onDone()
        }.onFailure { showMessage(it.message ?: "Error") }
    }

    fun recordLoanRepayment(
        loanId: Long,
        paymentPoisha: Long,
        paymentMethod: String,
        note: String,
        dateMillis: Long,
        successMsg: String,
        onDone: () -> Unit
    ) = viewModelScope.launch {
        val bookId = uiState.value.activeNotebook?.id ?: return@launch
        repo.recordLoanRepayment(bookId, loanId, paymentPoisha, paymentMethod, note, dateMillis)
            .onSuccess {
                val updatedLoan = repo.db.loanDao().getLoanById(loanId)
                if (updatedLoan?.isSettled == true) {
                    ReminderScheduler.cancelLoanDueReminder(container.appContext, loanId)
                }
                scheduleCloudSync()
                showMessage(successMsg)
                onDone()
            }
            .onFailure { showMessage(it.message ?: "Error") }
    }

    // Transaction Edit / Soft Delete with Audit Log
    fun updateTransaction(
        txId: Long,
        newTotalPoisha: Long,
        newPaidPoisha: Long,
        newNote: String,
        newCategoryId: Long?,
        newPaymentMethod: String,
        newDate: Long,
        reason: String,
        successMsg: String,
        onDone: () -> Unit
    ) = viewModelScope.launch {
        repo.updateTransactionWithAudit(
            txId, newTotalPoisha, newPaidPoisha, newNote, newCategoryId, newPaymentMethod, newDate, reason
        ).onSuccess {
            scheduleCloudSync()
            showMessage(successMsg)
            onDone()
        }.onFailure { showMessage(it.message ?: "Error") }
    }

    fun deleteTransaction(txId: Long, reason: String, successMsg: String) = viewModelScope.launch {
        repo.softDeleteTransactionWithAudit(txId, reason)
            .onSuccess {
                scheduleCloudSync()
                showMessage(successMsg)
            }
            .onFailure { showMessage(it.message ?: "Error") }
    }

    // Budgets
    fun saveBudget(categoryId: Long?, limitPoisha: Long, successMsg: String) = viewModelScope.launch {
        val bookId = uiState.value.activeNotebook?.id ?: return@launch
        repo.upsertBudget(bookId, selectedBudgetMonth.value, categoryId, limitPoisha)
            .onSuccess {
                scheduleCloudSync()
                showMessage(successMsg)
            }
            .onFailure { showMessage(it.message ?: "Error") }
    }

    fun deleteBudget(budgetId: Long, successMsg: String) = viewModelScope.launch {
        repo.deleteBudget(budgetId)
        scheduleCloudSync()
        showMessage(successMsg)
    }

    // Diary Notes & Attachments
    fun saveNote(
        existingId: Long = 0L,
        existingCreatedAt: Long = System.currentTimeMillis(),
        title: String,
        content: String,
        category: String,
        checklistJson: String,
        contactPhone: String,
        contactAddress: String,
        isPinned: Boolean,
        pendingPhotoUris: List<Uri>,
        context: Context,
        successMsg: String,
        onDone: () -> Unit
    ) = viewModelScope.launch {
        val bookId = uiState.value.activeNotebook?.id ?: return@launch
        val savedId = repo.saveNote(
            NoteEntity(
                id = existingId,
                notebookId = bookId,
                title = title.trim(),
                content = content.trim(),
                category = category,
                checklistJson = checklistJson,
                contactPhone = contactPhone.trim(),
                contactAddress = contactAddress.trim(),
                isPinned = isPinned,
                createdAt = existingCreatedAt
            )
        )
        for (uri in pendingPhotoUris) {
            AttachmentStorage.copyPickedImageToInternal(context, uri).onSuccess { path ->
                repo.addNoteAttachment(savedId, path)
            }
        }
        scheduleCloudSync()
        showMessage(successMsg)
        onDone()
    }

    fun deleteNoteAttachment(attachment: NoteAttachmentEntity, context: Context) = viewModelScope.launch {
        AttachmentStorage.deleteInternalAttachment(context, attachment.filePath)
        repo.deleteNoteAttachment(attachment.id)
    }

    fun updateNoteChecklist(note: NoteEntity, newChecklistJson: String) = viewModelScope.launch {
        repo.saveNote(note.copy(checklistJson = newChecklistJson))
        scheduleCloudSync()
    }

    fun togglePinNote(note: NoteEntity) = viewModelScope.launch {
        repo.togglePinNote(note)
        scheduleCloudSync()
    }

    fun deleteNote(noteId: Long, context: Context, successMsg: String) = viewModelScope.launch {
        uiState.value.attachmentsByNoteId[noteId].orEmpty().forEach {
            AttachmentStorage.deleteInternalAttachment(context, it.filePath)
        }
        repo.deleteNote(noteId)
        scheduleCloudSync()
        showMessage(successMsg)
    }

    // Tasks & Offline Reminders
    fun saveTask(
        context: Context,
        title: String,
        description: String,
        taskType: String,
        dueAt: Long?,
        reminderAt: Long?,
        successMsg: String,
        onDone: () -> Unit
    ) = viewModelScope.launch {
        val bookId = uiState.value.activeNotebook?.id ?: return@launch
        val taskId = repo.saveTask(
            TaskEntity(
                notebookId = bookId,
                title = title.trim(),
                description = description.trim(),
                taskType = taskType,
                dueAt = dueAt,
                reminderAt = reminderAt
            )
        )
        if (reminderAt != null && reminderAt > System.currentTimeMillis()) {
            ReminderScheduler.scheduleReminder(context, taskId, title.trim(), description.trim(), reminderAt)
        }
        scheduleCloudSync()
        showMessage(successMsg)
        onDone()
    }

    fun toggleTaskCompleted(context: Context, task: TaskEntity) = viewModelScope.launch {
        val next = !task.isCompleted
        repo.setTaskCompleted(task.id, next)
        if (next) ReminderScheduler.cancelReminder(context, task.id)
        scheduleCloudSync()
    }

    fun deleteTask(context: Context, taskId: Long, successMsg: String) = viewModelScope.launch {
        ReminderScheduler.cancelReminder(context, taskId)
        repo.deleteTask(taskId)
        scheduleCloudSync()
        showMessage(successMsg)
    }

    // Encrypted Backup & Restore
    suspend fun buildEncryptedBackupBytes(passphrase: String) =
        backupManager.createEncryptedBackup(passphrase)

    fun logBackupExport(fileName: String, recordCount: Int, checksum: String, schemaVersion: Int) =
        viewModelScope.launch {
            repo.recordBackupHistory(
                BackupHistoryEntity(
                    fileName = fileName,
                    backupType = "ENCRYPTED_EXPORT",
                    schemaVersion = schemaVersion,
                    recordCount = recordCount,
                    checksumSha256 = checksum
                )
            )
        }

    fun saveLocalSnapshot(context: Context, passphrase: String, successMsg: String) = viewModelScope.launch {
        backupManager.saveLocalSnapshot(context, passphrase)
            .onSuccess { showMessage("$successMsg (${it.name})") }
            .onFailure { showMessage(it.message ?: "Snapshot failed") }
    }

    suspend fun inspectBackup(bytes: ByteArray, passphrase: String) =
        backupManager.inspectEncryptedBackup(bytes, passphrase)

    fun confirmRestoreBackup(preview: BackupPreview, fileName: String, successMsg: String, onDone: () -> Unit) =
        viewModelScope.launch {
            backupManager.restoreFromPreview(preview, fileName)
                .onSuccess { newActiveId ->
                    prefsRepo.setActiveNotebookId(newActiveId)
                    scheduleCloudSync()
                    showMessage(successMsg)
                    onDone()
                }
                .onFailure { showMessage(it.message ?: "Restore failed") }
        }

    fun completeOnboarding(openingBalancePoisha: Long?) = viewModelScope.launch {
        if (openingBalancePoisha != null && openingBalancePoisha > 0L) {
            val bookId = uiState.value.activeNotebook?.id ?: prefsRepo.preferencesFlow.first().activeNotebookId
            repo.updateNotebookOpeningBalance(bookId, openingBalancePoisha)
        }
        prefsRepo.setOnboardingCompleted(true)
        scheduleCloudSync()
    }

    companion object {
        fun factory(
            container: AppContainer,
            currentUserId: String,
            userDisplayName: String,
            userEmail: String?
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return JomaKhorochViewModel(
                        container = container,
                        currentUserId = currentUserId,
                        userDisplayName = userDisplayName,
                        userEmail = userEmail
                    ) as T
                }
            }
    }
}
