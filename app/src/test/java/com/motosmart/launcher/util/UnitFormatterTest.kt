package com.motosmart.launcher.util

import com.motosmart.launcher.data.preferences.SpeedUnit
import com.motosmart.launcher.data.preferences.TemperatureUnit
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitFormatterTest {

    @Test
    fun speedConvertsToMilesPerHour() {
        assertEquals(100, UnitFormatter.speedValue(100f, SpeedUnit.KMH))
        assertEquals(62, UnitFormatter.speedValue(100f, SpeedUnit.MPH))
        assertEquals("mph", UnitFormatter.speedSymbol(SpeedUnit.MPH))
    }

    @Test
    fun negativeSpeedIsClampedToZero() {
        assertEquals(0, UnitFormatter.speedValue(-5f, SpeedUnit.KMH))
    }

    @Test
    fun gaugeMaximumMatchesUnit() {
        assertEquals(200, UnitFormatter.gaugeMaximum(SpeedUnit.KMH))
        assertEquals(120, UnitFormatter.gaugeMaximum(SpeedUnit.MPH))
    }

    @Test
    fun temperatureConvertsBetweenUnits() {
        assertEquals(20, UnitFormatter.temperature(20f, TemperatureUnit.CELSIUS))
        assertEquals(68, UnitFormatter.temperature(20f, TemperatureUnit.FAHRENHEIT))
        assertEquals("°C", UnitFormatter.temperatureSymbol(TemperatureUnit.CELSIUS))
    }

    @Test
    fun clockFormatsTwelveAndTwentyFourHour() {
        val time = LocalTime.of(9, 5, 7)
        assertEquals("09:05", UnitFormatter.clock(time, use24Hour = true))
        assertEquals("09:05", UnitFormatter.clock(time, use24Hour = false))
        assertEquals("07", UnitFormatter.seconds(time))
    }

    @Test
    fun distanceUsesOneDecimalPlace() {
        assertEquals("12.3", UnitFormatter.distanceKm(12.34f))
    }
}
