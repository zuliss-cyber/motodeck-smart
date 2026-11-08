package com.motosmart.launcher.ui.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motosmart.launcher.data.applications.AppEntry
import com.motosmart.launcher.data.battery.BatteryState
import com.motosmart.launcher.data.location.GpsState
import com.motosmart.launcher.data.music.MusicState
import com.motosmart.launcher.data.preferences.AppSettings
import com.motosmart.launcher.data.preferences.SavedDestination
import com.motosmart.launcher.data.sensors.CompassState
import com.motosmart.launcher.ui.components.MotoPanel
import com.motosmart.launcher.ui.components.SectionLabel
import com.motosmart.launcher.ui.components.StatusPill
import com.motosmart.launcher.ui.theme.LocalMotoPalette
import com.motosmart.launcher.util.HeadingUtils

@Composable
fun DashboardScreen(
    settings: AppSettings,
    gps: GpsState,
    compass: CompassState,
    battery: BatteryState,
    weather: WeatherUiState,
    locationLabel: String?,
    music: MusicState,
    dockApps: List<AppEntry>,
    isDefaultLauncher: Boolean,
    onOpenDrawer: () -> Unit,
    onOpenNavigation: () -> Unit,
    onOpenSettings: () -> Unit,
    onRequestExit: () -> Unit,
    onSetRidingMode: (Boolean) -> Unit,
    onRequestLocationPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onRefreshWeather: () -> Unit,
    onResetSession: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
    onEnableMusicAccess: () -> Unit,
    onOpenMusicApp: () -> Unit,
    onPlayPauseMusic: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onLaunchApp: (AppEntry) -> Unit,
    onNavigateSaved: (SavedDestination) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 14.dp)
    ) {
        LauncherTopBar(
            gps = gps,
            battery = battery,
            ridingMode = settings.ridingMode,
            onToggleRidingMode = { onSetRidingMode(!settings.ridingMode) },
            onOpenSettings = onOpenSettings,
            onRequestExit = onRequestExit
        )
        if (!isDefaultLauncher && !settings.ridingMode) {
            DefaultHomeBanner(onClick = onSetDefaultLauncher)
        }
        Box(modifier = Modifier.weight(1f)) {
            if (settings.ridingMode) {
                RidingDashboard(
                    settings = settings,
                    gps = gps,
                    compass = compass,
                    music = music,
                    onRequestLocationPermission = onRequestLocationPermission,
                    onResetSession = onResetSession,
                    onEnableMusicAccess = onEnableMusicAccess,
                    onOpenMusicApp = onOpenMusicApp,
                    onPlayPauseMusic = onPlayPauseMusic,
                    onNextTrack = onNextTrack,
                    onPreviousTrack = onPreviousTrack
                )
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    if (maxWidth >= 760.dp) {
                        WideDashboard(
                            settings = settings,
                            gps = gps,
                            compass = compass,
                            battery = battery,
                            weather = weather,
                            locationLabel = locationLabel,
                            music = music,
                            onRequestLocationPermission = onRequestLocationPermission,
                            onOpenLocationSettings = onOpenLocationSettings,
                            onRefreshWeather = onRefreshWeather,
                            onResetSession = onResetSession,
                            onOpenNavigation = onOpenNavigation,
                            onEnableMusicAccess = onEnableMusicAccess,
                            onOpenMusicApp = onOpenMusicApp,
                            onPlayPauseMusic = onPlayPauseMusic,
                            onNextTrack = onNextTrack,
                            onPreviousTrack = onPreviousTrack,
                            onNavigateSaved = onNavigateSaved
                        )
                    } else {
                        PortraitDashboard(
                            settings = settings,
                            gps = gps,
                            compass = compass,
                            battery = battery,
                            weather = weather,
                            locationLabel = locationLabel,
                            music = music,
                            onRequestLocationPermission = onRequestLocationPermission,
                            onOpenLocationSettings = onOpenLocationSettings,
                            onRefreshWeather = onRefreshWeather,
                            onResetSession = onResetSession,
                            onOpenNavigation = onOpenNavigation,
                            onEnableMusicAccess = onEnableMusicAccess,
                            onOpenMusicApp = onOpenMusicApp,
                            onPlayPauseMusic = onPlayPauseMusic,
                            onNextTrack = onNextTrack,
                            onPreviousTrack = onPreviousTrack,
                            onNavigateSaved = onNavigateSaved
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        DockBar(
            dockApps = dockApps,
            ridingMode = settings.ridingMode,
            onLaunch = onLaunchApp,
            onOpenDrawer = onOpenDrawer,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun LauncherTopBar(
    gps: GpsState,
    battery: BatteryState,
    ridingMode: Boolean,
    onToggleRidingMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onRequestExit: () -> Unit
) {
    val palette = LocalMotoPalette.current
    var menuExpanded by remember { mutableStateOf(false) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        val showBattery = maxWidth >= 420.dp
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(50))
                    .background(palette.accent)
            )
            Spacer(Modifier.width(9.dp))
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "MOTODECK",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp
                        ),
                        color = palette.textPrimary
                    )
                    Text(
                        text = "//SMART",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = palette.accent,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Text(
                    text = if (ridingMode) "RIDE MODE // ARMED" else "RACE CONTROL // HOME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        letterSpacing = 1.5.sp
                    ),
                    color = palette.textMuted
                )
            }
            Spacer(Modifier.weight(1f))
            StatusPill(
                text = if (gps.hasFix) "GPS" else "NO GPS",
                active = gps.hasFix,
                color = if (gps.hasFix) palette.positive else palette.warning
            )
            if (showBattery) {
                Spacer(Modifier.width(5.dp))
                StatusPill(
                    text = if (battery.isAvailable) "${battery.percentage}%" else "—",
                    active = battery.isCharging,
                    color = if (battery.isCharging) palette.positive else palette.textSecondary
                )
            }
            IconButton(onClick = onToggleRidingMode, modifier = Modifier.size(42.dp)) {
                Icon(
                    imageVector = Icons.Outlined.TwoWheeler,
                    contentDescription = if (ridingMode) "Exit riding mode" else "Riding mode",
                    tint = if (ridingMode) palette.accent else palette.textSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "More options",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Settings",
                                style = MaterialTheme.typography.titleSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null,
                                tint = palette.textSecondary
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenSettings()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Exit MotoDeck//smart",
                                style = MaterialTheme.typography.titleSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Logout,
                                contentDescription = null,
                                tint = palette.warning
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onRequestExit()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultHomeBanner(onClick: () -> Unit) {
    val palette = LocalMotoPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(palette.accent.copy(alpha = 0.09f))
            .border(1.dp, palette.accent.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Home,
            contentDescription = null,
            tint = palette.accent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Make this your home screen",
                style = MaterialTheme.typography.titleSmall,
                color = palette.textPrimary
            )
            Text(
                text = "Set MotoDeck//smart as your default home app.",
                style = MaterialTheme.typography.labelSmall,
                color = palette.textSecondary
            )
        }
        Text(
            text = "SET UP",
            style = MaterialTheme.typography.labelSmall,
            color = palette.accent
        )
    }
}

@Composable
private fun PortraitDashboard(
    settings: AppSettings,
    gps: GpsState,
    compass: CompassState,
    battery: BatteryState,
    weather: WeatherUiState,
    locationLabel: String?,
    music: MusicState,
    onRequestLocationPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onRefreshWeather: () -> Unit,
    onResetSession: () -> Unit,
    onOpenNavigation: () -> Unit,
    onEnableMusicAccess: () -> Unit,
    onOpenMusicApp: () -> Unit,
    onPlayPauseMusic: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onNavigateSaved: (SavedDestination) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LiveClockCard(use24Hour = settings.use24HourClock)
        SpeedometerCard(
            gps = gps,
            speedUnit = settings.speedUnit,
            ridingMode = false,
            onRequestPermission = onRequestLocationPermission,
            onResetSession = onResetSession
        )
        if (settings.showCompass) {
            CompassCard(compass = compass)
        }
        if (settings.showWeather) {
            WeatherCard(
                state = weather,
                temperatureUnit = settings.temperatureUnit,
                provider = settings.weatherProvider,
                hasLocationPermission = gps.hasPermission,
                onRequestPermission = onRequestLocationPermission,
                onRefresh = onRefreshWeather
            )
        }
        if (settings.showMusic) {
            MusicCard(
                state = music,
                onEnableAccess = onEnableMusicAccess,
                onOpenMusicApp = onOpenMusicApp,
                onPlayPause = onPlayPauseMusic,
                onNext = onNextTrack,
                onPrevious = onPreviousTrack
            )
        }
        NavigationQuickCard(
            savedDestinations = settings.savedDestinations,
            locationLabel = locationLabel,
            onOpenNavigation = onOpenNavigation,
            onNavigate = onNavigateSaved
        )
        if (settings.showBattery) {
            BatteryCard(battery = battery)
        }
        if (settings.showGps) {
            GpsCard(
                gps = gps,
                locationLabel = locationLabel,
                onRequestPermission = onRequestLocationPermission,
                onOpenLocationSettings = onOpenLocationSettings
            )
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun WideDashboard(
    settings: AppSettings,
    gps: GpsState,
    compass: CompassState,
    battery: BatteryState,
    weather: WeatherUiState,
    locationLabel: String?,
    music: MusicState,
    onRequestLocationPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onRefreshWeather: () -> Unit,
    onResetSession: () -> Unit,
    onOpenNavigation: () -> Unit,
    onEnableMusicAccess: () -> Unit,
    onOpenMusicApp: () -> Unit,
    onPlayPauseMusic: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onNavigateSaved: (SavedDestination) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier.weight(1.08f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SpeedometerCard(
                    gps = gps,
                    speedUnit = settings.speedUnit,
                    ridingMode = false,
                    onRequestPermission = onRequestLocationPermission,
                    onResetSession = onResetSession,
                    modifier = Modifier.fillMaxWidth()
                )
                if (settings.showBattery) {
                    BatteryCard(battery = battery, modifier = Modifier.fillMaxWidth())
                }
                if (settings.showGps) {
                    GpsCard(
                        gps = gps,
                        locationLabel = locationLabel,
                        onRequestPermission = onRequestLocationPermission,
                        onOpenLocationSettings = onOpenLocationSettings,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Column(
                modifier = Modifier.weight(0.92f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LiveClockCard(
                    use24Hour = settings.use24HourClock,
                    modifier = Modifier.fillMaxWidth()
                )
                if (settings.showCompass) {
                    CompassCard(compass = compass, modifier = Modifier.fillMaxWidth())
                }
                if (settings.showWeather) {
                    WeatherCard(
                        state = weather,
                        temperatureUnit = settings.temperatureUnit,
                        provider = settings.weatherProvider,
                        hasLocationPermission = gps.hasPermission,
                        onRequestPermission = onRequestLocationPermission,
                        onRefresh = onRefreshWeather,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            NavigationQuickCard(
                savedDestinations = settings.savedDestinations,
                locationLabel = locationLabel,
                onOpenNavigation = onOpenNavigation,
                onNavigate = onNavigateSaved,
                modifier = Modifier.weight(0.85f)
            )
            if (settings.showMusic) {
                MusicCard(
                    state = music,
                    onEnableAccess = onEnableMusicAccess,
                    onOpenMusicApp = onOpenMusicApp,
                    onPlayPause = onPlayPauseMusic,
                    onNext = onNextTrack,
                    onPrevious = onPreviousTrack,
                    modifier = Modifier.weight(1.15f)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun RidingDashboard(
    settings: AppSettings,
    gps: GpsState,
    compass: CompassState,
    music: MusicState,
    onRequestLocationPermission: () -> Unit,
    onResetSession: () -> Unit,
    onEnableMusicAccess: () -> Unit,
    onOpenMusicApp: () -> Unit,
    onPlayPauseMusic: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit
) {
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (landscape) {
        // Landscape racing layout: speed stays on the left, rider info stacks on the right.
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpeedometerCard(
                    gps = gps,
                    speedUnit = settings.speedUnit,
                    ridingMode = true,
                    onRequestPermission = onRequestLocationPermission,
                    onResetSession = onResetSession,
                    modifier = Modifier.fillMaxWidth(),
                    compact = true
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RidingWarningBanner()
                LiveClockCard(
                    use24Hour = settings.use24HourClock,
                    compact = true,
                    modifier = Modifier.fillMaxWidth()
                )
                RidingCompassStrip(compass = compass)
                RidingMusicBar(
                    state = music,
                    onEnableAccess = onEnableMusicAccess,
                    onOpenMusicApp = onOpenMusicApp,
                    onPlayPause = onPlayPauseMusic,
                    onNext = onNextTrack,
                    onPrevious = onPreviousTrack
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RidingWarningBanner()
            LiveClockCard(
                use24Hour = settings.use24HourClock,
                compact = true,
                modifier = Modifier.fillMaxWidth()
            )
            SpeedometerCard(
                gps = gps,
                speedUnit = settings.speedUnit,
                ridingMode = true,
                onRequestPermission = onRequestLocationPermission,
                onResetSession = onResetSession,
                modifier = Modifier.fillMaxWidth()
            )
            RidingCompassStrip(compass = compass)
            RidingMusicBar(
                state = music,
                onEnableAccess = onEnableMusicAccess,
                onOpenMusicApp = onOpenMusicApp,
                onPlayPause = onPlayPauseMusic,
                onNext = onNextTrack,
                onPrevious = onPreviousTrack
            )
            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun RidingWarningBanner(modifier: Modifier = Modifier) {
    val palette = LocalMotoPalette.current
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.accent.copy(alpha = 0.08f))
            .border(
                1.dp,
                palette.accent.copy(alpha = 0.18f),
                shape
            )
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = null,
            tint = palette.warning,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = "Configure before riding. Do not interact while moving. GPS speed is estimated.",
            style = MaterialTheme.typography.labelSmall,
            color = palette.textSecondary
        )
    }
}

@Composable
private fun RidingCompassStrip(compass: CompassState) {
    val palette = LocalMotoPalette.current
    MotoPanel(
        modifier = Modifier.fillMaxWidth(),
        padding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 14.dp,
            vertical = 11.dp
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel(text = "COMPASS", modifier = Modifier.weight(1f))
            if (compass.isAvailable) {
                Text(
                    text = HeadingUtils.bearing(compass.headingDegrees),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = palette.textPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = HeadingUtils.cardinal(compass.headingDegrees),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.accent
                )
            } else {
                Text(
                    text = "Sensor unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted
                )
            }
        }
    }
}

@Composable
private fun RidingMusicBar(
    state: MusicState,
    onEnableAccess: () -> Unit,
    onOpenMusicApp: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val palette = LocalMotoPalette.current
    MotoPanel(
        modifier = Modifier.fillMaxWidth(),
        padding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 12.dp,
            vertical = 8.dp
        )
    ) {
        if (!state.hasAccess) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = palette.warning,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Music access not enabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onEnableAccess) {
                    Text("ENABLE", color = palette.accent)
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    palette.accent.copy(alpha = 0.3f),
                                    palette.panelHigh
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.title ?: "Nothing playing",
                        style = MaterialTheme.typography.titleSmall,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = state.artist ?: "Open a music app",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onPrevious,
                    enabled = state.isConnected,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous track",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = onPlayPause,
                    enabled = state.isConnected,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        tint = palette.accent,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(
                    onClick = onNext,
                    enabled = state.isConnected,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        Icons.Rounded.SkipNext,
                        contentDescription = "Next track",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onOpenMusicApp, modifier = Modifier.size(34.dp)) {
                    Icon(
                        Icons.Rounded.MusicNote,
                        contentDescription = "Music app",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
