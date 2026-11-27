package com.example.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.example.ui.theme.CanopyUrgentBadge
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LocalFinanceColors
import com.example.ui.theme.PlusJakartaSansFont
import com.example.ui.theme.SoftGold
import com.example.ui.theme.WarmGold

/**
 * Branded Deep Emerald curved canopy header with Warm Gold bottom accent line,
 * wallet brand emblem, notification badge, and optional settings / back controls.
 */
@Composable
fun PremiumHeader(
    modifier: Modifier = Modifier,
    notificationCount: Int = 0,
    hasUrgentNotification: Boolean = false,
    onNotificationClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    subtitleOverride: String? = null,
    expandedContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val bgColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(finance.canopyTop, finance.canopyMid, finance.canopyBottom)
                )
            )
    ) {
        // Subtle tonal arcs and crisp Warm Gold bottom curve sweep
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.045f),
                radius = size.width * 0.44f,
                center = Offset(size.width * 0.88f, size.height * 0.14f)
            )
            drawCircle(
                color = WarmGold.copy(alpha = 0.06f),
                radius = size.width * 0.28f,
                center = Offset(size.width * 0.12f, size.height * 0.82f)
            )

            // Bottom curved cutout into Warm Ivory / Dark background + gold rim
            val curveHeight = 20.dp.toPx()
            val goldBand = Path().apply {
                moveTo(0f, size.height - curveHeight - 3.dp.toPx())
                quadraticTo(
                    size.width * 0.5f,
                    size.height + curveHeight * 0.35f,
                    size.width,
                    size.height - curveHeight - 3.dp.toPx()
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(
                path = goldBand,
                brush = Brush.horizontalGradient(
                    colors = listOf(WarmGold, SoftGold, WarmGold)
                )
            )

            val bgCutout = Path().apply {
                moveTo(0f, size.height - curveHeight + 1.5.dp.toPx())
                quadraticTo(
                    size.width * 0.5f,
                    size.height + curveHeight * 0.55f,
                    size.width,
                    size.height - curveHeight + 1.5.dp.toPx()
                )
                lineTo(size.width, size.height + 2f)
                lineTo(0f, size.height + 2f)
                close()
            }
            drawPath(path = bgCutout, color = bgColor)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 26.dp)
        ) {
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
                    if (onBackClick != null) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable(role = Role.Button, onClick = onBackClick)
                                .testTag("header_back_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.14f),
                                shape = CircleShape,
                                border = BorderStroke(0.8.dp, WarmGold.copy(alpha = 0.45f)),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = strings.close,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.jomakhoroch_ic),
                            contentDescription = strings.appName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .testTag("header_brand_logo")
                        )
                    }

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
                            text = subtitleOverride ?: strings.tagline,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (onNotificationClick != null) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable(role = Role.Button, onClick = onNotificationClick)
                                .testTag("dashboard_notifications_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (notificationCount > 0) Icons.Filled.NotificationsActive else Icons.Filled.Notifications,
                                contentDescription = if (LocalLanguage.current == AppLanguage.BANGLA) "নোটিফিকেশন" else "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(23.dp)
                            )
                            if (notificationCount > 0) {
                                Surface(
                                    color = if (hasUrgentNotification) CanopyUrgentBadge else WarmGold,
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 6.dp, end = 6.dp)
                                        .size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = CurrencyFormatter.toLocalizedDigits(
                                                notificationCount.coerceAtMost(9).let {
                                                    if (notificationCount > 9) "9+" else it.toString()
                                                },
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
                    }

                    if (onSettingsClick != null) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable(role = Role.Button, onClick = onSettingsClick)
                                .testTag("header_settings_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = strings.navMore,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            if (expandedContent != null) {
                Spacer(Modifier.height(14.dp))
                expandedContent()
            }
        }
    }
}

/**
 * Standard screen title row placed immediately below [PremiumHeader] on sub-screens.
 */
@Composable
fun PremiumPageTitleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

/**
 * Deep Emerald section header banner with icon, title, and optional outlined action pill
 * (matches the reference Accounts, Diary, and Settings sections).
 */
@Composable
fun PremiumSectionBanner(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    actionTag: String? = null
) {
    val finance = LocalFinanceColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
        color = finance.sectionBannerBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
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
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = finance.sectionBannerContent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (actionLabel != null && onActionClick != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onActionClick)
                        .let { m -> if (actionTag != null) m.testTag(actionTag) else m }
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

/**
 * Hero Deep Emerald Balance Card with Warm Gold corner sweep, subtle vector bar motif,
 * and Hide/Show balance toggle.
 */
@Composable
fun FinancialBalanceCard(
    balancePoisha: Long,
    hidden: Boolean,
    onToggleHide: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current

    val finance = LocalFinanceColors.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_balance_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (finance.isDark) Color(0xFF0D3A2D) else DeepEmerald),
        border = BorderStroke(1.2.dp, WarmGold.copy(alpha = if (finance.isDark) 0.62f else 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (finance.isDark) 4.dp else 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = if (finance.isDark) {
                            listOf(Color(0xFF115740), Color(0xFF0B3A2C), Color(0xFF062119))
                        } else {
                            listOf(Color(0xFF0A4E3B), DeepEmerald, DarkGreen)
                        }
                    )
                )
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                // Subtle ascending bars motif on the right
                val barWidth = 10.dp.toPx()
                val gap = 6.dp.toPx()
                val baseX = size.width - 96.dp.toPx()
                val baseY = size.height - 26.dp.toPx()
                val heights = listOf(18f, 28f, 38f, 50f, 64f)
                heights.forEachIndexed { idx, h ->
                    val barH = h.dp.toPx().coerceAtMost(size.height * 0.55f)
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.08f),
                        topLeft = Offset(baseX + idx * (barWidth + gap), baseY - barH),
                        size = Size(barWidth, barH),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }

                // Warm Gold corner ribbon sweep
                val ribbon = Path().apply {
                    moveTo(size.width * 0.55f, size.height)
                    quadraticTo(
                        size.width * 0.85f,
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
                        colors = listOf(WarmGold.copy(alpha = 0.72f), SoftGold.copy(alpha = 0.92f))
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Wallet Visual Badge
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ForestGreen.copy(alpha = 0.85f),
                    border = BorderStroke(1.2.dp, SoftGold.copy(alpha = 0.7f)),
                    modifier = Modifier.size(62.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = null,
                            tint = SoftGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label ?: strings.cashBalance,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.86f)
                    )
                    Spacer(Modifier.height(2.dp))
                    AnimatedContent(
                        targetState = hidden,
                        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                        label = "hero_balance_anim"
                    ) { isHidden ->
                        val hiddenDesc = if (language == AppLanguage.BANGLA) "ব্যালেন্স গোপন করা আছে" else "Balance hidden"
                        Text(
                            text = if (isHidden) "৳ ••••••"
                            else CurrencyFormatter.formatPoisha(balancePoisha, numerals),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = PlusJakartaSansFont,
                                fontSize = 31.sp,
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
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.14f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onToggleHide)
                            .testTag("toggle_hide_balance_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (hidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (language == AppLanguage.BANGLA) "ব্যালেন্স দেখান বা লুকান" else "Toggle balance visibility",
                                tint = SoftGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (language == AppLanguage.BANGLA) {
                                    if (hidden) "ব্যালেন্স দেখান" else "দেখান / লুকান"
                                } else {
                                    if (hidden) "Show Balance" else "Show / Hide"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.92f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tinted Income or Expense metric card with circular directional icon badge and optional comparison line.
 */
@Composable
fun IncomeExpenseCard(
    title: String,
    amountPoisha: Long,
    isIncome: Boolean,
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
    deltaNote: String? = null,
    onClick: (() -> Unit)? = null
) {
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val accent = if (isIncome) finance.income else finance.expense
    val container = if (isIncome) finance.incomeContainer else finance.expenseContainer
    val borderColor = if (isIncome) finance.incomeBorder else finance.expenseBorder

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
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
                        text = if (hidden) "৳ ••••" else CurrencyFormatter.formatPoisha(amountPoisha, numerals),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PlusJakartaSansFont,
                            fontWeight = FontWeight.Bold
                        ),
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!deltaNote.isNullOrBlank()) {
                Surface(
                    color = accent.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = deltaNote,
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * Reusable Financial KPI Summary Card (for Shop Due, Receivable, Payable, Notebook count, etc.)
 */
@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val finance = LocalFinanceColors.current
    val bgColor = if (highlighted) {
        if (finance.isDark) Color(0xFF123F32) else DeepEmerald
    } else containerColor
    val titleColor = if (highlighted) Color.White.copy(alpha = 0.88f) else MaterialTheme.colorScheme.onSurfaceVariant
    val valueColor = if (highlighted) Color.White else MaterialTheme.colorScheme.onSurface
    val subColor = if (highlighted) SoftGold else accentColor
    val border = if (highlighted) BorderStroke(1.2.dp, WarmGold.copy(alpha = if (finance.isDark) 0.68f else 0.6f))
    else BorderStroke(1.dp, accentColor.copy(alpha = if (finance.isDark) 0.34f else 0.24f))

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlighted) 2.dp else 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (highlighted) Color.White.copy(alpha = 0.16f) else accentColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (highlighted) SoftGold else accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PlusJakartaSansFont,
                        fontWeight = FontWeight.Bold
                    ),
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = subColor,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (highlighted) SoftGold else accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Clean square/rounded Quick Action Tile matching Dashboard and Pocket Diary mockups.
 */
@Composable
fun QuickActionTile(
    label: String,
    icon: ImageVector,
    iconBgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.White,
    cardContainerColor: Color = MaterialTheme.colorScheme.surface,
    tag: String? = null
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .let { if (tag != null) it.testTag(tag) else it },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(21.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 34.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
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

/**
 * Clean Ledger Transaction List Row with circular category icon, note/time/method subtitle, and bold +/- amount.
 */
@Composable
fun TransactionListItem(
    tx: LedgerTransactionEntity,
    category: CategoryEntity?,
    shop: ShopEntity?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hidden: Boolean = false
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val txType = LedgerTransactionType.valueOf(tx.type)

    val badgeColor: Color
    val badgeBg: Color
    val icon: ImageVector
    val signPrefix: String
    val typeLabel: String

    when (txType) {
        LedgerTransactionType.INCOME -> {
            badgeColor = finance.income
            badgeBg = finance.incomeContainer
            icon = iconForKey(category?.iconKey ?: "payments")
            signPrefix = "+"
            typeLabel = if (language == AppLanguage.BANGLA) category?.nameBn ?: strings.incomeLabel
            else category?.nameEn ?: strings.incomeLabel
        }
        LedgerTransactionType.EXPENSE -> {
            badgeColor = finance.expense
            badgeBg = finance.expenseContainer
            icon = iconForKey(category?.iconKey ?: "receipt")
            signPrefix = "-"
            typeLabel = if (language == AppLanguage.BANGLA) category?.nameBn ?: strings.expenseLabel
            else category?.nameEn ?: strings.expenseLabel
        }
        LedgerTransactionType.SHOP_PURCHASE -> {
            badgeColor = finance.payable
            badgeBg = finance.payableContainer
            icon = Icons.Filled.Storefront
            signPrefix = "-"
            typeLabel = shop?.name ?: strings.shopDueLabel
        }
        LedgerTransactionType.SHOP_DUE_PAYMENT -> {
            badgeColor = finance.income
            badgeBg = finance.incomeContainer
            icon = Icons.Filled.AccountBalanceWallet
            signPrefix = "-"
            typeLabel = "${shop?.name ?: strings.shopLabel} (${strings.recordPayment})"
        }
        LedgerTransactionType.LENDING_GIVEN -> {
            badgeColor = finance.receivable
            badgeBg = finance.receivableContainer
            icon = Icons.Filled.ArrowUpward
            signPrefix = "-"
            typeLabel = strings.moneyGiven
        }
        LedgerTransactionType.LENDING_REPAYMENT -> {
            badgeColor = finance.income
            badgeBg = finance.incomeContainer
            icon = Icons.Filled.ArrowDownward
            signPrefix = "+"
            typeLabel = strings.receiveRepayment
        }
        LedgerTransactionType.BORROWING_TAKEN -> {
            badgeColor = finance.payable
            badgeBg = finance.payableContainer
            icon = Icons.Filled.Handshake
            signPrefix = "+"
            typeLabel = strings.moneyBorrowed
        }
        LedgerTransactionType.BORROWING_REPAYMENT -> {
            badgeColor = finance.expense
            badgeBg = finance.expenseContainer
            icon = Icons.Filled.ArrowUpward
            signPrefix = "-"
            typeLabel = strings.makeRepayment
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("tx_card_${tx.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = typeLabel,
                tint = badgeColor,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = typeLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val meta = buildString {
                if (tx.note.isNotBlank()) {
                    append(tx.note)
                    append(" • ")
                }
                append(DateUtils.formatDate(tx.transactionDate, language, numerals))
                append(" • ")
                append(paymentMethodLabel(tx.paymentMethod, strings))
            }
            Text(
                text = meta,
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
        Spacer(Modifier.width(10.dp))
        Text(
            text = if (hidden) "৳ ••••" else "$signPrefix${CurrencyFormatter.formatPoisha(tx.totalAmountPoisha, numerals)}",
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = PlusJakartaSansFont,
                fontWeight = FontWeight.Bold
            ),
            color = badgeColor,
            maxLines = 1
        )
    }
}

/**
 * Generic Financial Ledger List Row for shops, contacts, or notebooks.
 */
@Composable
fun LedgerListItem(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    trailingText: String,
    trailingColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingBadge: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            if (!trailingBadge.isNullOrBlank()) {
                Surface(
                    color = trailingColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = trailingBadge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = trailingColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            if (trailingText.isNotBlank()) {
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = PlusJakartaSansFont,
                        fontWeight = FontWeight.Bold
                    ),
                    color = trailingColor
                )
            }
        }
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Pill-shaped Search Bar matching Transactions and Pocket Diary mockups.
 */
@Composable
fun PremiumSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    inputTag: String = "premium_search_input"
) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(180),
        label = "search_border"
    )

    Surface(
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isFocused) 1.4.dp else 1.dp, borderColor),
        shadowElevation = if (isFocused) 2.dp else 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused }
                        .testTag(inputTag)
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(40.dp)
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
}

/**
 * Pill-shaped Filter Chip with optional leading icon and smooth selected state.
 */
@Composable
fun PremiumFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    selectedBgColor: Color = MaterialTheme.colorScheme.primary,
    unselectedBgColor: Color = MaterialTheme.colorScheme.surface,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val haptic = LocalHapticFeedback.current
    val finance = LocalFinanceColors.current
    val resolvedUnselectedBg = if (finance.isDark && unselectedBgColor == MaterialTheme.colorScheme.surface) {
        MaterialTheme.colorScheme.surfaceContainer
    } else unselectedBgColor
    val bg by animateColorAsState(
        targetValue = if (selected) selectedBgColor else resolvedUnselectedBg,
        animationSpec = tween(180),
        label = "chip_bg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) selectedContentColor else unselectedContentColor,
        animationSpec = tween(180),
        label = "chip_fg"
    )

    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(50))
            .clickable(role = Role.Tab) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) selectedBgColor else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

data class PremiumNavItem(
    val id: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Floating Deep Emerald pill-shaped bottom navigation bar with vertical hairline dividers
 * and Warm Gold selected accent — respects system navigation bar insets safely.
 */
@Composable
fun PremiumBottomNavigation(
    items: List<PremiumNavItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val finance = LocalFinanceColors.current
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp),
            shape = RoundedCornerShape(22.dp),
            color = finance.navBarContainer,
            border = BorderStroke(
                width = if (finance.isDark) 1.15.dp else 1.dp,
                color = if (finance.isDark) finance.goldAccent.copy(alpha = 0.45f) else WarmGold.copy(alpha = 0.35f)
            ),
            shadowElevation = if (finance.isDark) 10.dp else 6.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { idx, item ->
                    val isSelected = item.id == selectedId
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) finance.navBarSelected else finance.navBarUnselected,
                        animationSpec = tween(200),
                        label = "nav_fg_${item.id}"
                    )
                    val indicatorWidth by animateDpAsState(
                        targetValue = if (isSelected) 44.dp else 0.dp,
                        animationSpec = tween(220),
                        label = "nav_ind_${item.id}"
                    )
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 1f,
                        animationSpec = tween(180),
                        label = "nav_scale_${item.id}"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics { selected = isSelected }
                            .clickable(role = Role.Tab) {
                                if (!isSelected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelect(item.id)
                                }
                            }
                            .testTag("nav_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (indicatorWidth > 0.dp) {
                            // Animated Warm Gold top & bottom indicator bars
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .width(indicatorWidth)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                                    .background(WarmGold)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .width(indicatorWidth)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(WarmGold)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = contentColor,
                                modifier = Modifier
                                    .size(23.dp)
                                    .scale(iconScale)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = contentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (idx < items.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(30.dp)
                                .background(Color.White.copy(alpha = 0.18f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = modifier.height(50.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = modifier.height(50.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) = EmptyStateCard(
    icon = icon,
    title = title,
    subtitle = subtitle,
    modifier = modifier,
    actionLabel = actionLabel,
    onAction = onAction
)

@Composable
fun LoadingState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorState(
    title: String,
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            if (onRetry != null) {
                Spacer(Modifier.height(4.dp))
                PrimaryActionButton(
                    text = if (language == AppLanguage.BANGLA) "আবার চেষ্টা করুন" else "Try Again",
                    onClick = onRetry
                )
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    val strings = LocalAppStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    contentColor = if (isDestructive) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                )
            ) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        content = content
    )
}
