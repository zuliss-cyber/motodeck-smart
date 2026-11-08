package com.motosmart.launcher.ui.dashboard

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.motosmart.launcher.MotoApplication
import com.motosmart.launcher.data.location.GeoPoint
import com.motosmart.launcher.data.location.LocationLabelResolver
import com.motosmart.launcher.data.preferences.AppSettings
import com.motosmart.launcher.data.preferences.WeatherProvider
import com.motosmart.launcher.data.weather.WeatherData
import com.motosmart.launcher.data.weather.WeatherRepository
import com.motosmart.launcher.data.weather.WeatherResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    data object Idle : WeatherUiState
    data object Loading : WeatherUiState
    data object Disabled : WeatherUiState
    data class Ready(val data: WeatherData) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as MotoApplication).container
    private val locationRepository = container.location
    private val compassRepository = container.compass
    private val batteryRepository = container.battery
    private val weatherRepository = container.weather
    private val musicRepository = container.music
    private val settingsRepository = container.settings

    val gps = locationRepository.state
    val compass = compassRepository.state
    val battery = batteryRepository.state
    val music = musicRepository.state

    private val _weather = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val weather: StateFlow<WeatherUiState> = _weather.asStateFlow()

    private val _locationLabel = MutableStateFlow<String?>(null)
    val locationLabel: StateFlow<String?> = _locationLabel.asStateFlow()

    private var latestSettings = AppSettings()
    private var dashboardVisible = false
    private var weatherJob: Job? = null
    private var geocodeJob: Job? = null
    private var lastWeatherFetchAt = 0L
    private var lastWeatherPoint: GeoPoint? = null
    private var lastGeocodePoint: GeoPoint? = null
    private var lastGeocodeAt = 0L

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { newSettings ->
                val previous = latestSettings
                latestSettings = newSettings
                if (newSettings.weatherProvider == WeatherProvider.DISABLED) {
                    weatherJob?.cancel()
                    _weather.value = WeatherUiState.Disabled
                } else if (previous.weatherProvider != newSettings.weatherProvider ||
                    previous.weatherApiKey != newSettings.weatherApiKey
                ) {
                    if (dashboardVisible) refreshWeather(force = true)
                }
            }
        }
        viewModelScope.launch {
            gps.collect { state ->
                state.point?.let(::onPointChanged)
                if (!state.hasPermission && _weather.value is WeatherUiState.Ready) {
                    // Keep the last reading visible; the weather card shows the GPS state.
                }
            }
        }
    }

    fun onDashboardVisible() {
        dashboardVisible = true
        locationRepository.refreshStatus()
        locationRepository.start()
        compassRepository.start()
        batteryRepository.start()
        musicRepository.refreshAccessState()
        musicRepository.start()
        refreshWeatherIfStale()
    }

    fun onDashboardHidden() {
        dashboardVisible = false
        locationRepository.stop()
        compassRepository.stop()
        batteryRepository.stop()
        musicRepository.stop()
    }

    fun onHostResume() {
        locationRepository.refreshStatus()
        musicRepository.refreshAccessState()
        if (!dashboardVisible) return
        locationRepository.start()
        compassRepository.start()
        batteryRepository.start()
        musicRepository.start()
        refreshWeatherIfStale()
    }

    /**
     * Stops sensors and media callbacks while the activity is paused, but keeps the
     * dashboard marked visible so returning to the app can restart them immediately.
     */
    fun onHostPause() {
        locationRepository.stop()
        compassRepository.stop()
        batteryRepository.stop()
        musicRepository.stop()
    }

    fun onLocationPermissionChanged() {
        locationRepository.refreshStatus()
        if (dashboardVisible) {
            locationRepository.start()
            refreshWeatherIfStale()
        }
    }

    fun refreshWeather(force: Boolean = true) {
        if (latestSettings.weatherProvider == WeatherProvider.DISABLED) {
            _weather.value = WeatherUiState.Disabled
            return
        }
        val point = gps.value.point
        if (point == null) {
            _weather.value = if (gps.value.hasPermission) {
                WeatherUiState.Error("Waiting for a GPS fix")
            } else {
                WeatherUiState.Idle
            }
            return
        }
        if (!force && weatherJob?.isActive == true) return
        weatherJob?.cancel()
        _weather.value = WeatherUiState.Loading
        weatherJob = viewModelScope.launch {
            val label = _locationLabel.value
                ?: LocationLabelResolver.formatCoordinates(point)
            when (
                val result = weatherRepository.fetch(
                    settings = latestSettings,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    locationLabel = label
                )
            ) {
                is WeatherResult.Success -> {
                    _weather.value = WeatherUiState.Ready(result.data)
                    lastWeatherFetchAt = System.currentTimeMillis()
                    lastWeatherPoint = point
                }
                is WeatherResult.Error -> {
                    _weather.value = WeatherUiState.Error(result.message)
                    // Record the attempt so a persistent failure is retried on the next
                    // refresh interval instead of on every GPS update.
                    lastWeatherFetchAt = System.currentTimeMillis()
                    lastWeatherPoint = point
                }
            }
        }
    }

    fun resetSession() = locationRepository.resetSession()

    fun playPauseMusic() = musicRepository.playPause()
    fun nextTrack() = musicRepository.next()
    fun previousTrack() = musicRepository.previous()
    fun openMusicApp(): Boolean = musicRepository.openMusicApp()

    fun openMusicAccessSettings() {
        musicRepository.openAccessSettings()
    }

    fun setMusicAccessState() {
        musicRepository.refreshAccessState()
    }

    private fun onPointChanged(point: GeoPoint) {
        val now = System.currentTimeMillis()
        val moved = lastGeocodePoint?.let { distanceMeters(it, point) } ?: Double.MAX_VALUE
        if (moved > 500 || now - lastGeocodeAt > 10 * 60_000L) {
            lastGeocodeAt = now
            lastGeocodePoint = point
            geocodeJob?.cancel()
            geocodeJob = viewModelScope.launch {
                val label = LocationLabelResolver.resolve(getApplication(), point)
                _locationLabel.value = label
                val current = _weather.value
                if (current is WeatherUiState.Ready &&
                    current.data.locationLabel != label
                ) {
                    _weather.value = WeatherUiState.Ready(
                        current.data.copy(locationLabel = label)
                    )
                }
            }
        }
        refreshWeatherIfStale(point)
    }

    private fun refreshWeatherIfStale(point: GeoPoint? = gps.value.point) {
        if (!dashboardVisible || point == null) return
        if (latestSettings.weatherProvider == WeatherProvider.DISABLED) {
            _weather.value = WeatherUiState.Disabled
            return
        }
        val stale = System.currentTimeMillis() - lastWeatherFetchAt > WEATHER_REFRESH_INTERVAL_MS
        val movedFar = lastWeatherPoint?.let { distanceMeters(it, point) > 5_000 } ?: true
        if (stale || movedFar) {
            if (weatherJob?.isActive != true) refreshWeather(force = false)
        }
    }

    private fun distanceMeters(first: GeoPoint, second: GeoPoint): Double {
        val result = FloatArray(1)
        Location.distanceBetween(
            first.latitude,
            first.longitude,
            second.latitude,
            second.longitude,
            result
        )
        return result[0].toDouble()
    }

    override fun onCleared() {
        locationRepository.stop()
        compassRepository.stop()
        batteryRepository.stop()
        musicRepository.stop()
        weatherJob?.cancel()
        geocodeJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val WEATHER_REFRESH_INTERVAL_MS = 15 * 60_000L
    }
}
