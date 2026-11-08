package com.motosmart.launcher.data.location

import android.content.Context
import android.location.Geocoder
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Best-effort, local reverse geocoding. Falls back to coordinates when unavailable. */
object LocationLabelResolver {

    @Suppress("DEPRECATION")
    suspend fun resolve(context: Context, point: GeoPoint): String = withContext(Dispatchers.IO) {
        val fallback = formatCoordinates(point)
        val geocoder = Geocoder(context, Locale.getDefault())
        if (!Geocoder.isPresent()) return@withContext fallback
        runCatching {
            val results = geocoder.getFromLocation(point.latitude, point.longitude, 1)
            val address = results?.firstOrNull() ?: return@withContext fallback
            address.locality
                ?: address.subAdminArea
                ?: address.adminArea
                ?: address.countryName
                ?: fallback
        }.getOrDefault(fallback)
    }

    fun formatCoordinates(point: GeoPoint): String = String.format(
        Locale.US,
        "%.4f, %.4f",
        point.latitude,
        point.longitude
    )
}
