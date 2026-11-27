package com.example.presentation.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.BuildConfig
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.LocalLanguage
import com.example.core.localization.LocalNumeralStyle
import com.example.core.util.CurrencyFormatter
import com.example.ui.theme.CanopyCreamText
import com.example.ui.theme.CanopyMintAccent
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LocalFinanceColors
import com.example.ui.theme.PlusJakartaSansFont
import com.example.ui.theme.SoftGold
import com.example.ui.theme.WarmGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * Full-bleed Deep Emerald & Warm Gold ceremonial splash screen with choreographed
 * entrance motion, botanical canopy atmosphere, and live shimmer progress bar.
 */
@Composable
fun JomaKhorochSplashScreen(
    isDataReady: Boolean,
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val language = LocalLanguage.current
    val isBn = language == AppLanguage.BANGLA

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    val logoScale = remember { Animatable(0.72f) }
    val logoOffsetY = remember { Animatable(24f) }
    val contentAlpha = remember { Animatable(0f) }
    val dividerProgress = remember { Animatable(0f) }
    var minDelayElapsed by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_ambient")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )
    val shimmerSweep by infiniteTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_sweep"
    )

    LaunchedEffect(Unit) {
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            logoOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        }
        launch {
            delay(180L)
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
        launch {
            delay(260L)
            dividerProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 560, easing = FastOutSlowInEasing)
            )
        }
        delay(1250L)
        minDelayElapsed = true
    }

    LaunchedEffect(isDataReady, minDelayElapsed) {
        if (isDataReady && minDelayElapsed) {
            onSplashFinished()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF042216),
                        DarkGreen,
                        DeepEmerald,
                        Color(0xFF0A4931),
                        Color(0xFF042619)
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Layered atmospheric canopy rings, botanical arcs & golden ribbon sweep
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft radial emerald spotlight behind center logo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ForestGreen.copy(alpha = 0.42f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.44f),
                    radius = w * 0.72f
                ),
                radius = w * 0.72f,
                center = Offset(w * 0.5f, h * 0.44f)
            )

            // Upper-right organic botanical halo
            drawCircle(
                color = WarmGold.copy(alpha = 0.08f),
                radius = w * 0.58f,
                center = Offset(w * 0.94f, h * 0.10f)
            )
            drawCircle(
                color = SoftGold.copy(alpha = 0.12f),
                radius = w * 0.40f,
                center = Offset(w * 0.94f, h * 0.10f),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // Lower-left botanical sweep
            drawCircle(
                color = CanopyMintAccent.copy(alpha = 0.06f),
                radius = w * 0.52f,
                center = Offset(w * 0.06f, h * 0.88f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = w * 0.34f,
                center = Offset(w * 0.06f, h * 0.88f),
                style = Stroke(width = 1.dp.toPx())
            )

            // Subtle sweeping Warm Gold ornamental arc near bottom third
            val ribbonPath = Path().apply {
                moveTo(0f, h * 0.76f)
                quadraticTo(w * 0.50f, h * 0.84f, w, h * 0.72f)
            }
            drawPath(
                path = ribbonPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        WarmGold.copy(alpha = 0.28f),
                        SoftGold.copy(alpha = 0.42f),
                        WarmGold.copy(alpha = 0.24f),
                        Color.Transparent
                    )
                ),
                style = Stroke(width = 1.4.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1.1f))

            // Hero Logo Medallion with pulsing Warm Gold aura & filigree ring
            Box(
                modifier = Modifier
                    .size(226.dp)
                    .offset(y = logoOffsetY.value.dp)
                    .scale(logoScale.value),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val baseR = size.minDimension * 0.48f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                WarmGold.copy(alpha = 0.34f),
                                WarmGold.copy(alpha = 0.09f),
                                Color.Transparent
                            )
                        ),
                        radius = baseR * haloPulse
                    )
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                WarmGold.copy(alpha = 0.08f),
                                SoftGold.copy(alpha = 0.55f),
                                WarmGold.copy(alpha = 0.12f),
                                SoftGold.copy(alpha = 0.42f),
                                WarmGold.copy(alpha = 0.08f)
                            )
                        ),
                        radius = size.minDimension * 0.44f,
                        style = Stroke(width = 1.3.dp.toPx())
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.jomakhoroch_logo),
                    contentDescription = strings.appName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(182.dp)
                        .testTag("splash_brand_logo")
                )
            }

            Spacer(Modifier.height(20.dp))

            // Brand Wordmark + Animated Gold Rule + Tagline
            Column(
                modifier = Modifier.alpha(contentAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "JomaKhoroch",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = PlusJakartaSansFont,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = SoftGold,
                    textAlign = TextAlign.Center
                )

                // Expanding Warm Gold accent bar
                Box(
                    modifier = Modifier
                        .width((64f * dividerProgress.value).dp)
                        .height(2.5.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    WarmGold.copy(alpha = 0.25f),
                                    SoftGold,
                                    WarmGold.copy(alpha = 0.25f)
                                )
                            )
                        )
                )

                Text(
                    text = strings.tagline,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = CanopyCreamText.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.weight(1.25f))

            // Bottom Shimmer Loader + Trust Pill + Version
            Column(
                modifier = Modifier.alpha(contentAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Sleek Golden Shimmer Loading Track
                Canvas(
                    modifier = Modifier
                        .width(124.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                ) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.14f),
                        cornerRadius = CornerRadius(size.height / 2f)
                    )
                    val segWidth = size.width * 0.42f
                    val startX = (size.width * shimmerSweep) - (segWidth * 0.5f)
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                WarmGold,
                                SoftGold,
                                WarmGold,
                                Color.Transparent
                            ),
                            startX = startX,
                            endX = startX + segWidth
                        ),
                        topLeft = Offset(startX.coerceIn(0f, size.width), 0f),
                        size = Size(
                            width = (startX + segWidth).coerceAtMost(size.width) - startX.coerceAtLeast(0f),
                            height = size.height
                        ),
                        cornerRadius = CornerRadius(size.height / 2f)
                    )
                }

                Surface(
                    color = Color.White.copy(alpha = 0.11f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.9.dp, WarmGold.copy(alpha = 0.42f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = null,
                            tint = SoftGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isBn) "ক্লাউড সিঙ্ক ও অফলাইন সুরক্ষিত" else "Cloud Sync & Offline Ready",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CanopyCreamText
                        )
                    }
                }

                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PlusJakartaSansFont,
                        letterSpacing = 0.6.sp
                    ),
                    color = Color.White.copy(alpha = 0.52f)
                )
            }
        }
    }
}

private data class OnboardingHighlight(
    val icon: ImageVector,
    val labelBn: String,
    val labelEn: String
)

private data class OnboardingPageData(
    @DrawableRes val illustrationRes: Int,
    val badgeIcon: ImageVector,
    val badgeBn: String,
    val badgeEn: String,
    val floatingPillBn: String,
    val floatingPillEn: String,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val descriptionEn: String,
    val highlights: List<OnboardingHighlight>
)

/**
 * Redesigned Onboarding experience using JomaKhoroch's signature curved Deep Emerald
 * & Warm Gold canopy, parallax floating editorial cards, feature badges, and 1-tap
 * starting balance setup on the final slide.
 */
@Composable
fun OnboardingScreen(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onComplete: (openingBalancePoisha: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguage.current
    val numerals = LocalNumeralStyle.current
    val finance = LocalFinanceColors.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val isBn = language == AppLanguage.BANGLA

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = !finance.isDark
        }
    }

    val pages = remember {
        listOf(
            OnboardingPageData(
                illustrationRes = R.drawable.img_onboard_ledger,
                badgeIcon = Icons.Filled.AccountBalanceWallet,
                badgeBn = "স্মার্ট দৈনিক হিসাব",
                badgeEn = "Smart Daily Ledger",
                floatingPillBn = "ব্যক্তিগত • পরিবার • ব্যবসা",
                floatingPillEn = "Personal • Family • Business",
                titleBn = "আয়-খরচের নিখুঁত হিসাব এখন হাতের মুঠোয়",
                titleEn = "Every Taka tracked with clarity",
                descriptionBn = "আলাদা খাতায় প্রতিদিনের আয়-ব্যয়, মাসিক বাজেট এবং দৃষ্টিনন্দন পিডিএফ রিপোর্ট রাখুন এক জায়গায়।",
                descriptionEn = "Organize separate books with live cash balance, category budgets, and instant PDF & CSV statements.",
                highlights = listOf(
                    OnboardingHighlight(Icons.Filled.MenuBook, "একাধিক খাতা", "Multi-Book"),
                    OnboardingHighlight(Icons.Filled.BarChart, "মাসিক বাজেট", "Budgets"),
                    OnboardingHighlight(Icons.Filled.PictureAsPdf, "রিপোর্ট", "PDF Report")
                )
            ),
            OnboardingPageData(
                illustrationRes = R.drawable.img_onboard_shop_loan,
                badgeIcon = Icons.Filled.Storefront,
                badgeBn = "বাকি ও ধারের খাতা",
                badgeEn = "Shop Dues & Loans",
                floatingPillBn = "দোকানের বাকি • পাওনা-দেনা",
                floatingPillEn = "Shop Ledger • Lend & Borrow",
                titleBn = "দোকানের বাকি ও ধার-দেনা কখনো ভুলবেন না",
                titleEn = "Track shop dues & personal loans effortlessly",
                descriptionBn = "পণ্যভিত্তিক বাজারের তালিকা, আংশিক পরিশোধ এবং বন্ধুবান্ধবের পাওনা-দেনার স্বচ্ছ হিসাব রাখুন।",
                descriptionEn = "Log itemized shop bills, partial repayments, and personal lending or borrowing without double-counting.",
                highlights = listOf(
                    OnboardingHighlight(Icons.Filled.Storefront, "দোকানের বাকি", "Shop Dues"),
                    OnboardingHighlight(Icons.Filled.ReceiptLong, "পণ্যের বিল", "Itemized"),
                    OnboardingHighlight(Icons.Filled.Handshake, "পাওনা-দেনা", "Loans")
                )
            ),
            OnboardingPageData(
                illustrationRes = R.drawable.img_onboard_diary_vault,
                badgeIcon = Icons.Filled.Shield,
                badgeBn = "নিরাপদ ও সিঙ্কড",
                badgeEn = "Private & Synced",
                floatingPillBn = "অফলাইন সচল • ক্লাউড ব্যাকআপ",
                floatingPillEn = "Offline Ready • Cloud Backup",
                titleBn = "অফলাইন সুবিধা, পিন লক ও ক্লাউড ব্যাকআপ",
                titleEn = "Offline-first speed with cloud safety",
                descriptionBn = "ইন্টারনেট ছাড়াই দ্রুত হিসাব লিখুন, পিন লক দিয়ে সুরক্ষিত রাখুন এবং Google অ্যাকাউন্টে ব্যাকআপ নিন।",
                descriptionEn = "Record entries anywhere offline, protect with PIN/biometrics, and back up securely to your Google account.",
                highlights = listOf(
                    OnboardingHighlight(Icons.Filled.CloudDone, "ক্লাউড সিঙ্ক", "Cloud Sync"),
                    OnboardingHighlight(Icons.Filled.Lock, "পিন লক", "PIN Lock"),
                    OnboardingHighlight(Icons.Filled.VerifiedUser, "সুরক্ষিত ডেটা", "Encrypted")
                )
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLastPage = pagerState.currentPage == pages.lastIndex
    var openingBalanceInput by rememberSaveable { mutableStateOf("") }
    var openingBalanceError by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    val bgColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .testTag("onboarding_screen")
    ) {
        // Signature Deep Emerald curved canopy backdrop behind top bar + hero illustration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(316.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF052417),
                            finance.canopyTop,
                            finance.canopyMid,
                            finance.canopyBottom
                        )
                    )
                )
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                drawCircle(
                    color = WarmGold.copy(alpha = 0.08f),
                    radius = w * 0.46f,
                    center = Offset(w * 0.90f, h * 0.18f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = w * 0.30f,
                    center = Offset(w * 0.10f, h * 0.56f)
                )

                val curveDepth = 30.dp.toPx()
                val goldArc = Path().apply {
                    moveTo(0f, h - curveDepth - 3.dp.toPx())
                    quadraticTo(
                        w * 0.50f,
                        h + curveDepth * 0.42f,
                        w,
                        h - curveDepth - 3.dp.toPx()
                    )
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = goldArc,
                    brush = Brush.horizontalGradient(
                        colors = listOf(WarmGold, SoftGold, WarmGold)
                    )
                )

                val bgCutout = Path().apply {
                    moveTo(0f, h - curveDepth + 1.5.dp.toPx())
                    quadraticTo(
                        w * 0.50f,
                        h + curveDepth * 0.62f,
                        w,
                        h - curveDepth + 1.5.dp.toPx()
                    )
                    lineTo(w, h + 2f)
                    lineTo(0f, h + 2f)
                    close()
                }
                drawPath(path = bgCutout, color = bgColor)
            }
        }

        // Foreground Adaptive Content Column
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 580.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Top Bar inside Emerald Canopy: Official Brand Mark + Language Toggle + Skip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.jomakhoroch_ic),
                            contentDescription = "JomaKhoroch",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .testTag("onboarding_brand_logo")
                        )
                        Column {
                            Text(
                                text = "JomaKhoroch",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PlusJakartaSansFont,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = SoftGold,
                                maxLines = 1
                            )
                            Text(
                                text = CurrencyFormatter.toLocalizedDigits(
                                    "${pagerState.currentPage + 1}/${pages.size}",
                                    numerals
                                ),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PlusJakartaSansFont,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.White.copy(alpha = 0.72f)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.13f),
                            border = BorderStroke(0.9.dp, WarmGold.copy(alpha = 0.42f))
                        ) {
                            Row(
                                modifier = Modifier.padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                OnboardingLangPill(
                                    label = "বাংলা",
                                    selected = currentLanguage == AppLanguage.BANGLA,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSelectLanguage(AppLanguage.BANGLA)
                                    },
                                    tag = "onboard_lang_bn"
                                )
                                OnboardingLangPill(
                                    label = "EN",
                                    selected = currentLanguage == AppLanguage.ENGLISH,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSelectLanguage(AppLanguage.ENGLISH)
                                    },
                                    tag = "onboard_lang_en"
                                )
                            }
                        }

                        if (!isLastPage) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.10f),
                                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.22f)),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 40.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable(role = Role.Button) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onComplete(null)
                                    }
                                    .testTag("onboarding_skip_button")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isBn) "এড়িয়ে যান" else "Skip",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CanopyCreamText
                                    )
                                }
                            }
                        }
                    }
                }

                // Swipeable Horizontal Pager with Overlapping Hero Illustration + Editorial Deck
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    pageSpacing = 16.dp,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { pageIndex ->
                    val page = pages[pageIndex]
                    val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction)
                        .absoluteValue
                        .coerceIn(0f, 1f)
                    val cardScale = 1f - (0.06f * pageOffset)
                    val cardAlpha = 1f - (0.28f * pageOffset)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 6.dp, bottom = 12.dp)
                            .graphicsLayer {
                                scaleX = cardScale
                                scaleY = cardScale
                                alpha = cardAlpha
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Overlapping Framed Editorial Showcase Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(28.dp),
                                color = Color(0xFFF9F6EE),
                                border = BorderStroke(
                                    width = 1.6.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(SoftGold, WarmGold.copy(alpha = 0.65f), SoftGold)
                                    )
                                ),
                                shadowElevation = 8.dp
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        painter = painterResource(id = page.illustrationRes),
                                        contentDescription = if (isBn) page.titleBn else page.titleEn,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Subtle bottom vignette for crisp floating badge legibility
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(64.dp)
                                            .align(Alignment.BottomCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Transparent,
                                                        DarkGreen.copy(alpha = 0.24f)
                                                    )
                                                )
                                            )
                                    )

                                    // Floating Deep Emerald + Gold Micro-Stat Ribbon
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = DeepEmerald.copy(alpha = 0.92f),
                                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.72f)),
                                        shadowElevation = 4.dp,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = page.badgeIcon,
                                                contentDescription = null,
                                                tint = SoftGold,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (isBn) page.floatingPillBn else page.floatingPillEn,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = CanopyCreamText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(22.dp))

                        // Feature Category Pill
                        Surface(
                            color = if (finance.isDark) MaterialTheme.colorScheme.primaryContainer
                            else Color(0xFFE5F3EC),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                1.dp,
                                if (finance.isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                else Color(0xFFBEE2CF)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(WarmGold)
                                )
                                Text(
                                    text = if (isBn) page.badgeBn else page.badgeEn,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Editorial Headline
                        Text(
                            text = if (isBn) page.titleBn else page.titleEn,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 31.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        Spacer(Modifier.height(9.dp))

                        // Concise Supporting Copy
                        Text(
                            text = if (isBn) page.descriptionBn else page.descriptionEn,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(Modifier.height(18.dp))

                        // 3 Balanced Tactile Feature Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            page.highlights.forEach { hl ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f)
                                    ),
                                    shadowElevation = 0.8.dp,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = hl.icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Text(
                                            text = if (isBn) hl.labelBn else hl.labelEn,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Slide 3: Interactive Starting Cash Setup Card with 1-Tap Presets
                        AnimatedVisibility(
                            visible = pageIndex == pages.lastIndex,
                            enter = expandVertically(tween(240)) + fadeIn(tween(220)),
                            exit = shrinkVertically(tween(180)) + fadeOut(tween(140))
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    WarmGold.copy(alpha = if (finance.isDark) 0.45f else 0.55f)
                                ),
                                shadowElevation = 1.5.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (isBn) "হাতে থাকা নগদ টাকা (ঐচ্ছিক)"
                                            else "Starting Cash Balance (Optional)",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // 1-Tap Quick Amount Preset Chips
                                    val presets = listOf("0", "5000", "10000", "25000")
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(presets) { amountStr ->
                                            val selected = openingBalanceInput.trim() == amountStr
                                            val displayLabel = "৳ " + CurrencyFormatter.toLocalizedDigits(
                                                when (amountStr) {
                                                    "5000" -> "5,000"
                                                    "10000" -> "10,000"
                                                    "25000" -> "25,000"
                                                    else -> "0"
                                                },
                                                numerals
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = if (selected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceContainerLow,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (selected) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.outlineVariant
                                                ),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        openingBalanceInput = amountStr
                                                        openingBalanceError = null
                                                    }
                                            ) {
                                                Text(
                                                    text = displayLabel,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = openingBalanceInput,
                                        onValueChange = {
                                            openingBalanceInput = it
                                            openingBalanceError = null
                                        },
                                        placeholder = {
                                            Text(if (isBn) "যেমন: ৫০০০" else "e.g. 5000")
                                        },
                                        prefix = {
                                            Text(
                                                text = "৳ ",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        isError = openingBalanceError != null,
                                        supportingText = openingBalanceError?.let { err -> { Text(err) } },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("onboarding_opening_balance_input")
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Grounded Navigation Dock
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tappable Animated Pill Page Indicators
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pages.size) { idx ->
                            val active = pagerState.currentPage == idx
                            val indicatorWidth by animateDpAsState(
                                targetValue = if (active) 30.dp else 9.dp,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "onboard_dot_width"
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(9.dp)
                                    .width(indicatorWidth)
                                    .clip(CircleShape)
                                    .background(
                                        if (active) Brush.horizontalGradient(listOf(DeepEmerald, ForestGreen))
                                        else Brush.horizontalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.outlineVariant,
                                                MaterialTheme.colorScheme.outlineVariant
                                            )
                                        )
                                    )
                                    .clickable {
                                        scope.launch { pagerState.animateScrollToPage(idx) }
                                    }
                            )
                        }
                    }

                    // Primary CTA Row (Back + Deep Emerald & Gold Action Button)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(visible = pagerState.currentPage > 0) {
                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scope.launch {
                                        pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0))
                                    }
                                },
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .size(56.dp)
                                    .testTag("onboarding_back_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = if (isBn) "পেছনে যান" else "Back",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (!isLastPage) {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                } else {
                                    if (openingBalanceInput.isBlank()) {
                                        onComplete(null)
                                    } else {
                                        val parsedPoisha = CurrencyFormatter.parseTakaToPoisha(openingBalanceInput)
                                        if (parsedPoisha == null || parsedPoisha < 0L) {
                                            openingBalanceError = if (isBn) {
                                                "সঠিক টাকার পরিমাণ লিখুন"
                                            } else {
                                                "Enter a valid amount"
                                            }
                                        } else {
                                            onComplete(parsedPoisha)
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag(if (isLastPage) "onboarding_start_button" else "onboarding_next_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(DeepEmerald, ForestGreen)
                                        ),
                                        shape = RoundedCornerShape(18.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isLastPage) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = SoftGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = if (isLastPage) {
                                            if (isBn) "হিসাব শুরু করুন" else "Get Started"
                                        } else {
                                            if (isBn) "পরবর্তী" else "Continue"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = SoftGold,
                                        modifier = Modifier.size(18.dp)
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
private fun OnboardingLangPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) SoftGold else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(role = Role.RadioButton, onClick = onClick)
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (selected) DarkGreen else Color.White.copy(alpha = 0.88f),
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
        )
    }
}
