package io.mawaqit.lib

import io.mawaqit.lib.model.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Interface for prayer time calculation.
 *
 * Use [Mawaqit] as the default implementation.
 * Implement this interface to create test doubles or custom calculation engines.
 *
 * ```kotlin
 * // Production
 * val calculator: PrayerCalculator = Mawaqit
 *
 * // Test
 * val calculator: PrayerCalculator = FakePrayerCalculator()
 * ```
 */
interface PrayerCalculator {

    fun calculate(
        date: LocalDate,
        coordinates: Coordinates,
        method: CalculationMethod = CalculationMethod.MWL,
        asrJuristic: AsrJuristic = AsrJuristic.STANDARD,
        highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
        suhurOffsetMinutes: Int = 10,
        adjustments: Map<PrayerEvent, Int> = emptyMap(),
    ): PrayerDay

    fun qiblaDirection(from: Coordinates): Double

    fun distanceToKaaba(from: Coordinates): Double

    fun toHijri(year: Int, month: Int, day: Int): HijriDate

    fun toHijri(date: LocalDate): HijriDate
}
