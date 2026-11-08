package com.motosmart.launcher.ui.theme

import androidx.compose.ui.graphics.Color
import com.motosmart.launcher.data.preferences.AccentColor

// Deep teal-black cockpit surfaces inspired by icy HUD instrumentation.
val CockpitBlack = Color(0xFF02090B)
val CockpitGraphite = Color(0xFF071417)
val CockpitPanel = Color(0xFF0A1B20)
val CockpitPanelHigh = Color(0xFF102830)
val CockpitLine = Color(0xFF1D3C45)
val TextPrimary = Color(0xFFEFF8FC)
val TextSecondary = Color(0xFF9DBBC6)
val TextMuted = Color(0xFF61828D)
val DangerRed = Color(0xFFFF5C63)
val SuccessGreen = Color(0xFF57E3B0)

val IcyBlue = Color(0xFFB9E2F2)
val IcyWhite = Color(0xFFE4F7FF)
val IcyGlow = Color(0xFF5EC8E8)

val LightBackground = Color(0xFFE6F0F5)
val LightSurface = Color(0xFFF8FCFE)
val LightPanel = Color(0xFFFFFFFF)
val LightLine = Color(0xFFC6DCE6)
val LightText = Color(0xFF04161B)
val LightTextSecondary = Color(0xFF47626C)

data class AccentPalette(
    val primary: Color,
    val secondary: Color,
    val glow: Color,
    val label: String
)

fun AccentColor.palette(): AccentPalette = when (this) {
    AccentColor.ICE -> AccentPalette(
        primary = IcyBlue,
        secondary = IcyWhite,
        glow = IcyGlow,
        label = "Ice Blue"
    )
    AccentColor.ORANGE -> AccentPalette(
        primary = Color(0xFFFF6A00),
        secondary = Color(0xFFFFA149),
        glow = Color(0xFFFF6A00),
        label = "Ignition Orange"
    )
    AccentColor.RED -> AccentPalette(
        primary = Color(0xFFFF3B30),
        secondary = Color(0xFFFF7A70),
        glow = Color(0xFFFF3B30),
        label = "Race Red"
    )
    AccentColor.AMBER -> AccentPalette(
        primary = Color(0xFFFFC107),
        secondary = Color(0xFFFFDC6B),
        glow = Color(0xFFFFC107),
        label = "Amber"
    )
    AccentColor.CYAN -> AccentPalette(
        primary = Color(0xFF00E5FF),
        secondary = Color(0xFF7DF6FF),
        glow = Color(0xFF00E5FF),
        label = "Cyan"
    )
    AccentColor.LIME -> AccentPalette(
        primary = Color(0xFFB6FF3B),
        secondary = Color(0xFFD4FF8A),
        glow = Color(0xFFB6FF3B),
        label = "Lime"
    )
}

/** Darker accent variants so text and icons stay legible on the light cockpit theme. */
fun AccentColor.lightPrimary(): Color = when (this) {
    AccentColor.ICE -> Color(0xFF136B84)
    AccentColor.ORANGE -> Color(0xFFB04A00)
    AccentColor.RED -> Color(0xFFB3261E)
    AccentColor.AMBER -> Color(0xFF8A5F00)
    AccentColor.CYAN -> Color(0xFF00707F)
    AccentColor.LIME -> Color(0xFF537300)
}

fun AccentColor.lightSecondary(): Color = when (this) {
    AccentColor.ICE -> Color(0xFF2A93AE)
    AccentColor.ORANGE -> Color(0xFFD06414)
    AccentColor.RED -> Color(0xFFD1453C)
    AccentColor.AMBER -> Color(0xFFA97900)
    AccentColor.CYAN -> Color(0xFF0093A6)
    AccentColor.LIME -> Color(0xFF6B8F00)
}
