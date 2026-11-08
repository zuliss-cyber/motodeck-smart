package com.motosmart.launcher.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motosmart.launcher.ui.theme.LocalMotoPalette
import com.motosmart.launcher.util.AppWallpaperStore

@Composable
fun CockpitBackground(
    modifier: Modifier = Modifier,
    wallpaperVersion: Long = 0L,
    content: @Composable () -> Unit
) {
    val palette = LocalMotoPalette.current
    val context = LocalContext.current
    var wallpaper by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(wallpaperVersion) {
        wallpaper = if (wallpaperVersion > 0L) AppWallpaperStore.load(context) else null
    }
    val base = if (palette.isDark) {
        Brush.verticalGradient(
            0f to Color(0xFF040E12),
            0.45f to Color(0xFF02090B),
            1f to Color(0xFF010607)
        )
    } else {
        Brush.verticalGradient(
            0f to Color(0xFFF4FAFD),
            0.5f to Color(0xFFE6F0F5),
            1f to Color(0xFFDCEAF1)
        )
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(base)
    ) {
        wallpaper?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Keep cockpit text readable over any photo without hiding it completely.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = if (palette.isDark) {
                                listOf(
                                    Color.Black.copy(alpha = 0.62f),
                                    Color.Black.copy(alpha = 0.74f),
                                    Color.Black.copy(alpha = 0.82f)
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.70f),
                                    Color.White.copy(alpha = 0.78f),
                                    Color.White.copy(alpha = 0.86f)
                                )
                            }
                        )
                    )
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                // Quiet technical grid; the icy palette carries the theme instead of livery stripes.
                val gridColor = palette.outline.copy(
                    alpha = if (palette.isDark) 0.035f else 0.03f
                )
                val gridStep = 52.dp.toPx()
                var gridX = 0f
                while (gridX <= size.width) {
                    drawLine(
                        color = gridColor,
                        start = Offset(gridX, 0f),
                        end = Offset(gridX, size.height),
                        strokeWidth = 0.5.dp.toPx()
                    )
                    gridX += gridStep
                }
                var gridY = 0f
                while (gridY <= size.height) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, gridY),
                        end = Offset(size.width, gridY),
                        strokeWidth = 0.5.dp.toPx()
                    )
                    gridY += gridStep
                }

                // Icy aurora glow in the upper-right corner.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.glow.copy(alpha = if (palette.isDark) 0.16f else 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.84f, size.height * 0.04f),
                        radius = size.width * 0.78f
                    ),
                    radius = size.width * 0.78f,
                    center = Offset(size.width * 0.84f, size.height * 0.04f)
                )

                // Cool teal wash rising from the bottom-left.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            palette.accentSecondary.copy(alpha = if (palette.isDark) 0.07f else 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.02f, size.height * 0.96f),
                        radius = size.width * 0.72f
                    ),
                    radius = size.width * 0.72f,
                    center = Offset(size.width * 0.02f, size.height * 0.96f)
                )

                // Faint instrument rings anchored off-screen, like a HUD visor.
                drawCircle(
                    color = palette.outline.copy(alpha = if (palette.isDark) 0.20f else 0.28f),
                    radius = size.width * 0.42f,
                    center = Offset(size.width * 0.96f, size.height * 0.16f),
                    style = Stroke(width = 1.dp.toPx())
                )
                drawCircle(
                    color = palette.outline.copy(alpha = if (palette.isDark) 0.13f else 0.20f),
                    radius = size.width * 0.58f,
                    center = Offset(size.width * 0.96f, size.height * 0.16f),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Thin icy horizon line across the very top.
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            palette.accent.copy(alpha = 0.50f),
                            palette.glow.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.5.dp.toPx()
                )
                }
        ) {
            content()
        }
    }
}

@Composable
fun MotoPanel(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(18.dp),
    elevated: Boolean = false,
    glow: Boolean = false,
    accentEdge: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalMotoPalette.current
    val shape = RoundedCornerShape(24.dp)
    val borderBrush = Brush.linearGradient(
        listOf(
            if (glow) {
                palette.accent.copy(alpha = 0.55f)
            } else {
                palette.outline.copy(alpha = 0.60f)
            },
            palette.outline.copy(alpha = if (glow) 0.55f else 0.24f),
            palette.accentSecondary.copy(alpha = if (glow) 0.22f else 0.10f)
        )
    )
    val shadowModifier = if (glow && palette.isDark) {
        Modifier.shadow(
            elevation = 22.dp,
            shape = shape,
            ambientColor = palette.glow.copy(alpha = 0.30f),
            spotColor = palette.glow.copy(alpha = 0.40f)
        )
    } else {
        Modifier
    }
    Surface(
        modifier = modifier
            .then(shadowModifier)
            .clip(shape)
            .border(1.dp, borderBrush, shape),
        color = if (elevated) palette.panelHigh else palette.panel.copy(
            alpha = if (palette.isDark) 0.88f else 0.96f
        ),
        shape = shape
    ) {
        Box {
            if (accentEdge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(0.32f)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    palette.accent,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
            Column(
                modifier = Modifier.padding(padding),
                content = content
            )
        }
    }
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    accent: Boolean = true
) {
    val palette = LocalMotoPalette.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (accent) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(11.dp)
                    .clip(RoundedCornerShape(50))
                    .background(palette.accent)
            )
            Spacer(Modifier.width(7.dp))
        }
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = palette.textSecondary,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelSmall,
                color = palette.textMuted
            )
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    active: Boolean = true,
    color: Color? = null,
    icon: ImageVector? = null
) {
    val palette = LocalMotoPalette.current
    val resolved = color ?: if (active) palette.accent else palette.textMuted
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(resolved.copy(alpha = if (palette.isDark) 0.12f else 0.10f))
            .border(1.dp, resolved.copy(alpha = 0.22f), shape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = resolved,
                modifier = Modifier.size(13.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(resolved)
            )
        }
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = resolved,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DataLabel(text: String, value: String, modifier: Modifier = Modifier) {
    val palette = LocalMotoPalette.current
    Column(modifier = modifier) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontStyle = FontStyle.Italic
            ),
            color = palette.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    val palette = LocalMotoPalette.current
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(palette.accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = palette.accent, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = palette.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary,
            textAlign = TextAlign.Center
        )
        if (action != null) {
            Spacer(Modifier.height(16.dp))
            action()
        }
    }
}

/** A soft accent halo used behind gauges and key figures. */
@Composable
fun GlowHalo(
    modifier: Modifier = Modifier,
    intensity: Float = 0.22f,
    size: Dp = 280.dp
) {
    val palette = LocalMotoPalette.current
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { alpha = intensity }
            .background(
                Brush.radialGradient(
                    listOf(palette.glow, Color.Transparent)
                ),
                CircleShape
            )
    )
}
