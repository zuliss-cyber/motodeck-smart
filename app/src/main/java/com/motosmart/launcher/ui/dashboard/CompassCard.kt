package com.motosmart.launcher.ui.dashboard

import android.graphics.Paint
import android.graphics.Typeface
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.ExploreOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motosmart.launcher.data.sensors.CompassState
import com.motosmart.launcher.ui.components.EmptyState
import com.motosmart.launcher.ui.components.MotoPanel
import com.motosmart.launcher.ui.components.SectionLabel
import com.motosmart.launcher.ui.components.StatusPill
import com.motosmart.launcher.ui.theme.LocalMotoPalette
import com.motosmart.launcher.util.HeadingUtils
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun CompassCard(
    compass: CompassState,
    modifier: Modifier = Modifier
) {
    val palette = LocalMotoPalette.current
    MotoPanel(modifier = modifier) {
        SectionLabel(
            text = "COMPASS",
            trailing = if (compass.isActive) "LIVE" else "STANDBY"
        )
        if (!compass.isAvailable) {
            EmptyState(
                icon = Icons.Outlined.ExploreOff,
                title = "No compass sensor",
                message = "Heading is unavailable on this device.",
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompassDial(
                    heading = compass.headingDegrees,
                    modifier = Modifier.size(154.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = HeadingUtils.bearing(compass.headingDegrees),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Light
                        ),
                        color = palette.textPrimary
                    )
                    Text(
                        text = HeadingUtils.cardinalName(compass.headingDegrees),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.accent
                    )
                    Spacer(Modifier.height(10.dp))
                    if (compass.accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
                        StatusPill(
                            text = "Calibrate",
                            active = false,
                            color = palette.warning
                        )
                    } else {
                        StatusPill(
                            text = HeadingUtils.cardinal(compass.headingDegrees),
                            active = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompassDial(
    heading: Float,
    modifier: Modifier = Modifier
) {
    val palette = LocalMotoPalette.current
    val cardinalColor = palette.textSecondary.toArgb()
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) / 2f - 8.dp.toPx()
        drawCircle(
            color = palette.outline.copy(alpha = 0.75f),
            radius = radius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawCircle(
            color = palette.panelHigh.copy(alpha = 0.7f),
            radius = radius - 4.dp.toPx(),
            center = center
        )

        for (index in 0 until 72) {
            val angle = index * 5f
            val isMajor = index % 9 == 0
            val isMedium = index % 3 == 0
            val length = when {
                isMajor -> 10.dp.toPx()
                isMedium -> 6.dp.toPx()
                else -> 3.dp.toPx()
            }
            val radians = Math.toRadians((angle - 90).toDouble())
            val outer = radius - 6.dp.toPx()
            val inner = outer - length
            drawLine(
                color = if (isMajor) palette.textSecondary else palette.textMuted.copy(alpha = 0.45f),
                start = Offset(
                    center.x + (cos(radians) * inner).toFloat(),
                    center.y + (sin(radians) * inner).toFloat()
                ),
                end = Offset(
                    center.x + (cos(radians) * outer).toFloat(),
                    center.y + (sin(radians) * outer).toFloat()
                ),
                strokeWidth = if (isMajor) 1.8.dp.toPx() else 0.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = 10.sp.toPx()
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = cardinalColor
        }
        val labelRadius = radius - 23.dp.toPx()
        listOf("N", "E", "S", "W").forEachIndexed { index, label ->
            val radians = Math.toRadians((index * 90 - 90).toDouble())
            val x = center.x + (cos(radians) * labelRadius).toFloat()
            val y = center.y + (sin(radians) * labelRadius).toFloat() + paint.textSize / 3f
            paint.color = if (label == "N") palette.accent.toArgb() else cardinalColor
            drawContext.canvas.nativeCanvas.drawText(label, x, y, paint)
        }

        rotate(degrees = heading) {
            val needleLength = radius * 0.62f
            val needleWidth = 8.dp.toPx()
            val tip = Offset(center.x, center.y - needleLength)
            val path = Path().apply {
                moveTo(tip.x, tip.y)
                lineTo(center.x - needleWidth, center.y + 8.dp.toPx())
                lineTo(center.x + needleWidth, center.y + 8.dp.toPx())
                close()
            }
            drawPath(path, color = palette.accent)
            drawLine(
                color = palette.textMuted.copy(alpha = 0.5f),
                start = Offset(center.x, center.y + 4.dp.toPx()),
                end = Offset(center.x, center.y + needleLength * 0.55f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        drawCircle(color = palette.panel, radius = 5.dp.toPx(), center = center)
        drawCircle(
            color = palette.accent,
            radius = 3.dp.toPx(),
            center = center
        )
        // Fixed reference marker at the top of the dial.
        val marker = Path().apply {
            moveTo(center.x, 1.dp.toPx())
            lineTo(center.x - 5.dp.toPx(), 9.dp.toPx())
            lineTo(center.x + 5.dp.toPx(), 9.dp.toPx())
            close()
        }
        drawPath(marker, color = palette.accentSecondary)
    }
}
