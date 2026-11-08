package com.hasbi.design.theme

import androidx.compose.ui.graphics.Color

/**
 * Hasbi color palette — Emerald (warmer, more Pakistani feel).
 *
 * Primary brand color is Emerald #1B5E43.
 * Accent gold #C9A961 for premium Islamic feel (use sparingly).
 * Income green / Expense red are soft, never aggressive.
 */
object HasbiColors {
    // Primary (Emerald)
    val Primary = Color(0xFF1B5E43)
    val PrimaryLight = Color(0xFF2E7D5B)
    val PrimaryDark = Color(0xFF0F3F2C)
    val PrimaryContainer = Color(0xFFB3E6D1)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnPrimaryContainer = Color(0xFF052015)

    // Accent (Gold)
    val Accent = Color(0xFFF4A340)
    val AccentLight = Color(0xFFF6B76A)
    val AccentContainer = Color(0xFFFFE8D1)
    val PrimaryGold = Color(0xFFC9A961)

    // Semantic
    val Income = Color(0xFF4CAF80)
    val IncomeContainer = Color(0xFFE8F5E9)
    val Expense = Color(0xFFE57373)
    val ExpenseContainer = Color(0xFFFFEBEE)
    val Warning = Color(0xFFF4A340)
    val WarningContainer = Color(0xFFFFF8E7)
    val Error = Color(0xFFB00020)

    // Neutral (warm off-white)
    val Background = Color(0xFFF7F5EE)
    val BackgroundWarm = Color(0xFFFAF8F2)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF1F0EB)
    val SurfaceTint = Color(0xFF1B5E43)

    // Text
    val TextPrimary = Color(0xFF1F2937)
    val TextSecondary = Color(0xFF6B7280)
    val TextTertiary = Color(0xFF9CA3AF)
    val OnSurface = Color(0xFF1F2937)
    val OnSurfaceVariant = Color(0xFF6B7280)

    // Borders / dividers
    val Border = Color(0xFFE5E7EB)
    val BorderSoft = Color(0xFFF3F4F6)
    val Outline = Color(0xFFD1D5DB)

    // Inverse
    val InverseSurface = Color(0xFF1F2937)
    val InverseOnSurface = Color(0xFFF7F5EE)
    val InversePrimary = Color(0xFFB3E6D1)
}

/**
 * Dark theme color palette.
 * Used when system theme is dark + user enabled dark mode.
 */
object HasbiDarkColors {
    val Primary = Color(0xFFB3E6D1)
    val PrimaryLight = Color(0xFF2E7D5B)
    val PrimaryDark = Color(0xFF0F3F2C)
    val PrimaryContainer = Color(0xFF0F3F2C)
    val OnPrimary = Color(0xFF052015)
    val OnPrimaryContainer = Color(0xFFB3E6D1)

    val Accent = Color(0xFFF6B76A)
    val AccentLight = Color(0xFFF4A340)
    val AccentContainer = Color(0xFF5D3D1A)
    val PrimaryGold = Color(0xFFE8C788)

    val Income = Color(0xFF66BB6A)
    val IncomeContainer = Color(0xFF1B3D2A)
    val Expense = Color(0xFFEF9A9A)
    val ExpenseContainer = Color(0xFF3D1F1F)
    val Warning = Color(0xFFF6B76A)
    val WarningContainer = Color(0xFF3D2E1A)
    val Error = Color(0xFFCF6679)

    val Background = Color(0xFF121212)
    val BackgroundWarm = Color(0xFF1A1A1A)
    val Surface = Color(0xFF1F1F1F)
    val SurfaceVariant = Color(0xFF2A2A2A)
    val SurfaceTint = Color(0xFFB3E6D1)

    val TextPrimary = Color(0xFFE5E7EB)
    val TextSecondary = Color(0xFF9CA3AF)
    val TextTertiary = Color(0xFF6B7280)
    val OnSurface = Color(0xFFE5E7EB)
    val OnSurfaceVariant = Color(0xFF9CA3AF)

    val Border = Color(0xFF2A2A2A)
    val BorderSoft = Color(0xFF1F1F1F)
    val Outline = Color(0xFF3A3A3A)

    val InverseSurface = Color(0xFFE5E7EB)
    val InverseOnSurface = Color(0xFF1F2937)
    val InversePrimary = Color(0xFF1B5E43)
}
