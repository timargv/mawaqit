package io.mawaqit.lib

import io.mawaqit.lib.internal.prayer.HijriConverter
import io.mawaqit.lib.internal.prayer.PrayerEngine
import io.mawaqit.lib.internal.prayer.QiblaCalculator
import io.mawaqit.lib.model.*
import kotlinx.datetime.LocalDate

/**
 * # Mawaqit — Islamic Prayer Times Library
 *
 * Computes prayer times using high-precision astronomical algorithms (Meeus).
 * Supports 15+ calculation methods including regional Russian methods (DUM RT/RF/CR).
 *
 * ## Quick Start
 * ```kotlin
 * val day = Mawaqit.calculate(
 *     date = LocalDate(2026, 4, 6),
 *     coordinates = Coordinates(21.4225, 39.8262), // Mecca
 * )
 * println(day.fajr())     // Fajr as Instant
 * println(day.maghrib())  // Maghrib as Instant
 * println(day[PrayerEvent.QIYAM]) // Qiyam as Instant?
 * ```
 */
object Mawaqit : PrayerCalculator {

    /**
     * Calculate all prayer times for a given date and location.
     *
     * @param date the Gregorian date to compute for
     * @param coordinates observer's geographic position
     * @param method calculation method (default: MWL)
     * @param asrJuristic Asr shadow ratio school (default: Standard/Shafi)
     * @param highLatitudeRule rule for Fajr/Isha at high latitudes (default: middle of night)
     * @param suhurOffsetMinutes minutes before Fajr for Suhur (default: 10)
     * @param adjustments per-event minute offsets applied on top of method defaults
     * @return [PrayerDay] containing all 14 prayer event times as UTC [kotlinx.datetime.Instant]
     */
    override fun calculate(
        date: LocalDate,
        coordinates: Coordinates,
        method: CalculationMethod,
        asrJuristic: AsrJuristic,
        highLatitudeRule: HighLatitudeRule,
        suhurOffsetMinutes: Int,
        adjustments: Map<PrayerEvent, Int>,
    ): PrayerDay {
        val day = PrayerEngine.computeDay(
            date = date,
            coords = coordinates,
            params = method.parameters,
            asrJuristic = asrJuristic,
            highLatRule = highLatitudeRule,
            suhurOffsetMinutes = suhurOffsetMinutes,
            userAdjustments = adjustments,
        )
        return day.copy(method = method)
    }

    /**
     * Calculate prayer times with custom [MethodParameters].
     * Use this when [CalculationMethod.OTHER] is selected or for testing custom angles.
     */
    fun calculate(
        date: LocalDate,
        coordinates: Coordinates,
        parameters: MethodParameters,
        asrJuristic: AsrJuristic = AsrJuristic.STANDARD,
        highLatitudeRule: HighLatitudeRule = HighLatitudeRule.MIDDLE_OF_NIGHT,
        suhurOffsetMinutes: Int = 10,
        adjustments: Map<PrayerEvent, Int> = emptyMap(),
    ): PrayerDay {
        return PrayerEngine.computeDay(
            date = date,
            coords = coordinates,
            params = parameters,
            asrJuristic = asrJuristic,
            highLatRule = highLatitudeRule,
            suhurOffsetMinutes = suhurOffsetMinutes,
            userAdjustments = adjustments,
        )
    }

    /**
     * True bearing from the observer to the Kaaba (Qibla direction) in degrees [0, 360).
     */
    override fun qiblaDirection(from: Coordinates): Double =
        QiblaCalculator.direction(from)

    /**
     * Great-circle distance from the observer to the Kaaba in kilometers.
     */
    override fun distanceToKaaba(from: Coordinates): Double =
        QiblaCalculator.distance(from)

    /**
     * Convert a Gregorian date to the Islamic (Hijri) calendar.
     */
    override fun toHijri(year: Int, month: Int, day: Int): HijriDate =
        HijriConverter.toHijri(year, month, day)

    /**
     * Convert a [LocalDate] to the Islamic (Hijri) calendar.
     */
    override fun toHijri(date: LocalDate): HijriDate =
        HijriConverter.toHijri(date.year, date.monthNumber, date.dayOfMonth)
}
