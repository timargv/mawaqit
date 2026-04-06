package io.mawaqit.lib

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class HijriTest {

    @Test
    fun knownDate2026() {
        // 2026-Apr-06 ≈ 18 Shawwal 1447 AH
        val hijri = Mawaqit.toHijri(2026, 4, 6)
        assertEquals(1447, hijri.year)
        assertEquals(10, hijri.month) // Shawwal = month 10
    }

    @Test
    fun ramadan2026() {
        // Ramadan 2026 starts approximately 2026-Feb-18
        val hijri = Mawaqit.toHijri(2026, 2, 18)
        assertEquals(9, hijri.month) // Ramadan = month 9
    }

    @Test
    fun fromLocalDate() {
        val hijri = Mawaqit.toHijri(LocalDate(2026, 4, 6))
        assertEquals(1447, hijri.year)
    }

    @Test
    fun monthNames() {
        val hijri = Mawaqit.toHijri(2026, 2, 18) // Ramadan
        assertEquals("Ramadan", hijri.monthNameEn)
        assertEquals("رمضان", hijri.monthNameAr)
    }

    @Test
    fun formatted() {
        val hijri = Mawaqit.toHijri(2026, 4, 6)
        // Should contain year and "AH"
        assert(hijri.formatted.contains("1447"))
        assert(hijri.formatted.contains("AH"))
    }
}
