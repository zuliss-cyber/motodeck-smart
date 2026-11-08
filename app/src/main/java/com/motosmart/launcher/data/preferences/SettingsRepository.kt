package com.motosmart.launcher.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "moto_settings")

/**
 * Single source of truth for rider preferences. All values are stored locally with DataStore.
 */
class SettingsRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { it.toAppSettings() }

    suspend fun current(): AppSettings = settings.first()

    /**
     * One-time theme revision migration: riders who never changed the old default accent move to
     * the icy cockpit palette, while any other explicit accent choice is preserved.
     */
    suspend fun migrateThemeDefaults() {
        context.settingsDataStore.edit { preferences ->
            val revision = preferences[Keys.THEME_REVISION] ?: 0
            if (revision < CURRENT_THEME_REVISION) {
                val stored = preferences[Keys.ACCENT]
                if (stored == null || stored == "ORANGE") {
                    preferences[Keys.ACCENT] = AccentColor.ICE.name
                }
                preferences[Keys.THEME_REVISION] = CURRENT_THEME_REVISION
            }
        }
    }

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.settingsDataStore.edit { preferences ->
            transform(preferences.toAppSettings()).writeTo(preferences)
        }
    }

    suspend fun setTheme(mode: ThemeMode) = update { it.copy(themeMode = mode) }
    suspend fun setAccent(color: AccentColor) = update { it.copy(accentColor = color) }
    suspend fun setSpeedUnit(unit: SpeedUnit) = update { it.copy(speedUnit = unit) }
    suspend fun set24HourClock(enabled: Boolean) = update { it.copy(use24HourClock = enabled) }
    suspend fun setTemperatureUnit(unit: TemperatureUnit) = update { it.copy(temperatureUnit = unit) }
    suspend fun setRidingMode(enabled: Boolean) = update { it.copy(ridingMode = enabled) }
    suspend fun setKeepScreenOn(enabled: Boolean) = update { it.copy(keepScreenOn = enabled) }

    suspend fun addFavourite(ref: AppRef) = update { current ->
        if (current.favouriteApps.any { it == ref }) current
        else current.copy(favouriteApps = current.favouriteApps + ref)
    }

    suspend fun removeFavourite(ref: AppRef) = update { current ->
        current.copy(favouriteApps = current.favouriteApps.filterNot { it == ref })
    }

    /** An empty dock list means "use the automatic system defaults". */
    suspend fun setDock(apps: List<AppRef>) = update { it.copy(dockApps = apps) }
    suspend fun resetDock() = update { it.copy(dockApps = emptyList()) }

    suspend fun addSavedDestination(destination: SavedDestination) = update { current ->
        val withoutDuplicate = current.savedDestinations.filterNot {
            it.label.equals(destination.label, ignoreCase = true) &&
                it.latitude == destination.latitude &&
                it.longitude == destination.longitude
        }
        current.copy(savedDestinations = (withoutDuplicate + destination).takeLast(12))
    }

    suspend fun removeSavedDestination(destination: SavedDestination) = update { current ->
        current.copy(savedDestinations = current.savedDestinations - destination)
    }

    suspend fun acknowledgeRidingWarning() = update { it.copy(hasAcknowledgedRidingWarning = true) }

    suspend fun resetAll() {
        context.settingsDataStore.edit { it.clear() }
    }

    private fun Preferences.toAppSettings(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            themeMode = enumOr(this[Keys.THEME_MODE], defaults.themeMode),
            accentColor = enumOr(this[Keys.ACCENT], defaults.accentColor),
            speedUnit = enumOr(this[Keys.SPEED_UNIT], defaults.speedUnit),
            use24HourClock = this[Keys.CLOCK_24H] ?: defaults.use24HourClock,
            temperatureUnit = enumOr(this[Keys.TEMPERATURE_UNIT], defaults.temperatureUnit),
            ridingMode = this[Keys.RIDING_MODE] ?: defaults.ridingMode,
            ridingAutoDark = this[Keys.RIDING_AUTO_DARK] ?: defaults.ridingAutoDark,
            keepScreenOn = this[Keys.KEEP_SCREEN_ON] ?: defaults.keepScreenOn,
            showCompass = this[Keys.SHOW_COMPASS] ?: defaults.showCompass,
            showWeather = this[Keys.SHOW_WEATHER] ?: defaults.showWeather,
            showMusic = this[Keys.SHOW_MUSIC] ?: defaults.showMusic,
            showBattery = this[Keys.SHOW_BATTERY] ?: defaults.showBattery,
            showGps = this[Keys.SHOW_GPS] ?: defaults.showGps,
            weatherProvider = enumOr(this[Keys.WEATHER_PROVIDER], defaults.weatherProvider),
            weatherApiKey = this[Keys.WEATHER_API_KEY].orEmpty(),
            dockApps = this[Keys.DOCK_APPS].decodeList(AppRef.serializer()),
            favouriteApps = this[Keys.FAVOURITES].decodeList(AppRef.serializer()),
            savedDestinations = this[Keys.SAVED_DESTINATIONS].decodeList(SavedDestination.serializer()),
            hasAcknowledgedRidingWarning = this[Keys.RIDING_WARNING_ACK] ?: false,
            appWallpaperVersion = this[Keys.APP_WALLPAPER_VERSION] ?: 0L
        )
    }

    private fun AppSettings.writeTo(preferences: MutablePreferences) {
        preferences[Keys.THEME_MODE] = themeMode.name
        preferences[Keys.ACCENT] = accentColor.name
        preferences[Keys.SPEED_UNIT] = speedUnit.name
        preferences[Keys.CLOCK_24H] = use24HourClock
        preferences[Keys.TEMPERATURE_UNIT] = temperatureUnit.name
        preferences[Keys.RIDING_MODE] = ridingMode
        preferences[Keys.RIDING_AUTO_DARK] = ridingAutoDark
        preferences[Keys.KEEP_SCREEN_ON] = keepScreenOn
        preferences[Keys.SHOW_COMPASS] = showCompass
        preferences[Keys.SHOW_WEATHER] = showWeather
        preferences[Keys.SHOW_MUSIC] = showMusic
        preferences[Keys.SHOW_BATTERY] = showBattery
        preferences[Keys.SHOW_GPS] = showGps
        preferences[Keys.WEATHER_PROVIDER] = weatherProvider.name
        preferences[Keys.WEATHER_API_KEY] = weatherApiKey
        preferences[Keys.DOCK_APPS] = json.encodeToString(ListSerializer(AppRef.serializer()), dockApps)
        preferences[Keys.FAVOURITES] = json.encodeToString(ListSerializer(AppRef.serializer()), favouriteApps)
        preferences[Keys.SAVED_DESTINATIONS] = json.encodeToString(
            ListSerializer(SavedDestination.serializer()),
            savedDestinations
        )
        preferences[Keys.RIDING_WARNING_ACK] = hasAcknowledgedRidingWarning
        preferences[Keys.APP_WALLPAPER_VERSION] = appWallpaperVersion
    }

    private fun <T> String?.decodeList(serializer: kotlinx.serialization.KSerializer<T>): List<T> {
        if (isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(serializer), this)
        }.getOrDefault(emptyList())
    }

    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T {
        if (name == null) return fallback
        return runCatching { enumValueOf<T>(name) }.getOrDefault(fallback)
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT = stringPreferencesKey("accent")
        val SPEED_UNIT = stringPreferencesKey("speed_unit")
        val CLOCK_24H = booleanPreferencesKey("clock_24h")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val RIDING_MODE = booleanPreferencesKey("riding_mode")
        val RIDING_AUTO_DARK = booleanPreferencesKey("riding_auto_dark")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val SHOW_COMPASS = booleanPreferencesKey("show_compass")
        val SHOW_WEATHER = booleanPreferencesKey("show_weather")
        val SHOW_MUSIC = booleanPreferencesKey("show_music")
        val SHOW_BATTERY = booleanPreferencesKey("show_battery")
        val SHOW_GPS = booleanPreferencesKey("show_gps")
        val WEATHER_PROVIDER = stringPreferencesKey("weather_provider")
        val WEATHER_API_KEY = stringPreferencesKey("weather_api_key")
        val DOCK_APPS = stringPreferencesKey("dock_apps")
        val FAVOURITES = stringPreferencesKey("favourites")
        val SAVED_DESTINATIONS = stringPreferencesKey("saved_destinations")
        val RIDING_WARNING_ACK = booleanPreferencesKey("riding_warning_ack")
        val APP_WALLPAPER_VERSION = longPreferencesKey("app_wallpaper_version")
        val THEME_REVISION = intPreferencesKey("theme_revision")
    }

    private companion object {
        const val CURRENT_THEME_REVISION = 1
    }
}
