package com.example

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.LocalLanguage
import com.example.core.localization.LocalNumeralStyle
import com.example.core.localization.stringsFor
import com.example.data.cloud.authStateFlow
import com.example.data.cloud.performAccountDeletion
import com.example.data.cloud.performGoogleSignOut
import com.example.data.local.entity.LedgerTransactionEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.ShopEntity
import com.example.data.preferences.AppPreferences
import com.example.domain.accounting.LedgerTransactionType
import com.example.domain.accounting.LoanDirection
import com.example.infrastructure.AppUpdateChecker
import com.example.infrastructure.ReminderScheduler
import com.example.presentation.components.AppUpdateDetailsDialog
import com.example.presentation.components.BookSwitcherSheet
import com.example.presentation.components.PinLockScreen
import com.example.presentation.components.PremiumBottomNavigation
import com.example.presentation.components.PremiumNavItem
import com.example.presentation.screens.AccountsScreen
import com.example.presentation.screens.AddAccountBookSheet
import com.example.presentation.screens.AddEditNoteSheet
import com.example.presentation.screens.AddPersonalLoanSheet
import com.example.presentation.screens.AddShopSheet
import com.example.presentation.screens.AddTaskSheet
import com.example.presentation.screens.AuthScreen
import com.example.presentation.screens.BackupRestoreScreen
import com.example.presentation.screens.BudgetsScreen
import com.example.presentation.screens.DashboardScreen
import com.example.presentation.screens.DiaryScreen
import com.example.presentation.screens.IncomeExpenseEntrySheet
import com.example.presentation.screens.JomaKhorochSplashScreen
import com.example.presentation.screens.LoanRepaymentSheet
import com.example.presentation.screens.MoreScreen
import com.example.presentation.screens.NotificationSettingsScreen
import com.example.presentation.screens.NotificationsScreen
import com.example.presentation.screens.OnboardingScreen
import com.example.presentation.screens.OpeningBalanceDialog
import com.example.presentation.screens.QuickActionType
import com.example.presentation.screens.ReportPeriod
import com.example.presentation.screens.ReportsScreen
import com.example.presentation.screens.ShopPaymentSheet
import com.example.presentation.screens.ShopPurchaseSheet
import com.example.presentation.screens.ShopStatementSheet
import com.example.presentation.screens.TransactionDetailEditSheet
import com.example.presentation.screens.TransactionsScreen
import com.example.presentation.state.JomaKhorochViewModel
import com.example.presentation.state.LoanWithBalance
import com.example.presentation.state.ShopWithBalance
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.JomaKhorochTheme
import com.example.ui.theme.LocalFinanceColors
import com.example.ui.theme.SoftGold
import com.example.ui.theme.WarmGold
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val openUpdateFromNotification = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        if (intent?.getBooleanExtra(ReminderScheduler.EXTRA_OPEN_APP_UPDATE, false) == true) {
            openUpdateFromNotification.value = true
        }
        val app = application as JomaKhorochApp
        setContent {
            JomaKhorochRoot(
                container = app.container,
                openUpdateRequested = openUpdateFromNotification.value,
                onConsumeOpenUpdateRequest = { openUpdateFromNotification.value = false }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(ReminderScheduler.EXTRA_OPEN_APP_UPDATE, false)) {
            openUpdateFromNotification.value = true
        }
    }
}

private enum class MainTab { HOME, TRANSACTIONS, ACCOUNTS, MORE }
private enum class MoreSubScreen { NONE, SETTINGS, BUDGETS, REPORTS, BACKUP, NOTIFICATIONS, NOTIFICATION_SETTINGS }

private data class NavDest(
    val tab: MainTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private enum class RootStage { SPLASH, ONBOARDING, AUTH, SESSION }

@Composable
fun JomaKhorochRoot(
    container: AppContainer,
    openUpdateRequested: Boolean = false,
    onConsumeOpenUpdateRequest: () -> Unit = {}
) {
    val rootContext = LocalContext.current
    val isFirebaseInitialized = remember(rootContext) { FirebaseApp.getApps(rootContext).isNotEmpty() }
    val authUser by remember(isFirebaseInitialized) {
        if (isFirebaseInitialized) Firebase.auth.authStateFlow() else flowOf(null)
    }.collectAsStateWithLifecycle(
        initialValue = if (isFirebaseInitialized) Firebase.auth.currentUser else null
    )
    var manualAuthUser by remember {
        mutableStateOf<FirebaseUser?>(if (isFirebaseInitialized) Firebase.auth.currentUser else null)
    }
    val effectiveAuthUser = authUser ?: manualAuthUser ?: if (isFirebaseInitialized) Firebase.auth.currentUser else null
    val preferencesNullable by container.preferences.preferencesFlow
        .collectAsStateWithLifecycle(initialValue = null)
    val prefs = preferencesNullable ?: AppPreferences()
    val strings = remember(prefs.language) { stringsFor(prefs.language) }
    val rootScope = rememberCoroutineScope()

    var splashCompletedSaved by rememberSaveable { mutableStateOf(false) }
    var localSeedReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val defaultBookId = container.repository.ensureInitialized()
        val allBooks = container.repository.db.notebookDao().getAllNotebooksOnce()
        if (allBooks.none { it.id == prefs.activeNotebookId && !it.isArchived }) {
            container.preferences.setActiveNotebookId(defaultBookId)
        }
        localSeedReady = true
    }

    val isBootstrapReady = preferencesNullable != null && localSeedReady
    val stage = when {
        !splashCompletedSaved || !isBootstrapReady -> RootStage.SPLASH
        !prefs.onboardingCompleted -> RootStage.ONBOARDING
        effectiveAuthUser == null -> RootStage.AUTH
        else -> RootStage.SESSION
    }

    JomaKhorochTheme(themeMode = prefs.themeMode) {
        CompositionLocalProvider(
            LocalAppStrings provides strings,
            LocalLanguage provides prefs.language,
            LocalNumeralStyle provides prefs.numeralStyle
        ) {
            AnimatedContent(
                targetState = stage,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "root_stage_transition"
            ) { currentStage ->
                when (currentStage) {
                    RootStage.SPLASH -> JomaKhorochSplashScreen(
                        isDataReady = isBootstrapReady,
                        onSplashFinished = { splashCompletedSaved = true }
                    )
                    RootStage.ONBOARDING -> OnboardingScreen(
                        currentLanguage = prefs.language,
                        onSelectLanguage = { lang ->
                            rootScope.launch { container.preferences.setLanguage(lang) }
                        },
                        onComplete = { openingPoisha ->
                            rootScope.launch {
                                val defaultBookId = container.repository.ensureInitialized()
                                if (openingPoisha != null && openingPoisha > 0L) {
                                    container.repository.updateNotebookOpeningBalance(defaultBookId, openingPoisha)
                                }
                                container.preferences.setOnboardingCompleted(true)
                            }
                        }
                    )
                    RootStage.AUTH -> AuthScreen(
                        currentLanguage = prefs.language,
                        onSelectLanguage = { lang ->
                            rootScope.launch { container.preferences.setLanguage(lang) }
                        },
                        onAuthSuccess = {
                            manualAuthUser = if (isFirebaseInitialized) Firebase.auth.currentUser else null
                        }
                    )
                    RootStage.SESSION -> {
                        val user = effectiveAuthUser
                        if (user != null) {
                            key(user.uid) {
                                AuthenticatedJomaKhorochSession(
                                    container = container,
                                    user = user,
                                    openUpdateRequested = openUpdateRequested,
                                    onConsumeOpenUpdateRequest = onConsumeOpenUpdateRequest
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedJomaKhorochSession(
    container: AppContainer,
    user: FirebaseUser,
    openUpdateRequested: Boolean,
    onConsumeOpenUpdateRequest: () -> Unit
) {
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val sessionScope = rememberCoroutineScope()
    val viewModel: JomaKhorochViewModel = viewModel(
        key = user.uid,
        factory = JomaKhorochViewModel.factory(
            container = container,
            currentUserId = user.uid,
            userDisplayName = user.displayName.orEmpty(),
            userEmail = user.email
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (!uiState.isUnlocked) {
        PinLockScreen(
            biometricEnabled = uiState.preferences.biometricEnabled,
            onVerifyPin = { viewModel.verifyAndUnlock(it) },
            onBiometricUnlocked = { viewModel.unlockViaBiometric() }
        )
    } else {
        JomaKhorochAppShell(
            viewModel = viewModel,
            uiState = uiState,
            openUpdateRequested = openUpdateRequested,
            onConsumeOpenUpdateRequest = onConsumeOpenUpdateRequest,
            onSignOut = {
                performGoogleSignOut(
                    credentialManager = credentialManager,
                    scope = sessionScope,
                    onBeforeSignOut = { viewModel.prepareSignOut() }
                )
            },
            onDeleteAccount = {
                performAccountDeletion(
                    credentialManager = credentialManager,
                    scope = sessionScope,
                    onBeforeDelete = { viewModel.deleteAccountAndCloudData() }
                )
            }
        )
    }
}

@Composable
private fun JomaKhorochAppShell(
    viewModel: JomaKhorochViewModel,
    uiState: com.example.presentation.state.JomaKhorochUiState,
    openUpdateRequested: Boolean,
    onConsumeOpenUpdateRequest: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit = {}
) {
    val strings = LocalAppStrings.current
    val finance = LocalFinanceColors.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled gracefully by ReminderScheduler */ }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    var currentTabName by rememberSaveable { mutableStateOf(MainTab.HOME.name) }
    var tabHistoryCsv by rememberSaveable { mutableStateOf("") }
    var accountsSubTab by rememberSaveable { mutableIntStateOf(0) }
    var moreSubScreenName by rememberSaveable { mutableStateOf(MoreSubScreen.NONE.name) }
    var moreHubSectionName by rememberSaveable { mutableStateOf("BUDGETS") }
    var returnToSettingsOnSubBack by rememberSaveable { mutableStateOf(false) }
    var returnToNotificationsOnSettingsBack by rememberSaveable { mutableStateOf(false) }
    var initialReportPeriodName by rememberSaveable { mutableStateOf(ReportPeriod.MONTHLY.name) }

    val currentTab = remember(currentTabName) {
        runCatching { MainTab.valueOf(currentTabName) }.getOrDefault(MainTab.HOME)
    }
    val moreSubScreen = remember(moreSubScreenName) {
        runCatching { MoreSubScreen.valueOf(moreSubScreenName) }.getOrDefault(MoreSubScreen.NONE)
    }

    val saveableStateHolder = rememberSaveableStateHolder()

    fun navigateToTab(target: MainTab) {
        if (target == MainTab.HOME) {
            tabHistoryCsv = ""
            currentTabName = MainTab.HOME.name
        } else if (target != currentTab) {
            val history = tabHistoryCsv.split(",").filter { it.isNotBlank() && it != target.name }.toMutableList()
            history.remove(currentTab.name)
            history.add(currentTab.name)
            tabHistoryCsv = history.takeLast(6).joinToString(",")
            currentTabName = target.name
        }
        moreSubScreenName = MoreSubScreen.NONE.name
        returnToSettingsOnSubBack = false
        returnToNotificationsOnSettingsBack = false
    }

    BackHandler(enabled = moreSubScreen == MoreSubScreen.NONE && (tabHistoryCsv.isNotBlank() || currentTab != MainTab.HOME)) {
        val history = tabHistoryCsv.split(",").filter { it.isNotBlank() }.toMutableList()
        if (history.isNotEmpty()) {
            val prev = history.removeAt(history.lastIndex)
            tabHistoryCsv = history.joinToString(",")
            currentTabName = prev
        } else {
            currentTabName = MainTab.HOME.name
        }
    }

    // Modal Sheet & Dialog states
    var showBookSwitcher by remember { mutableStateOf(false) }
    var showAddBookSheet by remember { mutableStateOf(false) }
    var showOpeningBalanceDialog by remember { mutableStateOf(false) }
    var incomeExpenseSheetType by remember { mutableStateOf<LedgerTransactionType?>(null) }
    var selectedTxForEdit by remember { mutableStateOf<LedgerTransactionEntity?>(null) }
    var showAddShopSheet by remember { mutableStateOf(false) }
    var editingShop by remember { mutableStateOf<ShopEntity?>(null) }
    var shopPurchaseInitialId by remember { mutableStateOf<Long?>(null) }
    var showShopPurchaseSheet by remember { mutableStateOf(false) }
    var shopPaymentTarget by remember { mutableStateOf<ShopWithBalance?>(null) }
    var showShopPaymentSheet by remember { mutableStateOf(false) }
    var shopStatementTarget by remember { mutableStateOf<ShopWithBalance?>(null) }
    var addLoanDirection by remember { mutableStateOf<LoanDirection?>(null) }
    var loanRepaymentTarget by remember { mutableStateOf<LoanWithBalance?>(null) }
    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }
    var newNoteInitialCategory by remember { mutableStateOf("PERSONAL") }
    var showNoteSheet by remember { mutableStateOf(false) }
    var showTaskSheet by remember { mutableStateOf(false) }
    var showAppUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(
        openUpdateRequested,
        uiState.appUpdate.availableRelease,
        uiState.appUpdate.isChecking,
        uiState.appUpdate.lastCheckedAtMillis
    ) {
        if (openUpdateRequested) {
            when {
                uiState.appUpdate.availableRelease != null -> {
                    showAppUpdateDialog = true
                    onConsumeOpenUpdateRequest()
                }
                !uiState.appUpdate.isChecking && uiState.appUpdate.lastCheckedAtMillis == null -> {
                    viewModel.checkForAppUpdate(silent = false)
                }
                !uiState.appUpdate.isChecking -> {
                    onConsumeOpenUpdateRequest()
                }
            }
        }
    }

    val navDestinations = listOf(
        NavDest(MainTab.HOME, strings.navHome, Icons.Filled.Home, Icons.Outlined.Home),
        NavDest(MainTab.TRANSACTIONS, strings.navTransactions, Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
        NavDest(MainTab.ACCOUNTS, strings.navAccounts, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
        NavDest(MainTab.MORE, strings.navMore, Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
    )

    val premiumNavItems = remember(navDestinations) {
        navDestinations.map {
            PremiumNavItem(
                id = it.tab.name.lowercase(),
                label = it.label,
                selectedIcon = it.selectedIcon,
                unselectedIcon = it.unselectedIcon
            )
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp
        val showBottomNav = !useNavRail && moreSubScreen == MoreSubScreen.NONE

        // Every screen uses the full-bleed Deep Emerald PremiumHeader, so status bar icons stay crisp white,
        // while system navigation bar icons adapt cleanly to Light/Dark theme.
        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as? Activity)?.window ?: return@SideEffect
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = !finance.isDark
            }
        }

        Scaffold(
            contentWindowInsets = if (showBottomNav) {
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
            } else {
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(16.dp),
                        containerColor = if (finance.isDark) MaterialTheme.colorScheme.surfaceContainerHigh else DeepEmerald,
                        contentColor = if (finance.isDark) MaterialTheme.colorScheme.onSurface else Color.White,
                        actionColor = finance.goldAccent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            },
            bottomBar = {
                if (showBottomNav) {
                    PremiumBottomNavigation(
                        items = premiumNavItems,
                        selectedId = currentTab.name.lowercase(),
                        onSelect = { id ->
                            MainTab.entries.firstOrNull { it.name.lowercase() == id }?.let { navigateToTab(it) }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = finance.navBarContainer,
                        contentColor = Color.White,
                        header = {
                            Image(
                                painter = painterResource(id = R.drawable.jomakhoroch_ic),
                                contentDescription = strings.appName,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .padding(top = 12.dp, bottom = 8.dp)
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .testTag("nav_rail_brand_logo")
                            )
                        },
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        navDestinations.forEach { dest ->
                            val selected = currentTab == dest.tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = SoftGold,
                                    selectedTextColor = SoftGold,
                                    unselectedIconColor = Color.White.copy(alpha = 0.78f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.78f),
                                    indicatorColor = WarmGold.copy(alpha = 0.22f)
                                ),
                                modifier = Modifier.testTag("nav_${dest.tab.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val contentModifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 840.dp)

                    AnimatedContent(
                        targetState = currentTab to moreSubScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
                        },
                        label = "main_nav_transition"
                    ) { (tabState, subState) ->
                        val stateKey = if (subState != MoreSubScreen.NONE) "sub_${subState.name}" else "tab_${tabState.name}"
                        saveableStateHolder.SaveableStateProvider(key = stateKey) {
                            val openSettingsScreen: () -> Unit = {
                                returnToSettingsOnSubBack = false
                                returnToNotificationsOnSettingsBack = false
                                moreSubScreenName = MoreSubScreen.SETTINGS.name
                            }
                            val closeToolSubScreen: () -> Unit = {
                                returnToNotificationsOnSettingsBack = false
                                moreSubScreenName = if (returnToSettingsOnSubBack) MoreSubScreen.SETTINGS.name else MoreSubScreen.NONE.name
                            }
                            when {
                                subState == MoreSubScreen.BUDGETS -> BudgetsScreen(
                                    uiState = uiState,
                                    onChangeMonth = { viewModel.setSelectedBudgetMonth(it) },
                                    onSaveBudget = { catId, limit -> viewModel.saveBudget(catId, limit, strings.savedSuccessfully) },
                                    onDeleteBudget = { viewModel.deleteBudget(it, strings.deletedSuccessfully) },
                                    onBack = closeToolSubScreen,
                                    onOpenReports = {
                                        initialReportPeriodName = ReportPeriod.MONTHLY.name
                                        moreSubScreenName = MoreSubScreen.REPORTS.name
                                    },
                                    onOpenReportsWithPeriod = { period ->
                                        initialReportPeriodName = period.name
                                        moreSubScreenName = MoreSubScreen.REPORTS.name
                                    },
                                    onOpenNotifications = { moreSubScreenName = MoreSubScreen.NOTIFICATIONS.name },
                                    onOpenSettings = openSettingsScreen,
                                    modifier = contentModifier
                                )
                                subState == MoreSubScreen.REPORTS -> ReportsScreen(
                                    uiState = uiState,
                                    onMessage = { viewModel.showMessage(it) },
                                    onBack = closeToolSubScreen,
                                    initialPeriod = runCatching { ReportPeriod.valueOf(initialReportPeriodName) }
                                        .getOrDefault(ReportPeriod.MONTHLY),
                                    onOpenBudgets = { moreSubScreenName = MoreSubScreen.BUDGETS.name },
                                    onOpenNotifications = { moreSubScreenName = MoreSubScreen.NOTIFICATIONS.name },
                                    onOpenSettings = openSettingsScreen,
                                    modifier = contentModifier
                                )
                                subState == MoreSubScreen.SETTINGS -> MoreScreen(
                                    uiState = uiState,
                                    onOpenBudgets = {
                                        returnToSettingsOnSubBack = true
                                        moreSubScreenName = MoreSubScreen.BUDGETS.name
                                    },
                                    onOpenReports = {
                                        returnToSettingsOnSubBack = true
                                        moreSubScreenName = MoreSubScreen.REPORTS.name
                                    },
                                    onOpenDiary = {
                                        moreHubSectionName = "DIARY"
                                        navigateToTab(MainTab.MORE)
                                    },
                                    onOpenBackup = {
                                        returnToSettingsOnSubBack = true
                                        moreSubScreenName = MoreSubScreen.BACKUP.name
                                    },
                                    onOpenNotifications = {
                                        returnToSettingsOnSubBack = true
                                        moreSubScreenName = MoreSubScreen.NOTIFICATIONS.name
                                    },
                                    onOpenNotificationSettings = {
                                        returnToSettingsOnSubBack = true
                                        returnToNotificationsOnSettingsBack = false
                                        moreSubScreenName = MoreSubScreen.NOTIFICATION_SETTINGS.name
                                    },
                                    onCheckForUpdates = { viewModel.checkForAppUpdate(silent = false) },
                                    onOpenAppUpdate = { showAppUpdateDialog = true },
                                    onSaveUpdateRepo = { repoInput ->
                                        val isBn = uiState.preferences.language == AppLanguage.BANGLA
                                        viewModel.setUpdateRepoOverride(
                                            rawInput = repoInput,
                                            savedMsg = if (isBn) "আপডেট রিপোজিটরি সংরক্ষিত হয়েছে" else "Update repository saved",
                                            invalidMsg = if (isBn) "সঠিক ফরম্যাটে লিখুন (যেমন: username/jomakhoroch)" else "Use owner/repo format (e.g., username/jomakhoroch)"
                                        )
                                    },
                                    onSyncNow = {
                                        val isBn = uiState.preferences.language == AppLanguage.BANGLA
                                        viewModel.syncNow(
                                            successMsg = if (isBn) "ক্লাউডে সিঙ্ক সম্পন্ন হয়েছে" else "Cloud sync complete",
                                            offlineMsg = if (isBn) "অফলাইনে আছেন — ইন্টারনেট পেলেই অটো সিঙ্ক হবে" else "Offline — changes will auto-sync when online"
                                        )
                                    },
                                    onSignOut = onSignOut,
                                    onDeleteAccount = onDeleteAccount,
                                    onSetLanguage = { viewModel.setLanguage(it) },
                                    onSetNumeralStyle = { viewModel.setNumeralStyle(it) },
                                    onSetThemeMode = { viewModel.setThemeMode(it) },
                                    onEnablePin = { viewModel.enablePin(it, strings.savedSuccessfully) },
                                    onDisablePin = { viewModel.disablePin(strings.savedSuccessfully) },
                                    onLockNow = { viewModel.lockNow() },
                                    onSetBiometric = { viewModel.setBiometricEnabled(it) },
                                    onUpdateOpeningBalance = { viewModel.updateOpeningBalance(it, strings.savedSuccessfully) },
                                    onAddCategory = { bn, en, type -> viewModel.addCategory(bn, en, type, strings.savedSuccessfully) },
                                    onDeleteCategory = { viewModel.deleteCategory(it, strings.deletedSuccessfully) },
                                    onUpdateProfile = { name, business ->
                                        viewModel.updateUserProfile(name, business, strings.savedSuccessfully)
                                    },
                                    onUpdateProfilePhoto = { uri ->
                                        val isBn = uiState.preferences.language == AppLanguage.BANGLA
                                        viewModel.updateProfilePhoto(
                                            context = context,
                                            uri = uri,
                                            savedMsg = if (uri != null) {
                                                if (isBn) "প্রোফাইল ছবি আপডেট হয়েছে" else "Profile photo updated"
                                            } else {
                                                if (isBn) "প্রোফাইল ছবি মুছে ফেলা হয়েছে" else "Profile photo removed"
                                            },
                                            errorMsg = if (isBn) "ছবি সংরক্ষণ করা যায়নি" else "Could not save photo"
                                        )
                                    },
                                    onShowMessage = { viewModel.showMessage(it) },
                                    onBack = { moreSubScreenName = MoreSubScreen.NONE.name },
                                    modifier = contentModifier
                                )
                                subState == MoreSubScreen.BACKUP -> BackupRestoreScreen(
                                    uiState = uiState,
                                    onBuildBackupBytes = { viewModel.buildEncryptedBackupBytes(it) },
                                    onLogBackupExport = { name, count, sum, ver ->
                                        viewModel.logBackupExport(name, count, sum, ver)
                                    },
                                    onSaveLocalSnapshot = { pass ->
                                        viewModel.saveLocalSnapshot(context, pass, strings.savedSuccessfully)
                                    },
                                    onInspectBackup = { bytes, pass -> viewModel.inspectBackup(bytes, pass) },
                                    onConfirmRestore = { preview, fileName, done ->
                                        viewModel.confirmRestoreBackup(preview, fileName, strings.savedSuccessfully, done)
                                    },
                                    onMessage = { viewModel.showMessage(it) },
                                    onBack = closeToolSubScreen,
                                    modifier = contentModifier
                                )
                                subState == MoreSubScreen.NOTIFICATIONS -> NotificationsScreen(
                                    uiState = uiState,
                                    onOpenNotificationSettings = {
                                        returnToNotificationsOnSettingsBack = true
                                        moreSubScreenName = MoreSubScreen.NOTIFICATION_SETTINGS.name
                                    },
                                    onOpenAppUpdate = { showAppUpdateDialog = true },
                                    onOpenBudgets = {
                                        moreHubSectionName = "BUDGETS"
                                        navigateToTab(MainTab.MORE)
                                    },
                                    onOpenAccounts = { navigateToTab(MainTab.ACCOUNTS) },
                                    onOpenDiary = {
                                        moreHubSectionName = "DIARY"
                                        navigateToTab(MainTab.MORE)
                                    },
                                    onBack = closeToolSubScreen,
                                    modifier = contentModifier
                                )
                                subState == MoreSubScreen.NOTIFICATION_SETTINGS -> NotificationSettingsScreen(
                                    uiState = uiState,
                                    onToggleMasterNotifications = { viewModel.setNotificationsEnabled(context, it) },
                                    onToggleBudgetAlerts = { viewModel.setBudgetAlertsEnabled(it) },
                                    onToggleLoanDueAlerts = { viewModel.setLoanDueAlertsEnabled(context, it) },
                                    onToggleUpdateAlerts = { viewModel.setUpdateAlertsEnabled(it) },
                                    onSetDailyReminder = { enabled, hour, minute ->
                                        viewModel.setDailyReminder(context, enabled, hour, minute)
                                    },
                                    onSendTestNotification = {
                                        val isBn = uiState.preferences.language == AppLanguage.BANGLA
                                        viewModel.sendTestNotification(
                                            context = context,
                                            sentMsg = if (isBn) "টেস্ট নোটিফিকেশন পাঠানো হয়েছে" else "Test notification sent",
                                            deniedMsg = if (isBn) "অনুগ্রহ করে আগে নোটিফিকেশন অনুমতি দিন" else "Please grant notification permission first"
                                        )
                                    },
                                    onBack = {
                                        if (returnToNotificationsOnSettingsBack) {
                                            returnToNotificationsOnSettingsBack = false
                                            moreSubScreenName = MoreSubScreen.NOTIFICATIONS.name
                                        } else {
                                            closeToolSubScreen()
                                        }
                                    },
                                    modifier = contentModifier
                                )
                                tabState == MainTab.HOME -> DashboardScreen(
                                    uiState = uiState,
                                    onSwitchBookClick = { showBookSwitcher = true },
                                    onToggleHideBalance = { viewModel.toggleHideBalance() },
                                    onSyncNow = {
                                        val isBn = uiState.preferences.language == AppLanguage.BANGLA
                                        viewModel.syncNow(
                                            successMsg = if (isBn) "ক্লাউডে সিঙ্ক সম্পন্ন হয়েছে" else "Cloud sync complete",
                                            offlineMsg = if (isBn) "অফলাইনে আছেন — ইন্টারনেট পেলেই অটো সিঙ্ক হবে" else "Offline — changes will auto-sync when online"
                                        )
                                    },
                                    onOpenNotifications = {
                                        returnToSettingsOnSubBack = false
                                        moreSubScreenName = MoreSubScreen.NOTIFICATIONS.name
                                    },
                                    onOpenAppUpdate = { showAppUpdateDialog = true },
                                    onDismissAppUpdate = { version -> viewModel.dismissUpdateBanner(version) },
                                    onDismissWelcome = { viewModel.dismissWelcomeBanner() },
                                    onSetOpeningBalanceClick = { showOpeningBalanceDialog = true },
                                    onQuickAction = { action ->
                                        when (action) {
                                            QuickActionType.ADD_INCOME -> incomeExpenseSheetType = LedgerTransactionType.INCOME
                                            QuickActionType.ADD_EXPENSE -> incomeExpenseSheetType = LedgerTransactionType.EXPENSE
                                            QuickActionType.ADD_SHOP_DUE -> {
                                                if (uiState.shops.isEmpty()) {
                                                    showAddShopSheet = true
                                                } else {
                                                    shopPurchaseInitialId = uiState.shops.first().shop.id
                                                    showShopPurchaseSheet = true
                                                }
                                            }
                                            QuickActionType.RECORD_PAYMENT -> {
                                                shopPaymentTarget = null
                                                showShopPaymentSheet = true
                                            }
                                            QuickActionType.GIVE_LOAN -> addLoanDirection = LoanDirection.GIVEN
                                            QuickActionType.BORROW_MONEY -> addLoanDirection = LoanDirection.BORROWED
                                        }
                                    },
                                    onViewAllTransactions = { navigateToTab(MainTab.TRANSACTIONS) },
                                    onTransactionClick = { selectedTxForEdit = it },
                                    onOpenAccountsTab = { sub ->
                                        accountsSubTab = sub
                                        navigateToTab(MainTab.ACCOUNTS)
                                    },
                                    onOpenSettings = openSettingsScreen,
                                    modifier = contentModifier
                                )
                                tabState == MainTab.ACCOUNTS -> AccountsScreen(
                                    uiState = uiState,
                                    selectedTab = accountsSubTab,
                                    onSelectTab = { accountsSubTab = it },
                                    onAddShopClick = {
                                        editingShop = null
                                        showAddShopSheet = true
                                    },
                                    onShopPurchaseClick = {
                                        shopPurchaseInitialId = it.shop.id
                                        showShopPurchaseSheet = true
                                    },
                                    onShopPaymentClick = {
                                        shopPaymentTarget = it
                                        showShopPaymentSheet = true
                                    },
                                    onShopStatementClick = { shopStatementTarget = it },
                                    onAddLoanClick = { addLoanDirection = it },
                                    onLoanRepayClick = { loanRepaymentTarget = it },
                                    onSelectBook = { viewModel.selectNotebook(it) },
                                    onAddBookClick = { showAddBookSheet = true },
                                    onArchiveBook = {
                                        viewModel.archiveNotebook(it.id, strings.savedSuccessfully, strings.cannotDeleteLastBook)
                                    },
                                    onDeleteBookPermanently = {
                                        viewModel.deleteNotebookPermanently(it.id, strings.deletedSuccessfully, strings.cannotDeleteLastBook)
                                    },
                                    onSwitchBookClick = { showBookSwitcher = true },
                                    onOpenNotifications = {
                                        returnToSettingsOnSubBack = false
                                        moreSubScreenName = MoreSubScreen.NOTIFICATIONS.name
                                    },
                                    onOpenSettings = openSettingsScreen,
                                    modifier = contentModifier
                                )
                                tabState == MainTab.TRANSACTIONS -> TransactionsScreen(
                                    uiState = uiState,
                                    onAddTransactionClick = { incomeExpenseSheetType = LedgerTransactionType.EXPENSE },
                                    onTransactionClick = { selectedTxForEdit = it },
                                    onOpenNotifications = {
                                        returnToSettingsOnSubBack = false
                                        moreSubScreenName = MoreSubScreen.NOTIFICATIONS.name
                                    },
                                    onOpenSettings = openSettingsScreen,
                                    modifier = contentModifier
                                )
                                tabState == MainTab.MORE -> {
                                    val isBn = uiState.preferences.language == AppLanguage.BANGLA
                                    val moreHubSwitcher: @Composable () -> Unit = {
                                        MoreTabHubSwitcher(
                                            selectedSection = moreHubSectionName,
                                            isBn = isBn,
                                            onSelect = { moreHubSectionName = it }
                                        )
                                    }
                                    AnimatedContent(
                                        targetState = moreHubSectionName,
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(190)) togetherWith fadeOut(animationSpec = tween(140))
                                        },
                                        label = "more_hub_section_transition"
                                    ) { hubSection ->
                                        if (hubSection == "DIARY") {
                                            DiaryScreen(
                                                uiState = uiState,
                                                onAddNoteClick = {
                                                    editingNote = null
                                                    newNoteInitialCategory = "PERSONAL"
                                                    showNoteSheet = true
                                                },
                                                onAddNoteWithCategory = { cat ->
                                                    editingNote = null
                                                    newNoteInitialCategory = cat
                                                    showNoteSheet = true
                                                },
                                                onEditNoteClick = { note ->
                                                    editingNote = note
                                                    newNoteInitialCategory = note.category
                                                    showNoteSheet = true
                                                },
                                                onTogglePinNote = { note -> viewModel.togglePinNote(note) },
                                                onUpdateChecklist = { note, json -> viewModel.updateNoteChecklist(note, json) },
                                                onDeleteNote = { id -> viewModel.deleteNote(id, context, strings.deletedSuccessfully) },
                                                onAddTaskClick = { showTaskSheet = true },
                                                onToggleTask = { task -> viewModel.toggleTaskCompleted(context, task) },
                                                onDeleteTask = { id -> viewModel.deleteTask(context, id, strings.deletedSuccessfully) },
                                                onOpenNotifications = {
                                                    returnToSettingsOnSubBack = false
                                                    moreSubScreenName = MoreSubScreen.NOTIFICATIONS.name
                                                },
                                                onOpenSettings = openSettingsScreen,
                                                topSwitcher = moreHubSwitcher,
                                                modifier = contentModifier
                                            )
                                        } else {
                                            BudgetsScreen(
                                                uiState = uiState,
                                                onChangeMonth = { viewModel.setSelectedBudgetMonth(it) },
                                                onSaveBudget = { catId, limit -> viewModel.saveBudget(catId, limit, strings.savedSuccessfully) },
                                                onDeleteBudget = { viewModel.deleteBudget(it, strings.deletedSuccessfully) },
                                                onBack = null,
                                                onOpenReports = {
                                                    returnToSettingsOnSubBack = false
                                                    initialReportPeriodName = ReportPeriod.MONTHLY.name
                                                    moreSubScreenName = MoreSubScreen.REPORTS.name
                                                },
                                                onOpenReportsWithPeriod = { period ->
                                                    returnToSettingsOnSubBack = false
                                                    initialReportPeriodName = period.name
                                                    moreSubScreenName = MoreSubScreen.REPORTS.name
                                                },
                                                onOpenNotifications = {
                                                    returnToSettingsOnSubBack = false
                                                    moreSubScreenName = MoreSubScreen.NOTIFICATIONS.name
                                                },
                                                onOpenSettings = openSettingsScreen,
                                                topSwitcher = moreHubSwitcher,
                                                modifier = contentModifier
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // All Modal Bottom Sheets & Dialogs
    if (showBookSwitcher) {
        BookSwitcherSheet(
            books = uiState.activeNotebooks,
            activeBookId = uiState.activeNotebook?.id,
            onSelectBook = { viewModel.selectNotebook(it) },
            onAddBookClick = { showAddBookSheet = true },
            onDismiss = { showBookSwitcher = false }
        )
    }

    if (showAddBookSheet) {
        AddAccountBookSheet(
            onSave = { name, type, icon, opening ->
                viewModel.createNotebook(name, type, icon, opening, strings.savedSuccessfully) {
                    showAddBookSheet = false
                }
            },
            onDismiss = { showAddBookSheet = false }
        )
    }

    if (showOpeningBalanceDialog) {
        OpeningBalanceDialog(
            currentPoisha = uiState.activeNotebook?.openingBalancePoisha ?: 0L,
            onSave = { poisha ->
                viewModel.updateOpeningBalance(poisha, strings.savedSuccessfully)
                showOpeningBalanceDialog = false
            },
            onDismiss = { showOpeningBalanceDialog = false }
        )
    }

    incomeExpenseSheetType?.let { type ->
        IncomeExpenseEntrySheet(
            initialType = type,
            categories = uiState.categories,
            onSave = { txType, poisha, catId, method, note, date ->
                viewModel.addIncomeOrExpense(txType, poisha, catId, method, note, date, strings.savedSuccessfully) {
                    incomeExpenseSheetType = null
                }
            },
            onAddCustomCategory = { bn, en, catType ->
                viewModel.addCategory(bn, en, catType, strings.savedSuccessfully)
            },
            onDismiss = { incomeExpenseSheetType = null }
        )
    }

    selectedTxForEdit?.let { tx ->
        TransactionDetailEditSheet(
            tx = tx,
            itemsFlow = viewModel.observeTransactionItems(tx.id),
            categories = uiState.categories,
            onUpdate = { total, paid, note, catId, method, date, reason ->
                viewModel.updateTransaction(tx.id, total, paid, note, catId, method, date, reason, strings.savedSuccessfully) {
                    selectedTxForEdit = null
                }
            },
            onDelete = { reason ->
                viewModel.deleteTransaction(tx.id, reason, strings.deletedSuccessfully)
                selectedTxForEdit = null
            },
            onDismiss = { selectedTxForEdit = null }
        )
    }

    if (showAddShopSheet) {
        AddShopSheet(
            existing = editingShop,
            onSave = { id, name, owner, phone, address, cat, opening, notes ->
                viewModel.saveShop(id, name, owner, phone, address, cat, opening, notes, strings.savedSuccessfully) {
                    showAddShopSheet = false
                    editingShop = null
                }
            },
            onDismiss = {
                showAddShopSheet = false
                editingShop = null
            }
        )
    }

    if (showShopPurchaseSheet) {
        ShopPurchaseSheet(
            shops = uiState.shops,
            initialShopId = shopPurchaseInitialId,
            expenseCategories = uiState.categories.filter { it.type == "EXPENSE" },
            onSave = { shopId, total, paid, catId, method, note, date, items ->
                viewModel.addShopPurchase(shopId, total, paid, catId, method, note, date, items, strings.savedSuccessfully) {
                    showShopPurchaseSheet = false
                }
            },
            onDismiss = { showShopPurchaseSheet = false }
        )
    }

    if (showShopPaymentSheet) {
        ShopPaymentSheet(
            shopsWithDue = uiState.shops.filter { it.balance.outstandingDuePoisha > 0L },
            initialShop = shopPaymentTarget,
            onPay = { shopId, payPoisha, method, note, date ->
                viewModel.recordShopPayment(shopId, payPoisha, method, note, date, strings.savedSuccessfully) {
                    showShopPaymentSheet = false
                    shopPaymentTarget = null
                }
            },
            onDismiss = {
                showShopPaymentSheet = false
                shopPaymentTarget = null
            }
        )
    }

    shopStatementTarget?.let { target ->
        val freshShop = uiState.shops.firstOrNull { it.shop.id == target.shop.id } ?: target
        ShopStatementSheet(
            shopWithBalance = freshShop,
            allTransactions = uiState.transactions,
            onEditShop = {
                shopStatementTarget = null
                editingShop = freshShop.shop
                showAddShopSheet = true
            },
            onAddPurchase = {
                shopStatementTarget = null
                shopPurchaseInitialId = freshShop.shop.id
                showShopPurchaseSheet = true
            },
            onRecordPayment = {
                shopStatementTarget = null
                shopPaymentTarget = freshShop
                showShopPaymentSheet = true
            },
            onTransactionClick = { tx ->
                shopStatementTarget = null
                selectedTxForEdit = tx
            },
            onDismiss = { shopStatementTarget = null }
        )
    }

    addLoanDirection?.let { dir ->
        AddPersonalLoanSheet(
            direction = dir,
            onSave = { person, phone, principal, loanDate, dueDate, desc, method ->
                viewModel.addPersonalLoan(dir, person, phone, principal, loanDate, dueDate, desc, method, strings.savedSuccessfully) {
                    addLoanDirection = null
                }
            },
            onDismiss = { addLoanDirection = null }
        )
    }

    loanRepaymentTarget?.let { target ->
        val freshLoan = (uiState.lendingLoans + uiState.borrowingLoans).firstOrNull { it.loan.id == target.loan.id } ?: target
        LoanRepaymentSheet(
            loanWithBalance = freshLoan,
            allTransactions = uiState.transactions,
            onRepay = { payPoisha, method, note, date ->
                viewModel.recordLoanRepayment(freshLoan.loan.id, payPoisha, method, note, date, strings.savedSuccessfully) {
                    loanRepaymentTarget = null
                }
            },
            onDismiss = { loanRepaymentTarget = null }
        )
    }

    if (showNoteSheet) {
        val currentNoteId = editingNote?.id
        val noteAttachments = if (currentNoteId != null) {
            uiState.attachmentsByNoteId[currentNoteId].orEmpty()
        } else emptyList()

        AddEditNoteSheet(
            existing = editingNote,
            existingAttachments = noteAttachments,
            initialCategory = newNoteInitialCategory,
            onDeleteAttachment = { att -> viewModel.deleteNoteAttachment(att, context) },
            onSave = { id, createdAt, title, content, cat, checkJson, phone, address, pinned, photos ->
                viewModel.saveNote(id, createdAt, title, content, cat, checkJson, phone, address, pinned, photos, context, strings.savedSuccessfully) {
                    showNoteSheet = false
                    editingNote = null
                }
            },
            onDismiss = {
                showNoteSheet = false
                editingNote = null
            }
        )
    }

    if (showTaskSheet) {
        AddTaskSheet(
            onSave = { title, desc, type, dueAt, reminderAt ->
                if (reminderAt != null &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !ReminderScheduler.hasNotificationPermission(context)
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                viewModel.saveTask(context, title, desc, type, dueAt, reminderAt, strings.savedSuccessfully) {
                    showTaskSheet = false
                }
            },
            onDismiss = { showTaskSheet = false }
        )
    }

    if (showAppUpdateDialog) {
        uiState.appUpdate.availableRelease?.let { release ->
            val isBn = uiState.preferences.language == AppLanguage.BANGLA
            AppUpdateDetailsDialog(
                currentVersion = uiState.appUpdate.currentVersion,
                release = release,
                onDownloadUpdate = { url ->
                    val opened = AppUpdateChecker.openUpdateInBrowser(context, url)
                    if (!opened) {
                        viewModel.showMessage(
                            if (isBn) "ব্রাউজার খুলতে সমস্যা হয়েছে" else "Could not open browser for download"
                        )
                    }
                    showAppUpdateDialog = false
                },
                onOpenReleasePage = { url ->
                    val opened = AppUpdateChecker.openUpdateInBrowser(context, url)
                    if (!opened) {
                        viewModel.showMessage(
                            if (isBn) "রিলিজ পেজ খোলা যায়নি" else "Could not open release page"
                        )
                    }
                    showAppUpdateDialog = false
                },
                onDismiss = { showAppUpdateDialog = false }
            )
        }
    }
}

@Composable
private fun MoreTabHubSwitcher(
    selectedSection: String,
    isBn: Boolean,
    onSelect: (String) -> Unit
) {
    val finance = LocalFinanceColors.current
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (finance.isDark) MaterialTheme.colorScheme.surfaceContainer else Color(0xFFF3EFE4),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("more_hub_switcher")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                Triple("BUDGETS", Icons.Filled.BarChart, if (isBn) "বাজেট ও রিপোর্ট" else "Budget & Reports"),
                Triple("DIARY", Icons.Filled.EditNote, if (isBn) "পকেট ডায়রি" else "Pocket Diary")
            )
            tabs.forEach { (key, icon, label) ->
                val isSelected = selectedSection == key
                val bgColor by animateColorAsState(
                    targetValue = when {
                        !isSelected -> Color.Transparent
                        finance.isDark -> MaterialTheme.colorScheme.primary
                        else -> DeepEmerald
                    },
                    animationSpec = tween(200),
                    label = "hub_bg_$key"
                )
                val iconTint by animateColorAsState(
                    targetValue = when {
                        !isSelected -> MaterialTheme.colorScheme.onSurfaceVariant
                        finance.isDark -> MaterialTheme.colorScheme.onPrimary
                        else -> SoftGold
                    },
                    animationSpec = tween(200),
                    label = "hub_icon_$key"
                )
                val textColor by animateColorAsState(
                    targetValue = when {
                        !isSelected -> MaterialTheme.colorScheme.onSurfaceVariant
                        finance.isDark -> MaterialTheme.colorScheme.onPrimary
                        else -> Color.White
                    },
                    animationSpec = tween(200),
                    label = "hub_text_$key"
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(bgColor)
                        .semantics { selected = isSelected }
                        .clickable(role = Role.Tab) {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelect(key)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                        .testTag("more_hub_tab_${key.lowercase()}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = textColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
