package com.motosmart.launcher.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motosmart.launcher.data.location.GpsState
import com.motosmart.launcher.data.preferences.SpeedUnit
import com.motosmart.launcher.ui.components.DataLabel
import com.motosmart.launcher.ui.components.MotoPanel
import com.motosmart.launcher.ui.components.SectionLabel
import com.motosmart.launcher.ui.components.StatusPill
import com.motosmart.launcher.ui.theme.LocalMotoPalette
import com.motosmart.launcher.util.UnitFormatter
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun SpeedometerCard(
    gps: GpsState,
    speedUnit: SpeedUnit,
    ridingMode: Boolean,
    onRequestPermission: () -> Unit,
    onResetSession: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val palette = LocalMotoPalette.current
    val displayedSpeed = UnitFormatter.speedValue(gps.speedKmh, speedUnit)
    val maximum = UnitFormatter.gaugeMaximum(speedUnit)
    val targetRatio = (displayedSpeed.toFloat() / maximum.toFloat()).coerceIn(0f, 1f)
    val animatedRatio by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = tween(durationMillis = if (ridingMode) 120 else 650),
        label = "speedRatio"
    )
    val ratio = if (ridingMode) targetRatio else animatedRatio
    val locked = gps.hasFix && gps.speedKmh >= 0f && gps.hasPermission
    val gaugeHeight = when {
        compact -> 150.dp
        ridingMode -> 292.dp
        else -> 254.dp
    }
    val speedSize = when {
        compact -> 56.sp
        ridingMode -> 106.sp
        else -> 82.sp
    }

    MotoPanel(
        modifier = modifier,
        glow = true,
        accentEdge = true,
        padding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = if (compact) 14.dp else 18.dp,
            vertical = if (compact) 10.dp else 16.dp
        )
    ) {
        SectionLabel(
            text = "GPS SPEED",
            trailing = UnitFormatter.speedSymbol(speedUnit).uppercase()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(gaugeHeight),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val glowStroke = if (compact) 18.dp.toPx() else 30.dp.toPx()
                val stroke = if (compact) 9.dp.toPx() else 12.dp.toPx()
                val inset = glowStroke / 2 + 4.dp.toPx()
                val diameter = min(size.width, size.height) - inset * 2
                val topLeft = Offset(
                    (size.width - diameter) / 2,
                    (size.height - diameter) / 2
                )
                val arcSize = Size(diameter, diameter)
                val startAngle = 140f
                val maxSweep = 260f
                val segments = 28
                val gap = 1.4f
                val segmentSweep = maxSweep / segments
                val center = Offset(size.width / 2f, size.height / 2f)

                // Outer technical ring.
                drawArc(
                    color = palette.outline.copy(alpha = 0.35f),
                    startAngle = startAngle - 4f,
                    sweepAngle = maxSweep + 8f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Redline zone.
                drawArc(
                    color = palette.warning.copy(alpha = 0.16f),
                    startAngle = startAngle + maxSweep * 0.8f,
                    sweepAngle = maxSweep * 0.2f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = glowStroke, cap = StrokeCap.Butt)
                )

                if (ratio > 0.005f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                palette.accent.copy(alpha = 0.10f),
                                palette.accent.copy(alpha = 0.22f),
                                palette.warning.copy(alpha = 0.22f)
                            ),
                            center = center
                        ),
                        startAngle = startAngle,
                        sweepAngle = maxSweep * ratio,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = glowStroke, cap = StrokeCap.Butt)
                    )
                }

                // Segmented tachometer-style power band.
                for (index in 0 until segments) {
                    val segmentStart = startAngle + segmentSweep * index + gap / 2f
                    val segmentEndRatio = (index + 1f) / segments
                    val lit = ratio >= index.toFloat() / segments
                    val color = when {
                        lit && segmentEndRatio > 0.8f -> palette.warning
                        lit -> palette.accent
                        else -> palette.outline.copy(alpha = 0.32f)
                    }
                    drawArc(
                        color = color,
                        startAngle = segmentStart,
                        sweepAngle = segmentSweep - gap,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(
                            width = if (lit) stroke else stroke * 0.72f,
                            cap = StrokeCap.Butt
                        )
                    )
                }

                val tickRadius = diameter / 2f
                for (index in 0..20) {
                    val tickAngle = startAngle + maxSweep * (index / 20f)
                    val isMajor = index % 5 == 0
                    val length = if (isMajor) 12.dp.toPx() else 5.dp.toPx()
                    val outer = tickRadius - 18.dp.toPx()
                    val inner = outer - length
                    val radians = Math.toRadians(tickAngle.toDouble())
                    drawLine(
                        color = if (isMajor) {
                            palette.textSecondary.copy(alpha = 0.75f)
                        } else {
                            palette.textMuted.copy(alpha = 0.35f)
                        },
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

                // Sharp racing needle.
                rotate(
                    degrees = startAngle + maxSweep * ratio,
                    pivot = center
                ) {
                    val needleLength = tickRadius - 26.dp.toPx()
                    val path = Path().apply {
                        moveTo(center.x, center.y - needleLength)
                        lineTo(center.x - 4.5.dp.toPx(), center.y + 4.dp.toPx())
                        lineTo(center.x + 4.5.dp.toPx(), center.y + 4.dp.toPx())
                        close()
                    }
                    drawPath(path, color = palette.accent)
                }
                drawCircle(
                    color = palette.panelHigh,
                    radius = 8.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = palette.accent,
                    radius = 3.dp.toPx(),
                    center = center
                )
            }

            Column(
                modifier = Modifier.padding(top = if (compact) 6.dp else 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = displayedSpeed.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = speedSize,
                        lineHeight = (speedSize.value * 1.0f).sp,
                        fontWeight = FontWeight.Light,
                        fontStyle = FontStyle.Italic,
                        shadow = Shadow(
                            color = palette.accent.copy(alpha = 0.5f),
                            blurRadius = 28f
                        )
                    ),
                    color = palette.textPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = UnitFormatter.speedSymbol(speedUnit),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = if (compact) 8.sp else 10.sp,
                        letterSpacing = 2.sp
                    ),
                    color = palette.accent
                )
                Spacer(Modifier.height(if (compact) 2.dp else 8.dp))
                StatusPill(
                    text = when {
                        !gps.hasPermission -> "No permission"
                        !gps.providerEnabled -> "Location off"
                        gps.hasFix -> "GPS lock"
                        else -> "Waiting"
                    },
                    active = locked,
                    color = if (locked) palette.accent else palette.warning
                )
            }
        }

        if (!ridingMode) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DataLabel(
                    text = "MAX",
                    value = "${UnitFormatter.speedValue(gps.maxSpeedKmh, speedUnit)}"
                )
                DataLabel(
                    text = "AVG",
                    value = "${UnitFormatter.speedValue(gps.averageSpeedKmh, speedUnit)}"
                )
                DataLabel(
                    text = "DIST",
                    value = "${UnitFormatter.distanceKm(gps.distanceKm)} km"
                )
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = palette.outline.copy(alpha = 0.55f))
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GPS-estimated, not an ECU reading.",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.2.sp),
                    color = palette.textMuted,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onResetSession) {
                    Text(
                        text = "RESET",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.accent
                    )
                }
            }
        } else {
            Spacer(Modifier.height(if (compact) 2.dp else 6.dp))
            Text(
                text = "GPS-ESTIMATED · NOT AN ECU READING",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = if (compact) 7.sp else 10.sp,
                    letterSpacing = 0.6.sp
                ),
                color = palette.textMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        if (!gps.hasPermission) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Location permission is required for GPS speed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onRequestPermission) {
                    Text("ENABLE", color = palette.accent)
                }
            }
        }
    }
}
