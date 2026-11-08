package com.motosmart.launcher.ui.dashboard

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
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motosmart.launcher.data.preferences.TemperatureUnit
import com.motosmart.launcher.data.preferences.WeatherProvider
import com.motosmart.launcher.data.weather.WeatherCondition
import com.motosmart.launcher.data.weather.WeatherData
import com.motosmart.launcher.ui.components.MotoPanel
import com.motosmart.launcher.ui.components.SectionLabel
import com.motosmart.launcher.ui.theme.LocalMotoPalette
import com.motosmart.launcher.util.UnitFormatter

@Composable
fun WeatherCard(
    state: WeatherUiState,
    temperatureUnit: TemperatureUnit,
    provider: WeatherProvider,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMotoPalette.current
    MotoPanel(modifier = modifier) {
        SectionLabel(
            text = "WEATHER",
            trailing = when (provider) {
                WeatherProvider.OPEN_METEO -> "OPEN-METEO"
                WeatherProvider.OPEN_WEATHER_MAP -> "OWM"
                WeatherProvider.DISABLED -> "OFF"
            }
        )
        Spacer(Modifier.height(10.dp))

        when (state) {
            is WeatherUiState.Ready -> WeatherContent(
                data = state.data,
                temperatureUnit = temperatureUnit,
                onRefresh = onRefresh
            )
            WeatherUiState.Loading -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = palette.accent
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Updating local weather",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
            }
            is WeatherUiState.Error -> Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Cloud,
                        contentDescription = null,
                        tint = palette.warning,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Weather unavailable",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textPrimary
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                TextButton(onClick = onRefresh) {
                    Text("RETRY", color = palette.accent)
                }
            }
            WeatherUiState.Disabled -> Text(
                text = "Weather is disabled in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary
            )
            WeatherUiState.Idle -> {
                Text(
                    text = "Enable GPS to load local weather.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary
                )
                Spacer(Modifier.height(6.dp))
                if (!hasLocationPermission) {
                    TextButton(onClick = onRequestPermission) {
                        Text("ENABLE LOCATION", color = palette.accent)
                    }
                } else {
                    TextButton(onClick = onRefresh) {
                        Text("REFRESH", color = palette.accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherContent(
    data: WeatherData,
    temperatureUnit: TemperatureUnit,
    onRefresh: () -> Unit
) {
    val palette = LocalMotoPalette.current
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = iconFor(data.condition),
                contentDescription = data.description,
                tint = palette.accent,
                modifier = Modifier.size(42.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = UnitFormatter.temperature(data.temperatureCelsius, temperatureUnit).toString(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Light
                        ),
                        color = palette.textPrimary
                    )
                    Text(
                        text = UnitFormatter.temperatureSymbol(temperatureUnit),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.accent,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    text = data.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Refresh weather",
                    tint = palette.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = palette.textMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = data.locationLabel,
                style = MaterialTheme.typography.bodySmall,
                color = palette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = palette.outline.copy(alpha = 0.5f))
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WeatherMetric(
                icon = Icons.Outlined.WaterDrop,
                label = "HUMIDITY",
                value = data.humidityPercent?.let { "$it%" } ?: "—"
            )
            WeatherMetric(
                icon = Icons.Outlined.Air,
                label = "WIND",
                value = data.windKmh?.let { "${it.toInt()} km/h" } ?: "—"
            )
            WeatherMetric(
                icon = Icons.Outlined.Cloud,
                label = "FEELS",
                value = data.feelsLikeCelsius?.let {
                    "${UnitFormatter.temperature(it, temperatureUnit)}°"
                } ?: "—"
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Updated ${UnitFormatter.relativeTime(data.fetchedAtMillis)} · ${data.providerLabel()}",
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted
        )
    }
}

@Composable
private fun WeatherMetric(icon: ImageVector, label: String, value: String) {
    val palette = LocalMotoPalette.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = palette.textMuted,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
            Text(
                value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = palette.textPrimary
            )
        }
    }
}

private fun WeatherData.providerLabel(): String = when (provider) {
    WeatherProvider.OPEN_METEO -> "Open-Meteo"
    WeatherProvider.OPEN_WEATHER_MAP -> "OpenWeatherMap"
    WeatherProvider.DISABLED -> "Disabled"
}

private fun iconFor(condition: WeatherCondition): ImageVector = when (condition) {
    WeatherCondition.CLEAR -> Icons.Outlined.WbSunny
    WeatherCondition.PARTLY_CLOUDY -> Icons.Outlined.WbSunny
    WeatherCondition.CLOUDY -> Icons.Outlined.Cloud
    WeatherCondition.FOG -> Icons.Outlined.Cloud
    WeatherCondition.DRIZZLE -> Icons.Outlined.Grain
    WeatherCondition.RAIN -> Icons.Outlined.WaterDrop
    WeatherCondition.SHOWERS -> Icons.Outlined.WaterDrop
    WeatherCondition.SNOW -> Icons.Outlined.AcUnit
    WeatherCondition.THUNDER -> Icons.Outlined.Thunderstorm
    WeatherCondition.UNKNOWN -> Icons.Outlined.Cloud
}
