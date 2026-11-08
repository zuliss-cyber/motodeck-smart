package com.motosmart.launcher.data.weather

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.motosmart.launcher.data.preferences.AppSettings
import com.motosmart.launcher.data.preferences.TemperatureUnit
import com.motosmart.launcher.data.preferences.WeatherProvider
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.roundToInt

class WeatherRepository(private val context: Context) {

    suspend fun fetch(
        settings: AppSettings,
        latitude: Double,
        longitude: Double,
        locationLabel: String
    ): WeatherResult = withContext(Dispatchers.IO) {
        if (settings.weatherProvider == WeatherProvider.DISABLED) {
            return@withContext WeatherResult.Error("Weather provider is disabled")
        }
        if (!hasNetwork()) {
            return@withContext WeatherResult.Error("No network connection")
        }
        try {
            when (settings.weatherProvider) {
                WeatherProvider.OPEN_METEO -> fetchOpenMeteo(latitude, longitude, locationLabel)
                WeatherProvider.OPEN_WEATHER_MAP -> {
                    if (settings.weatherApiKey.isBlank()) {
                        WeatherResult.Error("Add an OpenWeatherMap API key in Settings")
                    } else {
                        fetchOpenWeatherMap(
                            latitude,
                            longitude,
                            locationLabel,
                            settings.weatherApiKey.trim(),
                            settings.temperatureUnit
                        )
                    }
                }
                WeatherProvider.DISABLED -> WeatherResult.Error("Weather provider is disabled")
            }
        } catch (error: Exception) {
            WeatherResult.Error(error.message ?: "Weather service unreachable")
        }
    }

    private fun fetchOpenMeteo(
        latitude: Double,
        longitude: Double,
        locationLabel: String
    ): WeatherResult {
        val url = String.format(
            Locale.US,
            "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature," +
                "weather_code,wind_speed_10m&timezone=auto",
            latitude,
            longitude
        )
        val json = JSONObject(request(url))
        val current = json.optJSONObject("current")
            ?: return WeatherResult.Error("Unexpected weather response")
        val code = current.optInt("weather_code", -1)
        val condition = conditionFromWmo(code)
        return WeatherResult.Success(
            WeatherData(
                temperatureCelsius = current.optDouble("temperature_2m", Double.NaN)
                    .takeIf { !it.isNaN() }?.toFloat()
                    ?: return WeatherResult.Error("Temperature unavailable"),
                feelsLikeCelsius = current.optDouble("apparent_temperature", Double.NaN)
                    .takeIf { !it.isNaN() }?.toFloat(),
                humidityPercent = current.optInt("relative_humidity_2m", -1)
                    .takeIf { it in 0..100 },
                windKmh = current.optDouble("wind_speed_10m", Double.NaN)
                    .takeIf { !it.isNaN() }?.toFloat(),
                condition = condition,
                description = descriptionFor(condition),
                locationLabel = locationLabel,
                provider = WeatherProvider.OPEN_METEO,
                fetchedAtMillis = System.currentTimeMillis()
            )
        )
    }

    private fun fetchOpenWeatherMap(
        latitude: Double,
        longitude: Double,
        locationLabel: String,
        apiKey: String,
        temperatureUnit: TemperatureUnit
    ): WeatherResult {
        val units = if (temperatureUnit == TemperatureUnit.FAHRENHEIT) "imperial" else "metric"
        val url = String.format(
            Locale.US,
            "https://api.openweathermap.org/data/2.5/weather?lat=%.4f&lon=%.4f&units=%s&appid=%s",
            latitude,
            longitude,
            units,
            apiKey
        )
        val json = JSONObject(request(url))
        if (json.has("cod") && json.optString("cod") != "200") {
            return WeatherResult.Error(json.optString("message", "Weather request rejected"))
        }
        val main = json.optJSONObject("main")
            ?: return WeatherResult.Error("Unexpected weather response")
        val weather = json.optJSONArray("weather")?.optJSONObject(0)
        val rawTemp = main.optDouble("temp", Double.NaN)
        if (rawTemp.isNaN()) return WeatherResult.Error("Temperature unavailable")
        val tempCelsius = if (units == "imperial") ((rawTemp - 32.0) * 5.0 / 9.0) else rawTemp
        val feels = main.optDouble("feels_like", Double.NaN)
        val feelsCelsius = when {
            feels.isNaN() -> null
            units == "imperial" -> ((feels - 32.0) * 5.0 / 9.0).toFloat()
            else -> feels.toFloat()
        }
        val condition = conditionFromOwm(weather?.optInt("id", -1) ?: -1)
        val windMs = json.optJSONObject("wind")?.optDouble("speed", Double.NaN) ?: Double.NaN
        val city = json.optString("name").takeIf { it.isNotBlank() } ?: locationLabel
        return WeatherResult.Success(
            WeatherData(
                temperatureCelsius = tempCelsius.toFloat(),
                feelsLikeCelsius = feelsCelsius,
                humidityPercent = main.optInt("humidity", -1).takeIf { it in 0..100 },
                windKmh = windMs.takeIf { !it.isNaN() }?.let {
                    // With units=imperial OpenWeatherMap reports wind in mph,
                    // otherwise in metres per second.
                    if (units == "imperial") (it * 1.609344).toFloat()
                    else (it * 3.6).toFloat()
                },
                condition = condition,
                description = weather?.optString("description")
                    ?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
                    ?.takeIf { it.isNotBlank() }
                    ?: descriptionFor(condition),
                locationLabel = city,
                provider = WeatherProvider.OPEN_WEATHER_MAP,
                fetchedAtMillis = System.currentTimeMillis()
            )
        )
    }

    private fun request(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "MotorcycleSmartLauncher/1.0")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                val body = connection.errorStream?.bufferedReader()?.use { it.readText() }
                throw IOException("Weather service returned HTTP $code${body?.let { ": ${it.take(120)}" } ?: ""}")
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun hasNetwork(): Boolean {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    companion object {

        fun conditionFromWmo(code: Int): WeatherCondition = when (code) {
            0 -> WeatherCondition.CLEAR
            1, 2 -> WeatherCondition.PARTLY_CLOUDY
            3 -> WeatherCondition.CLOUDY
            45, 48 -> WeatherCondition.FOG
            51, 53, 55, 56, 57 -> WeatherCondition.DRIZZLE
            61, 63, 65, 66, 67 -> WeatherCondition.RAIN
            71, 73, 75, 77 -> WeatherCondition.SNOW
            80, 81, 82 -> WeatherCondition.SHOWERS
            85, 86 -> WeatherCondition.SNOW
            95, 96, 99 -> WeatherCondition.THUNDER
            else -> WeatherCondition.UNKNOWN
        }

        fun conditionFromOwm(id: Int): WeatherCondition = when (id) {
            in 200..232 -> WeatherCondition.THUNDER
            in 300..321 -> WeatherCondition.DRIZZLE
            in 500..531 -> WeatherCondition.RAIN
            in 600..622 -> WeatherCondition.SNOW
            in 701..781 -> WeatherCondition.FOG
            800 -> WeatherCondition.CLEAR
            801, 802 -> WeatherCondition.PARTLY_CLOUDY
            803, 804 -> WeatherCondition.CLOUDY
            else -> WeatherCondition.UNKNOWN
        }

        fun descriptionFor(condition: WeatherCondition): String = when (condition) {
            WeatherCondition.CLEAR -> "Clear sky"
            WeatherCondition.PARTLY_CLOUDY -> "Partly cloudy"
            WeatherCondition.CLOUDY -> "Cloudy"
            WeatherCondition.FOG -> "Fog"
            WeatherCondition.DRIZZLE -> "Drizzle"
            WeatherCondition.RAIN -> "Rain"
            WeatherCondition.SHOWERS -> "Rain showers"
            WeatherCondition.SNOW -> "Snow"
            WeatherCondition.THUNDER -> "Thunderstorm"
            WeatherCondition.UNKNOWN -> "Conditions unavailable"
        }

        fun temperature(valueCelsius: Float, unit: TemperatureUnit): Int = when (unit) {
            TemperatureUnit.CELSIUS -> valueCelsius.roundToInt()
            TemperatureUnit.FAHRENHEIT -> (valueCelsius * 9f / 5f + 32f).roundToInt()
        }

        fun temperatureSymbol(unit: TemperatureUnit): String = when (unit) {
            TemperatureUnit.CELSIUS -> "°C"
            TemperatureUnit.FAHRENHEIT -> "°F"
        }
    }
}
