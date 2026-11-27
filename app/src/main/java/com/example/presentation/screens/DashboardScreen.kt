package com.example.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.LocalLanguage
import com.example.core.localization.LocalNumeralStyle
import com.example.core.util.CurrencyFormatter
import com.example.core.util.DateUtils
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.LedgerTransactionEntity
import com.example.data.local.entity.ShopEntity
import com.example.domain.accounting.LedgerTransactionType
import com.example.presentation.components.AppUpdateDashboardBanner
import com.example.presentation.components.BookSwitcherPill
import com.example.presentation.components.EmptyState
import com.example.presentation.components.iconForKey
import com.example.presentation.state.JomaKhorochUiState
import com.example.presentation.state.SyncStatus
import com.example.ui.theme.CanopyMintAccent
import com.example.ui.theme.CanopyUrgentBadge
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.LocalFinanceColors
import com.example.ui.theme.PlusJakartaSansFont
import com.example.ui.theme.SoftGold
import com.example.ui.theme.WarmGold
import java.util.Calendar
import kotlin.math.abs

enum class QuickActionType {
    ADD_INCOME,
    ADD_EXPENSE,
    ADD_SHOP_DUE,
    RECORD_PAYMENT,
    GIVE_LOAN,
    BORROW_MONEY
}

private data class MonthlyBarPoint(
    val yearMonth: String,
    val shortLabel: String,
    val incomePoisha: Long,
    val expensePoisha: Long
)

@Composable
fun DashboardScreen(
    uiState: JomaKhorochUiState,
    onSwitchBookClick: () -> Unit,
    onToggleHideBalance: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAppUpdate: () -> Unit,
    onDismissAppUpdate: (String) -> Unit,
    onDismissWelcome: () -> Unit,
    onSetOpeningBalanceClick: () -> Unit,
    onQuickAction: (QuickActionType) -> Unit,
    onViewAllTransactions: () -> Unit,
    onTransactionClick: (LedgerTransactionEntity) -> Unit,
    onOpenAccountsTab: (Int) -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA
    val isDark = finance.isDark
    val hidden = uiState.preferences.hideBalance

    var showMoreQuickActions by rememberSaveable { mutableStateOf(false) }
    var chartMonthCount by rememberSaveable { mutableIntStateOf(5) } // 3, 5, or 6 months

    val categoriesById = remember(uiState.categories) { uiState.categories.associateBy { it.id } }
    val shopsById = remember(uiState.shops) { uiState.shops.associateBy { it.shop.id }.mapValues { it.value.shop } }
    val todayMillis = remember { System.currentTimeMillis() }
    val yesterdayMillis = remember(todayMillis) { todayMillis - 86_400_000L }

    val greeting = remember(language, uiState.cloudSync.userDisplayName) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val base = if (isBn) {
            when (hour) {
                in 5..11 -> "শুভ সকাল"
                in 12..16 -> "শুভ দুপুর"
                in 17..20 -> "শুভ সন্ধ্যা"
                else -> "শুভ রাত্রি"
            }
        } else {
            when (hour) {
                in 5..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                in 17..20 -> "Good Evening"
                else -> "Good Night"
            }
        }
        val firstName = uiState.cloudSync.userDisplayName.trim().substringBefore(" ")
        if (firstName.isNotBlank()) "$base, $firstName!" else "$base!"
    }

    // Real monthly cashflow dataset computed purely from live transactions
    val monthlyBars = remember(uiState.transactions, language, chartMonthCount) {
        val curYm = DateUtils.currentYearMonth(todayMillis)
        val months = (-(chartMonthCount - 1)..0).map { offset -> DateUtils.shiftYearMonth(curYm, offset) }
        months.map { ym ->
            val (start, end) = DateUtils.monthRange(ym)
            var inc = 0L
            var exp = 0L
            for (tx in uiState.transactions) {
                if (tx.transactionDate in start..end) {
                    when (LedgerTransactionType.valueOf(tx.type)) {
                        LedgerTransactionType.INCOME -> inc += tx.totalAmountPoisha
                        LedgerTransactionType.EXPENSE,
                        LedgerTransactionType.SHOP_PURCHASE -> exp += tx.totalAmountPoisha
                        else -> Unit
                    }
                }
            }
            val monthNum = ym.substringAfter("-").toIntOrNull()?.coerceIn(1, 12) ?: 1
            val shortLabel = shortMonthName(monthNum, isBn)
            MonthlyBarPoint(ym, shortLabel, inc, exp)
        }
    }

    // Real month-over-month comparison notes
    val prevMonthPoint = monthlyBars.getOrNull(monthlyBars.size - 2)
    val curMonthPoint = monthlyBars.lastOrNull()
    val incomeDeltaNote = remember(prevMonthPoint, curMonthPoint, isBn, numerals) {
        formatMoMDelta(curMonthPoint?.incomePoisha ?: 0L, prevMonthPoint?.incomePoisha ?: 0L, isBn, numerals)
    }
    val expenseDeltaNote = remember(prevMonthPoint, curMonthPoint, isBn, numerals) {
        formatMoMDelta(curMonthPoint?.expensePoisha ?: 0L, prevMonthPoint?.expensePoisha ?: 0L, isBn, numerals)
    }

    val recentTxs = remember(uiState.transactions) { uiState.transactions.take(5) }
    val hasUrgentAlert = remember(uiState.notifications) { uiState.notifications.any { it.isUrgent } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Unified Deep Emerald Canopy + Greeting + Overlapping Hero Balance Card (Exact mockup signature!)
        item(key = "dashboard_header") {
            DashboardHeroCanopyAndBalance(
                uiState = uiState,
                greeting = greeting,
                todayMillis = todayMillis,
                hasUrgentAlert = hasUrgentAlert,
                hidden = hidden,
                onSwitchBookClick = onSwitchBookClick,
                onSyncNow = onSyncNow,
                onOpenNotifications = onOpenNotifications,
                onOpenSettings = onOpenSettings,
                onToggleHideBalance = onToggleHideBalance
            )
        }

        // 2. Actionable App Update Banner (if release available)
        val availableRelease = uiState.appUpdate.availableRelease
        if (availableRelease != null && uiState.preferences.dismissedUpdateVersion != availableRelease.versionName) {
            item(key = "app_update_banner") {
                AppUpdateDashboardBanner(
                    currentVersion = uiState.appUpdate.currentVersion,
                    release = availableRelease,
                    onViewUpdate = onOpenAppUpdate,
                    onDismiss = { onDismissAppUpdate(availableRelease.versionName) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // 2.5 First-Launch Opening Balance Banner (only when empty & not dismissed)
        if (!uiState.preferences.welcomeBannerDismissed && uiState.transactions.isEmpty()) {
            item(key = "welcome_banner") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${strings.appName} — ${strings.tagline}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = onSetOpeningBalanceClick,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "+ ${strings.openingBalance}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(onClick = onDismissWelcome) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = strings.close,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }

        // 3. Total Income & Total Expense 2-Column Cards (Matching mockup soft mint / soft blush)
        item(key = "income_expense_cards") {
            val fallbackIncomeSub = if (isBn) {
                "এই মাস: ${if (hidden) "৳ ••••" else CurrencyFormatter.formatPoisha(uiState.currentMonthIncomePoisha, numerals)}"
            } else {
                "This mo: ${if (hidden) "৳ ••••" else CurrencyFormatter.formatPoisha(uiState.currentMonthIncomePoisha, numerals)}"
            }
            val fallbackExpenseSub = if (isBn) {
                "এই মাস: ${if (hidden) "৳ ••••" else CurrencyFormatter.formatPoisha(uiState.currentMonthExpensePoisha, numerals)}"
            } else {
                "This mo: ${if (hidden) "৳ ••••" else CurrencyFormatter.formatPoisha(uiState.currentMonthExpensePoisha, numerals)}"
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DashboardIncomeExpenseCard(
                    title = strings.totalIncome,
                    amountPoisha = uiState.financialSummary.totalIncomePoisha,
                    isIncome = true,
                    hidden = hidden,
                    deltaNote = incomeDeltaNote ?: fallbackIncomeSub,
                    onClick = { onQuickAction(QuickActionType.ADD_INCOME) },
                    modifier = Modifier.weight(1f)
                )
                DashboardIncomeExpenseCard(
                    title = strings.totalExpense,
                    amountPoisha = uiState.financialSummary.totalExpensePoisha,
                    isIncome = false,
                    hidden = hidden,
                    deltaNote = expenseDeltaNote ?: fallbackExpenseSub,
                    onClick = { onQuickAction(QuickActionType.ADD_EXPENSE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Quick Transaction Actions ("⚡ দ্রুত কাজ" + "সবগুলো দেখুন →")
        item(key = "quick_actions") {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ElectricBolt,
                            contentDescription = null,
                            tint = Color(0xFFD69A2D),
                            modifier = Modifier.size(19.dp)
                        )
                        Text(
                            text = if (isBn) "দ্রুত কাজ" else strings.quickActions,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { showMoreQuickActions = !showMoreQuickActions }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showMoreQuickActions) {
                                if (isBn) "কম দেখুন" else "Show less"
                            } else {
                                if (isBn) "সবগুলো দেখুন" else strings.viewAll
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // 4 Crisp Rounded Square Tiles matching the mockup's exact colors
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardQuickActionTile(
                        label = if (isBn) "জমা\n(আয়)" else strings.addIncome,
                        icon = Icons.Filled.Add,
                        circleColor = Color(0xFF14934A),
                        onClick = { onQuickAction(QuickActionType.ADD_INCOME) },
                        tag = "quick_action_add_income",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    DashboardQuickActionTile(
                        label = if (isBn) "খরচ\n(ব্যয়)" else strings.addExpense,
                        icon = Icons.Filled.Remove,
                        circleColor = Color(0xFFDF362D),
                        onClick = { onQuickAction(QuickActionType.ADD_EXPENSE) },
                        tag = "quick_action_add_expense",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    DashboardQuickActionTile(
                        label = if (isBn) "দোকানের\nবাকি" else strings.addShopDue,
                        icon = Icons.Filled.Storefront,
                        circleColor = Color(0xFF1D6CE0),
                        onClick = { onQuickAction(QuickActionType.ADD_SHOP_DUE) },
                        tag = "quick_action_add_shop_due",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    DashboardQuickActionTile(
                        label = if (isBn) "ধার-দেনা" else strings.giveLoan,
                        icon = Icons.Filled.Handshake,
                        circleColor = Color(0xFF7E2FD0),
                        onClick = { onQuickAction(QuickActionType.GIVE_LOAN) },
                        tag = "quick_action_give_loan",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                // Expandable extra quick actions (Shop Due Payment & Borrow Money)
                AnimatedVisibility(
                    visible = showMoreQuickActions,
                    enter = expandVertically(tween(200)) + fadeIn(tween(180)),
                    exit = shrinkVertically(tween(180)) + fadeOut(tween(140))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onQuickAction(QuickActionType.RECORD_PAYMENT) }
                                .testTag("quick_action_record_payment")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Payments,
                                    contentDescription = null,
                                    tint = finance.income,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "বাকি পরিশোধ" else strings.recordPayment,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onQuickAction(QuickActionType.BORROW_MONEY) }
                                .testTag("quick_action_borrow_money")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDownward,
                                    contentDescription = null,
                                    tint = finance.payable,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = strings.borrowMoney,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Compact 2-Column Dues & Receivables Strip ("দোকানের বাকি" | "পাওনা") + Payable strip when present
        item(key = "dues_and_loans_summary") {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardCompactLedgerTile(
                        title = strings.shopDueLabel,
                        value = if (hidden) "৳ ••••" else CurrencyFormatter.formatPoisha(
                            uiState.financialSummary.totalShopPayablePoisha,
                            numerals
                        ),
                        icon = Icons.Filled.Storefront,
                        badgeBg = if (isDark) Color(0xFF332715) else Color(0xFFF6EBD3),
                        iconTint = if (isDark) Color(0xFFF0B866) else Color(0xFF8C561B),
                        onClick = { onOpenAccountsTab(0) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_shop_due_card")
                    )
                    DashboardCompactLedgerTile(
                        title = if (isBn) "পাওনা" else strings.totalReceivable,
                        value = if (hidden) "৳ ••••" else CurrencyFormatter.formatPoisha(
                            uiState.financialSummary.totalReceivablePoisha,
                            numerals
                        ),
                        icon = Icons.Filled.Handshake,
                        badgeBg = if (isDark) Color(0xFF152942) else Color(0xFFE3EEFA),
                        iconTint = if (isDark) Color(0xFF6BB1FF) else Color(0xFF1A62C6),
                        onClick = { onOpenAccountsTab(1) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_receivable_card")
                    )
                }

                if (uiState.financialSummary.totalPersonalPayablePoisha > 0L) {
                    DashboardCompactLedgerTile(
                        title = if (isBn) "ধার নেওয়া (দেনা)" else strings.totalPayable,
                        value = if (hidden) "৳ ••••" else CurrencyFormatter.formatPoisha(
                            uiState.financialSummary.totalPersonalPayablePoisha,
                            numerals
                        ),
                        icon = Icons.Filled.ArrowUpward,
                        badgeBg = if (isDark) Color(0xFF3A1D1E) else Color(0xFFFDE6E4),
                        iconTint = finance.expense,
                        onClick = { onOpenAccountsTab(2) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_payable_card")
                    )
                }
            }
        }

        // 6. Monthly Cashflow Grouped Bar Chart Card with Period Selector Dropdown
        item(key = "monthly_cashflow_chart") {
            MonthlyCashflowBarChartCard(
                points = monthlyBars,
                selectedMonths = chartMonthCount,
                onSelectMonths = { chartMonthCount = it },
                hidden = hidden,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 7. Recent Transactions Card ("সাম্প্রতিক লেনদেন" + "সব দেখুন →")
        item(key = "recent_transactions") {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (recentTxs.isEmpty()) {
                    EmptyState(
                        icon = Icons.Filled.ReceiptLong,
                        title = strings.noTransactionsYet,
                        subtitle = strings.noTransactionsHint,
                        actionLabel = strings.addIncome,
                        onAction = { onQuickAction(QuickActionType.ADD_INCOME) }
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 14.dp, end = 10.dp, top = 12.dp, bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = strings.recentTransactions,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable(onClick = onViewAllTransactions)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strings.viewAll,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = strings.viewAll,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            recentTxs.forEachIndexed { idx, tx ->
                                DashboardRecentTxRow(
                                    tx = tx,
                                    category = tx.categoryId?.let { categoriesById[it] },
                                    shop = tx.shopId?.let { shopsById[it] },
                                    todayMillis = todayMillis,
                                    yesterdayMillis = yesterdayMillis,
                                    hidden = hidden,
                                    onClick = { onTransactionClick(tx) }
                                )
                                if (idx < recentTxs.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 66.dp, end = 14.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Unified Curved Deep Emerald Canopy with Brand Header, Greeting, Book Selector,
 * and the overlapping Hero Balance Card resting across the curved transition.
 */
@Composable
private fun DashboardHeroCanopyAndBalance(
    uiState: JomaKhorochUiState,
    greeting: String,
    todayMillis: Long,
    hasUrgentAlert: Boolean,
    hidden: Boolean,
    onSwitchBookClick: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleHideBalance: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA
    val bgColor = MaterialTheme.colorScheme.background

    Box(modifier = Modifier.fillMaxWidth()) {
        // Curved Deep Emerald Background Canopy behind the header & upper half of Balance Card
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = 50.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(finance.canopyTop, finance.canopyMid, finance.canopyBottom)
                    )
                )
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                // Subtle botanical foliage silhouettes & tonal glow
                drawCircle(
                    color = Color.White.copy(alpha = 0.045f),
                    radius = size.width * 0.48f,
                    center = Offset(size.width * 0.85f, size.height * 0.14f)
                )
                drawCircle(
                    color = WarmGold.copy(alpha = 0.055f),
                    radius = size.width * 0.34f,
                    center = Offset(size.width * 0.10f, size.height * 0.70f)
                )

                // Left & right stylized emerald leaves peeking near the Hero Balance Card edges
                val leftLeaf = Path().apply {
                    moveTo(-10f, size.height * 0.48f)
                    quadraticTo(size.width * 0.14f, size.height * 0.56f, size.width * 0.18f, size.height * 0.78f)
                    quadraticTo(size.width * 0.04f, size.height * 0.76f, -10f, size.height * 0.64f)
                    close()
                }
                drawPath(
                    path = leftLeaf,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF3FA85D).copy(alpha = 0.48f), Color(0xFF0E4D2F).copy(alpha = 0.32f))
                    )
                )
                val rightLeaf = Path().apply {
                    moveTo(size.width + 10f, size.height * 0.46f)
                    quadraticTo(size.width * 0.85f, size.height * 0.55f, size.width * 0.82f, size.height * 0.78f)
                    quadraticTo(size.width * 0.96f, size.height * 0.75f, size.width + 10f, size.height * 0.62f)
                    close()
                }
                drawPath(
                    path = rightLeaf,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF47B365).copy(alpha = 0.48f), Color(0xFF0E4D2F).copy(alpha = 0.32f))
                    )
                )

                // Soft curved transition at the bottom of canopy into Warm Ivory / Dark background
                val curveHeight = 28.dp.toPx()
                val bgCutout = Path().apply {
                    moveTo(0f, size.height - curveHeight)
                    quadraticTo(
                        size.width * 0.5f,
                        size.height + curveHeight * 0.85f,
                        size.width,
                        size.height - curveHeight
                    )
                    lineTo(size.width, size.height + 4f)
                    lineTo(0f, size.height + 4f)
                    close()
                }
                drawPath(path = bgCutout, color = bgColor)
            }
        }

        // Foreground Content: Top Bar + Greeting Row + Overlapping Hero Balance Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: Golden Wallet Brand Emblem + JomaKhoroch + Bell & Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.jomakhoroch_ic),
                        contentDescription = strings.appName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .testTag("dashboard_brand_logo")
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "JomaKhoroch",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = PlusJakartaSansFont,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp
                            ),
                            color = SoftGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = strings.tagline,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onOpenNotifications)
                            .testTag("dashboard_notifications_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.notifications.isNotEmpty()) Icons.Filled.NotificationsActive else Icons.Filled.Notifications,
                            contentDescription = if (isBn) "নোটিফিকেশন" else "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        if (uiState.notifications.isNotEmpty()) {
                            Surface(
                                color = if (hasUrgentAlert) CanopyUrgentBadge else WarmGold,
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp)
                                    .size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = CurrencyFormatter.toLocalizedDigits(
                                            if (uiState.notifications.size > 9) "9+" else uiState.notifications.size.toString(),
                                            numerals
                                        ),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onOpenSettings)
                            .testTag("header_settings_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = strings.navMore,
                            tint = Color.White,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }
            }

            // Row 2: Crisp Greeting + Motivational Subtitle + Book Switcher Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 21.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isBn) "আজ আপনার আর্থিক দিনটি হোক আরও ভালো।" else "Make today a great financial day.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!uiState.cloudSync.isOnline ||
                        uiState.cloudSync.syncStatus == SyncStatus.SYNCING ||
                        uiState.cloudSync.syncStatus == SyncStatus.ERROR
                    ) {
                        Box(modifier = Modifier.padding(top = 3.dp)) {
                            DashboardSyncBadge(uiState = uiState, onSyncNow = onSyncNow)
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                BookSwitcherPill(
                    book = uiState.activeNotebook,
                    onClick = onSwitchBookClick,
                    containerColor = Color.White.copy(alpha = 0.13f),
                    contentColor = Color.White
                )
            }

            // Row 3: Overlapping Hero Deep Emerald Balance Card with Wallet + Coins Art & Gold Ribbon
            DashboardHeroBalanceCard(
                balancePoisha = uiState.financialSummary.currentCashBalancePoisha,
                hidden = hidden,
                onToggleHide = onToggleHideBalance
            )
        }
    }
}

@Composable
private fun DashboardHeroBalanceCard(
    balancePoisha: Long,
    hidden: Boolean,
    onToggleHide: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_balance_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (finance.isDark) Color(0xFF0C3A2C) else DeepEmerald),
        border = BorderStroke(
            width = 1.4.dp,
            brush = Brush.linearGradient(
                colors = if (finance.isDark) {
                    listOf(Color(0xFF4BE0A0).copy(alpha = 0.65f), finance.goldAccent.copy(alpha = 0.90f))
                } else {
                    listOf(Color(0xFF5FC68E).copy(alpha = 0.72f), WarmGold.copy(alpha = 0.85f))
                }
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (finance.isDark) 6.dp else 5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = if (finance.isDark) {
                            listOf(Color(0xFF105D42), Color(0xFF0A392B), Color(0xFF051E17))
                        } else {
                            listOf(Color(0xFF0E6E47), DeepEmerald, DarkGreen)
                        }
                    )
                )
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                // Ascending translucent bars + growth arrow with arrowhead on the right
                val barWidth = 10.dp.toPx()
                val gap = 6.dp.toPx()
                val baseX = size.width - 96.dp.toPx()
                val baseY = size.height - 26.dp.toPx()
                val heights = listOf(18f, 28f, 38f, 52f, 68f)
                heights.forEachIndexed { idx, h ->
                    val barH = h.dp.toPx().coerceAtMost(size.height * 0.58f)
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.10f),
                        topLeft = Offset(baseX + idx * (barWidth + gap), baseY - barH),
                        size = Size(barWidth, barH),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }

                // Growth arrow curve + crisp arrowhead tip
                val tipX = size.width - 18.dp.toPx()
                val tipY = baseY - 82.dp.toPx()
                val arrowPath = Path().apply {
                    moveTo(baseX - 2.dp.toPx(), baseY - 28.dp.toPx())
                    quadraticTo(
                        baseX + 40.dp.toPx(),
                        baseY - 46.dp.toPx(),
                        tipX,
                        tipY
                    )
                }
                drawPath(
                    path = arrowPath,
                    color = Color.White.copy(alpha = 0.18f),
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                val arrowHead = Path().apply {
                    moveTo(tipX, tipY)
                    lineTo(tipX - 11.dp.toPx(), tipY + 2.dp.toPx())
                    moveTo(tipX, tipY)
                    lineTo(tipX - 3.dp.toPx(), tipY + 11.dp.toPx())
                }
                drawPath(
                    path = arrowHead,
                    color = Color.White.copy(alpha = 0.20f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Flowing Warm Gold bottom-right curved ribbon
                val ribbon = Path().apply {
                    moveTo(size.width * 0.42f, size.height)
                    quadraticTo(
                        size.width * 0.80f,
                        size.height * 0.90f,
                        size.width,
                        size.height * 0.50f
                    )
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(
                    path = ribbon,
                    brush = Brush.linearGradient(
                        colors = listOf(WarmGold.copy(alpha = 0.82f), SoftGold, WarmGold)
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Custom Drawn 3D-Shaded Emerald Wallet + Gold Coins Artwork (Matching mockup left emblem!)
                Canvas(modifier = Modifier.size(width = 82.dp, height = 74.dp)) {
                    val w = size.width
                    val h = size.height

                    // Ground soft shadow
                    drawOval(
                        color = Color.Black.copy(alpha = 0.28f),
                        topLeft = Offset(w * 0.04f, h * 0.84f),
                        size = Size(w * 0.86f, h * 0.14f)
                    )

                    // Gold coin peeking out top of wallet
                    drawCircle(
                        brush = Brush.linearGradient(listOf(SoftGold, WarmGold)),
                        radius = w * 0.23f,
                        center = Offset(w * 0.50f, h * 0.25f)
                    )
                    drawCircle(
                        color = Color(0xFFB5822A),
                        radius = w * 0.17f,
                        center = Offset(w * 0.50f, h * 0.25f),
                        style = Stroke(width = 1.6.dp.toPx())
                    )

                    // Wallet back flap
                    drawRoundRect(
                        color = Color(0xFF07492E),
                        topLeft = Offset(w * 0.12f, h * 0.23f),
                        size = Size(w * 0.76f, h * 0.62f),
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )

                    // Wallet main front emerald leather body
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF18A65E), Color(0xFF0B6E3E))
                        ),
                        topLeft = Offset(w * 0.10f, h * 0.29f),
                        size = Size(w * 0.76f, h * 0.58f),
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                    drawRoundRect(
                        color = SoftGold.copy(alpha = 0.45f),
                        topLeft = Offset(w * 0.10f, h * 0.29f),
                        size = Size(w * 0.76f, h * 0.58f),
                        cornerRadius = CornerRadius(12.dp.toPx()),
                        style = Stroke(width = 1.1.dp.toPx())
                    )

                    // Wallet clasp tab on right + gold button
                    drawRoundRect(
                        color = Color(0xFF0A5533),
                        topLeft = Offset(w * 0.64f, h * 0.47f),
                        size = Size(w * 0.28f, h * 0.24f),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                    drawCircle(
                        brush = Brush.radialGradient(listOf(SoftGold, WarmGold)),
                        radius = 4.8.dp.toPx(),
                        center = Offset(w * 0.77f, h * 0.59f)
                    )

                    // Two small stacked gold coins at bottom-left
                    drawOval(
                        brush = Brush.horizontalGradient(listOf(WarmGold, SoftGold)),
                        topLeft = Offset(w * 0.02f, h * 0.73f),
                        size = Size(w * 0.27f, h * 0.15f)
                    )
                    drawOval(
                        brush = Brush.horizontalGradient(listOf(SoftGold, WarmGold)),
                        topLeft = Offset(w * 0.14f, h * 0.81f),
                        size = Size(w * 0.29f, h * 0.15f)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBn) "বর্তমান ব্যালেন্স" else strings.cashBalance,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                    Spacer(Modifier.height(2.dp))
                    AnimatedContent(
                        targetState = hidden,
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                        label = "hero_balance_anim"
                    ) { isHidden ->
                        val hiddenDesc = if (isBn) "ব্যালেন্স গোপন করা আছে" else "Balance hidden"
                        Text(
                            text = if (isHidden) "৳ ••••••"
                            else CurrencyFormatter.formatPoisha(balancePoisha, numerals),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = PlusJakartaSansFont,
                                fontSize = 33.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .semantics {
                                    if (isHidden) contentDescription = hiddenDesc
                                }
                                .testTag("hero_cash_balance_value")
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onToggleHide)
                            .padding(vertical = 3.dp, horizontal = 2.dp)
                            .testTag("toggle_hide_balance_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (hidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (isBn) "ব্যালেন্স দেখান বা লুকান" else "Toggle balance visibility",
                            tint = Color.White.copy(alpha = 0.92f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isBn) {
                                if (hidden) "ব্যালেন্স দেখান" else "দেখান / লুকান"
                            } else {
                                if (hidden) "Show Balance" else "Show / Hide"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.92f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardIncomeExpenseCard(
    title: String,
    amountPoisha: Long,
    isIncome: Boolean,
    hidden: Boolean,
    deltaNote: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isDark = finance.isDark

    val containerBg = if (isIncome) {
        if (isDark) Color(0xFF122C21) else Color(0xFFE7F5E8)
    } else {
        if (isDark) Color(0xFF31191A) else Color(0xFFFDEAE9)
    }
    val borderColor = if (isIncome) {
        if (isDark) Color(0xFF234D3C) else Color(0xFFC2E3C8)
    } else {
        if (isDark) Color(0xFF5A2C2C) else Color(0xFFF4C5C2)
    }
    val badgeBg = if (isIncome) Color(0xFF0E8F47) else Color(0xFFC62828)
    val amountColor = if (isIncome) {
        if (isDark) Color(0xFF63E09C) else Color(0xFF0B5D30)
    } else {
        if (isDark) Color(0xFFFF8680) else Color(0xFFB71C1C)
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (hidden) "৳ ••••" else CurrencyFormatter.formatPoisha(amountPoisha, numerals),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSansFont,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = amountColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!deltaNote.isNullOrBlank()) {
                Text(
                    text = deltaNote,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DashboardQuickActionTile(
    label: String,
    icon: ImageVector,
    circleColor: Color,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    val finance = LocalFinanceColors.current
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .testTag(tag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (finance.isDark) MaterialTheme.colorScheme.surfaceContainer else Color(0xFFFCFBF7)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (finance.isDark) circleColor.copy(alpha = 0.30f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (finance.isDark) 0.5.dp else 1.2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 4.dp, vertical = 11.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = if (finance.isDark) {
                                listOf(circleColor.copy(alpha = 0.88f), circleColor.copy(alpha = 0.72f))
                            } else {
                                listOf(circleColor.copy(alpha = 0.92f), circleColor)
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(23.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DashboardCompactLedgerTile(
    title: String,
    value: String,
    icon: ImageVector,
    badgeBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(21.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlusJakartaSansFont,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val finance = LocalFinanceColors.current
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (finance.isDark) SoftGold else Color(0xFF9C621B),
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun DashboardRecentTxRow(
    tx: LedgerTransactionEntity,
    category: CategoryEntity?,
    shop: ShopEntity?,
    todayMillis: Long,
    yesterdayMillis: Long,
    hidden: Boolean,
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA
    val txType = LedgerTransactionType.valueOf(tx.type)

    val isPositive = txType == LedgerTransactionType.INCOME ||
        txType == LedgerTransactionType.LENDING_REPAYMENT ||
        txType == LedgerTransactionType.BORROWING_TAKEN

    val circleBg = if (isPositive) {
        if (finance.isDark) Color(0xFF153726) else Color(0xFFD5F3DA)
    } else {
        if (finance.isDark) Color(0xFF3A1C1E) else Color(0xFFFDE2E2)
    }
    val iconTint = if (isPositive) finance.income else finance.expense
    val icon = when (txType) {
        LedgerTransactionType.INCOME -> iconForKey(category?.iconKey ?: "payments")
        LedgerTransactionType.EXPENSE -> iconForKey(category?.iconKey ?: "shopping_cart")
        LedgerTransactionType.SHOP_PURCHASE,
        LedgerTransactionType.SHOP_DUE_PAYMENT -> Icons.Filled.Storefront
        else -> Icons.Filled.Handshake
    }
    val title = when (txType) {
        LedgerTransactionType.INCOME ->
            if (isBn) category?.nameBn ?: strings.incomeLabel else category?.nameEn ?: strings.incomeLabel
        LedgerTransactionType.EXPENSE ->
            if (isBn) category?.nameBn ?: strings.expenseLabel else category?.nameEn ?: strings.expenseLabel
        LedgerTransactionType.SHOP_PURCHASE -> shop?.name ?: strings.shopDueLabel
        LedgerTransactionType.SHOP_DUE_PAYMENT -> "${shop?.name ?: strings.shopLabel} (${strings.recordPayment})"
        LedgerTransactionType.LENDING_GIVEN -> strings.moneyGiven
        LedgerTransactionType.LENDING_REPAYMENT -> strings.receiveRepayment
        LedgerTransactionType.BORROWING_TAKEN -> strings.moneyBorrowed
        LedgerTransactionType.BORROWING_REPAYMENT -> strings.makeRepayment
    }

    val timeStr = DateUtils.formatTimeOfDay(tx.transactionDate, language, numerals)
    val relativeDate = when {
        DateUtils.isSameDay(tx.transactionDate, todayMillis) ->
            "${if (isBn) "আজ" else "Today"}, $timeStr"
        DateUtils.isSameDay(tx.transactionDate, yesterdayMillis) ->
            "${if (isBn) "গতকাল" else "Yesterday"}, $timeStr"
        else -> "${DateUtils.formatDate(tx.transactionDate, language, numerals)}, $timeStr"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("tx_card_${tx.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(circleBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (tx.note.isNotBlank() && !tx.note.equals(title, ignoreCase = true)) {
                    "$relativeDate • ${tx.note}"
                } else relativeDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        val sign = if (isPositive) "+" else "-"
        Text(
            text = if (hidden) "৳ ••••" else "$sign${CurrencyFormatter.formatPoisha(tx.totalAmountPoisha, numerals)}",
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = PlusJakartaSansFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.5.sp
            ),
            color = iconTint,
            maxLines = 1
        )
    }
}

@Composable
private fun DashboardSyncBadge(
    uiState: JomaKhorochUiState,
    onSyncNow: () -> Unit
) {
    val language = LocalLanguage.current
    val isBn = language == AppLanguage.BANGLA
    val sync = uiState.cloudSync

    val (icon, label, tint) = when {
        !sync.isOnline -> Triple(Icons.Filled.CloudOff, if (isBn) "অফলাইন" else "Offline", SoftGold)
        sync.syncStatus == SyncStatus.SYNCING -> Triple(Icons.Filled.CloudSync, if (isBn) "সিঙ্ক..." else "Syncing", SoftGold)
        sync.syncStatus == SyncStatus.ERROR -> Triple(Icons.Filled.SyncProblem, if (isBn) "পুনঃচেষ্টা" else "Retry", Color(0xFFFFB4AB))
        else -> Triple(Icons.Filled.CloudDone, if (isBn) "অনলাইন" else "Synced", CanopyMintAccent)
    }

    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onSyncNow)
            .testTag("dashboard_sync_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color.White.copy(alpha = 0.92f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MonthlyCashflowBarChartCard(
    points: List<MonthlyBarPoint>,
    selectedMonths: Int,
    onSelectMonths: (Int) -> Unit,
    hidden: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val haptic = LocalHapticFeedback.current
    val isBn = language == AppLanguage.BANGLA

    var showPeriodMenu by remember { mutableStateOf(false) }
    var selectedBarIdx by remember(points, selectedMonths) { mutableStateOf<Int?>(null) }

    val barProgress = remember { Animatable(0f) }
    LaunchedEffect(points, selectedMonths) {
        barProgress.snapTo(0f)
        barProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
        )
    }

    val maxPoisha = remember(points) {
        points.maxOfOrNull { maxOf(it.incomePoisha, it.expensePoisha) } ?: 0L
    }
    val hasData = maxPoisha > 0L
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    val periodLabel = when (selectedMonths) {
        3 -> if (isBn) "৩ মাস" else "3 Months"
        6 -> if (isBn) "৬ মাস" else "6 Months"
        else -> if (isBn) "এই মাস" else "This Month"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isBn) "মাসিক ক্যাশফ্লো" else "Monthly Cashflow",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (finance.isDark) MaterialTheme.colorScheme.surfaceContainer else Color(0xFFF6F2E6),
                        border = BorderStroke(
                            1.dp,
                            if (finance.isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE5DEC9)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { showPeriodMenu = true }
                            .testTag("cashflow_period_dropdown")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = periodLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showPeriodMenu,
                        onDismissRequest = { showPeriodMenu = false }
                    ) {
                        listOf(
                            5 to (if (isBn) "এই মাস (৫ মাস)" else "This Month (5 Mo)"),
                            3 to (if (isBn) "গত ৩ মাস" else "Last 3 Months"),
                            6 to (if (isBn) "গত ৬ মাস" else "Last 6 Months")
                        ).forEach { (count, lbl) ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = lbl,
                                        fontWeight = if (selectedMonths == count) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedMonths == count) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onSelectMonths(count)
                                    showPeriodMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Right-aligned Legend matching mockup ("● আয়   ● ব্যয়")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (hasData) {
                        if (isBn) "বিস্তারিত দেখতে মাসে ট্যাপ করুন" else "Tap a month to inspect"
                    } else "",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChartLegendDot(label = strings.incomeLabel, color = finance.chartIncome)
                    Spacer(Modifier.width(12.dp))
                    ChartLegendDot(label = strings.expenseLabel, color = finance.chartExpense)
                }
            }

            // Interactive tapped-month breakdown strip
            val inspectedPoint = selectedBarIdx?.let { points.getOrNull(it) }
            AnimatedVisibility(
                visible = hasData && inspectedPoint != null,
                enter = fadeIn(tween(180)) + expandVertically(tween(200)),
                exit = fadeOut(tween(140)) + shrinkVertically(tween(160))
            ) {
                inspectedPoint?.let { pt ->
                    val netPoisha = pt.incomePoisha - pt.expensePoisha
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DateUtils.formatMonthYear(pt.yearMonth, language, numerals),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (hidden) "৳ ••••" else "+${CurrencyFormatter.formatPoisha(pt.incomePoisha, numerals)}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = PlusJakartaSansFont,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = finance.income
                                )
                                Text(
                                    text = if (hidden) "৳ ••••" else "-${CurrencyFormatter.formatPoisha(pt.expensePoisha, numerals)}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = PlusJakartaSansFont,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = finance.expense
                                )
                                Text(
                                    text = if (hidden) "••" else "${if (netPoisha >= 0) "+" else ""}${CurrencyFormatter.formatPoisha(netPoisha, numerals)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PlusJakartaSansFont,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            if (!hasData) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBn) "আয় বা ব্যয় যোগ করলেই মাসিক ক্যাশফ্লো চার্ট দেখা যাবে"
                        else "Add income or expenses to view your monthly cashflow trend",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                val ceilTaka = (maxPoisha / 100L).coerceAtLeast(1L)
                val highlightBg = MaterialTheme.colorScheme.primary.copy(alpha = if (finance.isDark) 0.16f else 0.08f)
                val progressVal = barProgress.value

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(152.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Y-Axis compact labels
                    Column(
                        modifier = Modifier
                            .height(122.dp)
                            .width(38.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        listOf(ceilTaka, (ceilTaka * 2) / 3, ceilTaka / 3, 0L).forEach { v ->
                            Text(
                                text = if (hidden) "••" else formatCompactTaka(v, numerals),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PlusJakartaSansFont,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }

                    // Grouped Bars Canvas + Interactive X-Axis Month Labels
                    Column(modifier = Modifier.weight(1f)) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(122.dp)
                        ) {
                            val rows = 3
                            for (i in 0..rows) {
                                val y = size.height * (i.toFloat() / rows.toFloat())
                                drawLine(
                                    color = gridColor.copy(alpha = 0.65f),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                                drawLine(
                                    color = gridColor,
                                    start = Offset(-4.dp.toPx(), y),
                                    end = Offset(0f, y),
                                    strokeWidth = 1.2.dp.toPx()
                                )
                            }
                            drawLine(
                                color = gridColor.copy(alpha = 0.9f),
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 1.2.dp.toPx()
                            )

                            val groupWidth = size.width / points.size.coerceAtLeast(1)
                            val barWidth = (groupWidth * 0.28f).coerceIn(9.dp.toPx(), 18.dp.toPx())
                            val barGap = 4.dp.toPx()
                            val corner = CornerRadius(4.dp.toPx(), 4.dp.toPx())

                            points.forEachIndexed { idx, pt ->
                                val centerX = groupWidth * idx + groupWidth / 2f
                                val isPicked = selectedBarIdx == idx
                                if (isPicked) {
                                    drawRoundRect(
                                        color = highlightBg,
                                        topLeft = Offset(groupWidth * idx + 2.dp.toPx(), 0f),
                                        size = Size(groupWidth - 4.dp.toPx(), size.height),
                                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                    )
                                }
                                val barAlpha = if (selectedBarIdx != null && !isPicked) 0.48f else 1f
                                val incHeight = (if (hidden) size.height * 0.45f
                                else (pt.incomePoisha.toFloat() / maxPoisha.toFloat()).coerceIn(0f, 1f) * (size.height - 6.dp.toPx())) * progressVal
                                val expHeight = (if (hidden) size.height * 0.32f
                                else (pt.expensePoisha.toFloat() / maxPoisha.toFloat()).coerceIn(0f, 1f) * (size.height - 6.dp.toPx())) * progressVal

                                if (incHeight > 0f) {
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = if (finance.isDark) {
                                                listOf(
                                                    Color(0xFF56ECA8).copy(alpha = barAlpha),
                                                    Color(0xFF1FA86B).copy(alpha = barAlpha)
                                                )
                                            } else {
                                                listOf(
                                                    Color(0xFF19A159).copy(alpha = barAlpha),
                                                    Color(0xFF0C723C).copy(alpha = barAlpha)
                                                )
                                            },
                                            startY = size.height - incHeight,
                                            endY = size.height
                                        ),
                                        topLeft = Offset(centerX - barWidth - barGap / 2f, size.height - incHeight),
                                        size = Size(barWidth, incHeight),
                                        cornerRadius = corner
                                    )
                                }
                                if (expHeight > 0f) {
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            colors = if (finance.isDark) {
                                                listOf(
                                                    Color(0xFFFF9B91).copy(alpha = barAlpha),
                                                    Color(0xFFE0594E).copy(alpha = barAlpha)
                                                )
                                            } else {
                                                listOf(
                                                    Color(0xFFFF7262).copy(alpha = barAlpha),
                                                    Color(0xFFEC473A).copy(alpha = barAlpha)
                                                )
                                            },
                                            startY = size.height - expHeight,
                                            endY = size.height
                                        ),
                                        topLeft = Offset(centerX + barGap / 2f, size.height - expHeight),
                                        size = Size(barWidth, expHeight),
                                        cornerRadius = corner
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            points.forEachIndexed { idx, pt ->
                                val isPicked = selectedBarIdx == idx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isPicked) highlightBg else Color.Transparent)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedBarIdx = if (isPicked) null else idx
                                        }
                                        .padding(vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pt.shortLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isPicked) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isPicked) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
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

@Composable
private fun ChartLegendDot(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun shortMonthName(monthOneBased: Int, isBn: Boolean): String {
    val bn = arrayOf("জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে")
    val en = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val idx = (monthOneBased - 1).coerceIn(0, 11)
    return if (isBn) bn[idx] else en[idx]
}

private fun formatCompactTaka(taka: Long, numerals: com.example.core.localization.NumeralStyle): String {
    val raw = when {
        taka >= 100_000L -> "${taka / 1000}k"
        taka >= 1_000L -> {
            val whole = taka / 1000
            val tenths = (taka % 1000) / 100
            if (tenths == 0L) "${whole}k" else "$whole.${tenths}k"
        }
        else -> taka.toString()
    }
    return CurrencyFormatter.toLocalizedDigits(raw, numerals)
}

private fun formatMoMDelta(
    current: Long,
    previous: Long,
    isBn: Boolean,
    numerals: com.example.core.localization.NumeralStyle
): String? {
    if (previous <= 0L) return null
    val pct = (((current - previous).toDouble() / previous.toDouble()) * 100.0).toInt()
    val sign = if (pct >= 0) "+" else "-"
    val arrow = if (pct >= 0) "↑" else "↓"
    val formattedPct = CurrencyFormatter.toLocalizedDigits("${abs(pct)}%", numerals)
    return if (isBn) "$arrow $sign$formattedPct (গত মাসের তুলনায়)" else "$arrow $sign$formattedPct vs last mo"
}
