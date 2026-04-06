package io.mawaqit.lib.internal.astronomy

import kotlin.math.cos
import kotlin.math.sin

/**
 * IAU 1980 Nutation model — computes nutation in longitude (Δψ) and obliquity (Δε).
 * Based on Meeus "Astronomical Algorithms" Table 22.A (63 periodic terms).
 */
internal object Nutation {

    data class Result(
        val deltaPsi: Double,     // nutation in longitude, arcseconds
        val deltaEpsilon: Double, // nutation in obliquity, arcseconds
    )

    fun compute(T: Double): Result {
        // Fundamental arguments in degrees (Meeus Ch.22)
        val D = normalizeAngle(polynomial(T, 297.85036, 445267.111480, -0.0019142, 1.0 / 189474.0))
        val M = normalizeAngle(polynomial(T, 357.52772, 35999.050340, -0.0001603, -1.0 / 300000.0))
        val Mp = normalizeAngle(polynomial(T, 134.96298, 477198.867398, 0.0086972, 1.0 / 56250.0))
        val F = normalizeAngle(polynomial(T, 93.27191, 483202.017538, -0.0036825, 1.0 / 327270.0))
        val omega = normalizeAngle(polynomial(T, 125.04452, -1934.136261, 0.0020708, 1.0 / 450000.0))

        var dpsi = 0.0
        var deps = 0.0

        for (i in TABLE.indices step 9) {
            val arg = (TABLE[i] * D + TABLE[i + 1] * M + TABLE[i + 2] * Mp +
                    TABLE[i + 3] * F + TABLE[i + 4] * omega).toRadians()
            dpsi += (TABLE[i + 5] + TABLE[i + 6] * T) * sin(arg)
            deps += (TABLE[i + 7] + TABLE[i + 8] * T) * cos(arg)
        }

        return Result(
            deltaPsi = dpsi / 10000.0,     // convert from 0.0001" to arcseconds
            deltaEpsilon = deps / 10000.0,
        )
    }

    // Each row: D, M, M', F, Ω, ψ_sin, ψ_sin_T, ε_cos, ε_cos_T
    // Coefficients in units of 0.0001 arcsecond.
    // Source: Meeus Table 22.A (first 63 terms, sorted by magnitude)
    @Suppress("LongMethod")
    private val TABLE = intArrayOf(
         0,  0,  0,  0,  1, -171996, -1742, 92025,  89,
        -2,  0,  0,  2,  2,  -13187,   -16,  5736, -31,
         0,  0,  0,  2,  2,   -2274,    -2,   977,  -5,
         0,  0,  0,  0,  2,    2062,     2,  -895,   5,
         0,  1,  0,  0,  0,    1426,   -34,    54,  -1,
         0,  0,  1,  0,  0,     712,     1,    -7,   0,
        -2,  1,  0,  2,  2,    -517,    12,   224,  -6,
         0,  0,  0,  2,  1,    -386,    -4,   200,   0,
         0,  0,  1,  2,  2,    -301,     0,   129,  -1,
        -2, -1,  0,  2,  2,     217,    -5,   -95,   3,
        -2,  0,  1,  0,  0,    -158,     0,     0,   0,
        -2,  0,  0,  2,  1,     129,     1,   -70,   0,
         0,  0, -1,  2,  2,     123,     0,   -53,   0,
         2,  0,  0,  0,  0,      63,     0,     0,   0,
         0,  0,  1,  0,  1,      63,     1,   -33,   0,
         2,  0, -1,  2,  2,     -59,     0,    26,   0,
         0,  0, -1,  0,  1,     -58,    -1,    32,   0,
         0,  0,  1,  2,  1,     -51,     0,    27,   0,
        -2,  0,  2,  0,  0,      48,     0,     0,   0,
         0,  0, -2,  2,  1,      46,     0,   -24,   0,
         2,  0,  0,  2,  2,     -38,     0,    16,   0,
         0,  0,  2,  2,  2,     -31,     0,    13,   0,
         0,  0,  2,  0,  0,      29,     0,     0,   0,
        -2,  0,  1,  2,  2,      29,     0,   -12,   0,
         0,  0,  0,  2,  0,      26,     0,     0,   0,
        -2,  0,  0,  2,  0,     -22,     0,     0,   0,
         0,  0, -1,  2,  1,      21,     0,   -10,   0,
         0,  2,  0,  0,  0,      17,    -1,     0,   0,
         2,  0, -1,  0,  1,      16,     0,    -8,   0,
        -2,  2,  0,  2,  2,     -16,     1,     7,   0,
         0,  1,  0,  0,  1,     -15,     0,     9,   0,
        -2,  0,  1,  0,  1,     -13,     0,     7,   0,
         0, -1,  0,  0,  1,     -12,     0,     6,   0,
         0,  0,  2, -2,  0,      11,     0,     0,   0,
         2,  0, -1,  2,  1,     -10,     0,     5,   0,
         2,  0,  1,  2,  2,      -8,     0,     3,   0,
         0,  1,  0,  2,  2,       7,     0,    -3,   0,
        -2,  1,  1,  0,  0,      -7,     0,     0,   0,
         0, -1,  0,  2,  2,      -7,     0,     3,   0,
         2,  0,  0,  2,  1,      -7,     0,     3,   0,
         2,  0,  1,  0,  0,      -8,     0,     0,   0,
        -2,  0,  2,  2,  2,       6,     0,    -3,   0,
        -2,  0,  1,  2,  1,       6,     0,    -3,   0,
         2,  0, -2,  0,  1,      -6,     0,     3,   0,
         2,  0,  0,  0,  1,      -6,     0,     3,   0,
         0, -1,  1,  0,  0,       5,     0,     0,   0,
        -2, -1,  0,  2,  1,      -5,     0,     3,   0,
        -2,  0,  0,  0,  1,      -5,     0,     3,   0,
         0,  0,  2,  2,  1,      -5,     0,     3,   0,
        -2,  0,  2,  0,  1,       4,     0,     0,   0,
        -2,  1,  0,  2,  1,       4,     0,     0,   0,
         0,  0,  1, -2,  0,       4,     0,     0,   0,
        -1,  0,  1,  0,  0,      -4,     0,     0,   0,
        -2,  1,  0,  0,  0,      -4,     0,     0,   0,
         1,  0,  0,  0,  0,      -4,     0,     0,   0,
         0,  0,  1,  2,  0,       3,     0,     0,   0,
         0,  0, -2,  2,  2,      -3,     0,     0,   0,
        -1, -1,  1,  0,  0,      -3,     0,     0,   0,
         0,  1,  1,  0,  0,      -3,     0,     0,   0,
         0, -1,  1,  2,  2,      -3,     0,     0,   0,
         2, -1, -1,  2,  2,      -3,     0,     0,   0,
         0,  0,  3,  2,  2,      -3,     0,     0,   0,
         2, -1,  0,  2,  2,      -3,     0,     0,   0,
    )
}
