package io.mawaqit.lib

import io.mawaqit.lib.model.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EdgeCasesTest {

    @Test
    fun arcticSummerSolstice() {
        // Tromsø (69.6°N) — midnight sun, Fajr/Isha may not exist
        // With MIDDLE_OF_NIGHT rule, should still produce times
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 6, 21),
            coordinates = Coordinates(69.6, 18.9),
            highLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
        )
        assertNotNull(day[PrayerEvent.FAJR], "Arctic Fajr should be computed with high-lat rule")
        assertNotNull(day[PrayerEvent.ISHA], "Arctic Isha should be computed with high-lat rule")
    }

    @Test
    fun arcticSeventhOfNight() {
        // At 65°N (sub-arctic, no midnight sun but very short night)
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 6, 21),
            coordinates = Coordinates(65.0, 25.0),
            highLatitudeRule = HighLatitudeRule.SEVENTH_OF_NIGHT,
        )
        assertNotNull(day[PrayerEvent.FAJR])
        assertNotNull(day[PrayerEvent.ISHA])
        assertTrue(day.fajr() < day.sunrise())
        assertTrue(day.isha() > day.maghrib())
    }

    @Test
    fun equatorStability() {
        // Quito, Ecuador (0°, -78°) — near equator, Dhuhr time-of-day stable year-round
        val coords = Coordinates(0.0, -78.5)
        val summer = Mawaqit.calculate(LocalDate(2026, 6, 21), coords)
        val winter = Mawaqit.calculate(LocalDate(2026, 12, 21), coords)

        // Compare hour of day in UTC (should be within ~30 min)
        val summerDhuhr = summer.dhuhr().toLocalDateTime(TimeZone.UTC)
        val winterDhuhr = winter.dhuhr().toLocalDateTime(TimeZone.UTC)
        val diffMinutes = kotlin.math.abs(
            (summerDhuhr.hour * 60 + summerDhuhr.minute) -
            (winterDhuhr.hour * 60 + winterDhuhr.minute)
        )

        assertTrue(diffMinutes < 35, "Equatorial Dhuhr should vary < 35 min, diff=$diffMinutes")
    }

    @Test
    fun southernHemisphere() {
        // Cape Town (-33.9°, 18.4°)
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = Coordinates(-33.9, 18.4),
        )

        // All times should be valid and in order
        assertTrue(day.fajr() < day.sunrise())
        assertTrue(day.sunrise() < day.dhuhr())
        assertTrue(day.dhuhr() < day.asr())
        assertTrue(day.asr() < day.maghrib())
        assertTrue(day.maghrib() < day.isha())
    }

    @Test
    fun dateLineCrossing() {
        // Fiji (18°, 179°) — near date line
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = Coordinates(-18.0, 179.0),
        )
        assertNotNull(day[PrayerEvent.DHUHR])
        assertTrue(day.fajr() < day.isha())
    }

    @Test
    fun negativeLongitude() {
        // New York (-74°)
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = Coordinates(40.71, -74.01),
        )
        assertTrue(day.fajr() < day.sunrise())
        assertTrue(day.maghrib() < day.isha())
    }

    @Test
    fun highElevation() {
        // La Paz, Bolivia (3,640m elevation) — sunset should be slightly later
        val seaLevel = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = Coordinates(-16.5, -68.15, 0.0),
        )
        val highAlt = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = Coordinates(-16.5, -68.15, 3640.0),
        )

        // At higher elevation, sunrise is earlier and sunset is later
        assertTrue(highAlt.sunrise() < seaLevel.sunrise(),
            "Higher elevation should have earlier sunrise")
        assertTrue(highAlt.maghrib() > seaLevel.maghrib(),
            "Higher elevation should have later sunset")
    }

    @Test
    fun allMethodsProduceValidTimes() {
        val coords = Coordinates(21.4225, 39.8262) // Mecca
        val date = LocalDate(2026, 4, 6)

        for (method in CalculationMethod.entries) {
            val day = Mawaqit.calculate(date, coords, method)
            assertTrue(day.fajr() < day.sunrise(),
                "Method $method: Fajr should be before Sunrise")
            assertTrue(day.dhuhr() < day.asr(),
                "Method $method: Dhuhr should be before Asr")
            assertTrue(day.maghrib() < day.isha(),
                "Method $method: Maghrib should be before Isha")
        }
    }

    @Test
    fun leapYear() {
        // Feb 29 on a leap year
        val day = Mawaqit.calculate(
            date = LocalDate(2028, 2, 29),
            coordinates = Coordinates(21.4225, 39.8262),
        )
        assertNotNull(day[PrayerEvent.DHUHR])
    }

    @Test
    fun newYear() {
        // Jan 1 — next fajr crosses into Jan 2
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 1, 1),
            coordinates = Coordinates(21.4225, 39.8262),
        )
        val nextFajr = day[PrayerEvent.NEXT_FAJR]!!
        assertTrue(nextFajr > day.isha(), "Next Fajr should be after Isha")
    }
}
