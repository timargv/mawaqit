package io.mawaqit.lib.model

/**
 * Geographic coordinates of an observer.
 * @property latitude degrees North (positive) / South (negative), range [-90, 90]
 * @property longitude degrees East (positive) / West (negative), range [-180, 180]
 * @property elevation meters above sea level, default 0
 */
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double = 0.0,
)
