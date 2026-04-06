package io.mawaqit.lib.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Complete prayer times for a single day.
 *
 * All times are UTC [Instant] values — timezone-safe and unambiguous.
 * Convert to local display time via [toLocalTime] or `kotlinx.datetime` extensions.
 *
 * ## Usage
 * ```kotlin
 * val day = Mawaqit.calculate(date, coordinates)
 *
 * // Access obligatory prayers directly
 * val fajr: Instant = day.fajr()
 * val dhuhr: Instant = day.dhuhr()
 *
 * // Access any event by enum
 * val qiyam: Instant? = day[PrayerEvent.QIYAM]
 *
 * // Convert to local time for display
 * val fajrLocal = day.toLocalTime(PrayerEvent.FAJR, TimeZone.of("Asia/Riyadh"))
 *
 * // Iterate all events
 * day.times.forEach { (event, time) -> println("$event: $time") }
 * ```
 *
 * @property date the Gregorian date these times were computed for
 * @property times map of all 14 [PrayerEvent] to their UTC [Instant]
 * @property method the [CalculationMethod] used
 * @property location the [Coordinates] used for computation
 */
data class PrayerDay(
    val date: LocalDate,
    val times: Map<PrayerEvent, Instant>,
    val method: CalculationMethod,
    val location: Coordinates,
) {
    /**
     * Get the time for a specific event, or `null` if not computed.
     *
     * ```kotlin
     * val qiyam = day[PrayerEvent.QIYAM] // Instant?
     * ```
     */
    operator fun get(event: PrayerEvent): Instant? = times[event]

    /**
     * Convert an event time to a local date-time in the given timezone.
     * Returns `null` if the event is not present.
     *
     * ```kotlin
     * val fajrLocal = day.toLocalTime(PrayerEvent.FAJR, TimeZone.of("Europe/Moscow"))
     * // → LocalDateTime(2026-04-06T07:32)
     * ```
     */
    fun toLocalTime(event: PrayerEvent, timeZone: TimeZone) =
        times[event]?.toLocalDateTime(timeZone)

    /** Fajr (dawn) prayer time. @throws NoSuchElementException if not computed. */
    fun fajr(): Instant = times.getValue(PrayerEvent.FAJR)

    /** Sunrise time. @throws NoSuchElementException if not computed. */
    fun sunrise(): Instant = times.getValue(PrayerEvent.SUNRISE)

    /** Dhuhr (noon) prayer time. @throws NoSuchElementException if not computed. */
    fun dhuhr(): Instant = times.getValue(PrayerEvent.DHUHR)

    /** Asr (afternoon) prayer time. @throws NoSuchElementException if not computed. */
    fun asr(): Instant = times.getValue(PrayerEvent.ASR)

    /** Maghrib (sunset) prayer time. @throws NoSuchElementException if not computed. */
    fun maghrib(): Instant = times.getValue(PrayerEvent.MAGHRIB)

    /** Isha (night) prayer time. @throws NoSuchElementException if not computed. */
    fun isha(): Instant = times.getValue(PrayerEvent.ISHA)
}
