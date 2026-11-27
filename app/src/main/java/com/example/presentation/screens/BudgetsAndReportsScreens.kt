package com.example.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.LocalLanguage
import com.example.core.localization.LocalNumeralStyle
import com.example.core.localization.localizedBookName
import com.example.core.util.CurrencyFormatter
import com.example.core.util.DateUtils
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.LedgerTransactionEntity
import com.example.data.repository.toLedgerRecord
import com.example.domain.accounting.AccountingEngine
import com.example.domain.accounting.BudgetProgress
import com.example.domain.accounting.BudgetStatus
import com.example.domain.accounting.LedgerTransactionType
import com.example.infrastructure.ReportExportPayload
import com.example.infrastructure.ReportExporter
import com.example.presentation.components.EmptyState
import com.example.presentation.components.IncomeExpenseCard
import com.example.presentation.components.PremiumHeader
import com.example.presentation.components.PremiumPageTitleRow
import com.example.presentation.components.PremiumSectionBanner
import com.example.presentation.components.iconForKey
import com.example.presentation.state.JomaKhorochUiState
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LocalFinanceColors
import com.example.ui.theme.PlusJakartaSansFont
import com.example.ui.theme.SoftGold
import com.example.ui.theme.WarmGold
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max

private data class CategorySliceInfo(
    val label: String,
    val amountPoisha: Long,
    val iconKey: String
)

@Composable
fun BudgetsScreen(
    uiState: JomaKhorochUiState,
    onChangeMonth: (String) -> Unit,
    onSaveBudget: (categoryId: Long?, limitPoisha: Long) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onOpenReports: (() -> Unit)? = null,
    onOpenReportsWithPeriod: ((ReportPeriod) -> Unit)? = null,
    onOpenNotifications: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    topSwitcher: (@Composable () -> Unit)? = null
) {
    BackHandler(enabled = onBack != null) { onBack?.invoke() }
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA
    val isDark = finance.isDark

    val monthRange = remember(uiState.selectedBudgetMonth) { DateUtils.monthRange(uiState.selectedBudgetMonth) }
    val monthTxs = remember(uiState.transactions, monthRange) {
        uiState.transactions.filter { it.transactionDate in monthRange.first..monthRange.second }
    }
    val (monthIncome, totalMonthExpense) = remember(monthTxs, monthRange) {
        AccountingEngine.computePeriodIncomeExpense(
            monthTxs.map { it.toLedgerRecord() },
            monthRange.first,
            monthRange.second
        )
    }

    val overallBudget = uiState.budgets.firstOrNull { it.categoryId == null }
    val categoryBudgets = uiState.budgets.filter { it.categoryId != null }
    val expenseCategories = uiState.categories.filter { it.type == "EXPENSE" }
    val categoriesById = remember(uiState.categories) { uiState.categories.associateBy { it.id } }

    val categorySpendingSlices = remember(monthTxs, categoriesById, language) {
        monthTxs.filter {
            it.type == LedgerTransactionType.EXPENSE.name || it.type == LedgerTransactionType.SHOP_PURCHASE.name
        }.groupBy { it.categoryId }
            .map { (catId, list) ->
                val cat = catId?.let { categoriesById[it] }
                val label = when {
                    cat != null && isBn -> cat.nameBn
                    cat != null -> cat.nameEn
                    else -> strings.shopDueLabel
                }
                CategorySliceInfo(
                    label = label,
                    amountPoisha = list.sumOf { it.totalAmountPoisha },
                    iconKey = cat?.iconKey ?: "storefront"
                )
            }
            .filter { it.amountPoisha > 0L }
            .sortedByDescending { it.amountPoisha }
    }

    val currentYm = remember { DateUtils.currentYearMonth() }
    val monthPickerOptions = remember(currentYm, language, numerals) {
        (1 downTo -11).map { offset ->
            val ym = DateUtils.shiftYearMonth(currentYm, offset)
            ym to DateUtils.formatMonthYear(ym, language, numerals)
        }
    }

    var showMonthDropdown by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var dialogIsOverall by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PremiumHeader(
            onBackClick = onBack,
            notificationCount = uiState.notifications.size,
            hasUrgentNotification = uiState.notifications.any { it.isUrgent },
            onNotificationClick = onOpenNotifications,
            onSettingsClick = onOpenSettings
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (topSwitcher != null) {
                item(key = "budget_top_switcher") {
                    topSwitcher()
                }
            }

            // 1. Page Title ("বাজেট ও রিপোর্ট") + Month Dropdown Pill ("এপ্রিল ২০২৫ ⌄")
            item(key = "budget_title_row") {
                PremiumPageTitleRow(
                    icon = Icons.Filled.BarChart,
                    title = if (isBn) "বাজেট ও রিপোর্ট" else "Budget & Reports",
                    subtitle = if (isBn) "আপনার আর্থিক পরিকল্পনা ও বিশ্লেষণ" else "Financial planning & analytics",
                    trailing = {
                        Box {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isDark) MaterialTheme.colorScheme.surfaceContainer else Color(0xFFF8F4EA),
                                border = BorderStroke(
                                    1.dp,
                                    if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE5DEC9)
                                ),
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { showMonthDropdown = true }
                                    .testTag("budget_month_selector")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = DateUtils.formatMonthYear(uiState.selectedBudgetMonth, language, numerals),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
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
                                expanded = showMonthDropdown,
                                onDismissRequest = { showMonthDropdown = false }
                            ) {
                                monthPickerOptions.forEach { (ym, label) ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = label,
                                                fontWeight = if (uiState.selectedBudgetMonth == ym) FontWeight.Bold else FontWeight.Normal,
                                                color = if (uiState.selectedBudgetMonth == ym) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            onChangeMonth(ym)
                                            showMonthDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // 2. Hero Deep Emerald Monthly Budget Card (Bullseye Target Art + 2-Column Budget vs Spent + Gold Ribbon)
            item(key = "overall_budget_hero") {
                val progress = overallBudget?.let {
                    AccountingEngine.computeBudgetProgress(it.limitPoisha, totalMonthExpense)
                }
                HeroMonthlyBudgetCard(
                    progress = progress,
                    totalMonthExpense = totalMonthExpense,
                    onSetOrEditBudget = {
                        dialogIsOverall = true
                        showBudgetDialog = true
                    }
                )
            }

            // 3. 4-Column Single-Row Vertical Pastel KPI Strip (মোট আয় | মোট ব্যয় | পাওনা | দেনা)
            item(key = "budget_mini_kpis") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BudgetMiniKpiTile(
                        title = strings.totalIncome,
                        amountPoisha = monthIncome,
                        accent = if (isDark) Color(0xFF63E09C) else Color(0xFF0E683B),
                        iconBg = Color(0xFF0E8F47),
                        iconTint = Color.White,
                        container = if (isDark) Color(0xFF132E23) else Color(0xFFE7F6E9),
                        borderColor = if (isDark) Color(0xFF234D3C) else Color(0xFFC5E5CB),
                        icon = Icons.Filled.ArrowUpward,
                        filledCircleIcon = true,
                        modifier = Modifier.weight(1f)
                    )
                    BudgetMiniKpiTile(
                        title = strings.totalExpense,
                        amountPoisha = totalMonthExpense,
                        accent = if (isDark) Color(0xFFFF8680) else Color(0xFFC62828),
                        iconBg = Color(0xFFD93832),
                        iconTint = Color.White,
                        container = if (isDark) Color(0xFF331B1B) else Color(0xFFFDEAE8),
                        borderColor = if (isDark) Color(0xFF5A2C2C) else Color(0xFFF4C9C6),
                        icon = Icons.Filled.ArrowDownward,
                        filledCircleIcon = true,
                        modifier = Modifier.weight(1f)
                    )
                    BudgetMiniKpiTile(
                        title = if (isBn) "পাওনা" else "Receivable",
                        amountPoisha = uiState.financialSummary.totalPersonalReceivablePoisha,
                        accent = if (isDark) Color(0xFF6BB1FF) else Color(0xFF1A5FC4),
                        iconBg = Color.Transparent,
                        iconTint = if (isDark) Color(0xFF6BB1FF) else Color(0xFF1D6CE0),
                        container = if (isDark) Color(0xFF14273E) else Color(0xFFE9F2FC),
                        borderColor = if (isDark) Color(0xFF26476D) else Color(0xFFC8DFF6),
                        icon = Icons.Filled.Handshake,
                        filledCircleIcon = false,
                        modifier = Modifier.weight(1f)
                    )
                    BudgetMiniKpiTile(
                        title = if (isBn) "দেনা" else "Payable",
                        amountPoisha = uiState.financialSummary.totalPayablePoisha,
                        accent = if (isDark) Color(0xFFF0B866) else Color(0xFF8C561B),
                        iconBg = Color.Transparent,
                        iconTint = if (isDark) Color(0xFFF0B866) else Color(0xFFD29332),
                        container = if (isDark) Color(0xFF2E2416) else Color(0xFFFCF2DF),
                        borderColor = if (isDark) Color(0xFF4F3E26) else Color(0xFFEBD9B8),
                        icon = Icons.Filled.Savings,
                        filledCircleIcon = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Category-Wise Spending Donut Chart ("ক্যাটাগরি ভিত্তিক ব্যয় বিশ্লেষণ" + "বিস্তারিত দেখুন →")
            item(key = "budget_donut_chart") {
                CategorySpendingDonutCard(
                    title = if (isBn) "ক্যাটাগরি ভিত্তিক ব্যয় বিশ্লেষণ" else "Category-Wise Spending",
                    slices = categorySpendingSlices,
                    palette = listOf(
                        Color(0xFFF8B319),
                        Color(0xFFF34A42),
                        Color(0xFF209B54),
                        Color(0xFF8C3DD8),
                        Color(0xFF1B86E6),
                        Color(0xFF18BCE6)
                    ),
                    actionLabel = if (onOpenReports != null) (if (isBn) "বিস্তারিত দেখুন" else "View Details") else null,
                    onActionClick = onOpenReports
                )
            }

            // 5. Monthly Income vs Expense Summary 5-Month Bar Chart with Highlighted Selected Month
            item(key = "budget_monthly_bar_chart") {
                BudgetMonthlySummaryChartCard(
                    transactions = uiState.transactions,
                    selectedYearMonth = uiState.selectedBudgetMonth,
                    onSelectMonth = onChangeMonth
                )
            }

            // 6. Quick Reports 4-Tile Strip ("দ্রুত রিপোর্ট" + "সব রিপোর্ট →")
            if (onOpenReports != null || onOpenReportsWithPeriod != null) {
                item(key = "quick_reports_grid") {
                    QuickReportsGridCard(
                        onOpenAllReports = { onOpenReports?.invoke() },
                        onSelectPeriod = { period ->
                            if (onOpenReportsWithPeriod != null) {
                                onOpenReportsWithPeriod(period)
                            } else {
                                onOpenReports?.invoke()
                            }
                        }
                    )
                }
            }

            // 7. Category Budgets Section Banner + List
            item(key = "category_budget_banner") {
                PremiumSectionBanner(
                    icon = Icons.Filled.Category,
                    title = strings.categoryBudget,
                    actionLabel = "+ ${if (isBn) "যোগ করুন" else "Add"}",
                    onActionClick = {
                        dialogIsOverall = false
                        showBudgetDialog = true
                    },
                    actionTag = "add_category_budget_button"
                )
            }

            if (categoryBudgets.isEmpty()) {
                item(key = "category_budget_empty") {
                    EmptyState(
                        icon = Icons.Filled.Category,
                        title = if (isBn) "কোনো ক্যাটাগরি বাজেট সেট করা হয়নি" else "No category budgets yet",
                        subtitle = if (isBn) "বাজার, যাতায়াত বা বিলের জন্য আলাদা মাসিক সীমা নির্ধারণ করুন।" else "Set monthly limits for individual spending categories.",
                        actionLabel = strings.setCategoryBudget,
                        onAction = {
                            dialogIsOverall = false
                            showBudgetDialog = true
                        }
                    )
                }
            } else {
                items(categoryBudgets, key = { it.id }) { budget ->
                    val cat = budget.categoryId?.let { categoriesById[it] }
                    val catName = if (isBn) cat?.nameBn ?: "-" else cat?.nameEn ?: "-"
                    val spent = monthTxs.filter {
                        (it.type == LedgerTransactionType.EXPENSE.name || it.type == LedgerTransactionType.SHOP_PURCHASE.name) &&
                            it.categoryId == budget.categoryId
                    }.sumOf { it.totalAmountPoisha }
                    val progress = AccountingEngine.computeBudgetProgress(budget.limitPoisha, spent)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            width = if (progress.status == BudgetStatus.EXCEEDED) 1.2.dp else 1.dp,
                            color = when (progress.status) {
                                BudgetStatus.EXCEEDED -> finance.expense.copy(alpha = 0.45f)
                                BudgetStatus.WARNING -> WarmGold.copy(alpha = 0.6f)
                                BudgetStatus.HEALTHY -> MaterialTheme.colorScheme.outlineVariant
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = catName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    BudgetStatusBadge(progress.status)
                                }
                                IconButton(
                                    onClick = { onDeleteBudget(budget.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.DeleteOutline,
                                        contentDescription = strings.delete,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            BudgetProgressBlock(progress = progress)
                        }
                    }
                }
            }

            // 8. Monthly Budget History
            val historyMonths = uiState.allBudgetsForBook
                .map { it.yearMonth }
                .distinct()
                .filter { it != uiState.selectedBudgetMonth }
            if (historyMonths.isNotEmpty()) {
                item(key = "budget_history_banner") {
                    PremiumSectionBanner(
                        icon = Icons.Filled.History,
                        title = if (isBn) "পূর্ববর্তী মাসের বাজেট ইতিহাস" else "Monthly Budget History"
                    )
                }
                item(key = "budget_history_card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            historyMonths.forEachIndexed { idx, ym ->
                                val monthOverall = uiState.allBudgetsForBook
                                    .firstOrNull { it.yearMonth == ym && it.categoryId == null }
                                val limitSum = monthOverall?.limitPoisha
                                    ?: uiState.allBudgetsForBook.filter { it.yearMonth == ym }.sumOf { it.limitPoisha }
                                val (mStart, mEnd) = DateUtils.monthRange(ym)
                                val spent = uiState.transactions.filter {
                                    it.transactionDate in mStart..mEnd &&
                                        (it.type == LedgerTransactionType.EXPENSE.name || it.type == LedgerTransactionType.SHOP_PURCHASE.name)
                                }.sumOf { it.totalAmountPoisha }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onChangeMonth(ym) }
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = DateUtils.formatMonthYear(ym, language, numerals),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${CurrencyFormatter.formatPoisha(spent, numerals)} / ${CurrencyFormatter.formatPoisha(limitSum, numerals)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = PlusJakartaSansFont,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (spent > limitSum) finance.expense else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (idx < historyMonths.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBudgetDialog) {
        SetBudgetDialog(
            isOverall = dialogIsOverall,
            expenseCategories = expenseCategories,
            onSave = { catId, limit ->
                onSaveBudget(catId, limit)
                showBudgetDialog = false
            },
            onDismiss = { showBudgetDialog = false }
        )
    }
}

@Composable
private fun HeroMonthlyBudgetCard(
    progress: BudgetProgress?,
    totalMonthExpense: Long,
    onSetOrEditBudget: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val isBn = language == AppLanguage.BANGLA

    val animatedFraction by animateFloatAsState(
        targetValue = ((progress?.usagePercent ?: 0) / 100f).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "hero_budget_progress"
    )
    val barColor = when (progress?.status) {
        BudgetStatus.EXCEEDED -> Color(0xFFF87171)
        BudgetStatus.WARNING -> WarmGold
        else -> Color(0xFF7DE264)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onSetOrEditBudget)
            .testTag("hero_monthly_budget_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DeepEmerald),
        border = BorderStroke(1.2.dp, WarmGold.copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF0E6843), DeepEmerald, DarkGreen)
                    )
                )
        ) {
            // Flowing Warm Gold bottom-right curved ribbon matching mockup
            Canvas(modifier = Modifier.matchParentSize()) {
                val ribbon = Path().apply {
                    moveTo(size.width * 0.42f, size.height)
                    quadraticTo(
                        size.width * 0.80f,
                        size.height * 0.92f,
                        size.width,
                        size.height * 0.56f
                    )
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(
                    path = ribbon,
                    brush = Brush.linearGradient(
                        colors = listOf(WarmGold.copy(alpha = 0.72f), SoftGold.copy(alpha = 0.94f))
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    // Custom Canvas Bullseye Target + Green Arrow Artwork (Matching mockup!)
                    Canvas(modifier = Modifier.size(64.dp)) {
                        val cx = size.width * 0.46f
                        val cy = size.height * 0.54f
                        val rOuter = size.minDimension * 0.40f

                        // Soft shadow ellipse below target
                        drawOval(
                            color = Color.Black.copy(alpha = 0.25f),
                            topLeft = Offset(cx - rOuter * 0.9f, cy + rOuter * 0.78f),
                            size = Size(rOuter * 1.8f, rOuter * 0.34f)
                        )
                        // Outer Red Ring
                        drawCircle(color = Color(0xFFE53935), radius = rOuter, center = Offset(cx, cy))
                        // White Ring
                        drawCircle(color = Color.White, radius = rOuter * 0.72f, center = Offset(cx, cy))
                        // Inner Red Ring
                        drawCircle(color = Color(0xFFE53935), radius = rOuter * 0.45f, center = Offset(cx, cy))
                        // Bullseye White Core
                        drawCircle(color = Color.White, radius = rOuter * 0.19f, center = Offset(cx, cy))

                        // Emerald Arrow hitting center from top-right
                        val tailX = size.width * 0.90f
                        val tailY = size.height * 0.12f
                        drawLine(
                            color = Color(0xFF4ADE80),
                            start = Offset(cx, cy),
                            end = Offset(tailX, tailY),
                            strokeWidth = 3.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        // Arrow fletching feathers
                        drawLine(
                            color = Color(0xFF86EFAC),
                            start = Offset(tailX - 8.dp.toPx(), tailY + 7.dp.toPx()),
                            end = Offset(tailX - 2.dp.toPx(), tailY - 4.dp.toPx()),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0xFF22C55E),
                            start = Offset(tailX - 8.dp.toPx(), tailY + 7.dp.toPx()),
                            end = Offset(tailX + 3.dp.toPx(), tailY + 4.dp.toPx()),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Middle Column: মাসিক বাজেট + Total Limit + বাকি আছে pill
                    Column(
                        modifier = Modifier.weight(1.15f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = strings.monthlyBudget,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 1
                        )
                        Text(
                            text = if (progress != null) {
                                CurrencyFormatter.formatPoisha(progress.limitPoisha, numerals)
                            } else {
                                if (isBn) "+ সেট করুন" else "+ Set Limit"
                            },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = PlusJakartaSansFont,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        val remBg = when (progress?.status) {
                            BudgetStatus.EXCEEDED -> Color(0xFFFFDAD6)
                            else -> Color(0xFFE3F5E4)
                        }
                        val remTextColor = when (progress?.status) {
                            BudgetStatus.EXCEEDED -> Color(0xFF93000A)
                            else -> Color(0xFF0A4D30)
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = remBg
                        ) {
                            Text(
                                text = if (progress != null) {
                                    "${if (isBn) "বাকি আছে" else strings.remaining}: ${
                                        CurrencyFormatter.formatPoisha(progress.remainingPoisha, numerals)
                                    }"
                                } else {
                                    if (isBn) "ট্যাপ করে বাজেট সেট করুন" else "Tap to set budget"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                fontWeight = FontWeight.Bold,
                                color = remTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Thin Translucent Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(0.82f)
                            .background(Color.White.copy(alpha = 0.25f))
                    )

                    // Right Column: ব্যয় হয়েছে + Spent Amount + Progress Bar & Percentage Pill
                    Column(
                        modifier = Modifier.weight(0.95f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isBn) "ব্যয় হয়েছে" else strings.spent,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.86f),
                            maxLines = 1
                        )
                        Text(
                            text = CurrencyFormatter.formatPoisha(totalMonthExpense, numerals),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = PlusJakartaSansFont,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LinearProgressIndicator(
                                progress = { animatedFraction },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = barColor,
                                trackColor = Color.White.copy(alpha = 0.22f)
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (progress?.status == BudgetStatus.EXCEEDED) Color(0xFFB91C1C)
                                else Color(0xFF1F7A3E)
                            ) {
                                Text(
                                    text = "${CurrencyFormatter.toLocalizedDigits((progress?.usagePercent ?: 0).toString(), numerals)}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PlusJakartaSansFont,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // 80% / 100% Warning Strip (Preserved for budget alerts!)
                if (progress != null && progress.status != BudgetStatus.HEALTHY) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (progress.status == BudgetStatus.EXCEEDED) {
                            Color(0xFF7F1D1D).copy(alpha = 0.78f)
                        } else {
                            WarmGold.copy(alpha = 0.24f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (progress.status == BudgetStatus.EXCEEDED) Color(0xFFF87171) else SoftGold
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (progress.status == BudgetStatus.EXCEEDED) Color(0xFFFCA5A5) else SoftGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (progress.status == BudgetStatus.EXCEEDED) {
                                    if (isBn) "সতর্কতা: মাসিক ব্যয় ১০০% বাজেট সীমা অতিক্রম করেছে!"
                                    else "Alert: Monthly spending exceeded 100% of budget!"
                                } else {
                                    if (isBn) "সতর্কতা: ৮০%-এর বেশি বাজেট ব্যবহার হয়েছে।"
                                    else "Warning: Over 80% of monthly budget used."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetMiniKpiTile(
    title: String,
    amountPoisha: Long,
    accent: Color,
    iconBg: Color,
    iconTint: Color,
    container: Color,
    borderColor: Color,
    icon: ImageVector,
    filledCircleIcon: Boolean,
    modifier: Modifier = Modifier
) {
    val numerals = LocalNumeralStyle.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = container,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .then(
                        if (filledCircleIcon) {
                            Modifier
                                .clip(CircleShape)
                                .background(iconBg)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(if (filledCircleIcon) 18.dp else 26.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = CurrencyFormatter.formatPoisha(amountPoisha, numerals),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = PlusJakartaSansFont,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BudgetStatusBadge(status: BudgetStatus) {
    val strings = LocalAppStrings.current
    val finance = LocalFinanceColors.current
    val (bg, fg, label) = when (status) {
        BudgetStatus.EXCEEDED -> Triple(finance.expenseContainer, finance.expense, strings.budgetExceeded)
        BudgetStatus.WARNING -> Triple(finance.goldContainer, WarmGold, strings.budgetWarning)
        BudgetStatus.HEALTHY -> Triple(finance.incomeContainer, finance.income, strings.budgetHealthy)
    }
    Surface(color = bg, shape = RoundedCornerShape(50)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun BudgetProgressBlock(progress: BudgetProgress) {
    val strings = LocalAppStrings.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current

    val barColor = when (progress.status) {
        BudgetStatus.EXCEEDED -> finance.expense
        BudgetStatus.WARNING -> WarmGold
        BudgetStatus.HEALTHY -> finance.income
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = "${strings.spent}: ${CurrencyFormatter.formatPoisha(progress.spentPoisha, numerals)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${strings.remaining}: ${CurrencyFormatter.formatPoisha(progress.remainingPoisha, numerals)}",
            style = MaterialTheme.typography.bodySmall,
            color = if (progress.remainingPoisha < 0L) finance.expense else finance.income,
            fontWeight = FontWeight.SemiBold
        )
    }
    LinearProgressIndicator(
        progress = { (progress.usagePercent / 100f).coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth()
            .height(9.dp)
            .clip(RoundedCornerShape(50)),
        color = barColor,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = "${CurrencyFormatter.toLocalizedDigits(progress.usagePercent.toString(), numerals)}%",
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = PlusJakartaSansFont),
            color = barColor,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = CurrencyFormatter.formatPoisha(progress.limitPoisha, numerals),
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = PlusJakartaSansFont),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Custom Canvas Donut Chart matching the mockup's "ক্যাটাগরি ভিত্তিক ব্যয় বিশ্লেষণ" card
 * with white segment dividers, center total label, colored dots + category icons, percentages, and Taka totals.
 */
@Composable
private fun CategorySpendingDonutCard(
    title: String,
    slices: List<CategorySliceInfo>,
    palette: List<Color>,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA
    val total = remember(slices) { slices.sumOf { it.amountPoisha } }
    val surfaceColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PieChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(21.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (actionLabel != null && onActionClick != null) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (finance.isDark) Color(0xFF153427) else Color(0xFFE5F5E7),
                        border = BorderStroke(
                            1.dp,
                            if (finance.isDark) Color(0xFF285844) else Color(0xFFC5E3CA)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onActionClick)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            if (slices.isEmpty() || total <= 0L) {
                Text(
                    text = if (isBn) "এই সময়ে কোনো খরচের তথ্য নেই।" else "No spending data for this period.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                val topSlices = remember(slices, isBn) {
                    if (slices.size <= 5) slices
                    else {
                        val first4 = slices.take(4)
                        val otherSum = slices.drop(4).sumOf { it.amountPoisha }
                        first4 + CategorySliceInfo(
                            label = if (isBn) "অন্যান্য" else "Other",
                            amountPoisha = otherSum,
                            iconKey = "category"
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(124.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeW = 24.dp.toPx()
                            val diameter = size.minDimension - strokeW
                            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                            val arcSize = Size(diameter, diameter)
                            var startAngle = -90f
                            topSlices.forEachIndexed { idx, slice ->
                                val sweep = (slice.amountPoisha.toFloat() / total.toFloat()) * 360f
                                val gap = if (topSlices.size > 1) 2.2f else 0f
                                drawArc(
                                    color = palette[idx % palette.size],
                                    startAngle = startAngle,
                                    sweepAngle = (sweep - gap).coerceAtLeast(1f),
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeW, cap = StrokeCap.Butt)
                                )
                                startAngle += sweep
                            }
                            drawCircle(
                                color = surfaceColor,
                                radius = (diameter - strokeW) / 2f
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isBn) "মোট ব্যয়" else "Total",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.formatPoisha(total, numerals),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = PlusJakartaSansFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        topSlices.forEachIndexed { idx, slice ->
                            val pctVal = (slice.amountPoisha.toDouble() * 100.0) / total.coerceAtLeast(1L).toDouble()
                            val pctFormatted = CurrencyFormatter.toLocalizedDigits(
                                String.format(Locale.US, "%.1f%%", pctVal),
                                numerals
                            )
                            val dotColor = palette[idx % palette.size]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                                Spacer(Modifier.width(5.dp))
                                Icon(
                                    imageVector = iconForKey(slice.iconKey),
                                    contentDescription = null,
                                    tint = dotColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = slice.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = pctFormatted,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PlusJakartaSansFont,
                                        fontSize = 10.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = CurrencyFormatter.formatPoisha(slice.amountPoisha, numerals),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PlusJakartaSansFont,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                            if (idx < topSlices.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetMonthlySummaryChartCard(
    transactions: List<LedgerTransactionEntity>,
    selectedYearMonth: String,
    onSelectMonth: (String) -> Unit
) {
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA

    val buckets = remember(transactions, selectedYearMonth, language) {
        val records = transactions.map { it.toLedgerRecord() }
        // 5-month window placing the selected month at position 3 (matching "এপ্রিল" highlighted in mockup!)
        (-3..1).map { offset ->
            val ym = DateUtils.shiftYearMonth(selectedYearMonth, offset)
            val (start, end) = DateUtils.monthRange(ym)
            val (inc, exp) = AccountingEngine.computePeriodIncomeExpense(records, start, end)
            val mIdx = (ym.substringAfter("-").toIntOrNull() ?: 1).coerceIn(1, 12) - 1
            val shortLabel = if (isBn) {
                arrayOf("জানু", "ফেব্রু", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে")[mIdx]
            } else {
                arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[mIdx]
            }
            Triple(ym, shortLabel, inc to exp)
        }
    }

    val maxVal = remember(buckets) {
        buckets.maxOfOrNull { max(it.third.first, it.third.second) } ?: 0L
    }
    val hasData = maxVal > 0L
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val highlightColumnBg = if (finance.isDark) Color(0xFF153427).copy(alpha = 0.65f) else Color(0xFFE7F6EA)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        text = if (isBn) "মাসিক আয়-ব্যয় সারসংক্ষেপ" else "Monthly Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = finance.chartIncome, label = if (isBn) "আয়" else "Income")
                    LegendDot(color = finance.chartExpense, label = if (isBn) "ব্যয়" else "Expense")
                }
            }

            if (!hasData) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBn) "আয় বা ব্যয় যোগ করলেই মাসিক তুলনামূলক চার্ট দেখা যাবে।"
                        else "Add income or expenses to see your 5-month summary.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                val ceilTaka = (maxVal / 100L).coerceAtLeast(1L)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .height(118.dp)
                            .width(34.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        listOf(ceilTaka, (ceilTaka * 2) / 3, ceilTaka / 3, 0L).forEach { v ->
                            val raw = if (v >= 1000L) "${v / 1000}k" else v.toString()
                            Text(
                                text = CurrencyFormatter.toLocalizedDigits(raw, numerals),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PlusJakartaSansFont,
                                    fontSize = 10.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(118.dp)
                        ) {
                            val slotW = size.width / buckets.size
                            // Highlight column behind the currently selected month
                            buckets.forEachIndexed { idx, (ym, _, _) ->
                                if (ym == selectedYearMonth) {
                                    drawRoundRect(
                                        color = highlightColumnBg,
                                        topLeft = Offset(idx * slotW + 2.dp.toPx(), 0f),
                                        size = Size(slotW - 4.dp.toPx(), size.height),
                                        cornerRadius = CornerRadius(8.dp.toPx())
                                    )
                                }
                            }

                            for (i in 0..3) {
                                val y = size.height * (i / 3f)
                                drawLine(
                                    color = gridColor.copy(alpha = 0.55f),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            drawLine(
                                color = gridColor.copy(alpha = 0.8f),
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 1.2.dp.toPx()
                            )

                            val barW = (slotW * 0.28f).coerceIn(8.dp.toPx(), 17.dp.toPx())
                            val gap = 4.dp.toPx()
                            val corner = CornerRadius(4.dp.toPx())

                            buckets.forEachIndexed { idx, (_, _, pair) ->
                                val (inc, exp) = pair
                                val centerX = idx * slotW + slotW / 2f
                                val incH = if (inc > 0L) ((inc.toFloat() / maxVal.toFloat()) * (size.height - 6.dp.toPx())).coerceAtLeast(4.dp.toPx()) else 0f
                                val expH = if (exp > 0L) ((exp.toFloat() / maxVal.toFloat()) * (size.height - 6.dp.toPx())).coerceAtLeast(4.dp.toPx()) else 0f
                                if (incH > 0f) {
                                    drawRoundRect(
                                        color = finance.chartIncome,
                                        topLeft = Offset(centerX - barW - gap / 2f, size.height - incH),
                                        size = Size(barW, incH),
                                        cornerRadius = corner
                                    )
                                }
                                if (expH > 0f) {
                                    drawRoundRect(
                                        color = finance.chartExpense,
                                        topLeft = Offset(centerX + gap / 2f, size.height - expH),
                                        size = Size(barW, expH),
                                        cornerRadius = corner
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(5.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            buckets.forEach { (ym, label, _) ->
                                val isSel = ym == selectedYearMonth
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onSelectMonth(ym) }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                    if (isSel) {
                                        Spacer(Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(26.dp)
                                                .height(2.5.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(MaterialTheme.colorScheme.primary)
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

@Composable
private fun QuickReportsGridCard(
    onOpenAllReports: () -> Unit,
    onSelectPeriod: (ReportPeriod) -> Unit
) {
    val language = LocalLanguage.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA
    val isDark = finance.isDark

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
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
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isBn) "দ্রুত রিপোর্ট" else "Quick Reports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isDark) Color(0xFF153427) else Color(0xFFE5F5E7),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF285844) else Color(0xFFC5E3CA)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onOpenAllReports)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = if (isBn) "সব রিপোর্ট" else "All Reports",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickReportPastelTile(
                    label = if (isBn) "দৈনিক\nরিপোর্ট" else "Daily\nReport",
                    icon = Icons.Filled.Today,
                    bg = if (isDark) Color(0xFF14273E) else Color(0xFFEBF4FD),
                    border = if (isDark) Color(0xFF26476D) else Color(0xFFC9E0F7),
                    iconTint = if (isDark) Color(0xFF6BB1FF) else Color(0xFF1E73E8),
                    onClick = { onSelectPeriod(ReportPeriod.DAILY) },
                    modifier = Modifier.weight(1f)
                )
                QuickReportPastelTile(
                    label = if (isBn) "সাপ্তাহিক\nরিপোর্ট" else "Weekly\nReport",
                    icon = Icons.Filled.CalendarMonth,
                    bg = if (isDark) Color(0xFF132E23) else Color(0xFFE7F6EA),
                    border = if (isDark) Color(0xFF234D3C) else Color(0xFFC5E5CB),
                    iconTint = if (isDark) Color(0xFF63E09C) else Color(0xFF14823E),
                    onClick = { onSelectPeriod(ReportPeriod.WEEKLY) },
                    modifier = Modifier.weight(1f)
                )
                QuickReportPastelTile(
                    label = if (isBn) "মাসিক\nরিপোর্ট" else "Monthly\nReport",
                    icon = Icons.Filled.EventNote,
                    bg = if (isDark) Color(0xFF281938) else Color(0xFFF3EAFC),
                    border = if (isDark) Color(0xFF472C63) else Color(0xFFDFC7F7),
                    iconTint = if (isDark) Color(0xFFC69BFF) else Color(0xFF8E24AA),
                    onClick = { onSelectPeriod(ReportPeriod.MONTHLY) },
                    modifier = Modifier.weight(1f)
                )
                QuickReportPastelTile(
                    label = if (isBn) "বার্ষিক\nরিপোর্ট" else "Yearly\nReport",
                    icon = Icons.Filled.BarChart,
                    bg = if (isDark) Color(0xFF2E2416) else Color(0xFFFCF2DF),
                    border = if (isDark) Color(0xFF4F3E26) else Color(0xFFECD9B6),
                    iconTint = if (isDark) Color(0xFFF5BC66) else Color(0xFFDF8B14),
                    onClick = { onSelectPeriod(ReportPeriod.YEARLY) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickReportPastelTile(
    label: String,
    icon: ImageVector,
    bg: Color,
    border: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SetBudgetDialog(
    isOverall: Boolean,
    expenseCategories: List<CategoryEntity>,
    onSave: (Long?, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    var amountText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(expenseCategories.firstOrNull()?.id) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isOverall) strings.setMonthlyBudget else strings.setCategoryBudget,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!isOverall) {
                    LazyColumn(modifier = Modifier.height(148.dp)) {
                        items(expenseCategories, key = { it.id }) { cat ->
                            val label = if (language == AppLanguage.BANGLA) cat.nameBn else cat.nameEn
                            FilterChip(
                                selected = selectedCategoryId == cat.id,
                                onClick = { selectedCategoryId = cat.id },
                                label = { Text(label) }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; errorMsg = null },
                    label = { Text(strings.amount) },
                    prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                errorMsg?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val poisha = CurrencyFormatter.parseTakaToPoisha(amountText)
                    if (poisha == null || poisha <= 0L) {
                        errorMsg = strings.invalidAmountError
                        return@Button
                    }
                    onSave(if (isOverall) null else selectedCategoryId, poisha)
                },
                shape = RoundedCornerShape(12.dp)
            ) { Text(strings.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

enum class ReportPeriod { DAILY, WEEKLY, MONTHLY, YEARLY }

@Composable
fun ReportsScreen(
    uiState: JomaKhorochUiState,
    onMessage: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialPeriod: ReportPeriod = ReportPeriod.MONTHLY,
    onOpenBudgets: (() -> Unit)? = null,
    onOpenNotifications: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn = language == AppLanguage.BANGLA

    var period by rememberSaveable(initialPeriod) { mutableStateOf(initialPeriod) }
    val now = remember { System.currentTimeMillis() }

    val (rangeStart, rangeEnd, periodLabel) = remember(period, language, numerals) {
        when (period) {
            ReportPeriod.DAILY -> Triple(DateUtils.startOfDay(now), DateUtils.endOfDay(now), strings.dailyReport)
            ReportPeriod.WEEKLY -> Triple(DateUtils.startOfWeek(now), DateUtils.endOfDay(now), strings.weeklyReport)
            ReportPeriod.MONTHLY -> {
                val ym = DateUtils.currentYearMonth(now)
                val (s, e) = DateUtils.monthRange(ym)
                Triple(s, e, DateUtils.formatMonthYear(ym, language, numerals))
            }
            ReportPeriod.YEARLY -> {
                val yr = DateUtils.yearOf(now)
                val (s, e) = DateUtils.yearRange(yr)
                Triple(s, e, CurrencyFormatter.toLocalizedDigits(yr.toString(), numerals))
            }
        }
    }

    val periodTxs = remember(uiState.transactions, rangeStart, rangeEnd) {
        uiState.transactions.filter { it.transactionDate in rangeStart..rangeEnd }
    }
    val (periodIncome, periodExpense) = remember(periodTxs, rangeStart, rangeEnd) {
        AccountingEngine.computePeriodIncomeExpense(
            periodTxs.map { it.toLedgerRecord() },
            rangeStart,
            rangeEnd
        )
    }

    val categoriesById = remember(uiState.categories) { uiState.categories.associateBy { it.id } }
    val shopsById = remember(uiState.shops) { uiState.shops.associateBy { it.shop.id }.mapValues { it.value.shop } }

    val categoryExpenses = remember(periodTxs, categoriesById, language) {
        periodTxs.filter {
            it.type == LedgerTransactionType.EXPENSE.name || it.type == LedgerTransactionType.SHOP_PURCHASE.name
        }.groupBy { it.categoryId }
            .map { (catId, list) ->
                val cat = catId?.let { categoriesById[it] }
                val label = when {
                    cat != null && isBn -> cat.nameBn
                    cat != null -> cat.nameEn
                    else -> strings.shopDueLabel
                }
                label to list.sumOf { it.totalAmountPoisha }
            }
            .sortedByDescending { it.second }
    }

    val incomeCategories = remember(periodTxs, categoriesById, language) {
        periodTxs.filter { it.type == LedgerTransactionType.INCOME.name }
            .groupBy { it.categoryId }
            .map { (catId, list) ->
                val cat = catId?.let { categoriesById[it] }
                val label = when {
                    cat != null && isBn -> cat.nameBn
                    cat != null -> cat.nameEn
                    else -> strings.incomeLabel
                }
                label to list.sumOf { it.totalAmountPoisha }
            }
            .sortedByDescending { it.second }
    }

    fun buildExportPayload() = ReportExportPayload(
        bookName = localizedBookName(uiState.activeNotebook?.name, uiState.activeNotebook?.type, language),
        periodLabel = periodLabel,
        summary = uiState.financialSummary,
        periodIncomePoisha = periodIncome,
        periodExpensePoisha = periodExpense,
        transactions = periodTxs,
        categoriesById = categoriesById,
        shopsById = shopsById,
        shopBalances = uiState.shops.map { it.shop to it.balance },
        language = language,
        numeralStyle = numerals
    )

    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        ReportExporter.writePdf(context, buildExportPayload(), out).getOrThrow()
                    } ?: error("Cannot open output stream")
                }
                onMessage(if (res.isSuccess) "${strings.exportPdf} — ${strings.savedSuccessfully}" else res.exceptionOrNull()?.message ?: "Export failed")
            }
        }
    }

    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val res = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        ReportExporter.writeCsv(buildExportPayload(), out).getOrThrow()
                    } ?: error("Cannot open output stream")
                }
                onMessage(if (res.isSuccess) "${strings.exportCsv} — ${strings.savedSuccessfully}" else res.exceptionOrNull()?.message ?: "Export failed")
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PremiumHeader(
            onBackClick = onBack,
            notificationCount = uiState.notifications.size,
            hasUrgentNotification = uiState.notifications.any { it.isUrgent },
            onNotificationClick = onOpenNotifications,
            onSettingsClick = onOpenSettings
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Page Title
            item(key = "reports_title") {
                PremiumPageTitleRow(
                    icon = Icons.Filled.BarChart,
                    title = strings.reports,
                    subtitle = "${localizedBookName(uiState.activeNotebook?.name, uiState.activeNotebook?.type, language)} • $periodLabel",
                    trailing = if (onOpenBudgets != null) {
                        {
                            Surface(
                                onClick = onOpenBudgets,
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.PieChart,
                                        contentDescription = strings.budgets,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = strings.budgets,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    } else null
                )
            }

            // 4 Period Selector Tiles (Daily / Weekly / Monthly / Yearly)
            item(key = "period_selector") {
                val periodItems = listOf(
                    ReportPeriod.DAILY to strings.dailyReport,
                    ReportPeriod.WEEKLY to strings.weeklyReport,
                    ReportPeriod.MONTHLY to strings.monthlyReport,
                    ReportPeriod.YEARLY to strings.yearlyReport
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    periodItems.forEach { (p, label) ->
                        val selected = period == p
                        val selectedBg = if (finance.isDark) MaterialTheme.colorScheme.primary else DeepEmerald
                        val selectedIconTint = if (finance.isDark) MaterialTheme.colorScheme.onPrimary else SoftGold
                        val selectedTextTint = if (finance.isDark) MaterialTheme.colorScheme.onPrimary else Color.White
                        Surface(
                            onClick = { period = p },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) selectedBg else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (selected) {
                                    if (finance.isDark) MaterialTheme.colorScheme.primary else WarmGold.copy(alpha = 0.7f)
                                } else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = null,
                                    tint = if (selected) selectedIconTint else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) selectedTextTint else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Export PDF & UTF-8 CSV Action Row (SAF Offline)
            item(key = "export_actions") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { pdfExportLauncher.launch("jomakhoroch_report_${System.currentTimeMillis()}.pdf") },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_pdf_button")
                    ) {
                        Icon(
                            Icons.Filled.PictureAsPdf,
                            contentDescription = null,
                            tint = if (finance.isDark) MaterialTheme.colorScheme.onPrimary else SoftGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(strings.exportPdf, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { csvExportLauncher.launch("jomakhoroch_report_${System.currentTimeMillis()}.csv") },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_csv_button")
                    ) {
                        Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.exportCsv, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Income vs Expense Summary Cards + Net Cash Balance
            item(key = "period_summary_cards") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IncomeExpenseCard(
                            title = strings.incomeLabel,
                            amountPoisha = periodIncome,
                            isIncome = true,
                            modifier = Modifier.weight(1f)
                        )
                        IncomeExpenseCard(
                            title = strings.expenseLabel,
                            amountPoisha = periodExpense,
                            isIncome = false,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val periodNet = periodIncome - periodExpense
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isBn) "নিট ক্যাশফ্লো ($periodLabel)" else "Net Cashflow ($periodLabel)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${if (periodNet >= 0L) "+" else ""}${CurrencyFormatter.formatPoisha(periodNet, numerals)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = PlusJakartaSansFont,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (periodNet >= 0L) finance.income else finance.expense
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = strings.cashBalance,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.formatPoisha(uiState.financialSummary.currentCashBalancePoisha, numerals),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = PlusJakartaSansFont,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 5-Month Cashflow Trend Bar Chart
            item(key = "reports_cashflow_chart") {
                ReportsMonthlyCashflowCard(transactions = uiState.transactions)
            }

            // Category Breakdown Donut + Detailed Progress Bars
            item(key = "reports_category_donut") {
                val reportSlices = remember(categoryExpenses, uiState.categories, isBn) {
                    categoryExpenses.map { (label, amount) ->
                        val matched = uiState.categories.firstOrNull {
                            (if (isBn) it.nameBn else it.nameEn) == label
                        }
                        CategorySliceInfo(
                            label = label,
                            amountPoisha = amount,
                            iconKey = matched?.iconKey ?: "category"
                        )
                    }
                }
                CategorySpendingDonutCard(
                    title = strings.categoryBreakdown,
                    slices = reportSlices,
                    palette = finance.chartPalette
                )
            }

            if (categoryExpenses.isNotEmpty()) {
                item(key = "reports_category_bars") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val totalCatExp = categoryExpenses.sumOf { it.second }.coerceAtLeast(1L)
                            categoryExpenses.forEachIndexed { idx, (label, amount) ->
                                val pct = (amount.toFloat() / totalCatExp.toFloat()).coerceIn(0f, 1f)
                                val barColor = finance.chartPalette[idx % finance.chartPalette.size]
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        Text(
                                            text = CurrencyFormatter.formatPoisha(amount, numerals),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = PlusJakartaSansFont,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { pct },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(50)),
                                        color = barColor,
                                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Income Breakdown
            if (incomeCategories.isNotEmpty()) {
                item(key = "reports_income_breakdown") {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        PremiumSectionBanner(
                            icon = Icons.Filled.TrendingUp,
                            title = strings.totalIncome
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                incomeCategories.forEach { (label, amount) ->
                                    ReportAmountRow(label, amount, finance.income, numerals)
                                }
                            }
                        }
                    }
                }
            }

            // Shop Due & Personal Receivable/Payable Detailed Report
            item(key = "reports_receivable_payable") {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    PremiumSectionBanner(
                        icon = Icons.Filled.AccountBalanceWallet,
                        title = strings.receivablePayableReport
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReportAmountRow(strings.shopDueReport, uiState.financialSummary.totalShopPayablePoisha, finance.payable, numerals)
                            uiState.shops.filter { it.balance.outstandingDuePoisha > 0L }.forEach { s ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("• ${s.shop.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        CurrencyFormatter.formatPoisha(s.balance.outstandingDuePoisha, numerals),
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = PlusJakartaSansFont)
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ReportAmountRow(strings.totalReceivable, uiState.financialSummary.totalPersonalReceivablePoisha, finance.receivable, numerals)
                            uiState.lendingLoans.filter { it.balance.remainingPoisha > 0L }.forEach { l ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("• ${l.loan.personName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        CurrencyFormatter.formatPoisha(l.balance.remainingPoisha, numerals),
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = PlusJakartaSansFont)
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ReportAmountRow(strings.moneyBorrowed, uiState.financialSummary.totalPersonalPayablePoisha, finance.expense, numerals)
                            uiState.borrowingLoans.filter { it.balance.remainingPoisha > 0L }.forEach { l ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("• ${l.loan.personName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        CurrencyFormatter.formatPoisha(l.balance.remainingPoisha, numerals),
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = PlusJakartaSansFont)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Book-Wise Summary Report
            item(key = "reports_book_summary") {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    PremiumSectionBanner(
                        icon = Icons.Filled.MenuBook,
                        title = strings.bookSummaryReport
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            uiState.allBookSummaries.forEachIndexed { idx, row ->
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = localizedBookName(row.notebook.name, row.notebook.type, language),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            CurrencyFormatter.formatPoisha(row.cashBalancePoisha, numerals),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = PlusJakartaSansFont,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = "${strings.incomeLabel}: ${CurrencyFormatter.formatPoisha(row.incomePoisha, numerals)}  •  ${strings.expenseLabel}: ${CurrencyFormatter.formatPoisha(row.expensePoisha, numerals)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (idx < uiState.allBookSummaries.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
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
private fun ReportsMonthlyCashflowCard(
    transactions: List<LedgerTransactionEntity>
) {
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA

    val currentYm = remember { DateUtils.currentYearMonth() }
    val buckets = remember(transactions, currentYm, language, numerals) {
        (-4..0).map { offset ->
            val ym = DateUtils.shiftYearMonth(currentYm, offset)
            val (start, end) = DateUtils.monthRange(ym)
            val (inc, exp) = AccountingEngine.computePeriodIncomeExpense(
                transactions.map { it.toLedgerRecord() },
                start,
                end
            )
            val shortLabel = DateUtils.formatMonthYear(ym, language, numerals).substringBefore(" ").take(5)
            Triple(shortLabel, inc, exp)
        }
    }
    val hasData = remember(buckets) { buckets.any { it.second > 0L || it.third > 0L } }
    val maxVal = remember(buckets) {
        buckets.maxOfOrNull { max(it.second, it.third) }?.coerceAtLeast(1L) ?: 1L
    }
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "৫ মাসের ক্যাশফ্লো তুলনা" else "5-Month Cashflow",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = finance.chartIncome, label = if (isBn) "আয়" else "Income")
                    LegendDot(color = finance.chartExpense, label = if (isBn) "ব্যয়" else "Expense")
                }
            }

            if (!hasData) {
                Text(
                    text = if (isBn) "চার্ট দেখার জন্য আয় বা ব্যয় যোগ করুন।" else "Add income or expenses to view monthly cashflow.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val chartH = size.height - 6.dp.toPx()
                    for (i in 0..3) {
                        val y = chartH * (i / 3f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    val slotW = size.width / buckets.size
                    val barW = (slotW * 0.28f).coerceAtMost(18.dp.toPx())
                    val gap = 4.dp.toPx()
                    val radius = CornerRadius(4.dp.toPx())

                    buckets.forEachIndexed { idx, (_, inc, exp) ->
                        val centerX = idx * slotW + slotW / 2f
                        val incH = ((inc.toFloat() / maxVal.toFloat()) * (chartH - 6.dp.toPx())).coerceAtLeast(if (inc > 0) 4.dp.toPx() else 0f)
                        val expH = ((exp.toFloat() / maxVal.toFloat()) * (chartH - 6.dp.toPx())).coerceAtLeast(if (exp > 0) 4.dp.toPx() else 0f)

                        if (incH > 0f) {
                            drawRoundRect(
                                color = finance.chartIncome,
                                topLeft = Offset(centerX - barW - gap / 2f, chartH - incH),
                                size = Size(barW, incH),
                                cornerRadius = radius
                            )
                        }
                        if (expH > 0f) {
                            drawRoundRect(
                                color = finance.chartExpense,
                                topLeft = Offset(centerX + gap / 2f, chartH - expH),
                                size = Size(barW, expH),
                                cornerRadius = radius
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    buckets.forEach { (label, _, _) ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
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

@Composable
private fun ReportAmountRow(
    label: String,
    poisha: Long,
    color: Color,
    numerals: com.example.core.localization.NumeralStyle
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = CurrencyFormatter.formatPoisha(poisha, numerals),
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = PlusJakartaSansFont,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
    }
}
