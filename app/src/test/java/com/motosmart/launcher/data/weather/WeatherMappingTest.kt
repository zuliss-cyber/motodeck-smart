package com.motosmart.launcher.data.weather

import com.motosmart.launcher.data.preferences.WeatherProvider
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherMappingTest {

    @Test
    fun openMeteoWmoCodesMapToConditions() {
        assertEquals(WeatherCondition.CLEAR, WeatherRepository.conditionFromWmo(0))
        assertEquals(WeatherCondition.PARTLY_CLOUDY, WeatherRepository.conditionFromWmo(2))
        assertEquals(WeatherCondition.CLOUDY, WeatherRepository.conditionFromWmo(3))
        assertEquals(WeatherCondition.RAIN, WeatherRepository.conditionFromWmo(63))
        assertEquals(WeatherCondition.SNOW, WeatherRepository.conditionFromWmo(75))
        assertEquals(WeatherCondition.THUNDER, WeatherRepository.conditionFromWmo(95))
        assertEquals(WeatherCondition.UNKNOWN, WeatherRepository.conditionFromWmo(1234))
    }

    @Test
    fun openWeatherMapCodesMapToConditions() {
        assertEquals(WeatherCondition.THUNDER, WeatherRepository.conditionFromOwm(200))
        assertEquals(WeatherCondition.DRIZZLE, WeatherRepository.conditionFromOwm(310))
        assertEquals(WeatherCondition.RAIN, WeatherRepository.conditionFromOwm(501))
        assertEquals(WeatherCondition.SNOW, WeatherRepository.conditionFromOwm(601))
        assertEquals(WeatherCondition.FOG, WeatherRepository.conditionFromOwm(741))
        assertEquals(WeatherCondition.CLEAR, WeatherRepository.conditionFromOwm(800))
        assertEquals(WeatherCondition.CLOUDY, WeatherRepository.conditionFromOwm(804))
    }

    @Test
    fun descriptionsAreAvailableForEveryCondition() {
        WeatherCondition.entries.forEach { condition ->
            assertEquals(true, WeatherRepository.descriptionFor(condition).isNotBlank())
        }
        assertEquals("Clear sky", WeatherRepository.descriptionFor(WeatherCondition.CLEAR))
    }

    @Test
    fun weatherProviderEnumContainsLocalAndRemoteOptions() {
        assertEquals(3, WeatherProvider.entries.size)
    }
}
