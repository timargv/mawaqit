package io.mawaqit.lib.internal.astronomy

import kotlin.math.floor

/**
 * Julian Date conversions following Meeus "Astronomical Algorithms" Ch.7.
 */
internal object JulianDate {

    /** Convert Gregorian date + fractional hour (UTC) to Julian Day Number. */
    fun fromGregorian(year: Int, month: Int, day: Int, hourUTC: Double = 0.0): Double {
        var y = year.toDouble()
        var m = month.toDouble()
        if (m <= 2) {
            y -= 1.0
            m += 12.0
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716.0)) +
                floor(30.6001 * (m + 1.0)) +
                day.toDouble() + hourUTC / 24.0 + b - 1524.5
    }

    /** JD for a given date at 0h UT (midnight). */
    fun atMidnight(year: Int, month: Int, day: Int): Double =
        fromGregorian(year, month, day, 0.0)

    /** JD for a given date at 12h UT (noon) — standard astronomical convention. */
    fun atNoon(year: Int, month: Int, day: Int): Double =
        fromGregorian(year, month, day, 12.0)

    /** Julian centuries (T) from J2000.0 epoch. */
    fun julianCenturies(jd: Double): Double = (jd - 2451545.0) / 36525.0
}
