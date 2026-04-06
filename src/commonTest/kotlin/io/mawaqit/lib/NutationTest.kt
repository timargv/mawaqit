package io.mawaqit.lib

import io.mawaqit.lib.internal.astronomy.JulianDate
import io.mawaqit.lib.internal.astronomy.Nutation
import io.mawaqit.lib.internal.astronomy.Obliquity
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class NutationTest {

    @Test
    fun meeusSample22a() {
        // Meeus Example 22.a: 1987 Apr 10 at 0h TD
        // Expected: Δψ ≈ -3.788", Δε ≈ +9.443"
        val jd = JulianDate.fromGregorian(1987, 4, 10, 0.0)
        val T = JulianDate.julianCenturies(jd)
        val result = Nutation.compute(T)

        assertClose(-3.788, result.deltaPsi, 0.5)
        assertClose(9.443, result.deltaEpsilon, 0.5)
    }

    @Test
    fun nutationSignsReasonable() {
        // Nutation values should be small (typically < 20 arcseconds)
        val jd = JulianDate.atNoon(2026, 4, 6)
        val T = JulianDate.julianCenturies(jd)
        val result = Nutation.compute(T)

        assertTrue(abs(result.deltaPsi) < 20.0, "deltaPsi too large: ${result.deltaPsi}")
        assertTrue(abs(result.deltaEpsilon) < 15.0, "deltaEpsilon too large: ${result.deltaEpsilon}")
    }

    @Test
    fun obliquityMeanJ2000() {
        // Mean obliquity at J2000.0 ≈ 23.4393°
        val meanObl = Obliquity.mean(0.0)
        assertClose(23.4393, meanObl, 0.001)
    }

    @Test
    fun obliquityTrueWithNutation() {
        val jd = JulianDate.fromGregorian(1987, 4, 10, 0.0)
        val T = JulianDate.julianCenturies(jd)
        val nut = Nutation.compute(T)
        val trueObl = Obliquity.trueObliquity(T, nut.deltaEpsilon)

        // True obliquity should be near 23.44° for this date
        assertClose(23.44, trueObl, 0.05)
    }

    private fun assertClose(expected: Double, actual: Double, delta: Double) {
        assertTrue(
            abs(expected - actual) <= delta,
            "Expected $expected ± $delta, got $actual (diff=${abs(expected - actual)})"
        )
    }
}
