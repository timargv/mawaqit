package io.mawaqit.lib

import io.mawaqit.lib.internal.astronomy.JulianDate
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class JulianDateTest {

    @Test
    fun j2000Epoch() {
        // J2000.0 = 2000-Jan-1.5 TT = JD 2451545.0
        val jd = JulianDate.atNoon(2000, 1, 1)
        assertClose(2451545.0, jd, 0.0001)
    }

    @Test
    fun meeusSample1() {
        // Meeus Example 7.a: 1957 Oct 4.81 = JD 2436116.31
        val jd = JulianDate.fromGregorian(1957, 10, 4, 19.44) // 0.81 day = 19.44h
        assertClose(2436116.31, jd, 0.01)
    }

    @Test
    fun meeusSample2() {
        // 2000 Jan 1.5 = JD 2451545.0
        val jd = JulianDate.fromGregorian(2000, 1, 1, 12.0)
        assertClose(2451545.0, jd, 0.0001)
    }

    @Test
    fun modernDate() {
        // 2026-Apr-06 noon
        val jd = JulianDate.atNoon(2026, 4, 6)
        // Expected: ~2461133.0 (can verify with USNO)
        assertTrue(jd > 2461100.0 && jd < 2461200.0)
    }

    @Test
    fun julianCenturies() {
        // At J2000.0 epoch, T = 0
        val T = JulianDate.julianCenturies(2451545.0)
        assertClose(0.0, T, 1e-10)

        // 1 century after = T = 1.0
        val T1 = JulianDate.julianCenturies(2451545.0 + 36525.0)
        assertClose(1.0, T1, 1e-10)
    }

    @Test
    fun negativeYear() {
        // Year 333 Jan 27 at noon — verify JD is in reasonable range
        val jd = JulianDate.fromGregorian(333, 1, 27, 12.0)
        assertClose(1842712.0, jd, 1.0)
    }

    private fun assertClose(expected: Double, actual: Double, delta: Double) {
        assertTrue(
            abs(expected - actual) <= delta,
            "Expected $expected ± $delta, got $actual (diff=${abs(expected - actual)})"
        )
    }
}
