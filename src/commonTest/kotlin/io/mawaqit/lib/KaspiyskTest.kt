package io.mawaqit.lib

import io.mawaqit.lib.model.*
import kotlinx.datetime.*
import kotlin.test.Test

class KaspiyskTest {
    @Test
    fun kaspiyskTimes() {
        val day = Mawaqit.calculate(
            date = LocalDate(2026, 4, 6),
            coordinates = Coordinates(42.8938, 47.6159),
            method = CalculationMethod.MWL,
        )
        val utc = TimeZone.UTC
        val msk = TimeZone.of("Europe/Moscow")
        
        listOf(
            PrayerEvent.FAJR, PrayerEvent.SUNRISE, PrayerEvent.DHUHR,
            PrayerEvent.ASR, PrayerEvent.MAGHRIB, PrayerEvent.ISHA,
            PrayerEvent.ISLAMIC_MIDNIGHT, PrayerEvent.QIYAM,
        ).forEach { event ->
            val instant = day[event]!!
            val local = instant.toLocalDateTime(msk)
            val utcTime = instant.toLocalDateTime(utc)
            println("$event: UTC=${utcTime.hour}:${utcTime.minute.toString().padStart(2,'0')} Local=${local.hour}:${local.minute.toString().padStart(2,'0')}")
        }
    }
}
