package io.mawaqit.lib.internal.astronomy

/**
 * Obliquity of the ecliptic — the tilt of Earth's axis.
 * Mean obliquity: Capitaine et al. (2003) formula, valid for ±10,000 years from J2000.0.
 * True obliquity: mean + nutation correction.
 */
internal object Obliquity {

    /** Mean obliquity of the ecliptic in degrees. T = Julian centuries from J2000.0. */
    fun mean(T: Double): Double {
        // Capitaine et al. (2003) — IAU 2006 precession
        // U = T / 100 (Julian millennia)
        val U = T / 100.0
        return polynomial(U,
            84381.406,      // ε₀ in arcseconds
            -4683.4944,
            -1.7946,
            1999.25,
            -51.38,
            -249.67,
            -39.05,
            7.12,
            27.87,
            5.79,
            2.45,
        ) / 3600.0          // convert arcseconds to degrees
    }

    /** True obliquity (mean + nutation in obliquity). Both inputs in consistent units. */
    fun trueObliquity(T: Double, deltaEpsilonArcsec: Double): Double =
        mean(T) + deltaEpsilonArcsec / 3600.0
}
