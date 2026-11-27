package com.example.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.data.local.entity.NotebookEntity
import com.example.domain.accounting.LoanDirection
import com.example.presentation.components.BookSwitcherPill
import com.example.presentation.components.EmptyState
import com.example.presentation.components.PremiumHeader
import com.example.presentation.components.PremiumPageTitleRow
import com.example.presentation.components.iconForKey
import com.example.presentation.state.BookSummaryRow
import com.example.presentation.state.JomaKhorochUiState
import com.example.presentation.state.LoanWithBalance
import com.example.presentation.state.ShopWithBalance
import com.example.ui.theme.LocalFinanceColors
import com.example.ui.theme.PlusJakartaSansFont
import com.example.ui.theme.SoftGold
import com.example.ui.theme.WarmGold

private enum class StatusFilter { ALL, ACTIVE, SETTLED }

@Composable
fun AccountsScreen(
    uiState: JomaKhorochUiState,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    onAddShopClick: () -> Unit,
    onShopPurchaseClick: (ShopWithBalance) -> Unit,
    onShopPaymentClick: (ShopWithBalance) -> Unit,
    onShopStatementClick: (ShopWithBalance) -> Unit,
    onAddLoanClick: (LoanDirection) -> Unit,
    onLoanRepayClick: (LoanWithBalance) -> Unit,
    onSelectBook: (Long) -> Unit,
    onAddBookClick: () -> Unit,
    onArchiveBook: (NotebookEntity) -> Unit,
    onDeleteBookPermanently: (NotebookEntity) -> Unit,
    onSwitchBookClick: () -> Unit = {},
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

    val safeTab = selectedTab.coerceIn(0, 3)
    var statusFilter by rememberSaveable { mutableStateOf(StatusFilter.ALL) }
    var bookPendingDelete by remember { mutableStateOf<NotebookEntity?>(null) }

    BackHandler(enabled = safeTab > 0) {
        onSelectTab(0)
    }

    val dueShopCount = remember(uiState.shops) {
        uiState.shops.count { it.balance.outstandingDuePoisha > 0L }
    }
    val activeLendCount = remember(uiState.lendingLoans) {
        uiState.lendingLoans.count { it.balance.remainingPoisha > 0L }
    }
    val activeBorrowCount = remember(uiState.borrowingLoans) {
        uiState.borrowingLoans.count { it.balance.remainingPoisha > 0L }
    }

    val lastTxDateByShopId = remember(uiState.transactions) {
        uiState.transactions
            .filter { it.shopId != null }
            .groupBy { it.shopId!! }
            .mapValues { (_, list) -> list.maxOf { it.transactionDate } }
    }

    val summaryByBookId = remember(uiState.allBookSummaries) {
        uiState.allBookSummaries.associateBy { it.notebook.id }
    }

    val activeLoans = if (safeTab == 1) uiState.lendingLoans else uiState.borrowingLoans
    val filteredLoans = remember(activeLoans, statusFilter) {
        when (statusFilter) {
            StatusFilter.ALL -> activeLoans
            StatusFilter.ACTIVE -> activeLoans.filter { it.balance.remainingPoisha > 0L }
            StatusFilter.SETTLED -> activeLoans.filter { it.balance.remainingPoisha == 0L }
        }
    }

    val shopCountDisplay = if (dueShopCount > 0) dueShopCount else uiState.shops.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Curved Deep Emerald Header
        item(key = "accounts_header") {
            PremiumHeader(
                notificationCount = uiState.notifications.size,
                hasUrgentNotification = uiState.notifications.any { it.isUrgent },
                onNotificationClick = onOpenNotifications,
                onSettingsClick = onOpenSettings
            )
        }

        // 2. Title + Notebook Switcher Pill
        item(key = "accounts_title") {
            PremiumPageTitleRow(
                icon = Icons.Filled.Description,
                title = strings.navAccounts,
                subtitle = if (isBn) "দোকানের বাকি, ধার-দেনা এবং খাতার হিসাব" else "Shop dues, loans & account books",
                modifier = Modifier.padding(horizontal = 16.dp),
                trailing = {
                    BookSwitcherPill(
                        book = uiState.activeNotebook,
                        onClick = onSwitchBookClick,
                        containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainer else Color(0xFFF6F2E7),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }

        // 3. 2x2 KPI Summary Grid (Matches mockup palette & Deep Emerald active highlight)
        item(key = "accounts_kpi_grid") {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AccountsKpiCard(
                        title = strings.shopDueLabel,
                        value = CurrencyFormatter.formatPoisha(uiState.financialSummary.totalShopPayablePoisha, numerals),
                        subtitle = if (isBn) "${CurrencyFormatter.toLocalizedDigits(shopCountDisplay.toString(), numerals)}টি দোকান"
                        else "$shopCountDisplay shops",
                        icon = Icons.Filled.Storefront,
                        highlighted = safeTab == 0,
                        containerColor = if (isDark) Color(0xFF132D23) else Color(0xFFE3F5E6),
                        borderColor = if (isDark) Color(0xFF234D3C) else Color(0xFFC1E3C8),
                        accentColor = if (isDark) Color(0xFF56D496) else Color(0xFF0E6843),
                        onClick = {
                            onSelectTab(0)
                            statusFilter = StatusFilter.ALL
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("accounts_kpi_shop")
                    )
                    AccountsKpiCard(
                        title = if (isBn) "ধার দেওয়া" else strings.lendingTab,
                        value = CurrencyFormatter.formatPoisha(uiState.financialSummary.totalPersonalReceivablePoisha, numerals),
                        subtitle = if (isBn) "${CurrencyFormatter.toLocalizedDigits(activeLendCount.toString(), numerals)} জন"
                        else "$activeLendCount people",
                        icon = Icons.Filled.VolunteerActivism,
                        highlighted = safeTab == 1,
                        containerColor = if (isDark) Color(0xFF132E23) else Color(0xFFE2F5E5),
                        borderColor = if (isDark) Color(0xFF234E3C) else Color(0xFFBFE2C6),
                        accentColor = if (isDark) Color(0xFF52D18F) else Color(0xFF0E6B3E),
                        onClick = {
                            onSelectTab(1)
                            statusFilter = StatusFilter.ALL
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("accounts_kpi_lending")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AccountsKpiCard(
                        title = if (isBn) "ধার নেওয়া" else strings.borrowingTab,
                        value = CurrencyFormatter.formatPoisha(uiState.financialSummary.totalPersonalPayablePoisha, numerals),
                        subtitle = if (isBn) "${CurrencyFormatter.toLocalizedDigits(activeBorrowCount.toString(), numerals)} জন"
                        else "$activeBorrowCount lenders",
                        icon = Icons.Filled.Handshake,
                        highlighted = safeTab == 2,
                        containerColor = if (isDark) Color(0xFF2A2116) else Color(0xFFFAF0DD),
                        borderColor = if (isDark) Color(0xFF4A3B26) else Color(0xFFE7D4B4),
                        accentColor = if (isDark) Color(0xFFF0B866) else Color(0xFF8C5620),
                        onClick = {
                            onSelectTab(2)
                            statusFilter = StatusFilter.ALL
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("accounts_kpi_borrowing")
                    )
                    AccountsKpiCard(
                        title = if (isBn) "খাতার সংখ্যা" else strings.booksTab,
                        value = if (isBn) "${CurrencyFormatter.toLocalizedDigits(uiState.allNotebooks.size.toString(), numerals)}টি খাতা"
                        else "${uiState.allNotebooks.size} Books",
                        subtitle = if (isBn) "সক্রিয়: ${CurrencyFormatter.toLocalizedDigits(uiState.activeNotebooks.size.toString(), numerals)}টি"
                        else "Active: ${uiState.activeNotebooks.size}",
                        icon = Icons.Filled.MenuBook,
                        highlighted = safeTab == 3,
                        containerColor = if (isDark) Color(0xFF142C24) else Color(0xFFE4F4E7),
                        borderColor = if (isDark) Color(0xFF254B3E) else Color(0xFFC2E1C8),
                        accentColor = if (isDark) Color(0xFF5AD59C) else Color(0xFF0E5E3E),
                        onClick = {
                            onSelectTab(3)
                            statusFilter = StatusFilter.ALL
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("accounts_kpi_books")
                    )
                }
            }
        }

        // 4. 4-Pill Filter Strip (Shop Due | Money Lent | Money Borrowed | Notebooks)
        item(key = "accounts_pill_strip") {
            val pills = listOf(
                AccountPillSpec(
                    index = 0,
                    label = strings.shopDueLabel,
                    icon = Icons.Filled.Storefront,
                    inactiveBg = if (isDark) Color(0xFF142D24) else Color(0xFFE5F4E8),
                    inactiveBorder = if (isDark) Color(0xFF254D3E) else Color(0xFFC5E1CB),
                    inactiveIconColor = if (isDark) Color(0xFF5AD59C) else Color(0xFF0E6843)
                ),
                AccountPillSpec(
                    index = 1,
                    label = if (isBn) "ধার দেওয়া" else strings.lendingTab,
                    icon = Icons.Filled.SwapHoriz,
                    inactiveBg = if (isDark) Color(0xFF142D24) else Color(0xFFE5F5E7),
                    inactiveBorder = if (isDark) Color(0xFF254D3E) else Color(0xFFC5E3CA),
                    inactiveIconColor = if (isDark) Color(0xFF52D18F) else Color(0xFF0E7442)
                ),
                AccountPillSpec(
                    index = 2,
                    label = if (isBn) "ধার নেওয়া" else strings.borrowingTab,
                    icon = Icons.Filled.ArrowDownward,
                    inactiveBg = if (isDark) Color(0xFF331B1B) else Color(0xFFFDE8E6),
                    inactiveBorder = if (isDark) Color(0xFF5B2F2F) else Color(0xFFF3C6C2),
                    inactiveIconColor = if (isDark) Color(0xFFFF7B76) else Color(0xFFD32F2F)
                ),
                AccountPillSpec(
                    index = 3,
                    label = if (isBn) "খাতাসমূহ" else strings.booksTab,
                    icon = Icons.Filled.MenuBook,
                    inactiveBg = if (isDark) Color(0xFF292319) else Color(0xFFF7F2E4),
                    inactiveBorder = if (isDark) Color(0xFF473D2B) else Color(0xFFE4DAC0),
                    inactiveIconColor = if (isDark) Color(0xFF6DD4A2) else Color(0xFF0E5E3E)
                )
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pills, key = { it.index }) { pill ->
                    AccountSectionPill(
                        spec = pill,
                        selected = safeTab == pill.index,
                        onClick = {
                            onSelectTab(pill.index)
                            statusFilter = StatusFilter.ALL
                        },
                        modifier = Modifier.testTag("accounts_tab_${pill.index}")
                    )
                }
            }
        }

        // 5. Main Content Sections
        when (safeTab) {
            // Default Overview + Shop Due View (Matches attached "হিসাব" mockup 1:1)
            0 -> {
                // SECTION A: দোকানের বাকি (Shop Due List)
                item(key = "overview_shops_section") {
                    AccountsConnectedSectionCard(
                        icon = Icons.Filled.Storefront,
                        title = strings.shopDueLabel,
                        actionLabel = "+ ${if (isBn) "নতুন দোকান" else strings.addShop}",
                        onActionClick = onAddShopClick,
                        actionTag = "add_shop_button",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        if (uiState.shops.isEmpty()) {
                            CompactSectionEmptyRow(
                                icon = Icons.Filled.Storefront,
                                title = strings.noShopsYet,
                                subtitle = strings.noShopsHint,
                                actionLabel = "+ ${if (isBn) "নতুন দোকান" else strings.addShop}",
                                onAction = onAddShopClick
                            )
                        } else {
                            uiState.shops.forEachIndexed { idx, shopItem ->
                                MockupShopRow(
                                    item = shopItem,
                                    lastTxDateMillis = lastTxDateByShopId[shopItem.shop.id],
                                    alternateWarmBadge = idx % 2 == 1,
                                    onClick = { onShopStatementClick(shopItem) }
                                )
                                if (idx < uiState.shops.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION B: ধার-দেনার সারসংক্ষেপ (Lending & Borrowing Summary)
                item(key = "overview_loans_summary") {
                    AccountsConnectedSectionCard(
                        icon = Icons.Filled.Handshake,
                        title = if (isBn) "ধার-দেনার সারসংক্ষেপ" else "Lending & Borrowing Summary",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LoanSummaryMiniTile(
                                title = if (isBn) "মোট ধার দেওয়া" else "Total Lent",
                                amountText = CurrencyFormatter.formatPoisha(
                                    uiState.financialSummary.totalPersonalReceivablePoisha,
                                    numerals
                                ),
                                countText = if (isBn) "${CurrencyFormatter.toLocalizedDigits(activeLendCount.toString(), numerals)} জনকে"
                                else "To $activeLendCount people",
                                icon = Icons.Filled.VolunteerActivism,
                                containerColor = if (isDark) Color(0xFF132E23) else Color(0xFFE3F4E5),
                                borderColor = if (isDark) Color(0xFF234E3C) else Color(0xFFC3E4C8),
                                accentColor = if (isDark) Color(0xFF52D18F) else Color(0xFF0E7442),
                                onClick = {
                                    onSelectTab(1)
                                    statusFilter = StatusFilter.ALL
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("loan_summary_given_tile")
                            )
                            LoanSummaryMiniTile(
                                title = if (isBn) "মোট ধার নেওয়া" else "Total Borrowed",
                                amountText = CurrencyFormatter.formatPoisha(
                                    uiState.financialSummary.totalPersonalPayablePoisha,
                                    numerals
                                ),
                                countText = if (isBn) "${CurrencyFormatter.toLocalizedDigits(activeBorrowCount.toString(), numerals)} জনের থেকে"
                                else "From $activeBorrowCount lenders",
                                icon = Icons.Filled.Payments,
                                containerColor = if (isDark) Color(0xFF331B1B) else Color(0xFFFDE3E1),
                                borderColor = if (isDark) Color(0xFF5A2E2E) else Color(0xFFF4C5C1),
                                accentColor = if (isDark) Color(0xFFFF7B76) else Color(0xFFC62828),
                                onClick = {
                                    onSelectTab(2)
                                    statusFilter = StatusFilter.ALL
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("loan_summary_borrowed_tile")
                            )
                        }
                    }
                }

                // SECTION C: খাতার তালিকা (Account Books List)
                item(key = "overview_books_section") {
                    AccountsConnectedSectionCard(
                        icon = Icons.Filled.MenuBook,
                        title = if (isBn) "খাতার তালিকা" else strings.accountBooks,
                        actionLabel = "+ ${if (isBn) "নতুন খাতা" else strings.addBook}",
                        onActionClick = onAddBookClick,
                        actionTag = "add_book_button",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        uiState.allNotebooks.forEachIndexed { idx, book ->
                            MockupNotebookRow(
                                book = book,
                                isSelectedActive = book.id == uiState.activeNotebook?.id,
                                bookSummary = summaryByBookId[book.id],
                                onSelect = { onSelectBook(book.id) }
                            )
                            if (idx < uiState.allNotebooks.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                                )
                            }
                        }
                    }
                }
            }

            // Focused Lending / Borrowing View
            1, 2 -> {
                val isGiven = safeTab == 1
                val dir = if (isGiven) LoanDirection.GIVEN else LoanDirection.BORROWED
                item(key = "loans_banner_$safeTab") {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AccountsConnectedSectionCard(
                            icon = if (isGiven) Icons.Filled.VolunteerActivism else Icons.Filled.Handshake,
                            title = if (isGiven) strings.moneyGiven else strings.moneyBorrowed,
                            actionLabel = "+ ${if (isGiven) strings.giveLoan else strings.borrowMoney}",
                            onActionClick = { onAddLoanClick(dir) },
                            actionTag = if (isGiven) "add_lending_button" else "add_borrowing_button"
                        ) {
                            if (activeLoans.isEmpty()) {
                                CompactSectionEmptyRow(
                                    icon = if (isGiven) Icons.Filled.ArrowUpward else Icons.Filled.Handshake,
                                    title = if (isGiven) strings.noLendingYet else strings.noBorrowingYet,
                                    subtitle = if (isBn) "উপরের বাটনে ট্যাপ করে নতুন হিসাব যোগ করুন" else "Tap the button above to add a record",
                                    actionLabel = "+ ${if (isGiven) strings.giveLoan else strings.borrowMoney}",
                                    onAction = { onAddLoanClick(dir) }
                                )
                            } else {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    StatusFilterRow(
                                        selected = statusFilter,
                                        activeLabel = if (isBn) "চলমান" else "Active",
                                        settledLabel = strings.settled,
                                        onSelect = { statusFilter = it }
                                    )
                                }
                            }
                        }
                    }
                }

                if (activeLoans.isNotEmpty() && filteredLoans.isEmpty()) {
                    item(key = "loans_filter_empty_$safeTab") {
                        EmptyState(
                            icon = Icons.Filled.CheckCircle,
                            title = if (isBn) "এই ফিল্টারে কোনো হিসাব নেই" else "No records in this filter",
                            subtitle = "",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    items(filteredLoans, key = { "loan_${it.loan.id}" }) { item ->
                        PremiumLoanCard(
                            item = item,
                            isGiven = isGiven,
                            onRepayClick = { onLoanRepayClick(item) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // Focused Notebooks Management View
            else -> {
                item(key = "books_banner") {
                    AccountsConnectedSectionCard(
                        icon = Icons.Filled.MenuBook,
                        title = if (isBn) "খাতার তালিকা" else strings.accountBooks,
                        actionLabel = "+ ${if (isBn) "নতুন খাতা" else strings.addBook}",
                        onActionClick = onAddBookClick,
                        actionTag = "add_book_button",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = if (isBn) "যেকোনো খাতায় ট্যাপ করে সক্রিয় খাতা পরিবর্তন করুন অথবা ডিলিট আইকন থেকে আর্কাইভ/ডিলিট করুন।"
                            else "Tap a notebook to switch active ledger, or use the delete action to archive or remove.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
                items(uiState.allNotebooks, key = { "book_${it.id}" }) { book ->
                    PremiumNotebookCard(
                        book = book,
                        isActive = book.id == uiState.activeNotebook?.id,
                        bookSummary = summaryByBookId[book.id],
                        onSelect = { onSelectBook(book.id) },
                        onRequestDelete = { bookPendingDelete = book },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    bookPendingDelete?.let { book ->
        DeleteBookPolicyDialog(
            book = book,
            onArchive = {
                onArchiveBook(book)
                bookPendingDelete = null
            },
            onPermanentDelete = {
                onDeleteBookPermanently(book)
                bookPendingDelete = null
            },
            onDismiss = { bookPendingDelete = null }
        )
    }
}

private data class AccountPillSpec(
    val index: Int,
    val label: String,
    val icon: ImageVector,
    val inactiveBg: Color,
    val inactiveBorder: Color,
    val inactiveIconColor: Color
)

@Composable
private fun AccountsKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    highlighted: Boolean,
    containerColor: Color,
    borderColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val finance = LocalFinanceColors.current
    val shape = RoundedCornerShape(18.dp)

    Card(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) Color(0xFF0A4D35) else containerColor
        ),
        border = BorderStroke(
            width = if (highlighted) 1.2.dp else 1.dp,
            color = if (highlighted) WarmGold.copy(alpha = 0.55f) else borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlighted) 2.5.dp else 0.8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (highlighted) {
                        Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0D6443), Color(0xFF073A2A))
                            )
                        )
                    } else Modifier
                )
                .padding(horizontal = 11.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (highlighted) Color.White else accentColor,
                    modifier = Modifier.size(32.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (highlighted) Color.White.copy(alpha = 0.92f) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PlusJakartaSansFont,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (highlighted) Color.White else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = if (highlighted) SoftGold else accentColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (highlighted) Color.White.copy(alpha = 0.86f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Circular arrow indicator badge matching mockup
                Surface(
                    shape = CircleShape,
                    color = if (highlighted) WarmGold else accentColor.copy(alpha = if (finance.isDark) 0.25f else 0.16f),
                    border = if (highlighted) null else BorderStroke(1.2.dp, accentColor.copy(alpha = 0.65f)),
                    modifier = Modifier.size(21.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (highlighted) Color(0xFF073A2A) else accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountSectionPill(
    spec: AccountPillSpec,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF0A573B) else spec.inactiveBg,
        animationSpec = tween(180),
        label = "account_pill_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(180),
        label = "account_pill_text"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) Color.White else spec.inactiveIconColor,
        animationSpec = tween(180),
        label = "account_pill_icon"
    )

    Surface(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) WarmGold.copy(alpha = 0.55f) else spec.inactiveBorder
        ),
        shadowElevation = if (selected) 2.dp else 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = spec.icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = spec.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AccountsConnectedSectionCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val finance = LocalFinanceColors.current
    val shape = RoundedCornerShape(20.dp)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Deep Emerald Header Banner seamlessly joined to the white card body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(finance.sectionBannerBg, Color(0xFF0A4D38))
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = SoftGold,
                            modifier = Modifier.size(21.dp)
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.5.sp),
                            fontWeight = FontWeight.Bold,
                            color = finance.sectionBannerContent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (actionLabel != null && onActionClick != null) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onActionClick)
                                .let { if (actionTag != null) it.testTag(actionTag) else it }
                        ) {
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            content()
        }
    }
}

@Composable
private fun MockupShopRow(
    item: ShopWithBalance,
    lastTxDateMillis: Long?,
    alternateWarmBadge: Boolean,
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA

    val due = item.balance.outstandingDuePoisha
    val hasDue = due > 0L

    val badgeBg = if (alternateWarmBadge) {
        if (finance.isDark) Color(0xFF332815) else Color(0xFFFBE9C8)
    } else {
        if (finance.isDark) Color(0xFF143122) else Color(0xFFD8F3DC)
    }
    val badgeIconColor = if (alternateWarmBadge) {
        if (finance.isDark) Color(0xFFF0B866) else Color(0xFF9C5D18)
    } else {
        if (finance.isDark) Color(0xFF56D496) else Color(0xFF0E6843)
    }
    val amountColor = if (hasDue) finance.expense else finance.income

    val subtitleText = when {
        lastTxDateMillis != null -> {
            val formattedDate = DateUtils.formatDate(lastTxDateMillis, language, numerals)
            if (isBn) "শেষ লেনদেন: $formattedDate" else "Last transaction: $formattedDate"
        }
        item.shop.ownerName.isNotBlank() || item.shop.shopCategory.isNotBlank() -> {
            listOfNotNull(
                item.shop.ownerName.takeIf { it.isNotBlank() },
                item.shop.shopCategory.takeIf { it.isNotBlank() }
            ).joinToString(" • ")
        }
        else -> if (isBn) "হিসাব দেখতে ট্যাপ করুন" else "Tap to view statement"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("shop_card_${item.shop.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Storefront,
                contentDescription = item.shop.name,
                tint = badgeIconColor,
                modifier = Modifier.size(23.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.shop.name,
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
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatPoisha(due, numerals),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlusJakartaSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = amountColor,
                    maxLines = 1
                )
                if (!hasDue) {
                    Text(
                        text = strings.settled,
                        style = MaterialTheme.typography.labelSmall,
                        color = finance.income,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (hasDue) finance.payable else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LoanSummaryMiniTile(
    title: String,
    amountText: String,
    countText: String,
    icon: ImageVector,
    containerColor: Color,
    borderColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(30.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlusJakartaSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = countText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun MockupNotebookRow(
    book: NotebookEntity,
    isSelectedActive: Boolean,
    bookSummary: BookSummaryRow?,
    onSelect: () -> Unit
) {
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA

    val bookTint = when {
        book.isArchived -> MaterialTheme.colorScheme.outline
        book.type == "FAMILY" -> if (finance.isDark) Color(0xFFB388FF) else Color(0xFF7E22CE)
        book.type == "BUSINESS" -> if (finance.isDark) Color(0xFF64B5F6) else Color(0xFF1565C0)
        book.type == "PERSONAL" -> if (finance.isDark) Color(0xFF56D496) else Color(0xFF147A42)
        else -> if (finance.isDark) SoftGold else Color(0xFF0E6843)
    }

    val txCount = bookSummary?.transactionCount ?: 0
    val txCountStr = CurrencyFormatter.toLocalizedDigits(txCount.toString(), numerals)
    val lastDateStr = bookSummary?.lastTransactionDate?.let { DateUtils.formatDate(it, language, numerals) }

    val subtitle = buildString {
        append(if (isBn) "লেনদেন: ${txCountStr}টি" else "Entries: $txCountStr")
        if (lastDateStr != null) {
            append(" • ")
            append(if (isBn) "সর্বশেষ: $lastDateStr" else "Last: $lastDateStr")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !book.isArchived, onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("overview_book_row_${book.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val displayBookName = localizedBookName(book.name, book.type, language)
        Icon(
            imageVector = Icons.Filled.Book,
            contentDescription = displayBookName,
            tint = bookTint,
            modifier = Modifier.size(30.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayBookName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val pillBg = when {
                book.isArchived -> MaterialTheme.colorScheme.surfaceContainerHigh
                isSelectedActive -> if (finance.isDark) Color(0xFF184432) else Color(0xFFD2F3D7)
                else -> if (finance.isDark) Color(0xFF143125) else Color(0xFFE3F5E6)
            }
            val pillText = when {
                book.isArchived -> if (isBn) "আর্কাইভ" else "Archived"
                else -> if (isBn) "সক্রিয়" else "Active"
            }
            val pillTextColor = when {
                book.isArchived -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> finance.income
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = pillBg,
                border = if (isSelectedActive && !book.isArchived) {
                    BorderStroke(1.dp, finance.income.copy(alpha = 0.45f))
                } else null
            ) {
                Text(
                    text = pillText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = pillTextColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CompactSectionEmptyRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(2.dp))
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onAction)
        ) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
private fun PremiumLoanCard(
    item: LoanWithBalance,
    isGiven: Boolean,
    onRepayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current

    val remaining = item.balance.remainingPoisha
    val principal = item.balance.principalPoisha
    val repaid = (principal - remaining).coerceAtLeast(0L)
    val settled = remaining == 0L
    val accent = if (isGiven) finance.receivable else finance.payable
    val bg = if (isGiven) finance.receivableContainer else finance.payableContainer
    val statusColor = if (settled) finance.income else accent
    val statusBg = if (settled) finance.incomeContainer else bg

    val ratio = if (principal > 0L) (repaid.toFloat() / principal.toFloat()).coerceIn(0f, 1f) else 1f
    val animatedRatio by animateFloatAsState(
        targetValue = ratio,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "loan_avatar_ring"
    )
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val initial = item.loan.personName.trim().firstOrNull()?.uppercase() ?: "P"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onRepayClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokePx = 3.5.dp.toPx()
                        val arcSize = Size(size.width - strokePx, size.height - strokePx)
                        val topLeft = Offset(strokePx / 2f, strokePx / 2f)
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                        if (animatedRatio > 0f) {
                            drawArc(
                                color = statusColor,
                                startAngle = -90f,
                                sweepAngle = animatedRatio * 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(statusBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.loan.personName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.loan.phone.isNotBlank()) {
                        Text(
                            text = item.loan.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = DateUtils.formatDate(item.loan.loanDate, language, numerals),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        item.loan.dueDate?.let { due ->
                            Surface(color = statusBg, shape = RoundedCornerShape(6.dp)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.CalendarToday,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        text = DateUtils.formatDate(due, language, numerals),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (settled) strings.settled
                        else if (isGiven) strings.remainingReceivable else strings.remainingPayable,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                    Text(
                        text = CurrencyFormatter.formatPoisha(remaining, numerals),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PlusJakartaSansFont,
                            fontWeight = FontWeight.Bold
                        ),
                        color = statusColor
                    )
                    Text(
                        text = "${strings.amount}: ${CurrencyFormatter.formatPoisha(principal, numerals)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (item.loan.description.isNotBlank()) {
                Text(
                    text = item.loan.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val paidLabel = if (language == AppLanguage.BANGLA) "পরিশোধিত" else "Repaid"
                Text(
                    text = "$paidLabel: ${CurrencyFormatter.formatPoisha(repaid, numerals)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!settled) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onRepayClick),
                        shape = RoundedCornerShape(50),
                        color = accent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isGiven) Icons.Filled.ArrowDownward else Icons.Filled.Payments,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = if (isGiven) strings.receiveRepayment else strings.makeRepayment,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = finance.income,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = strings.settled,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = finance.income
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumNotebookCard(
    book: NotebookEntity,
    isActive: Boolean,
    bookSummary: BookSummaryRow?,
    onSelect: () -> Unit,
    onRequestDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current

    val accentColor = when {
        book.isArchived -> MaterialTheme.colorScheme.outline
        book.type == "FAMILY" -> if (finance.isDark) Color(0xFFB388FF) else Color(0xFF7E22CE)
        book.type == "BUSINESS" -> if (finance.isDark) Color(0xFF64B5F6) else Color(0xFF1565C0)
        isActive -> MaterialTheme.colorScheme.primary
        else -> finance.income
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = !book.isArchived, onClick = onSelect),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 1.5.dp else 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    val displayBookName = localizedBookName(book.name, book.type, language)
                    Icon(
                        imageVector = iconForKey(book.iconKey),
                        contentDescription = displayBookName,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = localizedBookName(book.name, book.type, language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isActive) {
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                color = finance.incomeContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BANGLA) "সক্রিয়" else "Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = finance.income,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        } else if (book.isArchived) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Filled.Archive,
                                contentDescription = "Archived",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = CurrencyFormatter.formatPoisha(
                            bookSummary?.cashBalancePoisha ?: book.openingBalancePoisha,
                            numerals
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PlusJakartaSansFont,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onRequestDelete) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = strings.delete,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BookMiniStat(
                    label = strings.openingBalance,
                    value = CurrencyFormatter.formatPoisha(book.openingBalancePoisha, numerals),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BookMiniStat(
                    label = strings.incomeLabel,
                    value = CurrencyFormatter.formatPoisha(bookSummary?.incomePoisha ?: 0L, numerals),
                    color = finance.income
                )
                BookMiniStat(
                    label = strings.expenseLabel,
                    value = CurrencyFormatter.formatPoisha(bookSummary?.expensePoisha ?: 0L, numerals),
                    color = finance.expense
                )
            }
        }
    }
}

@Composable
private fun BookMiniStat(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = PlusJakartaSansFont,
                fontWeight = FontWeight.Bold
            ),
            color = color,
            maxLines = 1
        )
    }
}

@Composable
private fun StatusFilterRow(
    selected: StatusFilter,
    activeLabel: String,
    settledLabel: String,
    onSelect: (StatusFilter) -> Unit
) {
    val strings = LocalAppStrings.current
    val options = listOf(
        StatusFilter.ALL to strings.allFilter,
        StatusFilter.ACTIVE to activeLabel,
        StatusFilter.SETTLED to settledLabel
    )
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (status, label) ->
                val isSelected = selected == status
                val bg by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    animationSpec = tween(180),
                    label = "status_filter_bg"
                )
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelect(status) },
                    shape = RoundedCornerShape(10.dp),
                    color = bg,
                    shadowElevation = if (isSelected) 1.dp else 0.dp
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
