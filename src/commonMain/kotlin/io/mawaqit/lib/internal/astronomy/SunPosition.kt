package io.mawaqit.lib.internal.astronomy

import kotlin.math.*

/**
 * Geocentric sun position using Meeus "Astronomical Algorithms" Ch.25.
 * Computes apparent RA, Declination, Equation of Time, and distance.
 *
 * EoT computed from first principles (L0 - RA + nutation) for ±2 sec precision
 * instead of the truncated series (±15 sec).
 */
internal object SunPosition {

    data class Result(
        val rightAscension: Double, // degrees
        val declination: Double,    // degrees
        val equationOfTime: Double, // minutes
        val distance: Double,       // AU
    )

    fun compute(jd: Double): Result {
        val T = JulianDate.julianCenturies(jd)

        // Geometric mean longitude of the Sun (degrees)
        val L0 = normalizeAngle(polynomial(T, 280.46646, 36000.76983, 0.0003032))

        // Mean anomaly of the Sun (degrees)
        val M = normalizeAngle(polynomial(T, 357.52911, 35999.05029, -0.0001537))
        val Mrad = M.toRadians()

        // Equation of center (degrees)
        val C = (1.9146 - 0.004817 * T - 0.000014 * T * T) * sin(Mrad) +
                (0.019993 - 0.000101 * T) * sin(2 * Mrad) +
                0.00029 * sin(3 * Mrad)

        // Sun's true longitude and true anomaly
        val sunTrueLon = L0 + C
        val sunTrueAnomaly = M + C

        // Sun's radius vector (distance in AU)
        val ecc = 0.016708634 - 0.000042037 * T - 0.0000001267 * T * T
        val trueAnomalyRad = sunTrueAnomaly.toRadians()
        val R = (1.000001018 * (1 - ecc * ecc)) / (1 + ecc * cos(trueAnomalyRad))

        // Nutation and obliquity
        val nut = Nutation.compute(T)
        val epsilon = Obliquity.trueObliquity(T, nut.deltaEpsilon)
        val epsilonRad = epsilon.toRadians()

        // Apparent longitude (with nutation + aberration)
        val deltaPsiDeg = nut.deltaPsi / 3600.0 // arcseconds to degrees
        val apparentLon = sunTrueLon + deltaPsiDeg - 20.4898 / 3600.0 / R
        val apparentLonRad = apparentLon.toRadians()

        // Apparent RA and Dec
        val sinApparentLon = sin(apparentLonRad)
        val cosApparentLon = cos(apparentLonRad)

        val RA = normalizeAngle(
            atan2(cos(epsilonRad) * sinApparentLon, cosApparentLon).toDegrees()
        )
        val dec = asin(sin(epsilonRad) * sinApparentLon).toDegrees()

        // Equation of Time from first principles (Meeus Ch.28, precise method):
        // EoT = L0 - 0.0057183° - RA + deltaPsi * cos(epsilon)
        // Result in degrees, convert to minutes (* 4 min/degree)
        val eotDeg = normalizeAngle(L0) - 0.0057183 - RA + deltaPsiDeg * cos(epsilonRad)
        // Normalize to [-180, 180] range
        val eotNormalized = if (eotDeg > 180) eotDeg - 360 else if (eotDeg < -180) eotDeg + 360 else eotDeg
        val eotMinutes = eotNormalized * 4.0

        return Result(
            rightAscension = RA,
            declination = dec,
            equationOfTime = eotMinutes,
            distance = R,
        )
    }
}
