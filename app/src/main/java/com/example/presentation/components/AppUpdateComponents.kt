package com.example.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.LocalLanguage
import com.example.core.localization.LocalNumeralStyle
import com.example.core.util.DateUtils
import com.example.infrastructure.AppReleaseInfo
import com.example.presentation.state.AppUpdateUiState
import com.example.ui.theme.LocalFinanceColors
import com.example.ui.theme.WarmGold

@Composable
fun AppUpdateDashboardBanner(
    currentVersion: String,
    release: AppReleaseInfo,
    onViewUpdate: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = finance.incomeContainer,
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onViewUpdate)
            .testTag("dashboard_update_banner")
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
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (isBn) "নতুন আপডেট পাওয়া গেছে (v${release.versionName})"
                    else "New Update Available (v${release.versionName})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isBn) "বর্তমান v$currentVersion • ডাউনলোড ও আপডেট করতে ট্যাপ করুন"
                    else "Current v$currentVersion • Tap to download & update",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onViewUpdate)
                    .testTag("dashboard_update_now_button")
            ) {
                Text(
                    text = if (isBn) "আপডেট" else "Update",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("dashboard_update_dismiss_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = LocalAppStrings.current.close,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AppUpdateSettingsSection(
    appUpdate: AppUpdateUiState,
    onCheckForUpdates: () -> Unit,
    onOpenUpdateDialog: () -> Unit,
    onConfigureRepoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA
    val availableRelease = appUpdate.availableRelease
    val hasUpdate = availableRelease != null

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (hasUpdate) WarmGold else MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = if (isBn) "অ্যাপ আপডেট ও ভার্সন" else "App Updates & Version",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Surface(
                color = when {
                    hasUpdate -> finance.goldContainer
                    appUpdate.isUpToDate -> finance.incomeContainer
                    else -> MaterialTheme.colorScheme.primaryContainer
                },
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = when {
                            hasUpdate -> Icons.Filled.NewReleases
                            appUpdate.isUpToDate -> Icons.Filled.CheckCircle
                            else -> Icons.Filled.SystemUpdate
                        },
                        contentDescription = null,
                        tint = when {
                            hasUpdate -> WarmGold
                            appUpdate.isUpToDate -> finance.income
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = when {
                            availableRelease != null -> if (isBn) "v${availableRelease.versionName} নতুন!" else "v${availableRelease.versionName} New"
                            appUpdate.isUpToDate -> if (isBn) "সর্বশেষ ভার্সন" else "Up to date"
                            else -> "v${appUpdate.currentVersion}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            hasUpdate -> WarmGold
                            appUpdate.isUpToDate -> finance.income
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.dp,
                if (hasUpdate) WarmGold.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
            ),
            shadowElevation = 0.5.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (hasUpdate) finance.goldContainer else MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasUpdate) Icons.Filled.NewReleases else Icons.Filled.SystemUpdate,
                            contentDescription = null,
                            tint = if (hasUpdate) WarmGold else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (isBn) "বর্তমান ভার্সন: v${appUpdate.currentVersion}"
                            else "Installed Version: v${appUpdate.currentVersion}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val repoDisplay = appUpdate.configuredRepo.ifBlank {
                            if (isBn) "রিলিজ রিপোজিটরি সেট করা নেই" else "Release repository not linked"
                        }
                        Text(
                            text = if (isBn) "সোর্স: $repoDisplay" else "Source: $repoDisplay",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val metaLine = appUpdate.statusMessage ?: appUpdate.lastCheckedAtMillis?.let { ts ->
                            val dt = DateUtils.formatDateTime(ts, language, numerals)
                            if (isBn) "সর্বশেষ যাচাই: $dt" else "Last checked: $dt"
                        } ?: if (isBn) {
                            "নতুন ভার্সন রিলিজ হলে স্বয়ংক্রিয় নোটিফিকেশন পাবেন"
                        } else {
                            "Automatic alerts when a new version is published"
                        }
                        Text(
                            text = metaLine,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasUpdate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onConfigureRepoClick,
                        modifier = Modifier.testTag("configure_update_repo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = if (isBn) "রিপোজিটরি পরিবর্তন" else "Configure repository",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (availableRelease != null) {
                    Button(
                        onClick = onOpenUpdateDialog,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("install_update_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "v${availableRelease.versionName} ডাউনলোড ও আপডেট করুন"
                            else "Download & Update to v${availableRelease.versionName}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onCheckForUpdates,
                    enabled = !appUpdate.isChecking,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("check_updates_button")
                ) {
                    if (appUpdate.isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (appUpdate.isChecking) {
                            if (isBn) "আপডেট যাচাই হচ্ছে..." else "Checking for updates..."
                        } else {
                            if (isBn) "নতুন আপডেট চেক করুন" else "Check for Updates"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun AppUpdateDetailsDialog(
    currentVersion: String,
    release: AppReleaseInfo,
    onDownloadUpdate: (String) -> Unit,
    onOpenReleasePage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val language = LocalLanguage.current
    val finance = LocalFinanceColors.current
    val isBn = language == AppLanguage.BANGLA

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(finance.goldContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isBn) "নতুন আপডেট প্রস্তুত!" else "New Update Ready!",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Surface(
                    color = finance.goldContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "v$currentVersion  →  v${release.versionName}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarmGold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (release.releaseTitle.isNotBlank()) {
                    Text(
                        text = release.releaseTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                val notes = release.releaseNotes.ifBlank {
                    if (isBn) "নতুন ফিচার, পারফরম্যান্স উন্নতি এবং বাগ ফিক্স যুক্ত করা হয়েছে।"
                    else "Includes new features, performance improvements, and bug fixes."
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Text(
                    text = if (isBn) {
                        "নিরাপদ আপডেটের জন্য ব্রাউজারে অফিসিয়াল ডাউনলোড পেজ খোলা হবে। আপনার কোনো হিসাব বা ডাটা মুছে যাবে না।"
                    } else {
                        "Opens the official release download in your browser safely. Your ledger data will remain intact."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDownloadUpdate(release.apkDownloadUrl)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("dialog_download_update_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(if (isBn) "আপডেট ডাউনলোড" else "Download Update")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = { onOpenReleasePage(release.releasePageUrl) },
                    modifier = Modifier.testTag("dialog_release_page_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isBn) "রিলিজ পেজ" else "Release Page")
                }
                TextButton(onClick = onDismiss) {
                    Text(LocalAppStrings.current.close)
                }
            }
        }
    )
}

@Composable
fun ConfigureUpdateRepoDialog(
    currentRepo: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val language = LocalLanguage.current
    val strings = LocalAppStrings.current
    val isBn = language == AppLanguage.BANGLA
    var repoText by remember(currentRepo) { mutableStateOf(currentRepo) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "GitHub রিলিজ রিপোজিটরি" else "GitHub Release Repository",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isBn) {
                        "যে GitHub রিপোজিটরিতে নতুন ভার্সন রিলিজ হয় তা লিখুন (যেমন: username/jomakhoroch বা সম্পূর্ণ GitHub লিংক)।"
                    } else {
                        "Enter the GitHub repository where new releases are published (e.g., username/jomakhoroch or full GitHub URL)."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = repoText,
                    onValueChange = { repoText = it },
                    label = { Text(if (isBn) "রিপোজিটরি (owner/repo)" else "Repository (owner/repo)") },
                    placeholder = { Text("username/jomakhoroch") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("update_repo_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(repoText)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_update_repo_button")
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
