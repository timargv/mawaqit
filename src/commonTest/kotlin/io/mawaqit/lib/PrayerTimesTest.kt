package io.mawaqit.lib

import io.mawaqit.lib.model.*
import kotlinx.datetime.*
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PrayerTimesTest {

    private val mecca = Coordinates(21.4225, 39.8262)
    private val moscow = Coordinates(55.7558, 37.6173)
    private val kualaLumpur = Coordinates(3.1390, 101.6869)

    @Test
    fun meccaBasicOrder() {
        // All prayer times should be in chronological order for Mecca
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = mecca,
        )

        val fajr = day.fajr()
        val sunrise = day.sunrise()
        val dhuhr = day.dhuhr()
        val asr = day.asr()
        val maghrib = day.maghrib()
        val isha = day.isha()

        assertTrue(fajr < sunrise, "Fajr should be before Sunrise")
        assertTrue(sunrise < dhuhr, "Sunrise should be before Dhuhr")
        assertTrue(dhuhr < asr, "Dhuhr should be before Asr")
        assertTrue(asr < maghrib, "Asr should be before Maghrib")
        assertTrue(maghrib < isha, "Maghrib should be before Isha")
    }

    @Test
    fun meccaTimesReasonable() {
        // Mecca Fajr should be roughly between 01:30-02:30 UTC (4:30-5:30 local)
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = mecca,
        )
        val fajrHour = day.fajr().toLocalDateTime(TimeZone.UTC).hour
        assertTrue(fajrHour in 1..3, "Mecca Fajr UTC hour should be 1-3, got $fajrHour")

        // Dhuhr should be around 09:00-10:00 UTC (12:00-13:00 local)
        val dhuhrHour = day.dhuhr().toLocalDateTime(TimeZone.UTC).hour
        assertTrue(dhuhrHour in 9..10, "Mecca Dhuhr UTC hour should be 9-10, got $dhuhrHour")
    }

    @Test
    fun allEventsPresent() {
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = mecca,
        )

        for (event in PrayerEvent.entries) {
            assertNotNull(day[event], "Event $event should be present")
        }
    }

    @Test
    fun suhurBeforeFajr() {
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = mecca,
            suhurOffsetMinutes = 10,
        )
        val suhur = day[PrayerEvent.SUHUR]!!
        val fajr = day.fajr()

        assertTrue(suhur < fajr, "Suhur should be before Fajr")
        val diffMinutes = (fajr - suhur).inWholeMinutes
        assertTrue(diffMinutes in 9..11, "Suhur should be ~10 min before Fajr, got $diffMinutes")
    }

    @Test
    fun islamicMidnightBetweenMaghribAndFajr() {
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = mecca,
        )
        val maghrib = day.maghrib()
        val midnight = day[PrayerEvent.ISLAMIC_MIDNIGHT]!!
        val nextFajr = day[PrayerEvent.NEXT_FAJR]!!

        assertTrue(midnight > maghrib, "Islamic midnight should be after Maghrib")
        assertTrue(midnight < nextFajr, "Islamic midnight should be before next Fajr")
    }

    @Test
    fun qiyamAfterMidnight() {
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = mecca,
        )
        val midnight = day[PrayerEvent.ISLAMIC_MIDNIGHT]!!
        val qiyam = day[PrayerEvent.QIYAM]!!

        assertTrue(qiyam > midnight, "Qiyam should be after Islamic midnight")
    }

    @Test
    fun asrHanafiAfterStandardAsr() {
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = mecca,
        )
        val asr = day[PrayerEvent.ASR]!!
        val asrHanafi = day[PrayerEvent.ASR_HANAFI]!!

        assertTrue(asrHanafi > asr, "Hanafi Asr should be later than Standard Asr")
    }

    @Test
    fun moscowHighLatitude() {
        // Moscow should still produce valid times with middle-of-night rule
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 6, 21), // summer solstice — short nights
            coordinates = moscow,
            method = CalculationMethod.DUM_RF,
            highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
        )

        assertNotNull(day[PrayerEvent.FAJR], "Moscow Fajr should be computed")
        assertNotNull(day[PrayerEvent.ISHA], "Moscow Isha should be computed")
        assertTrue(day.fajr() < day.sunrise(), "Fajr before Sunrise")
    }

    @Test
    fun differentMethodsDifferentTimes() {
        val mwl = Mawaqit.calculate(LocalDate(2026, 4, 6), mecca, CalculationMethod.MWL)
        val isna = Mawaqit.calculate(LocalDate(2026, 4, 6), mecca, CalculationMethod.ISNA)

        // ISNA uses 15° for Fajr (MWL uses 18°), so ISNA Fajr should be later
        assertTrue(isna.fajr() > mwl.fajr(), "ISNA Fajr should be later than MWL Fajr")
    }

    @Test
    fun kualaLumpurJakim() {
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = kualaLumpur,
            method = CalculationMethod.JAKIM,
        )

        // Near equator — Fajr/Isha should always be valid
        assertNotNull(day[PrayerEvent.FAJR])
        assertNotNull(day[PrayerEvent.ISHA])

        // Day length near equator is ~12h, so sunrise~6AM, sunset~6PM local (UTC+8)
        val sunriseLocal = day.toLocalTime(PrayerEvent.SUNRISE, TimeZone.of("Asia/Kuala_Lumpur"))!!
        assertTrue(sunriseLocal.hour in 6..8, "KL sunrise should be 6-8 local, got ${sunriseLocal.hour}")
    }

    @Test
    fun customAdjustments() {
        val base = Mawaqit.calculate(LocalDate(2026, 4, 6), mecca)
        val adjusted = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = mecca,
            adjustments = mapOf(PrayerEvent.FAJR to 5),
        )

        val diffMinutes = (adjusted.fajr() - base.fajr()).inWholeMinutes
        assertTrue(diffMinutes in 4..6, "Adjustment of +5 min should shift Fajr by ~5 min, got $diffMinutes")
    }
}
