package io.mawaqit.lib

import io.mawaqit.lib.model.Coordinates
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class QiblaTest {

    @Test
    fun moscowToMecca() {
        // Moscow → Mecca bearing ≈ 176°
        val bearing = Mawaqit.qiblaDirection(Coordinates(55.7558, 37.6173))
        assertClose(176.4, bearing, 3.0)
    }

    @Test
    fun newYorkToMecca() {
        // New York → Mecca bearing ≈ 58.5°
        val bearing = Mawaqit.qiblaDirection(Coordinates(40.7128, -74.0060))
        assertClose(58.5, bearing, 3.0)
    }

    @Test
    fun meccaToMecca() {
        // From Mecca itself — distance should be ~0
        val distance = Mawaqit.distanceToKaaba(Coordinates(21.4225, 39.8262))
        assertTrue(distance < 1.0, "Distance from Mecca to Kaaba should be ~0, got $distance")
    }

    @Test
    fun moscowDistance() {
        // Moscow → Mecca ≈ 3,822 km
        val distance = Mawaqit.distanceToKaaba(Coordinates(55.7558, 37.6173))
        assertClose(3822.0, distance, 50.0)
    }

    @Test
    fun kazanQibla() {
        // Kazan (Tatarstan) → Mecca — south-southwest
        val bearing = Mawaqit.qiblaDirection(Coordinates(55.7961, 49.1064))
        assertTrue(bearing > 180.0 && bearing < 210.0,
            "Kazan→Mecca should be ~190-200°, got $bearing")
    }

    private fun assertClose(expected: Double, actual: Double, delta: Double) {
        assertTrue(
            abs(expected - actual) <= delta,
            "Expected $expected ± $delta, got $actual (diff=${abs(expected - actual)})"
        )
    }
}
