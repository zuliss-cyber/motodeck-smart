package com.motosmart.launcher.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motosmart.launcher.data.preferences.AccentColor

data class MotoPalette(
    val accent: Color,
    val accentSecondary: Color,
    val glow: Color,
    val isDark: Boolean,
    val panel: Color,
    val panelHigh: Color,
    val outline: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val positive: Color,
    val warning: Color
)

val LocalMotoPalette = staticCompositionLocalOf {
    MotoPalette(
        accent = IcyBlue,
        accentSecondary = IcyWhite,
        glow = IcyGlow,
        isDark = true,
        panel = CockpitPanel,
        panelHigh = CockpitPanelHigh,
        outline = CockpitLine,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        textMuted = TextMuted,
        positive = SuccessGreen,
        warning = Color(0xFFFFC857)
    )
}

private val MotoShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val MotoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Light,
        fontStyle = FontStyle.Italic,
        fontSize = 76.sp,
        lineHeight = 78.sp,
        letterSpacing = (-3).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Light,
        fontStyle = FontStyle.Italic,
        fontSize = 54.sp,
        lineHeight = 56.sp,
        letterSpacing = (-2).sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        fontSize = 38.sp,
        lineHeight = 42.sp,
        letterSpacing = (-1).sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.2.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.4.sp
    )
)

@Composable
fun MotoTheme(
    darkTheme: Boolean,
    accentColor: AccentColor,
    content: @Composable () -> Unit
) {
    val accent = accentColor.palette()
    val palette = if (darkTheme) {
        MotoPalette(
            accent = accent.primary,
            accentSecondary = accent.secondary,
            glow = accent.glow,
            isDark = true,
            panel = CockpitPanel,
            panelHigh = CockpitPanelHigh,
            outline = CockpitLine,
            textPrimary = TextPrimary,
            textSecondary = TextSecondary,
            textMuted = TextMuted,
            positive = SuccessGreen,
            warning = Color(0xFFFFC857)
        )
    } else {
        MotoPalette(
            accent = accentColor.lightPrimary(),
            accentSecondary = accentColor.lightSecondary(),
            glow = accent.glow,
            isDark = false,
            panel = LightPanel,
            panelHigh = Color(0xFFF1F8FB),
            outline = LightLine,
            textPrimary = LightText,
            textSecondary = LightTextSecondary,
            textMuted = Color(0xFF647E88),
            positive = Color(0xFF0B8F68),
            warning = Color(0xFF9A6200)
        )
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = Color(0xFF04161B),
            primaryContainer = palette.accent.copy(alpha = 0.18f),
            onPrimaryContainer = palette.accentSecondary,
            secondary = palette.accentSecondary,
            onSecondary = Color(0xFF04161B),
            background = CockpitBlack,
            onBackground = palette.textPrimary,
            surface = palette.panel,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.panelHigh,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.outline,
            error = DangerRed
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = Color.White,
            primaryContainer = palette.accent.copy(alpha = 0.12f),
            onPrimaryContainer = palette.accent,
            secondary = palette.accentSecondary,
            onSecondary = Color.White,
            background = LightBackground,
            onBackground = palette.textPrimary,
            surface = palette.panel,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.panelHigh,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.outline,
            error = Color(0xFFC62828)
        )
    }

    CompositionLocalProvider(LocalMotoPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MotoTypography,
            shapes = MotoShapes,
            content = content
        )
    }
}
