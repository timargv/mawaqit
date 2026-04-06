package io.mawaqit.lib.internal.astronomy

/**
 * Estimate ΔT (TT − UT) in seconds for a given year.
 * Based on Espenak & Meeus polynomial expressions.
 * See: https://eclipse.gsfc.nasa.gov/SEhelp/deltatpoly2004.html
 */
internal object DeltaT {

    fun estimate(year: Double): Double {
        return when {
            year < -500 -> {
                val u = (year - 1820.0) / 100.0
                -20.0 + 32.0 * u * u
            }
            year < 500 -> {
                val u = year / 100.0
                polynomial(u, 10583.6, -1014.41, 33.78311, -5.952053,
                    -0.1798452, 0.022174192, 0.0090316521)
            }
            year < 1600 -> {
                val u = (year - 1000.0) / 100.0
                polynomial(u, 1574.2, -556.01, 71.23472, 0.319781,
                    -0.8503463, -0.005050998, 0.0083572073)
            }
            year < 1700 -> {
                val t = year - 1600.0
                polynomial(t, 120.0, -0.9808, -0.01532, 1.0 / 7129.0)
            }
            year < 1800 -> {
                val t = year - 1700.0
                polynomial(t, 8.83, 0.1603, -0.0059285, 0.00013336, -1.0 / 1174000.0)
            }
            year < 1860 -> {
                val t = year - 1800.0
                polynomial(t, 13.72, -0.332447, 0.0068612, 0.0041116,
                    -0.00037436, 0.0000121272, -0.0000001699, 0.000000000875)
            }
            year < 1900 -> {
                val t = year - 1860.0
                polynomial(t, 7.62, 0.5737, -0.251754, 0.01680668,
                    -0.0004473624, 1.0 / 233174.0)
            }
            year < 1920 -> {
                val t = year - 1900.0
                polynomial(t, -2.79, 1.494119, -0.0598939, 0.0061966, -0.000197)
            }
            year < 1941 -> {
                val t = year - 1920.0
                polynomial(t, 21.20, 0.84493, -0.076100, 0.0020936)
            }
            year < 1961 -> {
                val t = year - 1950.0
                polynomial(t, 29.07, 0.407, -1.0 / 233.0, 1.0 / 2547.0)
            }
            year < 1986 -> {
                val t = year - 1975.0
                polynomial(t, 45.45, 1.067, -1.0 / 260.0, -1.0 / 718.0)
            }
            year < 2005 -> {
                val t = year - 2000.0
                polynomial(t, 63.86, 0.3345, -0.060374, 0.0017275,
                    0.000651814, 0.00002373599)
            }
            year < 2050 -> {
                val t = year - 2000.0
                polynomial(t, 62.92, 0.32217, 0.005589)
            }
            year < 2150 -> {
                -20.0 + 32.0 * ((year - 1820.0) / 100.0).let { it * it } -
                        0.5628 * (2150.0 - year)
            }
            else -> {
                val u = (year - 1820.0) / 100.0
                -20.0 + 32.0 * u * u
            }
        }
    }
}
