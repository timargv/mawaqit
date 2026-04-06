package io.mawaqit.lib.internal.astronomy

import kotlin.math.*

/**
 * Convert geocentric sun position to observer-centric horizontal coordinates.
 */
internal object SolarCoordinates {

    data class Result(
        val azimuth: Double,   // degrees from North, clockwise
        val elevation: Double, // degrees above horizon
        val hourAngle: Double, // degrees
    )

    /**
     * Compute solar azimuth and elevation for an observer.
     * @param hourAngleDeg local hour angle in degrees
     * @param declinationDeg sun's declination in degrees
     * @param latitudeDeg observer's latitude in degrees
     */
    fun compute(hourAngleDeg: Double, declinationDeg: Double, latitudeDeg: Double): Result {
        val H = hourAngleDeg.toRadians()
        val dec = declinationDeg.toRadians()
        val lat = latitudeDeg.toRadians()

        // Altitude (elevation)
        val sinAlt = sin(lat) * sin(dec) + cos(lat) * cos(dec) * cos(H)
        val altitude = asin(sinAlt).toDegrees()

        // Azimuth (from North, clockwise)
        val azimuth = normalizeAngle(
            atan2(sin(H), cos(H) * sin(lat) - tan(dec) * cos(lat)).toDegrees() + 180.0
        )

        return Result(
            azimuth = azimuth,
            elevation = altitude,
            hourAngle = hourAngleDeg,
        )
    }
}
