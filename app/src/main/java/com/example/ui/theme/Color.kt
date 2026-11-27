package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Approved JomaKhoroch Premium Finance Palette
// ============================================================================

// Primary Brand Colors
val DeepEmerald = Color(0xFF123F35)      // Primary Deep Emerald
val ForestGreen = Color(0xFF087A50)      // Forest Green (Primary interactive / positive)
val DarkGreen = Color(0xFF082D26)        // Dark Canopy Green

// Accent Colors
val WarmGold = Color(0xFFD5AE63)         // Warm Gold Accent
val SoftGold = Color(0xFFF0D9A4)         // Soft Gold Highlight
val WarmGoldContainer = Color(0xFFFBF3E0)
val OnWarmGoldContainer = Color(0xFF4F380E)

// Background & Surfaces (Light)
val WarmIvory = Color(0xFFF7F5EF)        // Warm Ivory Page Background
val SurfaceWhite = Color(0xFFFFFFFF)     // Crisp Card Surface
val SoftSurfaceVariant = Color(0xFFEEECE4)
val OutlineLight = Color(0xFFD7DDD8)
val OutlineSubtleLight = Color(0xFFE6E4DC)
val DarkCharcoal = Color(0xFF13221D)     // Primary Text (Deep Pine Ink)
val MutedSlate = Color(0xFF5A6B66)       // Neutral Slate Gray

// Backward-compatible brand aliases used across sheets/screens
val DeepForestGreen = DeepEmerald
val DeepForestDark = DarkGreen
val EmeraldGreen = ForestGreen
val SoftMintContainer = Color(0xFFE3F2EC)
val OnSoftMint = Color(0xFF0A2E25)
val SoftOffWhite = WarmIvory
val PureWhite = SurfaceWhite
val MutedCharcoal = MutedSlate

// Semantic Finance Colors (Light)
val IncomeGreen = Color(0xFF087A50)
val IncomeContainerLight = Color(0xFFE6F5EC)
val IncomeBorderLight = Color(0xFFBEE3CD)

val ExpenseCrimson = Color(0xFFC62828)
val ExpenseContainerLight = Color(0xFFFDECEC)
val ExpenseBorderLight = Color(0xFFF6C6C6)

val ReceivableTeal = Color(0xFF1565C0)
val ReceivableContainerLight = Color(0xFFE7F1FC)
val ReceivableBorderLight = Color(0xFFC0D9F7)

val PayableAmber = Color(0xFFB85D19)
val PayableContainerLight = Color(0xFFFDF2E4)
val PayableBorderLight = Color(0xFFF2D4B0)

val NeutralSlate = MutedSlate
val NeutralContainerLight = Color(0xFFEDF1EE)

// Chart Colors (Light)
val ChartEmerald = Color(0xFF0E8A5B)
val ChartCoral = Color(0xFFE55B54)
val ChartGold = Color(0xFFD5AE63)
val ChartBlue = Color(0xFF2B79C2)
val ChartPurple = Color(0xFF7B5AC8)
val ChartTeal = Color(0xFF178D88)
val ChartAmber = Color(0xFFDC8A24)

// ============================================================================
// Independently Designed Dark Theme Palette (Obsidian Emerald — OLED Tuned)
// ============================================================================
val DarkBackground = Color(0xFF07110E)          // Deep obsidian-pine canvas
val DarkSurface = Color(0xFF10201B)             // Distinct elevated card layer
val DarkSurfaceElevated = Color(0xFF162C25)     // Nested chips, pills, controls
val DarkSurfaceVariant = Color(0xFF1C362E)      // Inputs & segmented tracks
val DarkOutline = Color(0xFF2D4F44)             // Tactile interactive borders
val DarkOutlineSubtle = Color(0xFF203B32)       // Hairline card rims & dividers

val DarkPrimary = Color(0xFF3ED398)             // Luminous emerald mint
val DarkOnPrimary = Color(0xFF042418)           // Ultra-crisp deep pine on primary (10.5:1)
val DarkPrimaryContainer = Color(0xFF123F32)    // Rich emerald container
val DarkOnPrimaryContainer = Color(0xFFD2F7E6)

val DarkSecondary = Color(0xFF66DCAC)
val DarkTertiary = Color(0xFFE8C476)            // Luminous champagne gold
val DarkOnTertiary = Color(0xFF2E1F04)
val DarkTertiaryContainer = Color(0xFF3D2D10)
val DarkOnTertiaryContainer = Color(0xFFFAE7BA)

val DarkTextPrimary = Color(0xFFF0F6F3)         // Soft zero-glare off-white
val DarkTextSecondary = Color(0xFFA3BAB2)       // High-legibility cool sage gray
val DarkError = Color(0xFFF58B84)
val DarkErrorContainer = Color(0xFF4A181A)

// Semantic Finance Colors (Dark — independently tuned for glare-free legibility)
val IncomeGreenDark = Color(0xFF4BE0A0)
val IncomeContainerDark = Color(0xFF0E2E22)
val IncomeBorderDark = Color(0xFF215B44)

val ExpenseCrimsonDark = Color(0xFFFF8A82)
val ExpenseContainerDark = Color(0xFF361719)
val ExpenseBorderDark = Color(0xFF662B2E)

val ReceivableTealDark = Color(0xFF74BCFF)
val ReceivableContainerDark = Color(0xFF10273E)
val ReceivableBorderDark = Color(0xFF224D75)

val PayableAmberDark = Color(0xFFF3B866)
val PayableContainerDark = Color(0xFF342410)
val PayableBorderDark = Color(0xFF63441D)

// Canopy / On-Dark-Header Accents
val CanopyCreamText = Color(0xFFFBF3DF)
val CanopyWarmGoldText = SoftGold
val CanopyMintAccent = Color(0xFF8BE8BF)
val CanopySyncedMint = Color(0xFFA7ECCB)
val CanopyCoralAccent = Color(0xFFFFC4BE)
val CanopyErrorTint = Color(0xFFFFB4AB)
val CanopyAmberAccent = Color(0xFFFFDF8E)
val CanopyOrangeAccent = Color(0xFFFFD8AC)
val CanopyUrgentBadge = Color(0xFFE53935)

// Legacy Canopy Gradient Aliases (mapped cleanly to Deep Emerald family)
val BronzeCanopyDark = DarkGreen
val BronzeCanopyMid = DeepEmerald
val BronzeCanopyLight = ForestGreen

val TealCanopyDark = DarkGreen
val TealCanopyMid = DeepEmerald
val TealCanopyLight = ForestGreen

val DiaryTealDark = DarkGreen
val DiaryTealMid = DeepEmerald
val DiaryTealLight = ForestGreen

val GarnetCanopyDark = Color(0xFF45191B)
val GarnetCanopyMid = Color(0xFF782427)
val GarnetCanopyLight = Color(0xFF9E3236)
