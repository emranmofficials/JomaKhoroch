package com.example.presentation.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.example.R
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.LocalLanguage
import com.example.core.localization.LocalNumeralStyle
import com.example.core.localization.NumeralStyle
import com.example.core.localization.localizedBookName
import com.example.core.util.CurrencyFormatter
import com.example.core.util.DateUtils
import com.example.infrastructure.BackupExportResult
import com.example.infrastructure.BackupPreview
import com.example.infrastructure.PinSecurityManager
import com.example.presentation.components.ConfigureUpdateRepoDialog
import com.example.presentation.components.EmptyState
import com.example.presentation.components.PremiumHeader
import com.example.presentation.components.PremiumPageTitleRow
import com.example.presentation.components.PremiumSectionBanner
import com.example.presentation.state.JomaKhorochUiState
import com.example.presentation.state.SyncStatus
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LocalFinanceColors
import com.example.ui.theme.PlusJakartaSansFont
import com.example.ui.theme.SoftGold
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.WarmGold
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun MoreScreen(
    uiState: JomaKhorochUiState,
    onOpenBudgets: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenNotifications: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onOpenAppUpdate: () -> Unit,
    onSaveUpdateRepo: (String) -> Unit,
    onSyncNow: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit = {},
    onSetLanguage: (AppLanguage) -> Unit,
    onSetNumeralStyle: (NumeralStyle) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onEnablePin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onLockNow: () -> Unit,
    onSetBiometric: (Boolean) -> Unit,
    onUpdateOpeningBalance: (Long) -> Unit,
    onAddCategory: (String, String, String) -> Unit,
    onDeleteCategory: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onOpenDiary: () -> Unit = {},
    onOpenNotificationSettings: () -> Unit = onOpenNotifications,
    onUpdateProfile: (String, String) -> Unit = { _, _ -> },
    onUpdateProfilePhoto: (Uri?) -> Unit = {},
    onShowMessage: (String) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    BackHandler(enabled = onBack != null) {
        onBack?.invoke()
    }
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val context = LocalContext.current
    val biometricSupported = remember(context) { PinSecurityManager.isBiometricAvailable(context) }
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var pendingEnableBiometricAfterPin by remember { mutableStateOf(false) }
    var showPinManageDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAppInfoDialog by remember { mutableStateOf(false) }
    var showHelpSupportDialog by remember { mutableStateOf(false) }
    var showOpeningBalanceDialog by remember { mutableStateOf(false) }
    var showCategoriesDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }
    var showUpdateRepoDialog by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showNumeralMenu by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdateProfilePhoto(uri)
        }
    }

    val isPinEnabled = uiState.preferences.isPinEnabled
    val sync = uiState.cloudSync
    val appUpdate = uiState.appUpdate
    val availableRelease = appUpdate.availableRelease

    val creamPillBg = if (finance.isDark) Color(0xFF232C25) else Color(0xFFFBF3DF)
    val creamPillBorder = if (finance.isDark) WarmGold.copy(alpha = 0.35f) else WarmGold.copy(alpha = 0.45f)
    val iconGreen = if (finance.isDark) finance.income else ForestGreen
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PremiumHeader(
            onBackClick = onBack,
            notificationCount = uiState.notifications.size,
            hasUrgentNotification = uiState.notifications.any { it.isUrgent },
            onNotificationClick = onOpenNotifications
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. User Profile & Live Cloud Sync Card
            item(key = "settings_profile_card") {
                SettingsProfileCard(
                    uiState = uiState,
                    onPickPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onEditProfile = { showEditProfileDialog = true },
                    onSyncNow = onSyncNow,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 2. App Customization Section
            item(key = "settings_customization") {
                SettingsSectionCard(
                    icon = Icons.Filled.Palette,
                    title = if (isBn) "অ্যাপ কাস্টমাইজেশন" else "App Customization",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    // Row 1: Theme Selector with 3-icon segmented pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ColorfulPaletteBadge(modifier = Modifier.size(34.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "থিম নির্বাচন" else "Theme",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isBn) "লাইট / ডার্ক / সিস্টেম" else "Light / Dark / System",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        ThemeIconSegmentedToggle(
                            currentMode = uiState.preferences.themeMode,
                            onSelectMode = onSetThemeMode,
                            pillContainerColor = creamPillBg,
                            pillBorderColor = creamPillBorder
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        color = dividerColor
                    )

                    // Row 2: Language Dropdown Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLanguageMenu = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("settings_language_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = null,
                            tint = iconGreen,
                            modifier = Modifier.size(30.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "ভাষা নির্বাচন" else "Language",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBn) "অ্যাপের ভাষা পরিবর্তন করুন" else "Choose app language",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box {
                            Surface(
                                onClick = { showLanguageMenu = true },
                                shape = RoundedCornerShape(50),
                                color = creamPillBg,
                                border = BorderStroke(1.dp, creamPillBorder),
                                shadowElevation = 1.dp,
                                modifier = Modifier.testTag("settings_language_dropdown")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (uiState.preferences.language == AppLanguage.BANGLA) "বাংলা" else "English",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showLanguageMenu,
                                onDismissRequest = { showLanguageMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("বাংলা", fontWeight = FontWeight.SemiBold) },
                                    trailingIcon = {
                                        if (uiState.preferences.language == AppLanguage.BANGLA) {
                                            Icon(Icons.Filled.Check, contentDescription = null, tint = finance.income)
                                        }
                                    },
                                    onClick = {
                                        showLanguageMenu = false
                                        onSetLanguage(AppLanguage.BANGLA)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("English", fontWeight = FontWeight.SemiBold) },
                                    trailingIcon = {
                                        if (uiState.preferences.language == AppLanguage.ENGLISH) {
                                            Icon(Icons.Filled.Check, contentDescription = null, tint = finance.income)
                                        }
                                    },
                                    onClick = {
                                        showLanguageMenu = false
                                        onSetLanguage(AppLanguage.ENGLISH)
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        color = dividerColor
                    )

                    // Row 3: Numeral Format Dropdown Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showNumeralMenu = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("settings_numeral_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(iconGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "123",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PlusJakartaSansFont,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    letterSpacing = (-0.4).sp
                                ),
                                color = Color.White,
                                maxLines = 1
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "সংখ্যার ধরন" else "Number Format",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBn) "টাকা ও তারিখের সংখ্যার ধরন" else "Number display format",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box {
                            Surface(
                                onClick = { showNumeralMenu = true },
                                shape = RoundedCornerShape(50),
                                color = creamPillBg,
                                border = BorderStroke(1.dp, creamPillBorder),
                                shadowElevation = 1.dp,
                                modifier = Modifier.testTag("settings_numeral_dropdown")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (uiState.preferences.numeralStyle == NumeralStyle.BANGLA) {
                                            if (isBn) "বাংলা সংখ্যা" else "Bangla"
                                        } else {
                                            if (isBn) "ইংরেজি সংখ্যা" else "English"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showNumeralMenu,
                                onDismissRequest = { showNumeralMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (isBn) "বাংলা সংখ্যা (০১২৩)" else "Bangla Digits (০১২৩)",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    trailingIcon = {
                                        if (uiState.preferences.numeralStyle == NumeralStyle.BANGLA) {
                                            Icon(Icons.Filled.Check, contentDescription = null, tint = finance.income)
                                        }
                                    },
                                    onClick = {
                                        showNumeralMenu = false
                                        onSetNumeralStyle(NumeralStyle.BANGLA)
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (isBn) "ইংরেজি সংখ্যা (0123)" else "English Digits (0123)",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    trailingIcon = {
                                        if (uiState.preferences.numeralStyle == NumeralStyle.ENGLISH) {
                                            Icon(Icons.Filled.Check, contentDescription = null, tint = finance.income)
                                        }
                                    },
                                    onClick = {
                                        showNumeralMenu = false
                                        onSetNumeralStyle(NumeralStyle.ENGLISH)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Ledger & Categories Section ('খাতা ও ক্যাটাগরি')
            item(key = "settings_ledger_categories") {
                val openingBalanceFormatted = CurrencyFormatter.formatPoisha(
                    uiState.activeNotebook?.openingBalancePoisha ?: 0L,
                    numerals
                )
                val categoryCountFormatted = CurrencyFormatter.toLocalizedDigits(
                    uiState.categories.size.toString(),
                    numerals
                )
                val notificationsOn = uiState.preferences.notificationsEnabled

                SettingsSectionCard(
                    icon = Icons.Filled.AccountBalanceWallet,
                    title = if (isBn) "খাতা ও ক্যাটাগরি" else "Ledger & Categories",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    SettingsChevronRow(
                        icon = Icons.Filled.AccountBalanceWallet,
                        iconTint = iconGreen,
                        title = strings.openingBalance,
                        subtitle = if (isBn) "সক্রিয় খাতার শুরুর ব্যালেন্স নির্ধারণ করুন" else "Set starting balance for active book",
                        onClick = { showOpeningBalanceDialog = true },
                        tag = "more_opening_balance_item",
                        trailingContent = {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = creamPillBg,
                                border = BorderStroke(1.dp, creamPillBorder)
                            ) {
                                Text(
                                    text = openingBalanceFormatted,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        color = dividerColor
                    )

                    SettingsChevronRow(
                        icon = Icons.Filled.Category,
                        iconTint = iconGreen,
                        title = strings.categoriesManager,
                        subtitle = if (isBn) "আয় ও ব্যয়ের ক্যাটাগরি যোগ বা পরিবর্তন করুন" else "Add or manage income & expense categories",
                        onClick = { showCategoriesDialog = true },
                        tag = "more_categories_item",
                        trailingContent = {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = creamPillBg,
                                border = BorderStroke(1.dp, creamPillBorder)
                            ) {
                                Text(
                                    text = if (isBn) "$categoryCountFormatted টি" else categoryCountFormatted,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        color = dividerColor
                    )

                    SettingsChevronRow(
                        icon = Icons.Filled.NotificationsActive,
                        iconTint = iconGreen,
                        title = if (isBn) "নোটিফিকেশন ও রিমাইন্ডার" else "Notifications & Reminders",
                        subtitle = if (isBn) "বাজেট সতর্কতা, বাকি ও ডায়রি রিমাইন্ডার" else "Budget alerts, dues & diary reminders",
                        onClick = onOpenNotificationSettings,
                        tag = "more_notifications_item",
                        trailingContent = {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (notificationsOn) finance.incomeContainer else creamPillBg,
                                border = BorderStroke(
                                    1.dp,
                                    if (notificationsOn) finance.incomeBorder else creamPillBorder
                                )
                            ) {
                                Text(
                                    text = if (notificationsOn) {
                                        if (isBn) "চালু" else "On"
                                    } else {
                                        if (isBn) "বন্ধ" else "Off"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (notificationsOn) finance.income else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    )
                }
            }

            // 4. Security & Privacy Section
            item(key = "settings_security") {
                SettingsSectionCard(
                    icon = Icons.Filled.VerifiedUser,
                    title = if (isBn) "নিরাপত্তা ও গোপনীয়তা" else "Security & Privacy",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    // Row 1: 4-Digit PIN Lock
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isPinEnabled) {
                                    showPinManageDialog = true
                                } else {
                                    pendingEnableBiometricAfterPin = false
                                    showPinSetupDialog = true
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 13.dp)
                            .testTag("pin_lock_switch"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = iconGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "৪ ডিজিটের PIN Lock" else "4-Digit PIN Lock",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isPinEnabled) {
                                    if (isBn) "পিন সক্রিয় আছে • লক বা পরিবর্তন করুন" else "PIN active • Tap to lock or manage"
                                } else {
                                    if (isBn) "অ্যাপ লক করতে PIN সেট করুন" else "Set a 4-digit PIN to lock app"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (isPinEnabled) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = finance.incomeContainer,
                                border = BorderStroke(1.dp, finance.incomeBorder)
                            ) {
                                Text(
                                    text = if (isBn) "চালু" else "On",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = finance.income,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        color = dividerColor
                    )

                    // Row 2: Biometric Unlock with Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = null,
                            tint = iconGreen,
                            modifier = Modifier.size(30.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Biometric Unlock",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBn) "ফিঙ্গারপ্রিন্ট / ফেস আনলক" else "Fingerprint / Face unlock",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isPinEnabled && uiState.preferences.biometricEnabled,
                            onCheckedChange = { checked ->
                                if (!biometricSupported) {
                                    onShowMessage(
                                        if (isBn) "এই ডিভাইসে ফিঙ্গারপ্রিন্ট বা ফেস আনলক সাপোর্ট নেই"
                                        else "Biometric hardware not available on this device"
                                    )
                                } else if (checked && !isPinEnabled) {
                                    pendingEnableBiometricAfterPin = true
                                    showPinSetupDialog = true
                                } else {
                                    onSetBiometric(checked)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ForestGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.testTag("biometric_unlock_switch")
                        )
                    }
                }
            }

            // 4. Backup & Sync Section
            item(key = "settings_backup_sync") {
                val syncPillLabel = when {
                    !sync.isOnline -> if (isBn) "অফলাইন" else "Offline"
                    sync.syncStatus == SyncStatus.SYNCING -> if (isBn) "সিঙ্ক হচ্ছে" else "Syncing"
                    sync.syncStatus == SyncStatus.ERROR -> if (isBn) "ত্রুটি" else "Error"
                    sync.syncStatus == SyncStatus.OFFLINE_PENDING -> if (isBn) "অপেক্ষমাণ" else "Pending"
                    else -> if (isBn) "সফল" else "Synced"
                }
                val syncHealthy = sync.isOnline && sync.syncStatus != SyncStatus.ERROR && sync.syncStatus != SyncStatus.OFFLINE_PENDING
                val lastSyncSubtitle = sync.lastSyncedAtMillis?.let { ts ->
                    val formatted = DateUtils.formatDateTime(ts, language, numerals)
                    if (isBn) "শেষ সিঙ্ক: $formatted" else "Last sync: $formatted"
                } ?: if (isBn) "ক্লাউডে এখনই সিঙ্ক করতে ট্যাপ করুন" else "Tap to sync with cloud now"

                SettingsSectionCard(
                    icon = Icons.Filled.CloudDone,
                    title = if (isBn) "ব্যাকআপ ও সিঙ্ক" else "Backup & Sync",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    SettingsChevronRow(
                        icon = Icons.Filled.CloudUpload,
                        iconTint = iconGreen,
                        title = if (isBn) "ব্যাকআপ নিন" else "Create Backup",
                        subtitle = if (isBn) "আপনার হিসাব নিরাপদে সংরক্ষণ করুন" else "Save encrypted backup of your accounts",
                        onClick = onOpenBackup,
                        tag = "more_open_backup"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        color = dividerColor
                    )

                    SettingsChevronRow(
                        icon = Icons.Filled.CloudDownload,
                        iconTint = iconGreen,
                        title = if (isBn) "ব্যাকআপ রিস্টোর" else "Restore Backup",
                        subtitle = if (isBn) "সংরক্ষিত ডেটা পুনরুদ্ধার করুন" else "Restore previously saved backup data",
                        onClick = onOpenBackup,
                        tag = "more_restore_backup"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        color = dividerColor
                    )

                    // Row 3: Sync Status with mint checkmark pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onSyncNow)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("more_sync_now_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = null,
                            tint = iconGreen,
                            modifier = Modifier.size(30.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "সিঙ্ক স্ট্যাটাস" else "Sync Status",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = lastSyncSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            onClick = onSyncNow,
                            shape = RoundedCornerShape(50),
                            color = if (syncHealthy) finance.incomeContainer else finance.goldContainer,
                            border = BorderStroke(
                                1.dp,
                                if (syncHealthy) finance.incomeBorder else WarmGold.copy(alpha = 0.45f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                if (sync.syncStatus == SyncStatus.SYNCING) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = finance.income
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (syncHealthy) Icons.Filled.CheckCircle else Icons.Filled.CloudOff,
                                        contentDescription = null,
                                        tint = if (syncHealthy) finance.income else WarmGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = syncPillLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (syncHealthy) finance.income else if (finance.isDark) SoftGold else Color(0xFF7B5610)
                                )
                            }
                        }
                    }
                }
            }

            // 5. About App Section (App Info, Check Updates, Help & Support)
            item(key = "settings_about_app") {
                SettingsSectionCard(
                    icon = Icons.Filled.Info,
                    title = if (isBn) "অ্যাপ সম্পর্কিত" else "About App",
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    SettingsChevronRow(
                        icon = Icons.Filled.Info,
                        iconTint = iconGreen,
                        title = if (isBn) "অ্যাপের তথ্য" else "App Information",
                        subtitle = "JomaKhoroch v${appUpdate.currentVersion}",
                        onClick = { showAppInfoDialog = true },
                        tag = "settings_app_info_row"
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        color = dividerColor
                    )

                    // Row 2: Check for Updates with pill action button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !appUpdate.isChecking) {
                                if (availableRelease != null) onOpenAppUpdate() else onCheckForUpdates()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("check_updates_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = null,
                            tint = iconGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "আপডেট চেক" else "Check for Updates",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when {
                                    availableRelease != null -> if (isBn) {
                                        "নতুন ভার্সন v${availableRelease.versionName} পাওয়া গেছে!"
                                    } else {
                                        "New version v${availableRelease.versionName} ready!"
                                    }
                                    appUpdate.isUpToDate -> if (isBn) "আপনি সর্বশেষ ভার্সন ব্যবহার করছেন" else "You're on the latest version"
                                    else -> if (isBn) "নতুন ভার্সন আছে কিনা দেখুন" else "See if a newer version is available"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (availableRelease != null) finance.income else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            onClick = {
                                if (!appUpdate.isChecking) {
                                    if (availableRelease != null) onOpenAppUpdate() else onCheckForUpdates()
                                }
                            },
                            shape = RoundedCornerShape(50),
                            color = if (availableRelease != null) {
                                if (finance.isDark) MaterialTheme.colorScheme.primary else DeepEmerald
                            } else creamPillBg,
                            border = BorderStroke(
                                1.dp,
                                if (availableRelease != null) {
                                    if (finance.isDark) MaterialTheme.colorScheme.primary else WarmGold.copy(alpha = 0.65f)
                                } else creamPillBorder
                            ),
                            shadowElevation = 1.dp,
                            modifier = Modifier.testTag("install_update_button")
                        ) {
                            val activeUpdateFg = if (finance.isDark) MaterialTheme.colorScheme.onPrimary else Color.White
                            val activeUpdateIcon = if (finance.isDark) MaterialTheme.colorScheme.onPrimary else SoftGold
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                if (appUpdate.isChecking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = if (availableRelease != null) activeUpdateIcon else MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (availableRelease != null) Icons.Filled.Download else Icons.Filled.Sync,
                                        contentDescription = null,
                                        tint = if (availableRelease != null) activeUpdateIcon else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = when {
                                        appUpdate.isChecking -> if (isBn) "যাচাই..." else "Checking"
                                        availableRelease != null -> if (isBn) "আপডেট করুন" else "Update"
                                        else -> if (isBn) "চেক করুন" else "Check"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (availableRelease != null) activeUpdateFg else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        color = dividerColor
                    )

                    SettingsChevronRow(
                        icon = Icons.AutoMirrored.Filled.Help,
                        iconTint = iconGreen,
                        title = if (isBn) "সাহায্য ও সাপোর্ট" else "Help & Support",
                        subtitle = if (isBn) "প্রশ্নের উত্তর ও যোগাযোগ" else "FAQs and usage tips",
                        onClick = { showHelpSupportDialog = true },
                        tag = "settings_help_support_row"
                    )
                }
            }

            // 6. Sign Out Card
            item(key = "settings_sign_out") {
                Surface(
                    onClick = { showSignOutConfirmDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("sign_out_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = iconGreen,
                            modifier = Modifier.size(26.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "সাইন আউট করুন" else "Sign Out",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBn) "এই ডিভাইস থেকে অ্যাকাউন্ট লগ-আউট করুন" else "Sign out of your account on this device",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 7. Destructive Delete Account Card (exact blush red style from mockup)
            item(key = "settings_delete_account") {
                Surface(
                    onClick = { showDeleteAccountConfirmDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    color = finance.expenseContainer,
                    border = BorderStroke(1.dp, finance.expenseBorder),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("delete_account_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = finance.expense,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "অ্যাকাউন্ট ডিলিট করুন" else "Delete Account",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = finance.expense
                            )
                            Text(
                                text = if (isBn) "এই কাজটি অপরিবর্তনীয়!" else "This action is irreversible!",
                                style = MaterialTheme.typography.bodySmall,
                                color = finance.expense.copy(alpha = 0.85f)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = finance.expense,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = uiState.preferences.profileNameOverride.ifBlank { sync.userDisplayName },
            currentBusiness = uiState.preferences.profileBusinessName.ifBlank { sync.userEmail.orEmpty() },
            hasPhoto = uiState.preferences.profilePhotoPath.isNotBlank() && File(uiState.preferences.profilePhotoPath).exists(),
            onPickPhoto = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemovePhoto = { onUpdateProfilePhoto(null) },
            onSave = { name, business ->
                onUpdateProfile(name, business)
                showEditProfileDialog = false
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }

    if (showPinSetupDialog) {
        SetupPinDialog(
            onConfirm = { pin ->
                onEnablePin(pin)
                if (pendingEnableBiometricAfterPin && biometricSupported) {
                    onSetBiometric(true)
                }
                pendingEnableBiometricAfterPin = false
                showPinSetupDialog = false
            },
            onDismiss = {
                pendingEnableBiometricAfterPin = false
                showPinSetupDialog = false
            }
        )
    }

    if (showPinManageDialog) {
        AlertDialog(
            onDismissRequest = { showPinManageDialog = false },
            icon = {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            title = {
                Text(
                    text = if (isBn) "PIN লক পরিচালনা" else "Manage PIN Lock",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBn) "আপনার অ্যাপ ৪ ডিজিটের PIN দিয়ে সুরক্ষিত আছে।"
                        else "Your app is protected with a 4-digit PIN.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            showPinManageDialog = false
                            onLockNow()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lock_now_button")
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isBn) "এখনই অ্যাপ লক করুন" else "Lock App Now")
                    }
                    OutlinedButton(
                        onClick = {
                            showPinManageDialog = false
                            pendingEnableBiometricAfterPin = false
                            showPinSetupDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isBn) "নতুন PIN সেট করুন" else "Change PIN")
                    }
                    TextButton(
                        onClick = {
                            showPinManageDialog = false
                            onDisablePin()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isBn) "PIN লক বন্ধ করুন" else "Disable PIN Lock")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPinManageDialog = false }) {
                    Text(strings.close)
                }
            }
        )
    }

    if (showAppInfoDialog) {
        AlertDialog(
            onDismissRequest = { showAppInfoDialog = false },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.jomakhoroch_logo),
                    contentDescription = strings.appName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(96.dp)
                        .testTag("about_app_brand_logo")
                )
            },
            title = {
                Text(
                    text = if (isBn) "অ্যাপের তথ্য" else "App Information",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "JomaKhoroch v${appUpdate.currentVersion}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = strings.tagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()
                    val booksCount = CurrencyFormatter.toLocalizedDigits(uiState.activeNotebooks.size.toString(), numerals)
                    val txCount = CurrencyFormatter.toLocalizedDigits(uiState.transactions.size.toString(), numerals)
                    Text(
                        text = if (isBn) "সক্রিয় খাতা: $booksCount টি • বর্তমান খাতায় লেনদেন: $txCount টি"
                        else "Active books: $booksCount • Transactions in book: $txCount",
                        style = MaterialTheme.typography.bodySmall
                    )
                    val repoDisplay = appUpdate.configuredRepo.ifBlank {
                        if (isBn) "সেট করা নেই" else "Not configured"
                    }
                    Text(
                        text = if (isBn) "আপডেট সোর্স: $repoDisplay" else "Update source: $repoDisplay",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = {
                            showAppInfoDialog = false
                            showUpdateRepoDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("configure_update_repo_button")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isBn) "আপডেট রিপোজিটরি কনফিগার করুন" else "Configure Update Repository")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppInfoDialog = false }) {
                    Text(strings.close)
                }
            }
        )
    }

    if (showHelpSupportDialog) {
        AlertDialog(
            onDismissRequest = { showHelpSupportDialog = false },
            icon = {
                Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = ForestGreen)
            },
            title = {
                Text(
                    text = if (isBn) "সাহায্য ও সাপোর্ট" else "Help & Support",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HelpTipItem(
                        title = if (isBn) "ক্লাউড সিঙ্ক ও অফলাইন ব্যবহার" else "Cloud Sync & Offline Mode",
                        body = if (isBn) "ইন্টারনেট ছাড়াই হিসাব লিখুন—অনলাইনে এলেই আপনার Google অ্যাকাউন্টে স্বয়ংক্রিয়ভাবে সিঙ্ক হয়ে যাবে।"
                        else "Record entries offline—your data automatically syncs to your Google account when online."
                    )
                    HelpTipItem(
                        title = if (isBn) "এনক্রিপ্টেড ব্যাকআপ" else "Encrypted Backups",
                        body = if (isBn) "'ব্যাকআপ নিন' থেকে AES-256 পাসওয়ার্ড সুরক্ষিত ব্যাকআপ ফাইল তৈরি করে ফোনে বা ড্রাইভে রাখতে পারেন।"
                        else "Export AES-256 passphrase-protected backup files anytime and restore them on any device."
                    )
                    HelpTipItem(
                        title = if (isBn) "বাজেট ও রিপোর্ট" else "Budgets & Reports",
                        body = if (isBn) "মাসিক ও ক্যাটাগরি বাজেট সেট করলে ৮০% ও ১০০% খরচে স্বয়ংক্রিয় সতর্কতা পাবেন, এবং PDF/CSV রিপোর্ট এক্সপোর্ট করতে পারবেন।"
                        else "Set category budgets for automatic 80%/100% alerts and export monthly PDF/CSV reports."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpSupportDialog = false }) {
                    Text(strings.close)
                }
            }
        )
    }

    if (showOpeningBalanceDialog) {
        OpeningBalanceDialog(
            currentPoisha = uiState.activeNotebook?.openingBalancePoisha ?: 0L,
            onSave = { poisha ->
                onUpdateOpeningBalance(poisha)
                showOpeningBalanceDialog = false
            },
            onDismiss = { showOpeningBalanceDialog = false }
        )
    }

    if (showCategoriesDialog) {
        ManageCategoriesDialog(
            categories = uiState.categories,
            onAdd = onAddCategory,
            onDelete = onDeleteCategory,
            onDismiss = { showCategoriesDialog = false }
        )
    }

    if (showUpdateRepoDialog) {
        ConfigureUpdateRepoDialog(
            currentRepo = uiState.appUpdate.configuredRepo,
            onSave = onSaveUpdateRepo,
            onDismiss = { showUpdateRepoDialog = false }
        )
    }

    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = if (isBn) "সাইন আউট করবেন?" else "Sign out of cloud account?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBn) {
                        "সাইন আউট করলে ক্লাউড সিঙ্ক সাময়িকভাবে বন্ধ হবে। পুনরায় Google দিয়ে সাইন ইন করলেই আপনার সব হিসাব ফিরে পাবেন।"
                    } else {
                        "Cloud sync will pause until you sign in again with your Google account."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_sign_out_button")
                ) {
                    Text(if (isBn) "সাইন আউট" else "Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showDeleteAccountConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirmDialog = false },
            title = {
                Text(
                    text = if (isBn) "অ্যাকাউন্ট ও সমস্ত ডেটা মুছবেন?" else "Delete Account & Data?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = if (isBn) {
                        "সতর্কতা: এই অ্যাকশনটি অপরিবর্তনীয়। আপনার ক্লাউড ব্যাকআপ, সমস্ত সিঙ্ক করা খাতা, লেনদেন এবং অ্যাকাউন্ট স্থায়ীভাবে মুছে যাবে।"
                    } else {
                        "Warning: This action is permanent and cannot be undone. All your cloud backups, synced notebooks, transactions, and account will be permanently deleted."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountConfirmDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_delete_account_button")
                ) {
                    Text(if (isBn) "হ্যাঁ, সম্পূর্ণ মুছুন" else "Permanently Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirmDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun SettingsProfileCard(
    uiState: JomaKhorochUiState,
    onPickPhoto: () -> Unit,
    onEditProfile: () -> Unit,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val sync = uiState.cloudSync
    val isBn = language == AppLanguage.BANGLA

    val displayName = uiState.preferences.profileNameOverride.ifBlank {
        sync.userDisplayName.ifBlank {
            if (isBn) "জমাখরচ ব্যবহারকারী" else "JomaKhoroch User"
        }
    }
    val subtitleText = uiState.preferences.profileBusinessName.ifBlank {
        sync.userEmail.orEmpty()
    }
    val initial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "J"
    val photoPath = uiState.preferences.profilePhotoPath
    val photoFile = remember(photoPath) {
        if (photoPath.isNotBlank()) File(photoPath).takeIf { it.exists() } else null
    }

    val statusText = when {
        !sync.isOnline -> if (isBn) "অফলাইন মোড" else "Offline"
        sync.syncStatus == SyncStatus.SYNCING -> if (isBn) "সিঙ্ক হচ্ছে..." else "Syncing..."
        sync.syncStatus == SyncStatus.SYNCED -> if (isBn) "সিঙ্ক করা হয়েছে" else "Synced"
        sync.syncStatus == SyncStatus.OFFLINE_PENDING -> if (isBn) "সিঙ্ক অপেক্ষমাণ" else "Pending"
        sync.syncStatus == SyncStatus.ERROR -> if (isBn) "সিঙ্ক ত্রুটি" else "Sync Error"
        else -> if (isBn) "সিঙ্ক করা হয়েছে" else "Ready"
    }
    val statusTint = when {
        !sync.isOnline || sync.syncStatus == SyncStatus.OFFLINE_PENDING -> WarmGold
        sync.syncStatus == SyncStatus.ERROR -> MaterialTheme.colorScheme.error
        sync.syncStatus == SyncStatus.SYNCING -> MaterialTheme.colorScheme.primary
        else -> finance.income
    }
    val statusIcon = when {
        !sync.isOnline -> Icons.Filled.CloudOff
        sync.syncStatus == SyncStatus.SYNCING -> Icons.Filled.CloudSync
        sync.syncStatus == SyncStatus.ERROR -> Icons.Filled.SyncProblem
        else -> Icons.Filled.CheckCircle
    }

    val bookBadgeBg = if (finance.isDark) Color(0xFF283A1E) else Color(0xFFE4F2AC)
    val bookBadgeFg = if (finance.isDark) SoftGold else Color(0xFF2B3D12)
    val editBtnBg = if (finance.isDark) Color(0xFF232C25) else Color(0xFFFBF2DC)
    val editBtnBorder = if (finance.isDark) WarmGold.copy(alpha = 0.4f) else WarmGold.copy(alpha = 0.5f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar with white ring + circular camera badge
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .clickable(onClick = onPickPhoto)
                        .testTag("settings_profile_avatar")
                ) {
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .border(
                                width = 2.5.dp,
                                color = if (finance.isDark) finance.goldAccent.copy(alpha = 0.72f) else Color.White,
                                shape = CircleShape
                            )
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF22A047), DeepEmerald)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoFile != null) {
                            AsyncImage(
                                model = photoFile,
                                contentDescription = displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = PlusJakartaSansFont,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = SoftGold
                            )
                        }
                    }

                    // Bottom-right camera badge
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .size(27.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.PhotoCamera,
                                contentDescription = if (isBn) "ছবি পরিবর্তন" else "Change photo",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Name + Subtitle + Active Notebook Pill
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitleText.isNotBlank()) {
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Surface(
                        color = bookBadgeBg,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, WarmGold.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WorkspacePremium,
                                contentDescription = null,
                                tint = bookBadgeFg,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = localizedBookName(
                                    uiState.activeNotebook?.name,
                                    uiState.activeNotebook?.type,
                                    LocalLanguage.current
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = bookBadgeFg,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Edit Profile Warm Cream Button
                Surface(
                    onClick = onEditProfile,
                    shape = RoundedCornerShape(16.dp),
                    color = editBtnBg,
                    border = BorderStroke(1.dp, editBtnBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier.testTag("settings_edit_profile_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (isBn) "প্রোফাইল\nসম্পাদনা" else "Edit\nProfile",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

            // Footer: Last sync timestamp + live synced pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSyncNow)
                    .testTag("profile_sync_now_button"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val syncTimeText = sync.lastSyncedAtMillis?.let { ts ->
                    val dt = DateUtils.formatDateTime(ts, language, numerals)
                    if (isBn) "সর্বশেষ সিঙ্ক: $dt" else "Last sync: $dt"
                } ?: if (isBn) "অফলাইন ও ক্লাউড রেডি" else "Offline & Cloud Ready"

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = if (finance.isDark) finance.income else DeepEmerald,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = syncTimeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (sync.syncStatus == SyncStatus.SYNCING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(13.dp),
                            strokeWidth = 2.dp,
                            color = statusTint
                        )
                    } else {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = statusText,
                            tint = statusTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusTint
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val finance = LocalFinanceColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        // Connected Deep Emerald top header bar with warm gold icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = if (finance.isDark) {
                            listOf(Color(0xFF0E342A), Color(0xFF17493B))
                        } else {
                            listOf(DarkGreen, DeepEmerald, ForestGreen)
                        }
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SoftGold,
                    modifier = Modifier.size(21.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Connected surface card body
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

@Composable
private fun ColorfulPaletteBadge(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF0D7D4E),
                    Color(0xFF1F9D55),
                    Color(0xFF2196F3),
                    Color(0xFF1565C0),
                    Color(0xFF0D7D4E)
                ),
                center = center
            ),
            radius = radius,
            center = center
        )
        // Palette paint dots
        drawCircle(color = Color(0xFFFFD54F), radius = radius * 0.17f, center = Offset(center.x - radius * 0.32f, center.y - radius * 0.25f))
        drawCircle(color = Color(0xFFFF7043), radius = radius * 0.17f, center = Offset(center.x + radius * 0.26f, center.y - radius * 0.30f))
        drawCircle(color = Color(0xFF81D4FA), radius = radius * 0.16f, center = Offset(center.x - radius * 0.34f, center.y + radius * 0.22f))
        drawCircle(color = Color.White, radius = radius * 0.19f, center = Offset(center.x + radius * 0.26f, center.y + radius * 0.24f))
    }
}

@Composable
private fun ThemeIconSegmentedToggle(
    currentMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit,
    pillContainerColor: Color,
    pillBorderColor: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = pillContainerColor,
        border = BorderStroke(1.dp, pillBorderColor),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val finance = LocalFinanceColors.current
            val modes = listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM)
            modes.forEach { mode ->
                val selected = currentMode == mode
                val bgColor by animateColorAsState(
                    targetValue = when {
                        !selected -> Color.Transparent
                        finance.isDark -> MaterialTheme.colorScheme.primary
                        else -> DeepEmerald
                    },
                    animationSpec = tween(180),
                    label = "theme_toggle_bg"
                )
                val contentColor by animateColorAsState(
                    targetValue = when {
                        !selected -> MaterialTheme.colorScheme.onSurface
                        finance.isDark -> MaterialTheme.colorScheme.onPrimary
                        else -> Color.White
                    },
                    animationSpec = tween(180),
                    label = "theme_toggle_fg"
                )
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(bgColor)
                        .clickable { onSelectMode(mode) }
                        .testTag("theme_mode_${mode.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    when (mode) {
                        ThemeMode.LIGHT -> Icon(
                            imageVector = Icons.Filled.WbSunny,
                            contentDescription = "Light theme",
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        ThemeMode.DARK -> Icon(
                            imageVector = Icons.Filled.DarkMode,
                            contentDescription = "Dark theme",
                            tint = if (selected && !finance.isDark) SoftGold else contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        ThemeMode.SYSTEM -> Text(
                            text = "A",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PlusJakartaSansFont,
                                fontWeight = FontWeight.Bold
                            ),
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsChevronRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tag: String,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(28.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (trailingContent != null) {
            trailingContent()
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun HelpTipItem(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EditProfileDialog(
    currentName: String,
    currentBusiness: String,
    hasPhoto: Boolean,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isBn = LocalLanguage.current == AppLanguage.BANGLA
    var name by remember(currentName) { mutableStateOf(currentName) }
    var business by remember(currentBusiness) { mutableStateOf(currentBusiness) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "প্রোফাইল সম্পাদনা" else "Edit Profile",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBn) "আপনার নাম" else "Your Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = business,
                    onValueChange = { business = it },
                    label = { Text(if (isBn) "প্রতিষ্ঠান / পরিচিতি (ঐচ্ছিক)" else "Business / Subtitle (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPickPhoto,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isBn) "ছবি বাছুন" else "Pick Photo", maxLines = 1)
                    }
                    if (hasPhoto) {
                        TextButton(
                            onClick = onRemovePhoto,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(if (isBn) "ছবি মুছুন" else "Remove")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, business) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
private fun SetupPinDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.setPinTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = pin1,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) pin1 = it; errorMsg = null },
                    label = { Text(strings.setPinTitle) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = pin2,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) pin2 = it; errorMsg = null },
                    label = { Text(strings.confirmPinTitle) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (pin1.length != 4) {
                    errorMsg = "4 digits required"
                } else if (pin1 != pin2) {
                    errorMsg = strings.wrongPin
                } else {
                    onConfirm(pin1)
                }
            }) { Text(strings.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}

@Composable
fun OpeningBalanceDialog(
    currentPoisha: Long,
    onSave: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var text by remember { mutableStateOf(CurrencyFormatter.poishaToPlainInput(currentPoisha)) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.openingBalance, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; errorMsg = null },
                    label = { Text("${strings.openingBalance} (৳)") },
                    prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                errorMsg?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val poisha = if (text.isBlank()) 0L else CurrencyFormatter.parseTakaToPoisha(text)
                if (poisha == null || poisha < 0L) {
                    errorMsg = strings.invalidAmountError
                } else {
                    onSave(poisha)
                }
            }) { Text(strings.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}

@Composable
private fun ManageCategoriesDialog(
    categories: List<com.example.data.local.entity.CategoryEntity>,
    onAdd: (String, String, String) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    var showAdd by remember { mutableStateOf(false) }
    var addType by remember { mutableStateOf("EXPENSE") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.categoriesManager, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { addType = "INCOME"; showAdd = true }, modifier = Modifier.weight(1f)) {
                        Text("+ ${strings.incomeLabel}")
                    }
                    OutlinedButton(onClick = { addType = "EXPENSE"; showAdd = true }, modifier = Modifier.weight(1f)) {
                        Text("+ ${strings.expenseLabel}")
                    }
                }
                LazyColumn(modifier = Modifier.height(260.dp)) {
                    items(categories, key = { it.id }) { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(if (language == AppLanguage.BANGLA) cat.nameBn else cat.nameEn, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(cat.type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (!cat.isDefault) {
                                IconButton(onClick = { onDelete(cat.id) }) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = strings.delete, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(strings.close) } }
    )

    if (showAdd) {
        AddCategoryDialog(
            type = addType,
            onConfirm = { bn, en ->
                onAdd(bn, en, addType)
                showAdd = false
            },
            onDismiss = { showAdd = false }
        )
    }
}

@Composable
fun BackupRestoreScreen(
    uiState: JomaKhorochUiState,
    onBuildBackupBytes: suspend (String) -> Result<BackupExportResult>,
    onLogBackupExport: (String, Int, String, Int) -> Unit,
    onSaveLocalSnapshot: (String) -> Unit,
    onInspectBackup: suspend (ByteArray, String) -> Result<BackupPreview>,
    onConfirmRestore: (BackupPreview, String, () -> Unit) -> Unit,
    onMessage: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn = language == AppLanguage.BANGLA

    var passphrase by remember { mutableStateOf("") }
    var pendingExport by remember { mutableStateOf<BackupExportResult?>(null) }
    var pendingRestorePreview by remember { mutableStateOf<Pair<String, BackupPreview>?>(null) }

    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val export = pendingExport
        if (uri != null && export != null) {
            scope.launch {
                val res = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(export.encryptedBytes)
                        out.flush()
                    } ?: error("Cannot write backup file")
                }
                if (res.isSuccess) {
                    val fileName = "jomakhoroch_backup_${System.currentTimeMillis()}.jkbackup"
                    onLogBackupExport(fileName, export.recordCount, export.checksumSha256, export.schemaVersion)
                    onMessage(strings.savedSuccessfully)
                } else {
                    onMessage(res.exceptionOrNull()?.message ?: "Export failed")
                }
                pendingExport = null
            }
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val readRes = runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: error("Cannot read backup file")
                }
                readRes.onSuccess { bytes ->
                    onInspectBackup(bytes, passphrase)
                        .onSuccess { preview ->
                            pendingRestorePreview = (uri.lastPathSegment ?: "backup.jkbackup") to preview
                        }
                        .onFailure {
                            onMessage(it.message ?: "Invalid backup or wrong passphrase")
                        }
                }.onFailure {
                    onMessage(it.message ?: "Failed to read file")
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PremiumHeader(onBackClick = onBack)

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(key = "backup_title") {
                PremiumPageTitleRow(
                    icon = Icons.Filled.Backup,
                    title = strings.backupRestore,
                    subtitle = if (isBn) "AES-256-GCM পাসওয়ার্ড-সুরক্ষিত ব্যাকআপ ফাইল" else "AES-256-GCM passphrase-protected backups"
                )
            }

            item(key = "backup_actions_card") {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    PremiumSectionBanner(
                        icon = Icons.Filled.Shield,
                        title = strings.createEncryptedBackup
                    )
                    Card(
                        shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = passphrase,
                                onValueChange = { passphrase = it },
                                label = {
                                    Text(
                                        if (isBn) "${strings.backupPassphrase} (কমপক্ষে ৪ অক্ষর)"
                                        else "${strings.backupPassphrase} (min 4 chars)"
                                    )
                                },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("backup_passphrase_input")
                            )

                            Button(
                                onClick = {
                                    if (passphrase.length < 4) {
                                        onMessage(
                                            if (isBn) "পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে"
                                            else "Passphrase must be at least 4 characters"
                                        )
                                        return@Button
                                    }
                                    scope.launch {
                                        onBuildBackupBytes(passphrase)
                                            .onSuccess { result ->
                                                pendingExport = result
                                                exportFileLauncher.launch("jomakhoroch_${System.currentTimeMillis()}.jkbackup")
                                            }
                                            .onFailure {
                                                onMessage(
                                                    it.message ?: if (isBn) "ব্যাকআপ তৈরি ব্যর্থ হয়েছে" else "Backup failed"
                                                )
                                            }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("export_backup_button")
                            ) {
                                Icon(
                                    Icons.Filled.Backup,
                                    contentDescription = null,
                                    tint = if (LocalFinanceColors.current.isDark) MaterialTheme.colorScheme.onPrimary else SoftGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(strings.createEncryptedBackup, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (passphrase.length < 4) {
                                        onMessage(
                                            if (isBn) "পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে"
                                            else "Passphrase must be at least 4 characters"
                                        )
                                        return@OutlinedButton
                                    }
                                    onSaveLocalSnapshot(passphrase)
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(strings.autoLocalSnapshot, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (passphrase.isBlank()) {
                                        onMessage(
                                            if (isBn) "আগে ব্যাকআপ পাসওয়ার্ড লিখুন"
                                            else "Enter backup passphrase first"
                                        )
                                        return@OutlinedButton
                                    }
                                    importFileLauncher.launch(arrayOf("*/*"))
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("restore_backup_button")
                            ) {
                                Icon(Icons.Filled.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(strings.restoreBackup, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item(key = "backup_history_banner") {
                PremiumSectionBanner(
                    icon = Icons.Filled.Restore,
                    title = strings.backupHistory
                )
            }

            if (uiState.backupHistory.isEmpty()) {
                item(key = "backup_history_empty") {
                    EmptyState(
                        icon = Icons.Filled.Backup,
                        title = if (isBn) "এখনো কোনো ব্যাকআপ তৈরি করা হয়নি" else "No backups yet",
                        subtitle = if (isBn) "পাসওয়ার্ড দিয়ে একটি এনক্রিপ্টেড ব্যাকআপ সংরক্ষণ করুন।" else "Create your first passphrase-protected backup above."
                    )
                }
            } else {
                items(uiState.backupHistory, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "${item.backupType} • ${item.fileName}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${DateUtils.formatDateTime(item.createdAt, language, numerals)} • v${item.schemaVersion} • ${CurrencyFormatter.toLocalizedDigits(item.recordCount.toString(), numerals)} ${if (isBn) "রেকর্ড" else "records"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    pendingRestorePreview?.let { (fileName, preview) ->
        AlertDialog(
            onDismissRequest = { pendingRestorePreview = null },
            title = { Text(strings.restoreConfirmTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(strings.restoreConfirmDesc, style = MaterialTheme.typography.bodyMedium)
                    HorizontalDivider()
                    val fmt: (Int) -> String = { CurrencyFormatter.toLocalizedDigits(it.toString(), numerals) }
                    Text(if (isBn) "স্কিমা ভার্সন: v${fmt(preview.schemaVersion)}" else "Schema v${preview.schemaVersion}")
                    Text(
                        if (isBn) "খাতা: ${fmt(preview.notebookCount)} • লেনদেন: ${fmt(preview.transactionCount)}"
                        else "Books: ${preview.notebookCount} • Transactions: ${preview.transactionCount}"
                    )
                    Text(
                        if (isBn) "দোকান: ${fmt(preview.shopCount)} • ধার-দেনা: ${fmt(preview.loanCount)}"
                        else "Shops: ${preview.shopCount} • Loans: ${preview.loanCount}"
                    )
                    Text(
                        if (isBn) "নোট: ${fmt(preview.noteCount)} • কাজ: ${fmt(preview.taskCount)}"
                        else "Notes: ${preview.noteCount} • Tasks: ${preview.taskCount}"
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onConfirmRestore(preview, fileName) {
                        pendingRestorePreview = null
                    }
                }) { Text(strings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestorePreview = null }) { Text(strings.cancel) }
            }
        )
    }
}
