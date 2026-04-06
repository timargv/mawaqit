package io.mawaqit.lib.internal.astronomy

import kotlin.math.PI

/** Convert degrees to radians. */
internal fun Double.toRadians(): Double = this * PI / 180.0

/** Convert radians to degrees. */
internal fun Double.toDegrees(): Double = this * 180.0 / PI

/** Normalize angle to [0, 360) range. Handles negative values correctly. */
internal fun normalizeAngle(degrees: Double): Double {
    val r = degrees % 360.0
    return if (r < 0) r + 360.0 else r
}

/** Evaluate polynomial using Horner's method: coeffs[0] + coeffs[1]*x + coeffs[2]*x^2 + ... */
internal fun polynomial(x: Double, vararg coeffs: Double): Double {
    var result = 0.0
    for (i in coeffs.indices.reversed()) {
        result = result * x + coeffs[i]
    }
    return result
}
