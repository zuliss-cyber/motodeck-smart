package com.motosmart.launcher.data.weather

import com.motosmart.launcher.data.preferences.WeatherProvider

enum class WeatherCondition {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SHOWERS,
    SNOW,
    THUNDER,
    UNKNOWN
}

data class WeatherData(
    val temperatureCelsius: Float,
    val feelsLikeCelsius: Float?,
    val humidityPercent: Int?,
    val windKmh: Float?,
    val condition: WeatherCondition,
    val description: String,
    val locationLabel: String,
    val provider: WeatherProvider,
    val fetchedAtMillis: Long
)

sealed interface WeatherResult {
    data class Success(val data: WeatherData) : WeatherResult
    data class Error(val message: String) : WeatherResult
}
