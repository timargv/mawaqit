package io.mawaqit.lib

import io.mawaqit.lib.internal.astronomy.Refraction
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class RefractionTest {

    @Test
    fun horizonRefraction() {
        // At 0° apparent elevation, refraction ≈ 0.48° (Bennett's formula)
        val r = Refraction.correction(0.0)
        assertClose(0.48, r, 0.05)
    }

    @Test
    fun highElevationMinimalRefraction() {
        // At 90° (zenith), refraction ≈ 0
        val r = Refraction.correction(90.0)
        assertClose(0.0, r, 0.01)
    }

    @Test
    fun tenDegreesRefraction() {
        // At 10° apparent elevation, refraction ≈ 0.09° (≈5.3 arcmin)
        val r = Refraction.correction(10.0)
        assertClose(0.09, r, 0.02)
    }

    @Test
    fun refractionDecreasesWithAltitude() {
        val r0 = Refraction.correction(0.0)
        val r10 = Refraction.correction(10.0)
        val r45 = Refraction.correction(45.0)
        val r80 = Refraction.correction(80.0)

        assertTrue(r0 > r10, "Refraction at 0° > 10°")
        assertTrue(r10 > r45, "Refraction at 10° > 45°")
        assertTrue(r45 > r80, "Refraction at 45° > 80°")
    }

    @Test
    fun sunAngleAtSeaLevel() {
        // At sea level, standard sunrise/sunset angle = -0.8333°
        val angle = Refraction.sunAngleAtHorizon(0.0)
        assertClose(-0.8333, angle, 0.001)
    }

    @Test
    fun sunAngleAtElevation() {
        // At 1000m elevation, angle should be more negative (sun sets later)
        val angle0 = Refraction.sunAngleAtHorizon(0.0)
        val angle1000 = Refraction.sunAngleAtHorizon(1000.0)

        assertTrue(angle1000 < angle0,
            "Higher elevation should give more negative angle: $angle1000 vs $angle0")
    }

    private fun assertClose(expected: Double, actual: Double, delta: Double) {
        assertTrue(
            abs(expected - actual) <= delta,
            "Expected $expected ± $delta, got $actual (diff=${abs(expected - actual)})"
        )
    }
}
