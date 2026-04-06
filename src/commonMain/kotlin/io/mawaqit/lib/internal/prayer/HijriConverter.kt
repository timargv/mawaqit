package io.mawaqit.lib.internal.prayer

import io.mawaqit.lib.model.HijriDate
import kotlin.math.floor

/**
 * Gregorian to Hijri (Islamic) calendar conversion.
 * Uses the Kuwaiti algorithm (tabular Islamic calendar).
 */
internal object HijriConverter {

    fun toHijri(year: Int, month: Int, day: Int): HijriDate {
        val jd = gregorianToJD(year, month, day)
        return jdToHijri(jd)
    }

    private fun gregorianToJD(year: Int, month: Int, day: Int): Int {
        var y = year
        var m = month
        if (m <= 2) { y--; m += 12 }
        val a = y / 100
        val b = 2 - a + a / 4
        return (floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524).toInt()
    }

    private fun jdToHijri(jd: Int): HijriDate {
        val l = jd - 1948440 + 10632
        val n = ((l - 1) / 10631.0).toInt()
        val lr = l - 10631 * n + 354
        val j = (((10985.0 - lr) / 5316.0).toInt() * ((50.0 * lr / 17719.0).toInt())) +
                ((lr / 5670.0).toInt() * ((43.0 * lr / 15238.0).toInt()))
        val ld = lr - (((30.0 - j) / 15.0).toInt() * ((17719.0 * j / 50.0).toInt())) -
                ((j / 16.0).toInt() * ((15238.0 * j / 43.0).toInt())) + 29
        val m = ((24.0 * ld / 709.0).toInt())
        val d = ld - ((709.0 * m / 24.0).toInt())
        val y = 30 * n + j - 30

        return HijriDate(day = d, month = m, year = y)
    }
}
