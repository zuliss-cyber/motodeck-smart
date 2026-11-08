package com.motosmart.launcher.util

import com.motosmart.launcher.data.preferences.SpeedUnit
import com.motosmart.launcher.data.preferences.TemperatureUnit
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

object UnitFormatter {

    fun speedValue(kmh: Float, unit: SpeedUnit): Int =
        (if (unit == SpeedUnit.MPH) kmh * 0.621371f else kmh).roundToInt().coerceAtLeast(0)

    fun speedText(kmh: Float, unit: SpeedUnit): String = speedValue(kmh, unit).toString()

    fun speedSymbol(unit: SpeedUnit): String =
        if (unit == SpeedUnit.MPH) "mph" else "km/h"

    fun gaugeMaximum(unit: SpeedUnit): Int = if (unit == SpeedUnit.MPH) 120 else 200

    fun temperature(valueCelsius: Float, unit: TemperatureUnit): Int = when (unit) {
        TemperatureUnit.CELSIUS -> valueCelsius.roundToInt()
        TemperatureUnit.FAHRENHEIT -> (valueCelsius * 9f / 5f + 32f).roundToInt()
    }

    fun temperatureSymbol(unit: TemperatureUnit): String =
        if (unit == TemperatureUnit.CELSIUS) "°C" else "°F"

    fun clock(time: LocalTime, use24Hour: Boolean): String {
        val pattern = if (use24Hour) "HH:mm" else "hh:mm"
        return time.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }

    fun seconds(time: LocalTime): String =
        time.format(DateTimeFormatter.ofPattern("ss", Locale.getDefault()))

    fun period(time: LocalTime): String =
        time.format(DateTimeFormatter.ofPattern("a", Locale.getDefault()))

    fun date(date: LocalDate, locale: Locale = Locale.getDefault()): String =
        date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale))

    fun distanceKm(value: Float): String = String.format(Locale.US, "%.1f", value)

    fun accuracy(meters: Float?): String = meters?.let { "${it.roundToInt()} m" } ?: "—"

    fun batteryTemperature(celsius: Float?): String =
        celsius?.let { String.format(Locale.US, "%.1f °C", it) } ?: "—"

    fun relativeTime(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
        if (epochMillis <= 0L) return "—"
        val seconds = ((now - epochMillis) / 1000L).coerceAtLeast(0L)
        return when {
            seconds < 10 -> "just now"
            seconds < 60 -> "${seconds}s ago"
            seconds < 3600 -> "${seconds / 60}m ago"
            seconds < 86_400 -> "${seconds / 3600}h ago"
            else -> "${seconds / 86_400}d ago"
        }
    }

    fun clockWithSeconds(time: LocalTime, use24Hour: Boolean): String {
        val pattern = if (use24Hour) "HH:mm:ss" else "hh:mm:ss a"
        return time.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }
}

object HeadingUtils {

    private val cardinalPoints = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    private val cardinalNames = listOf(
        "North", "North East", "East", "South East",
        "South", "South West", "West", "North West"
    )

    fun cardinal(headingDegrees: Float): String {
        val normalized = normalize(headingDegrees)
        val index = ((normalized + 22.5f) / 45f).toInt() % 8
        return cardinalPoints[index]
    }

    fun cardinalName(headingDegrees: Float): String {
        val normalized = normalize(headingDegrees)
        val index = ((normalized + 22.5f) / 45f).toInt() % 8
        return cardinalNames[index]
    }

    fun bearing(headingDegrees: Float): String =
        "${normalize(headingDegrees).roundToInt()}°"

    fun normalize(degrees: Float): Float = ((degrees % 360f) + 360f) % 360f
}
