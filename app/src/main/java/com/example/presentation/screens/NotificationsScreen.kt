package com.example.presentation.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalLanguage
import com.example.core.localization.LocalNumeralStyle
import com.example.core.util.CurrencyFormatter
import com.example.infrastructure.ReminderScheduler
import com.example.presentation.components.PremiumFilterChip
import com.example.presentation.components.PremiumHeader
import com.example.presentation.components.PremiumPageTitleRow
import com.example.presentation.components.PremiumSectionBanner
import com.example.presentation.state.InAppNotificationItem
import com.example.presentation.state.JomaKhorochUiState
import com.example.presentation.state.NotificationAlertType
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LocalFinanceColors
import com.example.ui.theme.WarmGold

private enum class AlertFilterTab { ALL, URGENT, BUDGET, ACCOUNTS, REMINDERS }

private data class DailyTimePreset(
    val hour: Int,
    val minute: Int,
    val labelBn: String,
    val labelEn: String
)

private val DAILY_TIME_PRESETS = listOf(
    DailyTimePreset(9, 0, "সকাল ৯:০০", "9:00 AM"),
    DailyTimePreset(14, 0, "দুপুর ২:০০", "2:00 PM"),
    DailyTimePreset(19, 0, "সন্ধ্যা ৭:০০", "7:00 PM"),
    DailyTimePreset(21, 0, "রাত ৯:০০", "9:00 PM"),
    DailyTimePreset(22, 0, "রাত ১০:০০", "10:00 PM")
)

/**
 * Pure Notification & Alerts Inbox opened from the top header Bell icon.
 * Displays active actionable alerts first with category filters and a header shortcut to Notification Settings.
 */
@Composable
fun NotificationsScreen(
    uiState: JomaKhorochUiState,
    onOpenNotificationSettings: () -> Unit,
    onOpenAppUpdate: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenDiary: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val haptic = LocalHapticFeedback.current
    val isBn = language == AppLanguage.BANGLA

    var selectedFilter by rememberSaveable { mutableStateOf(AlertFilterTab.ALL) }
    var hasSystemPermission by remember {
        mutableStateOf(ReminderScheduler.hasNotificationPermission(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasSystemPermission = ReminderScheduler.hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasSystemPermission = granted || ReminderScheduler.hasNotificationPermission(context)
    }

    val allAlerts = uiState.notifications
    val urgentCount = remember(allAlerts) { allAlerts.count { it.isUrgent } }
    val filteredAlerts = remember(allAlerts, selectedFilter) {
        when (selectedFilter) {
            AlertFilterTab.ALL -> allAlerts
            AlertFilterTab.URGENT -> allAlerts.filter { it.isUrgent }
            AlertFilterTab.BUDGET -> allAlerts.filter {
                it.type == NotificationAlertType.BUDGET_EXCEEDED || it.type == NotificationAlertType.BUDGET_WARNING
            }
            AlertFilterTab.ACCOUNTS -> allAlerts.filter {
                it.type == NotificationAlertType.LOAN_DUE || it.type == NotificationAlertType.SHOP_DUE
            }
            AlertFilterTab.REMINDERS -> allAlerts.filter {
                it.type == NotificationAlertType.TASK_REMINDER || it.type == NotificationAlertType.APP_UPDATE
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
            onSettingsClick = onOpenNotificationSettings
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Page Title Row
            item(key = "notif_inbox_title") {
                PremiumPageTitleRow(
                    icon = Icons.Filled.NotificationsActive,
                    title = if (isBn) "নোটিফিকেশন" else "Notifications",
                    subtitle = if (isBn) "আপনার সকল অ্যালার্ট ও রিমাইন্ডার" else "All active alerts & reminders",
                    trailing = {
                        Surface(
                            color = if (urgentCount > 0) finance.expenseContainer else MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(
                                1.dp,
                                if (urgentCount > 0) finance.expenseBorder else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            ),
                            shape = RoundedCornerShape(50)
                        ) {
                            val countStr = CurrencyFormatter.toLocalizedDigits(allAlerts.size.toString(), numerals)
                            Text(
                                text = if (isBn) "$countStr টি" else "$countStr",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (urgentCount > 0) finance.expense else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                            )
                        }
                    }
                )
            }

            // 2. Compact Permission Warning Banner (only when system permission is missing)
            if (!hasSystemPermission) {
                item(key = "inbox_permission_strip") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = finance.goldContainer,
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsOff,
                                contentDescription = null,
                                tint = WarmGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isBn) "পুশ অ্যালার্ট পেতে নোটিফিকেশন অনুমতি দিন"
                                else "Allow notifications for instant push alerts",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        }
                                        runCatching { context.startActivity(intent) }
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("grant_notification_permission_button")
                            ) {
                                Text(
                                    text = if (isBn) "চালু করুন" else "Allow",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Filter Chips Row
            item(key = "inbox_filter_chips") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    val tabs = listOf(
                        AlertFilterTab.ALL to (if (isBn) "সব" else "All"),
                        AlertFilterTab.URGENT to (if (isBn) "জরুরি" else "Urgent"),
                        AlertFilterTab.BUDGET to (if (isBn) "বাজেট" else "Budget"),
                        AlertFilterTab.ACCOUNTS to (if (isBn) "হিসাব ও বাকি" else "Accounts"),
                        AlertFilterTab.REMINDERS to (if (isBn) "রিমাইন্ডার" else "Reminders")
                    )
                    items(tabs, key = { it.first.name }) { (tab, label) ->
                        PremiumFilterChip(
                            label = label,
                            selected = selectedFilter == tab,
                            onClick = { selectedFilter = tab },
                            selectedBgColor = if (tab == AlertFilterTab.URGENT) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            selectedContentColor = if (tab == AlertFilterTab.URGENT) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("notif_filter_${tab.name.lowercase()}")
                        )
                    }
                }
            }

            // 4. Urgent Summary Banner (when urgent alerts exist)
            if (urgentCount > 0 && (selectedFilter == AlertFilterTab.ALL || selectedFilter == AlertFilterTab.URGENT)) {
                item(key = "inbox_urgent_summary") {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = finance.expenseContainer,
                        border = BorderStroke(1.dp, finance.expenseBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 13.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = finance.expense,
                                modifier = Modifier.size(18.dp)
                            )
                            val uStr = CurrencyFormatter.toLocalizedDigits(urgentCount.toString(), numerals)
                            Text(
                                text = if (isBn) "$uStr টি জরুরি অ্যালার্টে এখনই মনোযোগ দিন"
                                else "$uStr urgent alert${if (urgentCount > 1) "s" else ""} need attention",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = finance.expense
                            )
                        }
                    }
                }
            }

            // 5. Alerts Feed or Clean Empty State
            if (filteredAlerts.isEmpty()) {
                item(key = "empty_alerts") {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(finance.incomeContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = finance.income,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Text(
                                text = if (selectedFilter == AlertFilterTab.ALL) {
                                    if (isBn) "সবকিছু ঠিকঠাক আছে!" else "You're all caught up!"
                                } else {
                                    if (isBn) "এই ক্যাটাগরিতে কোনো অ্যালার্ট নেই" else "No alerts in this category"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (isBn) {
                                    "বাজেট সতর্কতা, ধার ফেরতের তারিখ, দোকানের বাকি বা ডায়রি রিমাইন্ডার থাকলে এখানে সাথে সাথে দেখতে পাবেন।"
                                } else {
                                    "Budget warnings, loan due dates, shop dues, and diary reminders will appear here automatically."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = onOpenNotificationSettings,
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.testTag("inbox_open_notif_settings")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "রিমাইন্ডার সেটিংস" else "Reminder Settings",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredAlerts, key = { it.id }) { alert ->
                    NotificationAlertCard(
                        item = alert,
                        isBn = isBn,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            when (alert.type) {
                                NotificationAlertType.APP_UPDATE -> onOpenAppUpdate()
                                NotificationAlertType.BUDGET_EXCEEDED,
                                NotificationAlertType.BUDGET_WARNING -> onOpenBudgets()
                                NotificationAlertType.LOAN_DUE,
                                NotificationAlertType.SHOP_DUE -> onOpenAccounts()
                                NotificationAlertType.TASK_REMINDER -> onOpenDiary()
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Notification & Reminder Settings screen opened from
 * Settings -> খাতা ও ক্যাটাগরি -> নোটিফিকেশন ও রিমাইন্ডার (or from the Inbox gear button).
 */
@Composable
fun NotificationSettingsScreen(
    uiState: JomaKhorochUiState,
    onToggleMasterNotifications: (Boolean) -> Unit,
    onToggleBudgetAlerts: (Boolean) -> Unit,
    onToggleLoanDueAlerts: (Boolean) -> Unit,
    onToggleUpdateAlerts: (Boolean) -> Unit,
    onSetDailyReminder: (Boolean, Int?, Int?) -> Unit,
    onSendTestNotification: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val language = LocalLanguage.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA
    val prefs = uiState.preferences

    var hasSystemPermission by remember {
        mutableStateOf(ReminderScheduler.hasNotificationPermission(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasSystemPermission = ReminderScheduler.hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasSystemPermission = granted || ReminderScheduler.hasNotificationPermission(context)
    }

    fun requestPermissionIfNeeded(onGrantedAction: () -> Unit = {}) {
        if (ReminderScheduler.hasNotificationPermission(context)) {
            hasSystemPermission = true
            onGrantedAction()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            onGrantedAction()
        } else {
            onGrantedAction()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PremiumHeader(onBackClick = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "notif_settings_title") {
                PremiumPageTitleRow(
                    icon = Icons.Filled.Tune,
                    title = if (isBn) "নোটিফিকেশন ও রিমাইন্ডার" else "Notifications & Reminders",
                    subtitle = if (isBn) "বাজেট, ধার, আপডেট ও দৈনিক স্মরণিকা নিয়ন্ত্রণ" else "Control alerts & daily accounting check-ins"
                )
            }

            if (!hasSystemPermission) {
                item(key = "permission_banner") {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = finance.goldContainer,
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsOff,
                                    contentDescription = null,
                                    tint = WarmGold,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBn) "নোটিফিকেশন অনুমতি প্রয়োজন" else "Notification Permission Needed",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isBn) {
                                            "বাজেট সতর্কতা, ধার ফেরত ও দৈনিক হিসাবের রিমাইন্ডার পেতে নোটিফিকেশন অনুমতি দিন।"
                                        } else {
                                            "Allow notifications to receive budget warnings, loan due dates, and daily reminders."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { requestPermissionIfNeeded() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("grant_notification_permission_button")
                                ) {
                                    Text(if (isBn) "অনুমতি দিন" else "Allow")
                                }
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        }
                                        runCatching { context.startActivity(intent) }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (isBn) "সেটিংস" else "Settings")
                                }
                            }
                        }
                    }
                }
            }

            item(key = "notification_settings_card") {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    PremiumSectionBanner(
                        icon = Icons.Filled.NotificationsActive,
                        title = if (isBn) "নোটিফিকেশন নিয়ন্ত্রণ" else "Notification Controls"
                    )

                    Surface(
                        shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = 0.5.dp
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            NotificationToggleRow(
                                icon = if (prefs.notificationsEnabled) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsOff,
                                iconTint = MaterialTheme.colorScheme.primary,
                                iconBg = MaterialTheme.colorScheme.primaryContainer,
                                title = if (isBn) "নোটিফিকেশন সিস্টেম চালু" else "Enable Notifications",
                                subtitle = if (isBn) "অফলাইন ও অনলাইন সব রিমাইন্ডার চালু/বন্ধ করুন" else "Master switch for all reminders and alerts",
                                checked = prefs.notificationsEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        requestPermissionIfNeeded { onToggleMasterNotifications(true) }
                                    } else {
                                        onToggleMasterNotifications(false)
                                    }
                                },
                                tag = "toggle_master_notifications"
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )

                            NotificationToggleRow(
                                icon = Icons.Filled.PieChart,
                                iconTint = finance.expense,
                                iconBg = finance.expenseContainer,
                                title = if (isBn) "বাজেট সতর্কতা (৮০% ও ১০০%)" else "Budget Alerts (80% & 100%)",
                                subtitle = if (isBn) "খরচ বাজেট সীমার কাছাকাছি বা বেশি হলে অ্যালার্ট" else "Instant alert when monthly or category budgets cross 80%",
                                checked = prefs.budgetAlertsEnabled && prefs.notificationsEnabled,
                                enabled = prefs.notificationsEnabled,
                                onCheckedChange = { onToggleBudgetAlerts(it) },
                                tag = "toggle_budget_alerts"
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )

                            NotificationToggleRow(
                                icon = Icons.Filled.Handshake,
                                iconTint = finance.receivable,
                                iconBg = finance.receivableContainer,
                                title = if (isBn) "ধার ফেরত ও পাওনা অ্যালার্ট" else "Loan Due-Date Reminders",
                                subtitle = if (isBn) "ফেরতের নির্ধারিত তারিখে স্বয়ংক্রিয় নোটিফিকেশন" else "Automatic alerts for personal loan due dates",
                                checked = prefs.loanDueAlertsEnabled && prefs.notificationsEnabled,
                                enabled = prefs.notificationsEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        requestPermissionIfNeeded { onToggleLoanDueAlerts(true) }
                                    } else {
                                        onToggleLoanDueAlerts(false)
                                    }
                                },
                                tag = "toggle_loan_alerts"
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )

                            NotificationToggleRow(
                                icon = Icons.Filled.SystemUpdate,
                                iconTint = MaterialTheme.colorScheme.primary,
                                iconBg = MaterialTheme.colorScheme.primaryContainer,
                                title = if (isBn) "নতুন ভার্সন আপডেট অ্যালার্ট" else "App Release Update Alerts",
                                subtitle = if (isBn) "নতুন ভার্সন রিলিজ হলে সাথে সাথে নোটিফিকেশন পাবেন" else "Get notified automatically when a new app version is released",
                                checked = prefs.updateAlertsEnabled && prefs.notificationsEnabled,
                                enabled = prefs.notificationsEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        requestPermissionIfNeeded { onToggleUpdateAlerts(true) }
                                    } else {
                                        onToggleUpdateAlerts(false)
                                    }
                                },
                                tag = "toggle_update_alerts"
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )

                            NotificationToggleRow(
                                icon = Icons.Filled.Alarm,
                                iconTint = WarmGold,
                                iconBg = finance.goldContainer,
                                title = if (isBn) "দৈনিক হিসাব রিমাইন্ডার" else "Daily Accounting Reminder",
                                subtitle = if (isBn) "প্রতিদিন নির্দিষ্ট সময়ে আজকের হিসাব লেখার স্মরণিকা" else "Daily check-in prompt to log today's entries",
                                checked = prefs.dailyReminderEnabled && prefs.notificationsEnabled,
                                enabled = prefs.notificationsEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        requestPermissionIfNeeded {
                                            onSetDailyReminder(true, prefs.dailyReminderHour, prefs.dailyReminderMinute)
                                        }
                                    } else {
                                        onSetDailyReminder(false, null, null)
                                    }
                                },
                                tag = "toggle_daily_reminder"
                            )

                            AnimatedVisibility(visible = prefs.dailyReminderEnabled && prefs.notificationsEnabled) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isBn) "রিমাইন্ডারের সময় নির্বাচন করুন:" else "Select reminder time:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(DAILY_TIME_PRESETS) { preset ->
                                            val selected = prefs.dailyReminderHour == preset.hour &&
                                                prefs.dailyReminderMinute == preset.minute
                                            FilterChip(
                                                selected = selected,
                                                onClick = {
                                                    onSetDailyReminder(true, preset.hour, preset.minute)
                                                },
                                                label = {
                                                    Text(if (isBn) preset.labelBn else preset.labelEn)
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        requestPermissionIfNeeded { onSendTestNotification() }
                                    },
                                    enabled = prefs.notificationsEnabled,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("send_test_notification_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = if (isBn) "টেস্ট নোটিফিকেশন পাঠান" else "Send Test Notification",
                                        fontWeight = FontWeight.SemiBold
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
private fun NotificationToggleRow(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (enabled) iconBg else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) iconTint else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ForestGreen
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}

@Composable
private fun NotificationAlertCard(
    item: InAppNotificationItem,
    isBn: Boolean,
    onClick: () -> Unit
) {
    val finance = LocalFinanceColors.current
    val (icon, tint, bg) = when (item.type) {
        NotificationAlertType.APP_UPDATE -> Triple(
            Icons.Filled.SystemUpdate,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
        NotificationAlertType.BUDGET_EXCEEDED -> Triple(
            Icons.Filled.Warning,
            finance.expense,
            finance.expenseContainer
        )
        NotificationAlertType.BUDGET_WARNING -> Triple(
            Icons.Filled.PieChart,
            WarmGold,
            finance.goldContainer
        )
        NotificationAlertType.LOAN_DUE -> Triple(
            Icons.Filled.Handshake,
            if (item.isUrgent) finance.expense else finance.receivable,
            if (item.isUrgent) finance.expenseContainer else finance.receivableContainer
        )
        NotificationAlertType.TASK_REMINDER -> Triple(
            Icons.Filled.EventNote,
            if (item.isUrgent) WarmGold else MaterialTheme.colorScheme.primary,
            if (item.isUrgent) finance.goldContainer else MaterialTheme.colorScheme.primaryContainer
        )
        NotificationAlertType.SHOP_DUE -> Triple(
            Icons.Filled.Storefront,
            finance.payable,
            finance.payableContainer
        )
    }

    val badgeLabel = when (item.type) {
        NotificationAlertType.APP_UPDATE -> if (isBn) "আপডেট" else "Update"
        NotificationAlertType.BUDGET_EXCEEDED -> if (isBn) "বাজেট অতিক্রম" else "Exceeded"
        NotificationAlertType.BUDGET_WARNING -> if (isBn) "বাজেট সতর্কতা" else "Budget"
        NotificationAlertType.LOAN_DUE -> if (isBn) "ধার-দেনা" else "Loan"
        NotificationAlertType.TASK_REMINDER -> if (isBn) "ডায়রি" else "Diary"
        NotificationAlertType.SHOP_DUE -> if (isBn) "দোকানের বাকি" else "Shop Due"
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (item.isUrgent) 1.3.dp else 1.dp,
            color = if (item.isUrgent) tint.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant
        ),
        shadowElevation = 0.8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("notification_alert_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = bg
                    ) {
                        Text(
                            text = badgeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = tint,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    if (item.isUrgent) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(tint)
                        )
                    }
                }
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
