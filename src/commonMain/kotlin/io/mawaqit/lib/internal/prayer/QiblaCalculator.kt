package io.mawaqit.lib.internal.prayer

import io.mawaqit.lib.internal.astronomy.toRadians
import io.mawaqit.lib.internal.astronomy.toDegrees
import io.mawaqit.lib.model.Coordinates
import kotlin.math.*

/**
 * Qibla direction and distance to the Kaaba.
 */
internal object QiblaCalculator {

    private const val KAABA_LAT = 21.4225
    private const val KAABA_LNG = 39.8262

    /** True bearing from observer to the Kaaba in degrees [0, 360). */
    fun direction(from: Coordinates): Double {
        val lat1 = from.latitude.toRadians()
        val lat2 = KAABA_LAT.toRadians()
        val dLng = (KAABA_LNG - from.longitude).toRadians()

        val x = sin(dLng) * cos(lat2)
        val y = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        val bearing = atan2(x, y).toDegrees()
        return (bearing + 360.0) % 360.0
    }

    /** Great-circle distance from observer to the Kaaba in kilometers (Haversine). */
    fun distance(from: Coordinates): Double {
        val R = 6371.0
        val lat1 = from.latitude.toRadians()
        val lat2 = KAABA_LAT.toRadians()
        val dLat = (KAABA_LAT - from.latitude).toRadians()
        val dLng = (KAABA_LNG - from.longitude).toRadians()

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}
