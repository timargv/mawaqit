package io.mawaqit.lib.internal.astronomy

import kotlin.math.*

/**
 * Geocentric sun position using Meeus "Astronomical Algorithms" Ch.25.
 * Computes apparent RA, Declination, Equation of Time, and distance.
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
        val trueAnomalyRad = sunTrueAnomaly.toRadians()
        val R = (1.000001018 * (1 - 0.016708634 * 0.016708634)) /
                (1 + 0.016708634 * cos(trueAnomalyRad))

        // Nutation and obliquity
        val nut = Nutation.compute(T)
        val epsilon = Obliquity.trueObliquity(T, nut.deltaEpsilon)
        val epsilonRad = epsilon.toRadians()

        // Apparent longitude (with nutation + aberration)
        val omega = (125.04 - 1934.136 * T).toRadians()
        val apparentLon = (sunTrueLon - 0.00569 - 0.00478 * sin(omega)).toRadians()

        // Apparent RA and Dec
        val sinApparentLon = sin(apparentLon)
        val cosApparentLon = cos(apparentLon)

        val RA = normalizeAngle(
            atan2(cos(epsilonRad) * sinApparentLon, cosApparentLon).toDegrees()
        )
        val dec = asin(sin(epsilonRad) * sinApparentLon).toDegrees()

        // Equation of Time (Meeus Ch.28)
        val y = tan(epsilonRad / 2).let { it * it }
        val L0rad = L0.toRadians()
        val ecc = 0.016708634 - 0.000042037 * T - 0.0000001267 * T * T
        val eot = y * sin(2 * L0rad) -
                2 * ecc * sin(Mrad) +
                4 * ecc * y * sin(Mrad) * cos(2 * L0rad) -
                0.5 * y * y * sin(4 * L0rad) -
                1.25 * ecc * ecc * sin(2 * Mrad)
        val eotMinutes = eot.toDegrees() * 4.0 // 1 degree = 4 minutes of time

        return Result(
            rightAscension = RA,
            declination = dec,
            equationOfTime = eotMinutes,
            distance = R,
        )
    }
}
