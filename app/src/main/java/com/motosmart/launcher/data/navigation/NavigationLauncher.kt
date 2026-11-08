package com.motosmart.launcher.data.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.motosmart.launcher.data.location.GeoPoint
import com.motosmart.launcher.data.preferences.SavedDestination
import java.net.URLEncoder
import java.util.Locale

/**
 * Navigation shortcuts are delivered through Android intents. The app never embeds
 * Google Maps or requires a Maps API key.
 */
object NavigationLauncher {

    private const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"

    fun openSearch(context: Context, query: String): Boolean {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return false
        val encoded = Uri.encode(trimmed)
        val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encoded"))
            .setPackage(GOOGLE_MAPS_PACKAGE)
        if (launch(context, mapsIntent)) return true
        if (launch(context, Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encoded")))) {
            return true
        }
        val web = "https://www.google.com/maps/search/?api=1&query=${urlEncode(trimmed)}"
        return launch(context, Intent(Intent.ACTION_VIEW, Uri.parse(web)))
    }

    fun navigate(context: Context, destination: SavedDestination): Boolean {
        val latitude = destination.latitude
        val longitude = destination.longitude
        if (latitude != null && longitude != null) {
            return navigateTo(context, latitude, longitude, destination.label)
        }
        return openSearch(context, destination.label)
    }

    fun navigateTo(context: Context, latitude: Double, longitude: Double, label: String): Boolean {
        val coords = String.format(Locale.US, "%.6f,%.6f", latitude, longitude)
        val mapsNavigation = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("google.navigation:q=$coords")
        ).setPackage(GOOGLE_MAPS_PACKAGE)
        if (launch(context, mapsNavigation)) return true

        val labelPart = if (label.isNotBlank()) "(${Uri.encode(label)})" else ""
        val geo = Uri.parse("geo:$coords?q=$coords$labelPart")
        if (launch(context, Intent(Intent.ACTION_VIEW, geo))) return true

        val web = "https://www.google.com/maps/dir/?api=1&destination=${urlEncode(coords)}"
        return launch(context, Intent(Intent.ACTION_VIEW, Uri.parse(web)))
    }

    fun showOnMap(context: Context, point: GeoPoint, label: String?): Boolean {
        val coords = String.format(Locale.US, "%.6f,%.6f", point.latitude, point.longitude)
        val encodedLabel = label?.takeIf { it.isNotBlank() }?.let { "(${Uri.encode(it)})" } ?: ""
        val geo = Uri.parse("geo:$coords?q=$coords$encodedLabel")
        if (launch(context, Intent(Intent.ACTION_VIEW, geo))) return true
        val web = "https://www.google.com/maps/search/?api=1&query=$coords"
        return launch(context, Intent(Intent.ACTION_VIEW, Uri.parse(web)))
    }

    fun openGoogleMaps(context: Context): Boolean {
        val launchMaps = context.packageManager.getLaunchIntentForPackage(GOOGLE_MAPS_PACKAGE)
        if (launchMaps != null && launch(context, launchMaps)) return true
        return launch(context, Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0")))
    }

    private fun launch(context: Context, intent: Intent): Boolean {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun urlEncode(value: String): String =
        URLEncoder.encode(value, "UTF-8")
}
