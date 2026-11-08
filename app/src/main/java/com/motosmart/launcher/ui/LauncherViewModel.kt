package com.motosmart.launcher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.motosmart.launcher.MotoApplication
import com.motosmart.launcher.data.applications.AppEntry
import com.motosmart.launcher.data.preferences.AccentColor
import com.motosmart.launcher.data.preferences.AppRef
import com.motosmart.launcher.data.preferences.AppSettings
import com.motosmart.launcher.data.preferences.SavedDestination
import com.motosmart.launcher.data.preferences.SpeedUnit
import com.motosmart.launcher.data.preferences.TemperatureUnit
import com.motosmart.launcher.data.preferences.ThemeMode
import com.motosmart.launcher.data.preferences.WeatherProvider
import com.motosmart.launcher.util.AppWallpaperStore
import com.motosmart.launcher.util.HomeRoleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LauncherScreen {
    data object Dashboard : LauncherScreen
    data object AppDrawer : LauncherScreen
    data object Navigation : LauncherScreen
    data object Settings : LauncherScreen
    data object DockEditor : LauncherScreen
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as MotoApplication).container
    private val settingsRepository = container.settings
    private val applicationsRepository = container.applications

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    val apps: StateFlow<List<AppEntry>> = applicationsRepository.apps

    private val _screen = MutableStateFlow<LauncherScreen>(LauncherScreen.Dashboard)
    val screen: StateFlow<LauncherScreen> = _screen.asStateFlow()

    private val _isDefaultLauncher = MutableStateFlow(false)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    val dockApps: StateFlow<List<AppEntry>> = combine(settings, apps) { current, installed ->
        val refs = if (current.dockApps.isEmpty()) {
            applicationsRepository.resolveDefaultDock()
        } else {
            current.dockApps
        }
        resolve(refs, installed).take(6)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val favouriteApps: StateFlow<List<AppEntry>> = combine(settings, apps) { current, installed ->
        resolve(current.favouriteApps, installed)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            settingsRepository.migrateThemeDefaults()
        }
        refreshLauncherState()
    }

    fun refreshLauncherState() {
        applicationsRepository.refresh()
        _isDefaultLauncher.value = HomeRoleManager.isDefaultLauncher(getApplication())
    }

    fun showDashboard() {
        _screen.value = LauncherScreen.Dashboard
    }

    fun openDrawer() {
        _screen.value = LauncherScreen.AppDrawer
    }

    fun openNavigation() {
        _screen.value = LauncherScreen.Navigation
    }

    fun openSettings() {
        _screen.value = LauncherScreen.Settings
    }

    fun openDockEditor() {
        _screen.value = LauncherScreen.DockEditor
    }

    fun onBack() {
        _screen.value = when (_screen.value) {
            LauncherScreen.Dashboard -> LauncherScreen.Dashboard
            LauncherScreen.AppDrawer -> LauncherScreen.Dashboard
            LauncherScreen.Navigation -> LauncherScreen.Dashboard
            LauncherScreen.Settings -> LauncherScreen.Dashboard
            LauncherScreen.DockEditor -> LauncherScreen.Settings
        }
    }

    fun launch(entry: AppEntry) {
        if (applicationsRepository.launch(entry)) {
            showDashboard()
        }
    }

    fun launchRef(ref: AppRef): Boolean = applicationsRepository.launchRef(ref)

    fun openAppInfo(ref: AppRef) {
        applicationsRepository.openAppInfo(ref)
    }

    fun openMusicApp(): Boolean = applicationsRepository.openMusicApp()

    fun openCamera(): Boolean = applicationsRepository.openCamera()

    fun openDialer(): Boolean = applicationsRepository.openDialer()

    fun setTheme(mode: ThemeMode) = update { settingsRepository.setTheme(mode) }
    fun setAccent(color: AccentColor) = update { settingsRepository.setAccent(color) }
    fun setSpeedUnit(unit: SpeedUnit) = update { settingsRepository.setSpeedUnit(unit) }
    fun set24HourClock(enabled: Boolean) = update { settingsRepository.set24HourClock(enabled) }
    fun setTemperatureUnit(unit: TemperatureUnit) = update { settingsRepository.setTemperatureUnit(unit) }
    fun setRidingMode(enabled: Boolean) = update { settingsRepository.setRidingMode(enabled) }
    fun setKeepScreenOn(enabled: Boolean) = update { settingsRepository.setKeepScreenOn(enabled) }
    fun setRidingAutoDark(enabled: Boolean) = update {
        settingsRepository.update { current -> current.copy(ridingAutoDark = enabled) }
    }
    fun setShowCompass(enabled: Boolean) = update { settingsRepository.update { it.copy(showCompass = enabled) } }
    fun setShowWeather(enabled: Boolean) = update { settingsRepository.update { it.copy(showWeather = enabled) } }
    fun setShowMusic(enabled: Boolean) = update { settingsRepository.update { it.copy(showMusic = enabled) } }
    fun setShowBattery(enabled: Boolean) = update { settingsRepository.update { it.copy(showBattery = enabled) } }
    fun setShowGps(enabled: Boolean) = update { settingsRepository.update { it.copy(showGps = enabled) } }

    fun setWeatherProvider(provider: WeatherProvider) = update {
        settingsRepository.update { current -> current.copy(weatherProvider = provider) }
    }

    fun setWeatherApiKey(key: String) = update {
        settingsRepository.update { current -> current.copy(weatherApiKey = key) }
    }

    fun acknowledgeRidingWarning() = update { settingsRepository.acknowledgeRidingWarning() }

    fun setAppWallpaperVersion(version: Long) = update {
        settingsRepository.update { current -> current.copy(appWallpaperVersion = version) }
    }

    fun addFavourite(ref: AppRef) = update { settingsRepository.addFavourite(ref) }
    fun removeFavourite(ref: AppRef) = update { settingsRepository.removeFavourite(ref) }

    fun addDockApp(ref: AppRef) = update {
        settingsRepository.update { current ->
            val currentRefs = if (current.dockApps.isEmpty()) {
                applicationsRepository.resolveDefaultDock()
            } else {
                current.dockApps
            }
            if (currentRefs.any { it == ref }) {
                current.copy(dockApps = currentRefs)
            } else {
                current.copy(dockApps = (currentRefs + ref).take(6))
            }
        }
    }

    fun removeDockApp(ref: AppRef) = update {
        settingsRepository.update { current ->
            val currentRefs = if (current.dockApps.isEmpty()) {
                applicationsRepository.resolveDefaultDock()
            } else {
                current.dockApps
            }
            current.copy(dockApps = currentRefs.filterNot { it == ref })
        }
    }

    fun setDock(refs: List<AppRef>) = update { settingsRepository.setDock(refs) }
    fun resetDock() = update { settingsRepository.resetDock() }

    fun addSavedDestination(destination: SavedDestination) = update {
        settingsRepository.addSavedDestination(destination)
    }

    fun removeSavedDestination(destination: SavedDestination) = update {
        settingsRepository.removeSavedDestination(destination)
    }

    fun resetAll() {
        viewModelScope.launch {
            runCatching {
                AppWallpaperStore.clear(getApplication())
                settingsRepository.resetAll()
            }
        }
    }

    fun toggleFavourite(entry: AppEntry) {
        if (settings.value.favouriteApps.any { it == entry.ref }) {
            removeFavourite(entry.ref)
        } else {
            addFavourite(entry.ref)
        }
    }

    fun addDockEntry(entry: AppEntry) = addDockApp(entry.ref)

    private fun resolve(refs: List<AppRef>, installed: List<AppEntry>): List<AppEntry> {
        val byKey = installed.associateBy { it.packageName to it.activityName }
        return refs.mapNotNull { byKey[it.packageName to it.className] }
            .distinctBy { it.key }
    }

    private fun update(block: suspend () -> Unit) {
        viewModelScope.launch { runCatching { block() } }
    }
}
