package com.example.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.LocalLanguage
import com.example.core.localization.LocalNumeralStyle
import com.example.core.util.CurrencyFormatter
import com.example.core.util.DateUtils
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.LedgerTransactionEntity
import com.example.data.local.entity.ShopEntity
import com.example.domain.accounting.LedgerTransactionType
import com.example.presentation.components.EmptyState
import com.example.presentation.components.PremiumHeader
import com.example.presentation.components.PremiumPageTitleRow
import com.example.presentation.components.PremiumSearchBar
import com.example.presentation.components.iconForKey
import com.example.presentation.components.paymentMethodLabel
import com.example.presentation.state.JomaKhorochUiState
import com.example.ui.theme.LocalFinanceColors
import com.example.ui.theme.PlusJakartaSansFont
import com.example.ui.theme.WarmGold
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class LedgerFilter { ALL, INCOME, EXPENSE, SHOP, LOAN }

@Composable
fun TransactionsScreen(
    uiState: JomaKhorochUiState,
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (LedgerTransactionEntity) -> Unit,
    onOpenNotifications: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA
    val isDark = finance.isDark

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(LedgerFilter.ALL) }
    var selectedScopeKey by rememberSaveable { mutableStateOf("ALL") } // ALL, TODAY, WEEK, or YYYY-MM
    var selectedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedPaymentMethod by rememberSaveable { mutableStateOf<String?>(null) }
    var showAuditDialog by remember { mutableStateOf(false) }
    var showScopeDropdown by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    val hasExtraFilters = selectedPaymentMethod != null || filter == LedgerFilter.SHOP || filter == LedgerFilter.LOAN
    val hasAnyFilter = searchQuery.isNotBlank() ||
        filter != LedgerFilter.ALL ||
        selectedScopeKey != "ALL" ||
        selectedCategoryId != null ||
        selectedPaymentMethod != null

    BackHandler(enabled = hasAnyFilter) {
        searchQuery = ""
        filter = LedgerFilter.ALL
        selectedScopeKey = "ALL"
        selectedCategoryId = null
        selectedPaymentMethod = null
    }

    val categoriesById = remember(uiState.categories) { uiState.categories.associateBy { it.id } }
    val shopsById = remember(uiState.shops) { uiState.shops.associateBy { it.shop.id }.mapValues { it.value.shop } }

    val nowMillis = remember { System.currentTimeMillis() }
    val yesterdayMillis = remember(nowMillis) { nowMillis - 86_400_000L }
    val currentYm = remember(nowMillis) { DateUtils.currentYearMonth(nowMillis) }

    // Available month/date range scopes
    val scopeOptions = remember(nowMillis, language, numerals) {
        val recentMonths = (0 downTo -5).map { offset ->
            val ym = DateUtils.shiftYearMonth(currentYm, offset)
            ym to DateUtils.formatMonthYear(ym, language, numerals)
        }
        listOf(
            "ALL" to (if (isBn) "সব সময়" else "All Time"),
            "TODAY" to (if (isBn) "আজ" else "Today"),
            "WEEK" to (if (isBn) "গত ৭ দিন" else "Last 7 Days")
        ) + recentMonths
    }
    val activeScopeLabel = scopeOptions.firstOrNull { it.first == selectedScopeKey }?.second
        ?: (if (isBn) "সব সময়" else "All Time")

    val filtered = remember(
        uiState.transactions,
        searchQuery,
        filter,
        selectedScopeKey,
        selectedCategoryId,
        selectedPaymentMethod,
        categoriesById,
        shopsById
    ) {
        val (scopeStart, scopeEnd) = when (selectedScopeKey) {
            "ALL" -> Long.MIN_VALUE to Long.MAX_VALUE
            "TODAY" -> DateUtils.startOfDay(nowMillis) to DateUtils.endOfDay(nowMillis)
            "WEEK" -> DateUtils.startOfWeek(nowMillis) to DateUtils.endOfDay(nowMillis)
            else -> DateUtils.monthRange(selectedScopeKey)
        }
        uiState.transactions.filter { tx ->
            if (tx.transactionDate !in scopeStart..scopeEnd) return@filter false
            if (selectedCategoryId != null && tx.categoryId != selectedCategoryId) return@filter false
            if (selectedPaymentMethod != null && !tx.paymentMethod.equals(selectedPaymentMethod, ignoreCase = true)) {
                return@filter false
            }
            val type = LedgerTransactionType.valueOf(tx.type)
            val matchesFilter = when (filter) {
                LedgerFilter.ALL -> true
                LedgerFilter.INCOME -> type == LedgerTransactionType.INCOME
                LedgerFilter.EXPENSE -> type == LedgerTransactionType.EXPENSE
                LedgerFilter.SHOP -> type == LedgerTransactionType.SHOP_PURCHASE || type == LedgerTransactionType.SHOP_DUE_PAYMENT
                LedgerFilter.LOAN -> type == LedgerTransactionType.LENDING_GIVEN ||
                    type == LedgerTransactionType.LENDING_REPAYMENT ||
                    type == LedgerTransactionType.BORROWING_TAKEN ||
                    type == LedgerTransactionType.BORROWING_REPAYMENT
            }
            if (!matchesFilter) return@filter false
            if (searchQuery.isBlank()) return@filter true
            val q = searchQuery.trim().lowercase()
            val cat = tx.categoryId?.let { categoriesById[it] }
            val shop = tx.shopId?.let { shopsById[it] }
            tx.note.lowercase().contains(q) ||
                (cat?.nameBn?.lowercase()?.contains(q) == true) ||
                (cat?.nameEn?.lowercase()?.contains(q) == true) ||
                (shop?.name?.lowercase()?.contains(q) == true)
        }
    }

    val (filteredInflow, filteredOutflow, inflowCount, outflowCount) = remember(filtered) {
        var inflow = 0L
        var outflow = 0L
        var inCnt = 0
        var outCnt = 0
        for (tx in filtered) {
            when (LedgerTransactionType.valueOf(tx.type)) {
                LedgerTransactionType.INCOME,
                LedgerTransactionType.LENDING_REPAYMENT,
                LedgerTransactionType.BORROWING_TAKEN -> {
                    inflow += tx.paidAmountPoisha
                    inCnt++
                }
                LedgerTransactionType.EXPENSE,
                LedgerTransactionType.SHOP_PURCHASE,
                LedgerTransactionType.SHOP_DUE_PAYMENT,
                LedgerTransactionType.LENDING_GIVEN,
                LedgerTransactionType.BORROWING_REPAYMENT -> {
                    outflow += tx.paidAmountPoisha
                    outCnt++
                }
            }
        }
        QuadMetric(inflow, outflow, inCnt, outCnt)
    }

    // Real month-over-month comparison delta for the comparison strip inside Income/Expense cards
    val (incomeDeltaNote, expenseDeltaNote) = remember(
        uiState.transactions,
        selectedScopeKey,
        inflowCount,
        outflowCount,
        language,
        numerals
    ) {
        val targetYm = if (selectedScopeKey.contains("-")) selectedScopeKey else currentYm
        val prevYm = DateUtils.shiftYearMonth(targetYm, -1)
        val (curStart, curEnd) = DateUtils.monthRange(targetYm)
        val (prevStart, prevEnd) = DateUtils.monthRange(prevYm)

        var curIn = 0L
        var curOut = 0L
        var prevIn = 0L
        var prevOut = 0L
        for (tx in uiState.transactions) {
            val isIn = when (LedgerTransactionType.valueOf(tx.type)) {
                LedgerTransactionType.INCOME,
                LedgerTransactionType.LENDING_REPAYMENT,
                LedgerTransactionType.BORROWING_TAKEN -> true
                else -> false
            }
            if (tx.transactionDate in curStart..curEnd) {
                if (isIn) curIn += tx.paidAmountPoisha else curOut += tx.paidAmountPoisha
            } else if (tx.transactionDate in prevStart..prevEnd) {
                if (isIn) prevIn += tx.paidAmountPoisha else prevOut += tx.paidAmountPoisha
            }
        }

        fun formatDelta(cur: Long, prev: Long, fallbackCount: Int, isIncomeCard: Boolean): String {
            if (prev > 0L) {
                val pct = (((cur - prev).toDouble() / prev.toDouble()) * 100.0).roundToInt()
                val arrow = if (pct >= 0) "↑" else "↓"
                val sign = if (pct >= 0) "+" else "-"
                val digits = CurrencyFormatter.toLocalizedDigits("${abs(pct)}%", numerals)
                return if (isBn) "গত মাসের তুলনায় $arrow $sign$digits" else "vs last month $arrow $sign$digits"
            }
            val countStr = CurrencyFormatter.toLocalizedDigits(fallbackCount.toString(), numerals)
            return if (isBn) {
                if (isIncomeCard) "মোট ${countStr}টি আয়" else "মোট ${countStr}টি ব্যয়"
            } else {
                if (isIncomeCard) "$countStr income entries" else "$countStr expense entries"
            }
        }

        formatDelta(curIn, prevIn, inflowCount, true) to formatDelta(curOut, prevOut, outflowCount, false)
    }

    val groupedByDate = remember(filtered, language, numerals) {
        filtered.groupBy { tx ->
            val dateWithDay = DateUtils.formatDateWithWeekday(tx.transactionDate, language, numerals)
            when {
                DateUtils.isSameDay(tx.transactionDate, nowMillis) ->
                    "${if (isBn) "আজ" else "Today"}, $dateWithDay"
                DateUtils.isSameDay(tx.transactionDate, yesterdayMillis) ->
                    "${if (isBn) "গতকাল" else "Yesterday"}, $dateWithDay"
                else -> dateWithDay
            }
        }
    }

    val activeCatName = selectedCategoryId?.let { id ->
        categoriesById[id]?.let { if (isBn) it.nameBn else it.nameEn }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Curved Emerald Header
            item(key = "tx_header") {
                PremiumHeader(
                    notificationCount = uiState.notifications.size,
                    hasUrgentNotification = uiState.notifications.any { it.isUrgent },
                    onNotificationClick = onOpenNotifications,
                    onSettingsClick = onOpenSettings
                )
            }

            // 2. Screen Title + Date Range / Month Selector Pill
            item(key = "tx_title_row") {
                PremiumPageTitleRow(
                    icon = Icons.Filled.Description,
                    title = strings.navTransactions,
                    subtitle = if (isBn) "আপনার সকল আয়-ব্যয়ের হিসাব" else "All income & expense records",
                    modifier = Modifier.padding(horizontal = 16.dp),
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
                                    .clickable { showScopeDropdown = true }
                                    .testTag("ledger_period_selector")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Text(
                                        text = activeScopeLabel,
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
                                expanded = showScopeDropdown,
                                onDismissRequest = { showScopeDropdown = false }
                            ) {
                                scopeOptions.forEach { (key, label) ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = label,
                                                fontWeight = if (selectedScopeKey == key) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selectedScopeKey == key) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            selectedScopeKey = key
                                            showScopeDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // 3. 2-Column Income & Expense Summary Cards (Matching mockup with circular arrow + bottom comparison strip)
            item(key = "tx_summary_cards") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LedgerSummaryMetricCard(
                        title = strings.totalIncome,
                        amountPoisha = filteredInflow,
                        deltaText = incomeDeltaNote,
                        isIncome = true,
                        onClick = {
                            filter = if (filter == LedgerFilter.INCOME) LedgerFilter.ALL else LedgerFilter.INCOME
                        },
                        modifier = Modifier.weight(1f)
                    )
                    LedgerSummaryMetricCard(
                        title = strings.totalExpense,
                        amountPoisha = filteredOutflow,
                        deltaText = expenseDeltaNote,
                        isIncome = false,
                        onClick = {
                            filter = if (filter == LedgerFilter.EXPENSE) LedgerFilter.ALL else LedgerFilter.EXPENSE
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Search Bar + "ফিল্টার ⌄" Dropdown Pill (Payment Method, Shop/Loan, Audit History)
            item(key = "tx_search_row") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    PremiumSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = strings.searchTransactions,
                        inputTag = "ledger_search_input",
                        modifier = Modifier.weight(1f)
                    )
                    Box {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = if (hasExtraFilters) MaterialTheme.colorScheme.primaryContainer
                            else if (isDark) MaterialTheme.colorScheme.surfaceContainer else Color(0xFFF7F3E7),
                            border = BorderStroke(
                                1.dp,
                                if (hasExtraFilters) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                                else if (isDark) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE6DFCB)
                            ),
                            shadowElevation = 0.8.dp,
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { showFilterMenu = true }
                                .testTag("ledger_filter_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = if (isBn) "ফিল্টার" else "Filter",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isBn) "ফিল্টার" else "Filter",
                                    style = MaterialTheme.typography.labelLarge,
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
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            // Payment method filters
                            val methods = listOf(
                                null to (if (isBn) "সব পেমেন্ট মাধ্যম" else "All Payment Methods"),
                                "CASH" to strings.cashMethod,
                                "MOBILE_BANKING" to strings.mobileBankingMethod,
                                "BANK" to strings.bankMethod
                            )
                            methods.forEach { (methodKey, methodLabel) ->
                                val isSel = selectedPaymentMethod == methodKey
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = methodLabel,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    trailingIcon = if (isSel) {
                                        {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        selectedPaymentMethod = methodKey
                                        showFilterMenu = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = strings.shopDueLabel,
                                        fontWeight = if (filter == LedgerFilter.SHOP) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    filter = if (filter == LedgerFilter.SHOP) LedgerFilter.ALL else LedgerFilter.SHOP
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = strings.loanLabel,
                                        fontWeight = if (filter == LedgerFilter.LOAN) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Handshake, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    filter = if (filter == LedgerFilter.LOAN) LedgerFilter.ALL else LedgerFilter.LOAN
                                    showFilterMenu = false
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(strings.auditHistory, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.testTag("ledger_audit_button"),
                                onClick = {
                                    showFilterMenu = false
                                    showAuditDialog = true
                                }
                            )
                            if (hasAnyFilter) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (isBn) "সব ফিল্টার মুছুন" else "Reset Filters",
                                            color = finance.expense,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.Refresh,
                                            contentDescription = null,
                                            tint = finance.expense,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        searchQuery = ""
                                        filter = LedgerFilter.ALL
                                        selectedScopeKey = "ALL"
                                        selectedCategoryId = null
                                        selectedPaymentMethod = null
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 5. 4-Pill Filter Row (সব লেনদেন | আয় | ব্যয় | ক্যাটাগরি)
            item(key = "tx_filter_pills") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "pill_all") {
                        LedgerTypePill(
                            label = if (isBn) "সব লেনদেন" else strings.allFilter,
                            icon = Icons.Filled.SwapHoriz,
                            selected = filter == LedgerFilter.ALL && selectedCategoryId == null,
                            selectedBg = Color(0xFF0A5A3C),
                            unselectedBg = if (isDark) Color(0xFF142D24) else Color(0xFFE5F4E8),
                            unselectedBorder = if (isDark) Color(0xFF254D3E) else Color(0xFFC5E1CB),
                            unselectedIconTint = if (isDark) Color(0xFF5AD59C) else Color(0xFF0E6843),
                            onClick = {
                                filter = LedgerFilter.ALL
                                selectedCategoryId = null
                            }
                        )
                    }
                    item(key = "pill_income") {
                        LedgerTypePill(
                            label = strings.incomeLabel,
                            icon = Icons.Filled.ArrowUpward,
                            selected = filter == LedgerFilter.INCOME,
                            selectedBg = Color(0xFF0E7A42),
                            unselectedBg = if (isDark) Color(0xFF132E23) else Color(0xFFE4F5E6),
                            unselectedBorder = if (isDark) Color(0xFF234E3C) else Color(0xFFC3E4C8),
                            unselectedIconTint = if (isDark) Color(0xFF52D18F) else Color(0xFF0E7A42),
                            onClick = { filter = LedgerFilter.INCOME }
                        )
                    }
                    item(key = "pill_expense") {
                        LedgerTypePill(
                            label = strings.expenseLabel,
                            icon = Icons.Filled.ArrowDownward,
                            selected = filter == LedgerFilter.EXPENSE,
                            selectedBg = Color(0xFFC62828),
                            unselectedBg = if (isDark) Color(0xFF331B1B) else Color(0xFFFDE7E5),
                            unselectedBorder = if (isDark) Color(0xFF5B2F2F) else Color(0xFFF3C6C2),
                            unselectedIconTint = if (isDark) Color(0xFFFF7B76) else Color(0xFFC62828),
                            onClick = { filter = LedgerFilter.EXPENSE }
                        )
                    }
                    item(key = "pill_category") {
                        LedgerTypePill(
                            label = activeCatName ?: strings.category,
                            icon = Icons.Filled.BarChart,
                            selected = selectedCategoryId != null,
                            selectedBg = Color(0xFF0A5A3C),
                            unselectedBg = if (isDark) Color(0xFF2A2318) else Color(0xFFF8F0DC),
                            unselectedBorder = if (isDark) Color(0xFF493D29) else Color(0xFFE7D8B6),
                            unselectedIconTint = if (isDark) Color(0xFFF0B866) else Color(0xFF9C621B),
                            onClick = { showCategoryDialog = true }
                        )
                    }
                }
            }

            // 6. Date-Grouped Transactions or Empty State
            if (filtered.isEmpty()) {
                item(key = "tx_empty") {
                    EmptyState(
                        icon = Icons.Filled.ReceiptLong,
                        title = if (hasAnyFilter) {
                            if (isBn) "এই ফিল্টারে কোনো লেনদেন পাওয়া যায়নি" else "No matching transactions"
                        } else strings.noTransactionsYet,
                        subtitle = if (hasAnyFilter) "" else strings.noTransactionsHint,
                        actionLabel = if (hasAnyFilter) {
                            if (isBn) "ফিল্টার মুছুন" else "Reset Filters"
                        } else strings.addExpense,
                        onAction = if (hasAnyFilter) {
                            {
                                searchQuery = ""
                                filter = LedgerFilter.ALL
                                selectedScopeKey = "ALL"
                                selectedCategoryId = null
                                selectedPaymentMethod = null
                            }
                        } else onAddTransactionClick,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                groupedByDate.forEach { (dateLabel, dayTxs) ->
                    val dayVolume = dayTxs.sumOf { it.totalAmountPoisha }

                    item(key = "day_group_$dateLabel") {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Soft Sage Date Header Strip with Weekday & Daily Total
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color(0xFF183127) else Color(0xFFE6EFE4),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CalendarMonth,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = dateLabel,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "${if (isBn) "মোট: " else "Total: "}${
                                            CurrencyFormatter.formatPoisha(dayVolume, numerals)
                                        }",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = PlusJakartaSansFont,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = finance.income
                                    )
                                }
                            }

                            // Crisp White Day Transactions Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.8.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    dayTxs.forEachIndexed { idx, tx ->
                                        MockupTransactionRow(
                                            tx = tx,
                                            category = tx.categoryId?.let { categoriesById[it] },
                                            shop = tx.shopId?.let { shopsById[it] },
                                            onClick = { onTransactionClick(tx) },
                                            onOpenAudit = { showAuditDialog = true }
                                        )
                                        if (idx < dayTxs.lastIndex) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(start = 72.dp, end = 14.dp),
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
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

        // Signature Circular Deep Emerald "+ নতুন লেনদেন" FAB (Matches mockup bottom-right!)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 14.dp)
                .size(72.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0E764E), Color(0xFF07442E))
                    )
                )
                .clickable(onClick = onAddTransactionClick)
                .testTag("ledger_add_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = if (isBn) "নতুন লেনদেন" else "New Entry",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = if (isBn) "নতুন\nলেনদেন" else "New\nEntry",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        lineHeight = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }

    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text(strings.category, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedCategoryId == null) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedCategoryId = null
                                    showCategoryDialog = false
                                }
                        ) {
                            Text(
                                text = strings.allFilter,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                            )
                        }
                    }
                    items(uiState.categories, key = { it.id }) { cat ->
                        val isSel = selectedCategoryId == cat.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedCategoryId = cat.id
                                    showCategoryDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBn) cat.nameBn else cat.nameEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = if (cat.type == "INCOME") strings.incomeLabel else strings.expenseLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (cat.type == "INCOME") finance.income else finance.expense
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) { Text(strings.close) }
            }
        )
    }

    if (showAuditDialog) {
        AuditLogDialog(
            logs = uiState.auditLogs,
            onDismiss = { showAuditDialog = false }
        )
    }
}

private data class QuadMetric(
    val inflow: Long,
    val outflow: Long,
    val inCount: Int,
    val outCount: Int
)

@Composable
private fun LedgerSummaryMetricCard(
    title: String,
    amountPoisha: Long,
    deltaText: String,
    isIncome: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isDark = finance.isDark

    val containerBg = if (isIncome) {
        if (isDark) Color(0xFF122C21) else Color(0xFFE5F5E7)
    } else {
        if (isDark) Color(0xFF31191A) else Color(0xFFFDE9E8)
    }
    val borderColor = if (isIncome) {
        if (isDark) Color(0xFF234D3C) else Color(0xFFBFE2C6)
    } else {
        if (isDark) Color(0xFF5A2C2C) else Color(0xFFF4C5C2)
    }
    val badgeBg = if (isIncome) Color(0xFF0E964A) else Color(0xFFC62828)
    val amountColor = if (isIncome) {
        if (isDark) Color(0xFF63E09C) else Color(0xFF0B5D30)
    } else {
        if (isDark) Color(0xFFFF8680) else Color(0xFFB71C1C)
    }
    val stripBg = if (isIncome) {
        if (isDark) Color(0xFF183A2B) else Color(0xFFD7EEDA)
    } else {
        if (isDark) Color(0xFF422224) else Color(0xFFFAD8D6)
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
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
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = CurrencyFormatter.formatPoisha(amountPoisha, numerals),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PlusJakartaSansFont,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = amountColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = stripBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = deltaText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.8.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LedgerTypePill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    selectedBg: Color,
    unselectedBg: Color,
    unselectedBorder: Color,
    unselectedIconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (selected) selectedBg else unselectedBg,
        animationSpec = tween(180),
        label = "tx_pill_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(180),
        label = "tx_pill_text"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) Color.White else unselectedIconTint,
        animationSpec = tween(180),
        label = "tx_pill_icon"
    )

    Surface(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) WarmGold.copy(alpha = 0.5f) else unselectedBorder
        ),
        shadowElevation = if (selected) 2.dp else 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MockupTransactionRow(
    tx: LedgerTransactionEntity,
    category: CategoryEntity?,
    shop: ShopEntity?,
    onClick: () -> Unit,
    onOpenAudit: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA
    val txType = LedgerTransactionType.valueOf(tx.type)

    var showMenu by remember { mutableStateOf(false) }

    val (circleBg, iconTint, icon, signPrefix, amountColor, titleText) = resolveTransactionVisuals(
        txType = txType,
        category = category,
        shop = shop,
        isBn = isBn,
        isDark = finance.isDark,
        incomeColor = finance.income,
        expenseColor = finance.expense,
        strings = strings
    )

    val timeStr = DateUtils.formatTimeOfDay(tx.transactionDate, language, numerals)
    val methodStr = paymentMethodLabel(tx.paymentMethod, strings)
    val subtitleText = buildString {
        append(timeStr)
        append("  •  ")
        append(methodStr)
        if (tx.note.isNotBlank() && !tx.note.equals(titleText, ignoreCase = true)) {
            append("  •  ")
            append(tx.note)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 4.dp, top = 11.dp, bottom = 11.dp)
            .testTag("tx_card_${tx.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(circleBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = titleText,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (txType == LedgerTransactionType.SHOP_PURCHASE && tx.dueAmountPoisha > 0L) {
                Spacer(Modifier.height(3.dp))
                Surface(
                    color = finance.payableContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${strings.paidAmount}: ${CurrencyFormatter.formatPoisha(tx.paidAmountPoisha, numerals)} • ${strings.remainingDue}: ${CurrencyFormatter.formatPoisha(tx.dueAmountPoisha, numerals)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = finance.payable,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = "$signPrefix${CurrencyFormatter.formatPoisha(tx.totalAmountPoisha, numerals)}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = PlusJakartaSansFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.5.sp
            ),
            color = amountColor,
            maxLines = 1
        )

        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = if (isBn) "অপশন" else "Options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (isBn) "বিস্তারিত ও সম্পাদনা" else "Details & Edit") },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    onClick = {
                        showMenu = false
                        onClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text(strings.auditHistory) },
                    leadingIcon = { Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    onClick = {
                        showMenu = false
                        onOpenAudit()
                    }
                )
            }
        }
    }
}

private data class TxRowVisual(
    val circleBg: Color,
    val iconTint: Color,
    val icon: ImageVector,
    val signPrefix: String,
    val amountColor: Color,
    val title: String
)

private fun resolveTransactionVisuals(
    txType: LedgerTransactionType,
    category: CategoryEntity?,
    shop: ShopEntity?,
    isBn: Boolean,
    isDark: Boolean,
    incomeColor: Color,
    expenseColor: Color,
    strings: com.example.core.localization.AppStrings
): TxRowVisual {
    return when (txType) {
        LedgerTransactionType.INCOME -> {
            val title = if (isBn) category?.nameBn ?: strings.incomeLabel else category?.nameEn ?: strings.incomeLabel
            val icon = iconForKey(category?.iconKey ?: "payments")
            TxRowVisual(
                circleBg = if (isDark) Color(0xFF153B2E) else Color(0xFFBDF2DF),
                iconTint = if (isDark) Color(0xFF5EE0A8) else Color(0xFF0A6B44),
                icon = icon,
                signPrefix = "+",
                amountColor = incomeColor,
                title = title
            )
        }
        LedgerTransactionType.EXPENSE -> {
            val title = if (isBn) category?.nameBn ?: strings.expenseLabel else category?.nameEn ?: strings.expenseLabel
            val iconKey = category?.iconKey ?: "receipt"
            val icon = iconForKey(iconKey)
            val (bg, tint) = when (iconKey.lowercase()) {
                "shopping_cart", "grocery", "store" ->
                    (if (isDark) Color(0xFF173A24) else Color(0xFFC5F0C9)) to (if (isDark) Color(0xFF66E08E) else Color(0xFF0F6B2E))
                "restaurant", "food", "fastfood" ->
                    Color(0xFF9127C6) to Color.White
                "home", "house", "rent" ->
                    Color(0xFFF6AE3B) to Color.White
                "shopping_bag", "checkroom", "clothing", "shopping" ->
                    Color(0xFF42A5F5) to Color.White
                "directions_bus", "commute", "transport", "car" ->
                    Color(0xFF00897B) to Color.White
                "local_hospital", "medical", "health" ->
                    Color(0xFFEC407A) to Color.White
                "school", "education" ->
                    Color(0xFF5C6BC0) to Color.White
                "bolt", "electric", "utility", "bill" ->
                    Color(0xFFFF7043) to Color.White
                else ->
                    (if (isDark) Color(0xFF3E1F20) else Color(0xFFFCDCD9)) to (if (isDark) Color(0xFFFF8680) else Color(0xFFC62828))
            }
            TxRowVisual(
                circleBg = bg,
                iconTint = tint,
                icon = icon,
                signPrefix = "-",
                amountColor = expenseColor,
                title = title
            )
        }
        LedgerTransactionType.SHOP_PURCHASE -> TxRowVisual(
            circleBg = if (isDark) Color(0xFF3B2B15) else Color(0xFFFCE8C7),
            iconTint = if (isDark) Color(0xFFF5BC66) else Color(0xFF9C5D18),
            icon = Icons.Filled.Storefront,
            signPrefix = "-",
            amountColor = expenseColor,
            title = shop?.name ?: strings.shopDueLabel
        )
        LedgerTransactionType.SHOP_DUE_PAYMENT -> TxRowVisual(
            circleBg = if (isDark) Color(0xFF143526) else Color(0xFFD5F3DD),
            iconTint = if (isDark) Color(0xFF5ED999) else Color(0xFF0E6843),
            icon = Icons.Filled.AccountBalanceWallet,
            signPrefix = "-",
            amountColor = expenseColor,
            title = "${shop?.name ?: strings.shopLabel} (${strings.recordPayment})"
        )
        LedgerTransactionType.LENDING_GIVEN -> TxRowVisual(
            circleBg = if (isDark) Color(0xFF1A3A28) else Color(0xFFBDECC3),
            iconTint = if (isDark) Color(0xFF66E08E) else Color(0xFF146E36),
            icon = Icons.Filled.People,
            signPrefix = "-",
            amountColor = expenseColor,
            title = strings.moneyGiven
        )
        LedgerTransactionType.LENDING_REPAYMENT -> TxRowVisual(
            circleBg = if (isDark) Color(0xFF143526) else Color(0xFFC8F3D0),
            iconTint = if (isDark) Color(0xFF5ED999) else Color(0xFF0E6E3B),
            icon = Icons.Filled.People,
            signPrefix = "+",
            amountColor = incomeColor,
            title = strings.receiveRepayment
        )
        LedgerTransactionType.BORROWING_TAKEN -> TxRowVisual(
            circleBg = if (isDark) Color(0xFF1A3A28) else Color(0xFFBDECC3),
            iconTint = if (isDark) Color(0xFF66E08E) else Color(0xFF146E36),
            icon = Icons.Filled.Handshake,
            signPrefix = "+",
            amountColor = incomeColor,
            title = strings.moneyBorrowed
        )
        LedgerTransactionType.BORROWING_REPAYMENT -> TxRowVisual(
            circleBg = if (isDark) Color(0xFF3E1F20) else Color(0xFFFCDCD9),
            iconTint = if (isDark) Color(0xFFFF8680) else Color(0xFFC62828),
            icon = Icons.Filled.Handshake,
            signPrefix = "-",
            amountColor = expenseColor,
            title = strings.makeRepayment
        )
    }
}

@Composable
private fun AuditLogDialog(
    logs: List<AuditLogEntity>,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.auditHistory, fontWeight = FontWeight.Bold) },
        text = {
            if (logs.isEmpty()) {
                Text(strings.noTransactionsYet, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        val (chipColor, chipBg) = when (log.action) {
                            "DELETE" -> finance.expense to finance.expenseContainer
                            "EDIT" -> finance.payable to finance.payableContainer
                            else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
                        }
                        Card(
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(color = chipBg, shape = RoundedCornerShape(50)) {
                                        Text(
                                            text = "${log.action} • ${log.entityType}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = chipColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = DateUtils.formatDateTime(log.createdAt, language, numerals),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (log.reason.isNotBlank()) {
                                    Text(
                                        text = log.reason,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Text("Before: ${log.previousSnapshot}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("After: ${log.newSnapshot}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.close) }
        }
    )
}
