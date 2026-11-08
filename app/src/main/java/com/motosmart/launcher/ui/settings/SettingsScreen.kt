package com.motosmart.launcher.ui.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motosmart.launcher.BuildConfig
import com.motosmart.launcher.data.preferences.AccentColor
import com.motosmart.launcher.data.preferences.AppSettings
import com.motosmart.launcher.data.preferences.SpeedUnit
import com.motosmart.launcher.data.preferences.TemperatureUnit
import com.motosmart.launcher.data.preferences.ThemeMode
import com.motosmart.launcher.data.preferences.WeatherProvider
import com.motosmart.launcher.ui.components.MotoPanel
import com.motosmart.launcher.ui.components.MotoTextField
import com.motosmart.launcher.ui.components.ScreenHeader
import com.motosmart.launcher.ui.components.SectionLabel
import com.motosmart.launcher.ui.components.StatusPill
import com.motosmart.launcher.ui.theme.LocalMotoPalette
import com.motosmart.launcher.ui.theme.palette
import com.motosmart.launcher.util.AppWallpaperStore
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settings: AppSettings,
    isDefaultLauncher: Boolean,
    hasLocationPermission: Boolean,
    hasMusicAccess: Boolean,
    onBack: () -> Unit,
    onSetTheme: (ThemeMode) -> Unit,
    onSetAccent: (AccentColor) -> Unit,
    onSetSpeedUnit: (SpeedUnit) -> Unit,
    onSetTemperatureUnit: (TemperatureUnit) -> Unit,
    onSet24Hour: (Boolean) -> Unit,
    onSetShowCompass: (Boolean) -> Unit,
    onSetShowWeather: (Boolean) -> Unit,
    onSetShowMusic: (Boolean) -> Unit,
    onSetShowBattery: (Boolean) -> Unit,
    onSetShowGps: (Boolean) -> Unit,
    onSetKeepScreenOn: (Boolean) -> Unit,
    onSetRidingMode: (Boolean) -> Unit,
    onSetRidingAutoDark: (Boolean) -> Unit,
    onSetWeatherProvider: (WeatherProvider) -> Unit,
    onSetWeatherApiKey: (String) -> Unit,
    onRequestHomeRole: () -> Unit,
    onOpenHomeSettings: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onOpenDockEditor: () -> Unit,
    onResetAll: () -> Unit,
    onRequestExit: () -> Unit,
    onSetAppWallpaper: (Long) -> Unit
) {
    val palette = LocalMotoPalette.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }
    var applyingWallpaper by remember { mutableStateOf(false) }
    var apiKeyDraft by rememberSaveable { mutableStateOf(settings.weatherApiKey) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            applyingWallpaper = true
            scope.launch {
                val result = AppWallpaperStore.save(context, uri)
                applyingWallpaper = false
                result.fold(
                    onSuccess = {
                        onSetAppWallpaper(System.currentTimeMillis())
                        Toast.makeText(
                            context,
                            "Cockpit wallpaper updated",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onFailure = {
                        Toast.makeText(
                            context,
                            it.message ?: "Could not load the selected image",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }
    }

    LaunchedEffect(settings.weatherApiKey) {
        apiKeyDraft = settings.weatherApiKey
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 14.dp)
    ) {
        ScreenHeader(
            title = "Settings",
            subtitle = "Cockpit and permissions",
            onBack = onBack,
            trailing = {
                Icon(
                    imageVector = Icons.Outlined.Dashboard,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "appearance") {
                SettingsSection(icon = Icons.Outlined.Palette, title = "Appearance") {
                    SettingLabel("Theme")
                    Spacer(Modifier.height(6.dp))
                    ChoiceChips(
                        options = listOf(
                            ThemeMode.DARK to "Dark",
                            ThemeMode.LIGHT to "Light",
                            ThemeMode.SYSTEM to "System"
                        ),
                        selected = settings.themeMode,
                        onSelect = onSetTheme
                    )
                    Spacer(Modifier.height(14.dp))
                    SettingLabel("Accent colour")
                    Spacer(Modifier.height(8.dp))
                    AccentSwatches(
                        selected = settings.accentColor,
                        onSelect = onSetAccent
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = settings.accentColor.palette().label,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted
                    )
                }
            }

            item(key = "wallpaper") {
                SettingsSection(icon = Icons.Outlined.Wallpaper, title = "Cockpit wallpaper") {
                    val hasCustomWallpaper = settings.appWallpaperVersion > 0L
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(
                            text = if (hasCustomWallpaper) "Custom image" else "Default livery",
                            active = hasCustomWallpaper,
                            color = if (hasCustomWallpaper) palette.accent else palette.textMuted
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (hasCustomWallpaper) {
                                "Your photo is shown behind the cockpit panels."
                            } else {
                                "The standard racing livery is shown behind the cockpit panels."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "This changes the background inside MotoDeck//smart only. Your phone's home and lock screen wallpaper stay untouched. The image is copied into app-private storage, so no storage permission is needed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        enabled = !applyingWallpaper,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Wallpaper,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (hasCustomWallpaper) {
                                "REPLACE WALLPAPER"
                            } else {
                                "CHOOSE WALLPAPER"
                            }
                        )
                    }
                    if (applyingWallpaper) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Saving image…",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.accent
                        )
                    }
                    if (hasCustomWallpaper) {
                        TextButton(
                            onClick = {
                                applyingWallpaper = true
                                scope.launch {
                                    AppWallpaperStore.clear(context)
                                    applyingWallpaper = false
                                    onSetAppWallpaper(0L)
                                    Toast.makeText(
                                        context,
                                        "Cockpit wallpaper reset",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = !applyingWallpaper
                        ) {
                            Text("REMOVE WALLPAPER", color = palette.warning)
                        }
                    }
                }
            }

            item(key = "units") {
                SettingsSection(icon = Icons.Outlined.Speed, title = "Units and clock") {
                    SettingLabel("Speed unit")
                    Spacer(Modifier.height(6.dp))
                    ChoiceChips(
                        options = listOf(
                            SpeedUnit.KMH to "km/h",
                            SpeedUnit.MPH to "mph"
                        ),
                        selected = settings.speedUnit,
                        onSelect = onSetSpeedUnit
                    )
                    Spacer(Modifier.height(14.dp))
                    SettingLabel("Temperature unit")
                    Spacer(Modifier.height(6.dp))
                    ChoiceChips(
                        options = listOf(
                            TemperatureUnit.CELSIUS to "Celsius",
                            TemperatureUnit.FAHRENHEIT to "Fahrenheit"
                        ),
                        selected = settings.temperatureUnit,
                        onSelect = onSetTemperatureUnit
                    )
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = palette.outline.copy(alpha = 0.45f))
                    SwitchRow(
                        title = "24-hour clock",
                        subtitle = "Use 24-hour time instead of AM/PM.",
                        checked = settings.use24HourClock,
                        onCheckedChange = onSet24Hour
                    )
                }
            }

            item(key = "dashboard") {
                SettingsSection(icon = Icons.Outlined.Dashboard, title = "Dashboard panels") {
                    Text(
                        text = "Choose which cockpit panels are visible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    SwitchRow(
                        title = "Compass",
                        checked = settings.showCompass,
                        onCheckedChange = onSetShowCompass
                    )
                    SwitchRow(
                        title = "Weather",
                        checked = settings.showWeather,
                        onCheckedChange = onSetShowWeather
                    )
                    SwitchRow(
                        title = "Music",
                        checked = settings.showMusic,
                        onCheckedChange = onSetShowMusic
                    )
                    SwitchRow(
                        title = "Battery",
                        checked = settings.showBattery,
                        onCheckedChange = onSetShowBattery
                    )
                    SwitchRow(
                        title = "GPS signal",
                        checked = settings.showGps,
                        onCheckedChange = onSetShowGps
                    )
                    HorizontalDivider(color = palette.outline.copy(alpha = 0.45f))
                    SwitchRow(
                        title = "Keep screen awake",
                        subtitle = "Only while this launcher is on screen. Useful when powered by the bike.",
                        checked = settings.keepScreenOn,
                        onCheckedChange = onSetKeepScreenOn
                    )
                }
            }

            item(key = "riding") {
                SettingsSection(icon = Icons.Outlined.TwoWheeler, title = "Riding mode") {
                    SwitchRow(
                        title = "Riding mode",
                        subtitle = "Simplified, high-contrast cockpit with larger clock and speed.",
                        checked = settings.ridingMode,
                        onCheckedChange = onSetRidingMode
                    )
                    SwitchRow(
                        title = "Force dark cockpit",
                        subtitle = "Keeps glare low in riding mode, regardless of the light theme.",
                        checked = settings.ridingAutoDark,
                        onCheckedChange = onSetRidingAutoDark
                    )
                    Text(
                        text = "Configure everything before you ride. Do not interact with the screen while moving. GPS speed is an estimate and must never replace your instruments or your own judgement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.warning
                    )
                }
            }

            item(key = "home") {
                SettingsSection(icon = Icons.Outlined.Home, title = "Home screen") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(
                            text = if (isDefaultLauncher) "Default home" else "Not default",
                            active = isDefaultLauncher,
                            color = if (isDefaultLauncher) palette.positive else palette.warning
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isDefaultLauncher) {
                                "This app is your default home screen."
                            } else {
                                "Choose this launcher as the default home app."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (!isDefaultLauncher) {
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = onRequestHomeRole,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Home,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("SET AS DEFAULT HOME")
                        }
                    }
                    TextButton(onClick = onOpenHomeSettings) {
                        Text("OPEN HOME SCREEN SETTINGS", color = palette.accent)
                    }
                }
            }

            item(key = "permissions") {
                SettingsSection(icon = Icons.Outlined.LocationOn, title = "Location and permissions") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GPS permission",
                                style = MaterialTheme.typography.titleSmall,
                                color = palette.textPrimary
                            )
                            Text(
                                text = if (hasLocationPermission) {
                                    "Precise or approximate location is granted."
                                } else {
                                    "Required for GPS speed, local weather and navigation."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                        }
                        StatusPill(
                            text = if (hasLocationPermission) "Granted" else "Off",
                            active = hasLocationPermission,
                            color = if (hasLocationPermission) palette.positive else palette.warning
                        )
                    }
                    if (!hasLocationPermission) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onRequestLocationPermission,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("GRANT LOCATION")
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Used for GPS speed, compass location labels, local weather and navigation shortcuts. The launcher itself never uploads your location. Weather coordinates go only to your chosen provider.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = palette.outline.copy(alpha = 0.45f))
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Music notification access",
                                style = MaterialTheme.typography.titleSmall,
                                color = palette.textPrimary
                            )
                            Text(
                                text = "Reads the active media session for title, artist and transport controls.",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                        }
                        StatusPill(
                            text = if (hasMusicAccess) "Granted" else "Off",
                            active = hasMusicAccess,
                            color = if (hasMusicAccess) palette.positive else palette.warning
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onOpenNotificationAccess,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (hasMusicAccess) {
                                "NOTIFICATION ACCESS SETTINGS"
                            } else {
                                "ENABLE MUSIC ACCESS"
                            }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Notification content is never collected, stored or transmitted.",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted
                    )
                    TextButton(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            runCatching { context.startActivity(intent) }
                        }
                    ) {
                        Text("OPEN ANDROID APP SETTINGS", color = palette.accent)
                    }
                }
            }

            item(key = "weather") {
                SettingsSection(icon = Icons.Outlined.Cloud, title = "Weather") {
                    SettingLabel("Provider")
                    Spacer(Modifier.height(6.dp))
                    ChoiceChips(
                        options = listOf(
                            WeatherProvider.OPEN_METEO to "Open-Meteo",
                            WeatherProvider.OPEN_WEATHER_MAP to "OpenWeatherMap",
                            WeatherProvider.DISABLED to "Off"
                        ),
                        selected = settings.weatherProvider,
                        onSelect = onSetWeatherProvider
                    )
                    if (settings.weatherProvider == WeatherProvider.OPEN_WEATHER_MAP) {
                        Spacer(Modifier.height(12.dp))
                        SettingLabel("API key")
                        Spacer(Modifier.height(6.dp))
                        MotoTextField(
                            value = apiKeyDraft,
                            onValueChange = { apiKeyDraft = it },
                            placeholder = "Paste your OpenWeatherMap key",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { onSetWeatherApiKey(apiKeyDraft.trim()) },
                            enabled = apiKeyDraft.trim() != settings.weatherApiKey,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SAVE API KEY")
                        }
                        Text(
                            text = "Stored only in local DataStore storage on this device.",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textMuted
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Open-Meteo needs no key. OpenWeatherMap requires a free key from openweathermap.org. Weather is optional and the launcher works offline without it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                }
            }

            item(key = "dock") {
                SettingsSection(icon = Icons.Outlined.Apps, title = "Dock and shortcuts") {
                    Text(
                        text = "Choose up to six apps for the one-touch home dock. ${settings.dockApps.size} custom slot(s) saved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onOpenDockEditor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Apps,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("CUSTOMISE DOCK")
                    }
                }
            }

            item(key = "about") {
                SettingsSection(icon = Icons.Outlined.Info, title = "About") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MotoDeck//smart",
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = palette.accent
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "A motorcycle-focused Android home screen with GPS instrumentation, navigation shortcuts, media controls and a customisable app dock.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "No analytics, no accounts and no location uploads. Preferences stay in local DataStore storage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                }
            }

            item(key = "reset") {
                SettingsSection(icon = Icons.Outlined.RestartAlt, title = "Reset") {
                    Text(
                        text = "Restores the default theme, units, dock and weather preferences. This cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.warning.copy(alpha = 0.16f),
                            contentColor = palette.warning
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("RESET ALL SETTINGS")
                    }
                }
            }

            item(key = "exit") {
                SettingsSection(icon = Icons.Outlined.Logout, title = "Exit") {
                    Text(
                        text = if (isDefaultLauncher) {
                            "This launcher is your default home screen. Exiting opens Android's Home app settings so you can choose another launcher."
                        } else {
                            "Close MotoDeck//smart and return to the previous app or the system home screen."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onRequestExit,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.warning.copy(alpha = 0.16f),
                            contentColor = palette.warning
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isDefaultLauncher) "EXIT TO HOME SETTINGS" else "EXIT LAUNCHER")
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset all settings?") },
            text = {
                Text(
                    "This restores the default theme, units, dock and weather preferences. It cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetAll()
                    }
                ) {
                    Text("RESET", color = palette.warning)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL", color = palette.textSecondary)
                }
            }
        )
    }

}

@Composable
private fun SettingsSection(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalMotoPalette.current
    MotoPanel(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = palette.textSecondary
            )
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SettingLabel(text: String) {
    val palette = LocalMotoPalette.current
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = palette.textPrimary
    )
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val palette = LocalMotoPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = palette.textPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = palette.accent,
                checkedTrackColor = palette.accent.copy(alpha = 0.35f),
                uncheckedThumbColor = palette.textSecondary,
                uncheckedTrackColor = palette.outline
            )
        )
    }
}

@Composable
private fun <T> ChoiceChips(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    val palette = LocalMotoPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = palette.panelHigh,
                    labelColor = palette.textSecondary,
                    selectedContainerColor = palette.accent.copy(alpha = 0.18f),
                    selectedLabelColor = palette.accent
                )
            )
        }
    }
}

@Composable
private fun AccentSwatches(
    selected: AccentColor,
    onSelect: (AccentColor) -> Unit
) {
    val palette = LocalMotoPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AccentColor.entries.forEach { color ->
            val accent = color.palette()
            val isSelected = color == selected
            val onSwatch = if (accent.primary.luminance() > 0.55f) {
                Color(0xFF04161B)
            } else {
                Color.White
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accent.primary)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) palette.textPrimary else palette.outline,
                        shape = CircleShape
                    )
                    .clickable { onSelect(color) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = accent.label,
                        tint = onSwatch,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
