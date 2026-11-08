package com.motosmart.launcher.data.preferences

import kotlinx.serialization.Serializable

@Serializable
enum class ThemeMode { DARK, LIGHT, SYSTEM }

@Serializable
enum class AccentColor { ICE, ORANGE, RED, AMBER, CYAN, LIME }

@Serializable
enum class SpeedUnit { KMH, MPH }

@Serializable
enum class TemperatureUnit { CELSIUS, FAHRENHEIT }

@Serializable
enum class WeatherProvider { OPEN_METEO, OPEN_WEATHER_MAP, DISABLED }

@Serializable
data class AppRef(
    val packageName: String,
    val className: String
)

@Serializable
data class SavedDestination(
    val label: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentColor: AccentColor = AccentColor.ICE,
    val speedUnit: SpeedUnit = SpeedUnit.KMH,
    val use24HourClock: Boolean = true,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val ridingMode: Boolean = false,
    val ridingAutoDark: Boolean = true,
    val keepScreenOn: Boolean = false,
    val showCompass: Boolean = true,
    val showWeather: Boolean = true,
    val showMusic: Boolean = true,
    val showBattery: Boolean = true,
    val showGps: Boolean = true,
    val weatherProvider: WeatherProvider = WeatherProvider.OPEN_METEO,
    val weatherApiKey: String = "",
    val dockApps: List<AppRef> = emptyList(),
    val favouriteApps: List<AppRef> = emptyList(),
    val savedDestinations: List<SavedDestination> = emptyList(),
    val hasAcknowledgedRidingWarning: Boolean = false,
    /** 0 means the default racing livery; any other value marks a custom cockpit image. */
    val appWallpaperVersion: Long = 0L
)
