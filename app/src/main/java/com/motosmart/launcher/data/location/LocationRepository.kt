package com.motosmart.launcher.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

data class GeoPoint(val latitude: Double, val longitude: Double)

data class GpsState(
    val hasPermission: Boolean = false,
    val isTracking: Boolean = false,
    val providerEnabled: Boolean = false,
    val point: GeoPoint? = null,
    val speedKmh: Float = 0f,
    val maxSpeedKmh: Float = 0f,
    val averageSpeedKmh: Float = 0f,
    val distanceKm: Float = 0f,
    val accuracyMeters: Float? = null,
    val lastFixAtMillis: Long = 0L
) {
    val hasFix: Boolean get() = point != null && lastFixAtMillis > 0L
}

/**
 * GPS-only telemetry. Updates run exclusively while the launcher is resumed so the
 * app never performs background location tracking.
 */
class LocationRepository(private val context: Context) {

    private val locationManager = context.getSystemService(LocationManager::class.java)
    private val _state = MutableStateFlow(GpsState())
    val state: StateFlow<GpsState> = _state.asStateFlow()

    private var tracking = false
    private var previous: Location? = null
    private var sessionStartedAtElapsed = 0L
    private var sessionDistanceMeters = 0.0
    private var maxSpeedKmh = 0f
    private var smoothedSpeedKmh = 0f

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) = accept(location)

        @Deprecated("Deprecated in API 29")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

        override fun onProviderEnabled(provider: String) {
            _state.update { it.copy(providerEnabled = hasAnyProviderEnabled()) }
        }

        override fun onProviderDisabled(provider: String) {
            _state.update { it.copy(providerEnabled = hasAnyProviderEnabled()) }
        }
    }

    fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun refreshStatus() {
        _state.update {
            it.copy(
                hasPermission = hasPermission(),
                providerEnabled = hasAnyProviderEnabled()
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        refreshStatus()
        if (!hasPermission()) {
            _state.update { it.copy(isTracking = false) }
            return
        }
        if (tracking) return
        tracking = true
        _state.update { it.copy(isTracking = true, providerEnabled = hasAnyProviderEnabled()) }

        val gpsEnabled = runCatching {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }.getOrDefault(false)
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Each provider is registered independently: a SecurityException on GPS (for
        // example when only coarse location was granted) must not block network updates.
        if (gpsEnabled && fineGranted) {
            runCatching {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    GPS_INTERVAL_MS,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )
            }
        }
        if (runCatching {
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }.getOrDefault(false)
        ) {
            runCatching {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    NETWORK_INTERVAL_MS,
                    10f,
                    listener,
                    Looper.getMainLooper()
                )
            }
        }
        if (!gpsEnabled) {
            // Still deliver an initial coarse position for weather/navigation.
            runCatching {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    ?.let(::accept)
            }
        }
    }

    fun stop() {
        if (!tracking) return
        tracking = false
        runCatching { locationManager.removeUpdates(listener) }
        _state.update { it.copy(isTracking = false, speedKmh = 0f) }
    }

    fun resetSession() {
        previous = null
        sessionStartedAtElapsed = 0L
        sessionDistanceMeters = 0.0
        maxSpeedKmh = 0f
        smoothedSpeedKmh = 0f
        _state.update {
            it.copy(
                speedKmh = 0f,
                maxSpeedKmh = 0f,
                averageSpeedKmh = 0f,
                distanceKm = 0f
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun accept(location: Location) {
        if (!hasPermission()) return
        val nowElapsed = android.os.SystemClock.elapsedRealtime()
        val fixAgeMs = nowElapsed - (location.elapsedRealtimeNanos / 1_000_000L)
        val isFresh = fixAgeMs in -2_000..30_000

        var speedKmh = 0f
        if (isFresh && location.hasSpeed()) {
            speedKmh = (location.speed * 3.6f).coerceAtLeast(0f)
        } else if (isFresh) {
            val last = previous
            if (last != null) {
                val seconds = (location.time - last.time) / 1000.0
                if (seconds > 0.4 && seconds < 30.0) {
                    speedKmh = ((location.distanceTo(last) / seconds) * 3.6).toFloat()
                }
            }
        }

        // Exponential smoothing keeps the digital readout stable without hiding real changes.
        smoothedSpeedKmh = if (smoothedSpeedKmh <= 0f) {
            speedKmh
        } else {
            smoothedSpeedKmh + (speedKmh - smoothedSpeedKmh) * 0.35f
        }
        val displayed = if (speedKmh < 1.5f) 0f else smoothedSpeedKmh

        if (sessionStartedAtElapsed == 0L && isFresh) {
            sessionStartedAtElapsed = nowElapsed
        }

        val last = previous
        if (isFresh && last != null && location.accuracy <= 100f) {
            val gap = (location.time - last.time) / 1000.0
            val step = location.distanceTo(last)
            if (gap in 0.4..30.0 && step in 1.0..500.0) {
                sessionDistanceMeters += step
            }
        }

        if (displayed > maxSpeedKmh) maxSpeedKmh = displayed

        val elapsedHours = if (sessionStartedAtElapsed > 0L) {
            ((nowElapsed - sessionStartedAtElapsed) / 3_600_000.0).coerceAtLeast(1.0 / 3600.0)
        } else {
            0.0
        }
        val average = (sessionDistanceMeters / 1000.0 / elapsedHours).toFloat()

        if (isFresh) previous = location

        _state.update {
            it.copy(
                hasPermission = true,
                providerEnabled = hasAnyProviderEnabled(),
                point = GeoPoint(location.latitude, location.longitude),
                speedKmh = if (isFresh) displayed else 0f,
                maxSpeedKmh = maxSpeedKmh,
                averageSpeedKmh = if (average.isFinite()) average else 0f,
                distanceKm = (sessionDistanceMeters / 1000.0).toFloat(),
                accuracyMeters = if (location.hasAccuracy()) location.accuracy else null,
                lastFixAtMillis = if (isFresh) System.currentTimeMillis() else 0L
            )
        }
    }

    private fun hasAnyProviderEnabled(): Boolean = runCatching {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }.getOrDefault(false)

    private companion object {
        const val GPS_INTERVAL_MS = 1_000L
        const val NETWORK_INTERVAL_MS = 5_000L
    }
}

fun GpsState.formattedAccuracy(): String? =
    accuracyMeters?.let { "${it.roundToInt()} m" }
