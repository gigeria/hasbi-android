package com.hasbi.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Hasbi custom color palette exposed via CompositionLocal.
 * Allows non-Material components to access brand colors directly.
 */
data class HasbiColorPalette(
    val primary: androidx.compose.ui.graphics.Color,
    val primaryLight: androidx.compose.ui.graphics.Color,
    val primaryDark: androidx.compose.ui.graphics.Color,
    val primaryGold: androidx.compose.ui.graphics.Color,
    val accent: androidx.compose.ui.graphics.Color,
    val income: androidx.compose.ui.graphics.Color,
    val expense: androidx.compose.ui.graphics.Color,
    val warning: androidx.compose.ui.graphics.Color,
    val background: androidx.compose.ui.graphics.Color,
    val backgroundWarm: androidx.compose.ui.graphics.Color,
    val surface: androidx.compose.ui.graphics.Color,
    val textPrimary: androidx.compose.ui.graphics.Color,
    val textSecondary: androidx.compose.ui.graphics.Color,
    val textTertiary: androidx.compose.ui.graphics.Color,
    val border: androidx.compose.ui.graphics.Color,
    val borderSoft: androidx.compose.ui.graphics.Color
)

val LocalHasbiColors = staticCompositionLocalOf {
    HasbiColorPalette(
        primary = HasbiColors.Primary,
        primaryLight = HasbiColors.PrimaryLight,
        primaryDark = HasbiColors.PrimaryDark,
        primaryGold = HasbiColors.PrimaryGold,
        accent = HasbiColors.Accent,
        income = HasbiColors.Income,
        expense = HasbiColors.Expense,
        warning = HasbiColors.Warning,
        background = HasbiColors.Background,
        backgroundWarm = HasbiColors.BackgroundWarm,
        surface = HasbiColors.Surface,
        textPrimary = HasbiColors.TextPrimary,
        textSecondary = HasbiColors.TextSecondary,
        textTertiary = HasbiColors.TextTertiary,
        border = HasbiColors.Border,
        borderSoft = HasbiColors.BorderSoft
    )
}

object HasbiTheme {
    val colors: HasbiColorPalette
        @Composable get() = LocalHasbiColors.current
}

/**
 * Hasbi theme — Material 3 + custom emerald palette.
 *
 * @param darkTheme follow system dark mode (default)
 * @param dynamicColor Material You support (Android 12+, default ON)
 */
@Composable
fun HasbiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default — Hasbi has its own emerald identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && darkTheme -> androidx.compose.material3.dynamicDarkColorScheme(
            androidx.compose.ui.platform.LocalContext.current
        )
        dynamicColor && !darkTheme -> androidx.compose.material3.dynamicLightColorScheme(
            androidx.compose.ui.platform.LocalContext.current
        )
        darkTheme -> darkColorScheme(
            primary = HasbiDarkColors.Primary,
            onPrimary = HasbiDarkColors.OnPrimary,
            primaryContainer = HasbiDarkColors.PrimaryContainer,
            onPrimaryContainer = HasbiDarkColors.OnPrimaryContainer,
            secondary = HasbiDarkColors.Accent,
            onSecondary = HasbiDarkColors.OnPrimary,
            secondaryContainer = HasbiDarkColors.AccentContainer,
            onSecondaryContainer = HasbiDarkColors.OnPrimary,
            tertiary = HasbiDarkColors.PrimaryGold,
            background = HasbiDarkColors.Background,
            onBackground = HasbiDarkColors.OnSurface,
            surface = HasbiDarkColors.Surface,
            onSurface = HasbiDarkColors.OnSurface,
            surfaceVariant = HasbiDarkColors.SurfaceVariant,
            onSurfaceVariant = HasbiDarkColors.OnSurfaceVariant,
            surfaceTint = HasbiDarkColors.SurfaceTint,
            outline = HasbiDarkColors.Outline,
            outlineVariant = HasbiDarkColors.Border,
            error = HasbiDarkColors.Error,
            onError = HasbiDarkColors.OnPrimary,
            inverseSurface = HasbiDarkColors.InverseSurface,
            inverseOnSurface = HasbiDarkColors.InverseOnSurface,
            inversePrimary = HasbiDarkColors.InversePrimary
        )
        else -> lightColorScheme(
            primary = HasbiColors.Primary,
            onPrimary = HasbiColors.OnPrimary,
            primaryContainer = HasbiColors.PrimaryContainer,
            onPrimaryContainer = HasbiColors.OnPrimaryContainer,
            secondary = HasbiColors.Accent,
            onSecondary = HasbiColors.OnPrimary,
            secondaryContainer = HasbiColors.AccentContainer,
            onSecondaryContainer = HasbiColors.OnPrimaryContainer,
            tertiary = HasbiColors.PrimaryGold,
            background = HasbiColors.Background,
            onBackground = HasbiColors.OnSurface,
            surface = HasbiColors.Surface,
            onSurface = HasbiColors.OnSurface,
            surfaceVariant = HasbiColors.SurfaceVariant,
            onSurfaceVariant = HasbiColors.OnSurfaceVariant,
            surfaceTint = HasbiColors.SurfaceTint,
            outline = HasbiColors.Outline,
            outlineVariant = HasbiColors.Border,
            error = HasbiColors.Error,
            onError = HasbiColors.OnPrimary,
            inverseSurface = HasbiColors.InverseSurface,
            inverseOnSurface = HasbiColors.InverseOnSurface,
            inversePrimary = HasbiColors.InversePrimary
        )
    }

    val hasbiPalette = if (darkTheme) {
        HasbiColorPalette(
            primary = HasbiDarkColors.Primary,
            primaryLight = HasbiDarkColors.PrimaryLight,
            primaryDark = HasbiDarkColors.PrimaryDark,
            primaryGold = HasbiDarkColors.PrimaryGold,
            accent = HasbiDarkColors.Accent,
            income = HasbiDarkColors.Income,
            expense = HasbiDarkColors.Expense,
            warning = HasbiDarkColors.Warning,
            background = HasbiDarkColors.Background,
            backgroundWarm = HasbiDarkColors.BackgroundWarm,
            surface = HasbiDarkColors.Surface,
            textPrimary = HasbiDarkColors.TextPrimary,
            textSecondary = HasbiDarkColors.TextSecondary,
            textTertiary = HasbiDarkColors.TextTertiary,
            border = HasbiDarkColors.Border,
            borderSoft = HasbiDarkColors.BorderSoft
        )
    } else {
        HasbiColorPalette(
            primary = HasbiColors.Primary,
            primaryLight = HasbiColors.PrimaryLight,
            primaryDark = HasbiColors.PrimaryDark,
            primaryGold = HasbiColors.PrimaryGold,
            accent = HasbiColors.Accent,
            income = HasbiColors.Income,
            expense = HasbiColors.Expense,
            warning = HasbiColors.Warning,
            background = HasbiColors.Background,
            backgroundWarm = HasbiColors.BackgroundWarm,
            surface = HasbiColors.Surface,
            textPrimary = HasbiColors.TextPrimary,
            textSecondary = HasbiColors.TextSecondary,
            textTertiary = HasbiColors.TextTertiary,
            border = HasbiColors.Border,
            borderSoft = HasbiColors.BorderSoft
        )
    }

    CompositionLocalProvider(LocalHasbiColors provides hasbiPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HasbiTypography,
            shapes = HasbiShapes,
            content = content
        )
    }
}
