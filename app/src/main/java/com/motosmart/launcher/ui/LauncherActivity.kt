package com.motosmart.launcher.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.motosmart.launcher.data.navigation.NavigationLauncher
import com.motosmart.launcher.data.preferences.ThemeMode
import com.motosmart.launcher.ui.appdrawer.AppDrawerScreen
import com.motosmart.launcher.ui.components.CockpitBackground
import com.motosmart.launcher.ui.dashboard.DashboardScreen
import com.motosmart.launcher.ui.dashboard.DashboardViewModel
import com.motosmart.launcher.ui.navigation.NavigationScreen
import com.motosmart.launcher.ui.settings.DockEditorScreen
import com.motosmart.launcher.ui.settings.SettingsScreen
import com.motosmart.launcher.ui.theme.LocalMotoPalette
import com.motosmart.launcher.ui.theme.MotoTheme
import com.motosmart.launcher.util.HomeRoleManager

class LauncherActivity : ComponentActivity() {

    private val launcherViewModel: LauncherViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by launcherViewModel.settings.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (settings.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> systemDark
            } || (settings.ridingMode && settings.ridingAutoDark)

            MotoTheme(darkTheme = darkTheme, accentColor = settings.accentColor) {
                LauncherRoot(
                    launcherViewModel = launcherViewModel,
                    dashboardViewModel = dashboardViewModel,
                    darkTheme = darkTheme
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        launcherViewModel.refreshLauncherState()
        dashboardViewModel.onHostResume()
    }

    override fun onPause() {
        dashboardViewModel.onHostPause()
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // A HOME intent always returns the rider to the dashboard.
        launcherViewModel.showDashboard()
    }
}

@Composable
private fun LauncherRoot(
    launcherViewModel: LauncherViewModel,
    dashboardViewModel: DashboardViewModel,
    darkTheme: Boolean
) {
    val context = LocalContext.current
    val palette = LocalMotoPalette.current
    val activity = context as? Activity
    val view = LocalView.current

    val settings by launcherViewModel.settings.collectAsStateWithLifecycle()
    val apps by launcherViewModel.apps.collectAsStateWithLifecycle()
    val dockApps by launcherViewModel.dockApps.collectAsStateWithLifecycle()
    val favouriteApps by launcherViewModel.favouriteApps.collectAsStateWithLifecycle()
    val screen by launcherViewModel.screen.collectAsStateWithLifecycle()
    val isDefaultLauncher by launcherViewModel.isDefaultLauncher.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }

    val gps by dashboardViewModel.gps.collectAsStateWithLifecycle()
    val compass by dashboardViewModel.compass.collectAsStateWithLifecycle()
    val battery by dashboardViewModel.battery.collectAsStateWithLifecycle()
    val weather by dashboardViewModel.weather.collectAsStateWithLifecycle()
    val locationLabel by dashboardViewModel.locationLabel.collectAsStateWithLifecycle()
    val music by dashboardViewModel.music.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        dashboardViewModel.onLocationPermissionChanged()
    }

    val homeRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        launcherViewModel.refreshLauncherState()
    }

    val requestLocationPermission: () -> Unit = {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    val performExit: () -> Unit = {
        showExitDialog = false
        // A default launcher cannot simply close: Android would relaunch it as HOME.
        // Send the rider to Home settings so another launcher can be selected.
        if (HomeRoleManager.isDefaultLauncher(context)) {
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_HOME_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        } else {
            activity?.finishAffinity()
        }
    }

    SideEffect {
        activity ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(activity.window, view)
        controller.isAppearanceLightStatusBars = !darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme
    }

    DisposableEffect(settings.keepScreenOn, activity) {
        val window = activity?.window
        if (settings.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(screen) {
        if (screen == LauncherScreen.Dashboard) {
            dashboardViewModel.onDashboardVisible()
        } else {
            dashboardViewModel.onDashboardHidden()
        }
    }

    BackHandler {
        if (screen != LauncherScreen.Dashboard) {
            launcherViewModel.onBack()
        }
    }

    CockpitBackground(
        modifier = Modifier.fillMaxSize(),
        wallpaperVersion = settings.appWallpaperVersion
    ) {
        Crossfade(
            targetState = screen,
            animationSpec = tween(durationMillis = 170),
            label = "launcherScreen"
        ) { currentScreen ->
            when (currentScreen) {
                LauncherScreen.Dashboard -> DashboardScreen(
                    settings = settings,
                    gps = gps,
                    compass = compass,
                    battery = battery,
                    weather = weather,
                    locationLabel = locationLabel,
                    music = music,
                    dockApps = dockApps,
                    isDefaultLauncher = isDefaultLauncher,
                    onOpenDrawer = launcherViewModel::openDrawer,
                    onOpenNavigation = launcherViewModel::openNavigation,
                    onOpenSettings = launcherViewModel::openSettings,
                    onRequestExit = { showExitDialog = true },
                    onSetRidingMode = launcherViewModel::setRidingMode,
                    onRequestLocationPermission = requestLocationPermission,
                    onOpenLocationSettings = {
                        runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    },
                    onRefreshWeather = { dashboardViewModel.refreshWeather() },
                    onResetSession = dashboardViewModel::resetSession,
                    onSetDefaultLauncher = {
                        runCatching {
                            homeRoleLauncher.launch(HomeRoleManager.requestIntent(context))
                        }
                    },
                    onEnableMusicAccess = dashboardViewModel::openMusicAccessSettings,
                    onOpenMusicApp = {
                        if (!dashboardViewModel.openMusicApp()) {
                            toast("No music app available")
                        }
                    },
                    onPlayPauseMusic = dashboardViewModel::playPauseMusic,
                    onNextTrack = dashboardViewModel::nextTrack,
                    onPreviousTrack = dashboardViewModel::previousTrack,
                    onLaunchApp = launcherViewModel::launch,
                    onNavigateSaved = { destination ->
                        if (!NavigationLauncher.navigate(context, destination)) {
                            toast("No maps or browser app available")
                        }
                    }
                )

                LauncherScreen.AppDrawer -> AppDrawerScreen(
                    apps = apps,
                    favouriteApps = favouriteApps,
                    favouriteRefs = settings.favouriteApps,
                    dockApps = dockApps,
                    dockRefs = settings.dockApps,
                    onBack = launcherViewModel::onBack,
                    onLaunch = launcherViewModel::launch,
                    onToggleFavourite = launcherViewModel::toggleFavourite,
                    onAddToDock = launcherViewModel::addDockEntry,
                    onRemoveFromDock = { entry -> launcherViewModel.removeDockApp(entry.ref) },
                    onOpenAppInfo = launcherViewModel::openAppInfo
                )

                LauncherScreen.Navigation -> NavigationScreen(
                    gps = gps,
                    locationLabel = locationLabel,
                    savedDestinations = settings.savedDestinations,
                    onBack = launcherViewModel::onBack,
                    onSaveDestination = launcherViewModel::addSavedDestination,
                    onRemoveDestination = launcherViewModel::removeSavedDestination
                )

                LauncherScreen.Settings -> SettingsScreen(
                    settings = settings,
                    isDefaultLauncher = isDefaultLauncher,
                    hasLocationPermission = gps.hasPermission,
                    hasMusicAccess = music.hasAccess,
                    onBack = launcherViewModel::onBack,
                    onSetTheme = launcherViewModel::setTheme,
                    onSetAccent = launcherViewModel::setAccent,
                    onSetSpeedUnit = launcherViewModel::setSpeedUnit,
                    onSetTemperatureUnit = launcherViewModel::setTemperatureUnit,
                    onSet24Hour = launcherViewModel::set24HourClock,
                    onSetShowCompass = launcherViewModel::setShowCompass,
                    onSetShowWeather = launcherViewModel::setShowWeather,
                    onSetShowMusic = launcherViewModel::setShowMusic,
                    onSetShowBattery = launcherViewModel::setShowBattery,
                    onSetShowGps = launcherViewModel::setShowGps,
                    onSetKeepScreenOn = launcherViewModel::setKeepScreenOn,
                    onSetRidingMode = launcherViewModel::setRidingMode,
                    onSetRidingAutoDark = launcherViewModel::setRidingAutoDark,
                    onSetWeatherProvider = launcherViewModel::setWeatherProvider,
                    onSetWeatherApiKey = launcherViewModel::setWeatherApiKey,
                    onRequestHomeRole = {
                        runCatching {
                            homeRoleLauncher.launch(HomeRoleManager.requestIntent(context))
                        }
                    },
                    onOpenHomeSettings = {
                        runCatching {
                            context.startActivity(
                                Intent(Settings.ACTION_HOME_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    },
                    onRequestLocationPermission = requestLocationPermission,
                    onOpenNotificationAccess = dashboardViewModel::openMusicAccessSettings,
                    onOpenDockEditor = launcherViewModel::openDockEditor,
                    onResetAll = launcherViewModel::resetAll,
                    onRequestExit = { showExitDialog = true },
                    onSetAppWallpaper = launcherViewModel::setAppWallpaperVersion
                )

                LauncherScreen.DockEditor -> DockEditorScreen(
                    apps = apps,
                    dockApps = dockApps,
                    onBack = launcherViewModel::onBack,
                    onAdd = launcherViewModel::addDockEntry,
                    onRemove = { entry -> launcherViewModel.removeDockApp(entry.ref) },
                    onSetDock = launcherViewModel::setDock,
                    onReset = launcherViewModel::resetDock
                )
            }
        }
    }

    if (settings.ridingMode && !settings.hasAcknowledgedRidingWarning) {
        AlertDialog(
            onDismissRequest = { launcherViewModel.setRidingMode(false) },
            title = {
                Text(
                    text = "Configure before riding",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Set up the launcher before you ride. Do not touch or read the screen while the motorcycle is moving. GPS speed is an estimate and must never replace your instruments or your own judgement.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { launcherViewModel.acknowledgeRidingWarning() }
                ) {
                    Text("I UNDERSTAND", color = palette.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { launcherViewModel.setRidingMode(false) }) {
                    Text("EXIT RIDING MODE", color = palette.warning)
                }
            }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = "Exit MotoDeck//smart?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = if (isDefaultLauncher) {
                        "This launcher is currently your default home screen. Exiting opens Android's Home app settings so you can choose another launcher. To come back, select MotoDeck//smart again."
                    } else {
                        "The launcher will close and return to the previous app or the system home screen."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = performExit) {
                    Text(
                        text = if (isDefaultLauncher) "OPEN HOME SETTINGS" else "EXIT",
                        color = palette.warning
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("CANCEL", color = palette.textSecondary)
                }
            }
        )
    }
}
