package io.mawaqit.lib.internal.astronomy

import kotlin.math.*

/**
 * Atmospheric refraction and elevation correction.
 */
internal object Refraction {

    /**
     * Standard atmospheric refraction correction in degrees.
     * Bennett's formula (1982), accurate to ~0.07 arcminutes.
     * @param apparentElevationDeg apparent elevation above horizon in degrees
     */
    fun correction(apparentElevationDeg: Double): Double {
        if (apparentElevationDeg > 85.0) return 0.0
        val h = apparentElevationDeg
        val r = 1.02 / tan((h + 10.3 / (h + 5.11)).toRadians()) + 0.0019279
        return r / 60.0 // arcminutes to degrees
    }

    /**
     * Effective sun elevation angle at the geometric horizon,
     * accounting for observer elevation above sea level.
     * Returns a negative value (sun is below geometric horizon at visible sunrise/sunset).
     * @param elevationMeters observer elevation in meters above sea level
     */
    fun sunAngleAtHorizon(elevationMeters: Double): Double {
        if (elevationMeters <= 0.0) return -0.8333 // standard 50' refraction
        // Geometric dip angle due to elevation
        val dip = acos(6371000.0 / (6371000.0 + elevationMeters)).toDegrees()
        return -(0.8333 + dip)
    }
}
