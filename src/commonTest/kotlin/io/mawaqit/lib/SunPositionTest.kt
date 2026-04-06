package io.mawaqit.lib

import io.mawaqit.lib.internal.astronomy.JulianDate
import io.mawaqit.lib.internal.astronomy.SunPosition
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class SunPositionTest {

    @Test
    fun meeusSample25a() {
        // Meeus Example 25.a: 1992 Oct 13 at 0h TD
        // Expected: RA ≈ 198.38°, Dec ≈ -7.78°
        val jd = JulianDate.fromGregorian(1992, 10, 13, 0.0)
        val sun = SunPosition.compute(jd)

        assertClose(198.38, sun.rightAscension, 0.5)
        assertClose(-7.78, sun.declination, 0.2)
    }

    @Test
    fun equinoxDeclination() {
        // Near vernal equinox (Mar 20, 2026), declination should be close to 0°
        val jd = JulianDate.atNoon(2026, 3, 20)
        val sun = SunPosition.compute(jd)

        assertClose(0.0, sun.declination, 1.0)
    }

    @Test
    fun solsticeDeclination() {
        // Near summer solstice (Jun 21, 2026), declination should be near +23.44°
        val jd = JulianDate.atNoon(2026, 6, 21)
        val sun = SunPosition.compute(jd)

        assertClose(23.44, sun.declination, 0.5)
    }

    @Test
    fun winterSolsticeDeclination() {
        // Near winter solstice (Dec 21, 2025), declination should be near -23.44°
        val jd = JulianDate.atNoon(2025, 12, 21)
        val sun = SunPosition.compute(jd)

        assertClose(-23.44, sun.declination, 0.5)
    }

    @Test
    fun equationOfTimeRange() {
        // EoT should be in range [-17, +17] minutes for any date
        for (month in 1..12) {
            val jd = JulianDate.atNoon(2026, month, 15)
            val sun = SunPosition.compute(jd)
            assertTrue(
                sun.equationOfTime > -17.0 && sun.equationOfTime < 17.0,
                "EoT out of range for month $month: ${sun.equationOfTime}"
            )
        }
    }

    @Test
    fun distanceRange() {
        // Sun distance should be ~0.983–1.017 AU
        val jd = JulianDate.atNoon(2026, 4, 6)
        val sun = SunPosition.compute(jd)

        assertTrue(sun.distance > 0.98 && sun.distance < 1.02,
            "Distance out of range: ${sun.distance}")
    }

    private fun assertClose(expected: Double, actual: Double, delta: Double) {
        assertTrue(
            abs(expected - actual) <= delta,
            "Expected $expected ± $delta, got $actual (diff=${abs(expected - actual)})"
        )
    }
}
