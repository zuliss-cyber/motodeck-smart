package com.motosmart.launcher.util

import org.junit.Assert.assertEquals
import org.junit.Test

class HeadingUtilsTest {

    @Test
    fun cardinalPointsFollowCompassDirections() {
        assertEquals("N", HeadingUtils.cardinal(0f))
        assertEquals("NE", HeadingUtils.cardinal(45f))
        assertEquals("E", HeadingUtils.cardinal(90f))
        assertEquals("SW", HeadingUtils.cardinal(225f))
        assertEquals("W", HeadingUtils.cardinal(270f))
        assertEquals("N", HeadingUtils.cardinal(359f))
    }

    @Test
    fun degreesAreNormalised() {
        assertEquals(10f, HeadingUtils.normalize(370f))
        assertEquals(350f, HeadingUtils.normalize(-10f))
    }

    @Test
    fun bearingFormatsWholeDegrees() {
        assertEquals("359°", HeadingUtils.bearing(359.2f))
        assertEquals("0°", HeadingUtils.bearing(0f))
    }
}
