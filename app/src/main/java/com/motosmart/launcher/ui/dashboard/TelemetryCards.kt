package com.motosmart.launcher.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motosmart.launcher.data.battery.BatteryState
import com.motosmart.launcher.data.location.GpsState
import com.motosmart.launcher.data.location.LocationLabelResolver
import com.motosmart.launcher.ui.components.DataLabel
import com.motosmart.launcher.ui.components.MotoPanel
import com.motosmart.launcher.ui.components.SectionLabel
import com.motosmart.launcher.ui.components.StatusPill
import com.motosmart.launcher.ui.theme.LocalMotoPalette
import com.motosmart.launcher.util.UnitFormatter

@Composable
fun BatteryCard(
    battery: BatteryState,
    modifier: Modifier = Modifier
) {
    val palette = LocalMotoPalette.current
    val accent = if (battery.isCharging) palette.positive else palette.accent
    MotoPanel(modifier = modifier) {
        SectionLabel(
            text = "BATTERY",
            trailing = if (battery.isCharging) "CHARGING" else "ON BATTERY"
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(54.dp, 27.dp)) {
                val stroke = 2.dp.toPx()
                val bodyWidth = size.width - 7.dp.toPx()
                val corner = CornerRadius(5.dp.toPx())
                drawRoundRect(
                    color = palette.outline,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(bodyWidth - stroke, size.height - stroke),
                    cornerRadius = corner,
                    style = Stroke(width = stroke)
                )
                drawRoundRect(
                    color = palette.outline,
                    topLeft = Offset(bodyWidth + 1.dp.toPx(), size.height * 0.32f),
                    size = Size(4.dp.toPx(), size.height * 0.36f),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                val fillWidth = (bodyWidth - 8.dp.toPx()) * (battery.percentage / 100f)
                if (fillWidth > 0f) {
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                        size = Size(fillWidth, size.height - 8.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = if (battery.isAvailable) "${battery.percentage}%" else "—",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp),
                color = palette.textPrimary
            )
            Spacer(Modifier.width(10.dp))
            StatusPill(
                text = if (battery.isCharging) "Charging" else "Battery",
                active = battery.isCharging,
                color = accent
            )
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { battery.percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = accent,
            trackColor = palette.outline.copy(alpha = 0.45f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = palette.outline.copy(alpha = 0.5f))
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BATTERY TEMP",
                style = MaterialTheme.typography.labelSmall,
                color = palette.textMuted
            )
            Text(
                text = UnitFormatter.batteryTemperature(battery.temperatureCelsius),
                style = MaterialTheme.typography.titleSmall,
                color = palette.textPrimary
            )
        }
    }
}

@Composable
fun GpsCard(
    gps: GpsState,
    locationLabel: String?,
    onRequestPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMotoPalette.current
    MotoPanel(modifier = modifier) {
        SectionLabel(
            text = "GPS SIGNAL",
            trailing = gps.accuracyMeters?.let { "±${it.toInt()} m" } ?: "—"
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.MyLocation,
                contentDescription = null,
                tint = if (gps.hasFix) palette.accent else palette.textMuted,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = locationLabel ?: "No location yet",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = gps.point?.let { LocationLabelResolver.formatCoordinates(it) }
                        ?: "Waiting for a position lock",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = palette.textSecondary,
                    maxLines = 1
                )
            }
            StatusPill(
                text = when {
                    !gps.hasPermission -> "Off"
                    !gps.providerEnabled -> "Services off"
                    gps.hasFix -> "Locked"
                    else -> "Search"
                },
                active = gps.hasFix,
                color = if (gps.hasFix) palette.positive else palette.warning
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DataLabel(text = "ACCURACY", value = UnitFormatter.accuracy(gps.accuracyMeters))
            DataLabel(text = "LAST FIX", value = UnitFormatter.relativeTime(gps.lastFixAtMillis))
            DataLabel(text = "TRACKING", value = if (gps.isTracking) "ACTIVE" else "IDLE")
        }
        if (!gps.hasPermission) {
            TextButton(onClick = onRequestPermission) {
                Text("ENABLE LOCATION", color = palette.accent)
            }
        } else if (!gps.providerEnabled) {
            TextButton(onClick = onOpenLocationSettings) {
                Text("TURN ON LOCATION SERVICES", color = palette.accent)
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Updates only while the dashboard is on screen.",
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted
        )
    }
}
