package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class FinanceColors(
    val income: Color,
    val incomeContainer: Color,
    val incomeBorder: Color,
    val expense: Color,
    val expenseContainer: Color,
    val expenseBorder: Color,
    val receivable: Color,
    val receivableContainer: Color,
    val receivableBorder: Color,
    val payable: Color,
    val payableContainer: Color,
    val payableBorder: Color,
    val goldAccent: Color,
    val goldSoft: Color,
    val goldContainer: Color,
    val canopyTop: Color,
    val canopyMid: Color,
    val canopyBottom: Color,
    val navBarContainer: Color,
    val navBarSelected: Color,
    val navBarUnselected: Color,
    val sectionBannerBg: Color,
    val sectionBannerContent: Color,
    val chartIncome: Color,
    val chartExpense: Color,
    val chartPalette: List<Color>,
    val isDark: Boolean
)

private val LightFinanceColors = FinanceColors(
    income = IncomeGreen,
    incomeContainer = IncomeContainerLight,
    incomeBorder = IncomeBorderLight,
    expense = ExpenseCrimson,
    expenseContainer = ExpenseContainerLight,
    expenseBorder = ExpenseBorderLight,
    receivable = ReceivableTeal,
    receivableContainer = ReceivableContainerLight,
    receivableBorder = ReceivableBorderLight,
    payable = PayableAmber,
    payableContainer = PayableContainerLight,
    payableBorder = PayableBorderLight,
    goldAccent = WarmGold,
    goldSoft = SoftGold,
    goldContainer = WarmGoldContainer,
    canopyTop = DarkGreen,
    canopyMid = DeepEmerald,
    canopyBottom = Color(0xFF0E4A39),
    navBarContainer = DeepEmerald,
    navBarSelected = SoftGold,
    navBarUnselected = Color(0xFFD5E6DF),
    sectionBannerBg = DeepEmerald,
    sectionBannerContent = Color.White,
    chartIncome = ChartEmerald,
    chartExpense = ChartCoral,
    chartPalette = listOf(ChartEmerald, ChartGold, ChartBlue, ChartCoral, ChartPurple, ChartTeal, ChartAmber),
    isDark = false
)

private val DarkFinanceColors = FinanceColors(
    income = IncomeGreenDark,
    incomeContainer = IncomeContainerDark,
    incomeBorder = IncomeBorderDark,
    expense = ExpenseCrimsonDark,
    expenseContainer = ExpenseContainerDark,
    expenseBorder = ExpenseBorderDark,
    receivable = ReceivableTealDark,
    receivableContainer = ReceivableContainerDark,
    receivableBorder = ReceivableBorderDark,
    payable = PayableAmberDark,
    payableContainer = PayableContainerDark,
    payableBorder = PayableBorderDark,
    goldAccent = DarkTertiary,
    goldSoft = SoftGold,
    goldContainer = DarkTertiaryContainer,
    canopyTop = Color(0xFF041510),
    canopyMid = Color(0xFF0A2920),
    canopyBottom = Color(0xFF0F392D),
    navBarContainer = Color(0xFF0D251E),
    navBarSelected = DarkTertiary,
    navBarUnselected = Color(0xFFA2BDB4),
    sectionBannerBg = Color(0xFF13382D),
    sectionBannerContent = Color(0xFFF0F6F3),
    chartIncome = IncomeGreenDark,
    chartExpense = ExpenseCrimsonDark,
    chartPalette = listOf(
        IncomeGreenDark,
        DarkTertiary,
        ReceivableTealDark,
        ExpenseCrimsonDark,
        Color(0xFFB39DFF),
        Color(0xFF4CD3CC),
        PayableAmberDark
    ),
    isDark = true
)

val LocalFinanceColors = staticCompositionLocalOf { LightFinanceColors }

private val LightColorScheme = lightColorScheme(
    primary = DeepEmerald,
    onPrimary = SurfaceWhite,
    primaryContainer = SoftMintContainer,
    onPrimaryContainer = OnSoftMint,
    secondary = ForestGreen,
    onSecondary = SurfaceWhite,
    secondaryContainer = Color(0xFFE2F3EB),
    onSecondaryContainer = DarkGreen,
    tertiary = WarmGold,
    onTertiary = DarkCharcoal,
    tertiaryContainer = WarmGoldContainer,
    onTertiaryContainer = OnWarmGoldContainer,
    background = WarmIvory,
    onBackground = DarkCharcoal,
    surface = SurfaceWhite,
    onSurface = DarkCharcoal,
    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = MutedSlate,
    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = Color(0xFFFCFBF7),
    surfaceContainer = Color(0xFFF2F0E8),
    surfaceContainerHigh = Color(0xFFEBE8DE),
    surfaceContainerHighest = Color(0xFFE3E0D5),
    outline = OutlineLight,
    outlineVariant = OutlineSubtleLight,
    error = ExpenseCrimson,
    onError = SurfaceWhite,
    errorContainer = ExpenseContainerLight,
    onErrorContainer = Color(0xFF681416)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnPrimary,
    secondaryContainer = Color(0xFF153D31),
    onSecondaryContainer = Color(0xFFD2F7E6),
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLowest = Color(0xFF050D0A),
    surfaceContainerLow = Color(0xFF0D1A16),
    surfaceContainer = DarkSurfaceElevated,
    surfaceContainerHigh = Color(0xFF1C352D),
    surfaceContainerHighest = Color(0xFF244339),
    outline = DarkOutline,
    outlineVariant = DarkOutlineSubtle,
    error = DarkError,
    onError = Color(0xFF470D0E),
    errorContainer = DarkErrorContainer,
    onErrorContainer = Color(0xFFFFDAD8)
)

val JomaKhorochShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun JomaKhorochTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val financeColors = if (darkTheme) DarkFinanceColors else LightFinanceColors

    CompositionLocalProvider(LocalFinanceColors provides financeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = JomaKhorochShapes,
            content = content
        )
    }
}
