package io.mawaqit.lib

import io.mawaqit.lib.internal.astronomy.DeltaT
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class DeltaTTest {

    @Test
    fun year2000() {
        // ΔT in 2000 ≈ 63.8 seconds
        val dt = DeltaT.estimate(2000.0)
        assertClose(63.8, dt, 1.0)
    }

    @Test
    fun year2020() {
        // ΔT in 2020 ≈ 69-72 seconds
        val dt = DeltaT.estimate(2020.0)
        assertClose(71.0, dt, 3.0)
    }

    @Test
    fun year1900() {
        // ΔT in 1900 ≈ -2.8 seconds
        val dt = DeltaT.estimate(1900.0)
        assertClose(-2.8, dt, 2.0)
    }

    @Test
    fun year1950() {
        // ΔT in 1950 ≈ 29.1 seconds
        val dt = DeltaT.estimate(1950.0)
        assertClose(29.1, dt, 1.0)
    }

    @Test
    fun monotonicRecentYears() {
        // ΔT should be generally increasing in recent decades
        val dt2000 = DeltaT.estimate(2000.0)
        val dt2010 = DeltaT.estimate(2010.0)
        val dt2020 = DeltaT.estimate(2020.0)

        assertTrue(dt2010 > dt2000, "ΔT should increase 2000→2010")
        assertTrue(dt2020 > dt2010, "ΔT should increase 2010→2020")
    }

    @Test
    fun futureReasonable() {
        // ΔT in 2050 should be positive and reasonable (< 200s)
        val dt = DeltaT.estimate(2050.0)
        assertTrue(dt > 50.0 && dt < 200.0, "ΔT 2050 should be 50-200s, got $dt")
    }

    @Test
    fun ancientDateDoesNotCrash() {
        // Should handle very old dates without exception
        val dt = DeltaT.estimate(-1000.0)
        assertTrue(dt.isFinite(), "ΔT for -1000 should be finite")
    }

    private fun assertClose(expected: Double, actual: Double, delta: Double) {
        assertTrue(
            abs(expected - actual) <= delta,
            "Expected $expected ± $delta, got $actual (diff=${abs(expected - actual)})"
        )
    }
}
