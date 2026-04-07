package io.mawaqit.lib.internal.prayer

import io.mawaqit.lib.internal.astronomy.*
import io.mawaqit.lib.model.*
import kotlinx.datetime.*
import kotlin.math.*

/**
 * Core prayer time computation engine.
 *
 * Converts astronomical sun position into prayer event times for a given date and location.
 * This is the heart of Mawaqit — it orchestrates all astronomical modules to produce
 * a complete [PrayerDay] with 14 events.
 *
 * Algorithm:
 * 1. Compute Julian Date at noon → [SunPosition] (RA, Dec, EoT)
 * 2. Calculate solar transit (Dhuhr) from longitude + EoT
 * 3. For each angular event, solve hour angle where sun elevation matches target
 * 4. Apply [HighLatitudeRule] for events that have no astronomical solution
 * 5. Compute derived events (Suhur, Duha, Midnight, Qiyam)
 * 6. Apply method + user adjustments
 * 7. Convert UTC hours to [kotlinx.datetime.Instant]
 */
internal object PrayerEngine {

    fun computeDay(
        date: LocalDate,
        coords: Coordinates,
        params: MethodParameters,
        asrJuristic: AsrJuristic,
        highLatRule: HighLatitudeRule,
        suhurOffsetMinutes: Int,
        userAdjustments: Map<PrayerEvent, Int>,
    ): PrayerDay {
        val jdNoon = JulianDate.atNoon(date.year, date.monthNumber, date.dayOfMonth)
        val horizonAngle = Refraction.sunAngleAtHorizon(coords.elevation)

        // Helper: compute sun position and transit for a given JD
        fun sunAt(jd: Double): SunPosition.Result = SunPosition.compute(jd)
        fun transitFor(sun: SunPosition.Result): Double =
            12.0 + (-coords.longitude / 15.0) - (sun.equationOfTime / 60.0)

        // Helper: hour angle for a given sun declination and target elevation
        fun hourAngleFor(angle: Double, dec: Double): Double {
            val latR = coords.latitude.toRadians()
            val decR = dec.toRadians()
            val cosHA = (sin((-angle).toRadians()) - sin(latR) * sin(decR)) /
                    (cos(latR) * cos(decR))
            if (cosHA > 1.0 || cosHA < -1.0) return Double.NaN
            return acos(cosHA.coerceIn(-1.0, 1.0)).toDegrees()
        }

        // Helper: Asr hour angle
        fun asrHourAngle(factor: Double, dec: Double): Double {
            val latR = coords.latitude.toRadians()
            val decR = dec.toRadians()
            val angle = atan(1.0 / (factor + tan(abs(latR - decR)))).toDegrees()
            return hourAngleFor(-angle, dec)
        }

        // ── Pass 1: approximate times using noon sun position ──
        val sunNoon = sunAt(jdNoon)
        val transitApprox = transitFor(sunNoon)

        val fajrApprox = transitApprox - hourAngleFor(params.fajrAngle, sunNoon.declination) / 15.0
        val sunriseApprox = transitApprox - hourAngleFor(-horizonAngle, sunNoon.declination) / 15.0
        val sunsetApprox = transitApprox + hourAngleFor(-horizonAngle, sunNoon.declination) / 15.0
        val asrApprox = transitApprox + asrHourAngle(AsrJuristic.STANDARD.shadowFactor, sunNoon.declination) / 15.0

        // ── Pass 2: refine each event using sun position at approximate time ──
        fun refineEvent(approxUTC: Double, angle: Double, isMorning: Boolean): Double {
            if (approxUTC.isNaN()) return Double.NaN
            val jdEvent = jdNoon - 0.5 + approxUTC / 24.0 // JD at approximate event time
            val sunEvent = sunAt(jdEvent)
            val transitEvent = transitFor(sunEvent)
            val ha = hourAngleFor(angle, sunEvent.declination)
            if (ha.isNaN()) return Double.NaN
            return if (isMorning) transitEvent - ha / 15.0 else transitEvent + ha / 15.0
        }

        fun refineAsr(approxUTC: Double, factor: Double): Double {
            val jdEvent = jdNoon - 0.5 + approxUTC / 24.0
            val sunEvent = sunAt(jdEvent)
            val transitEvent = transitFor(sunEvent)
            return transitEvent + asrHourAngle(factor, sunEvent.declination) / 15.0
        }

        // Refined transit
        val sunTransit = sunAt(jdNoon)
        val transit = transitFor(sunTransit)

        // Refined times
        var fajrUTC = refineEvent(fajrApprox, params.fajrAngle, true)
        val sunriseUTC = refineEvent(sunriseApprox, -horizonAngle, true)
        val dhuhrUTC = transit + 2.0 / 60.0
        val asrUTC = refineAsr(asrApprox, AsrJuristic.STANDARD.shadowFactor)
        val asrHanafiUTC = refineAsr(asrApprox, AsrJuristic.HANAFI.shadowFactor)
        var maghribUTC = refineEvent(sunsetApprox, -horizonAngle, false)

        // Maghrib with custom angle
        if (params.maghribAngle != 0.833) {
            val maghribAngleApprox = transitApprox + hourAngleFor(params.maghribAngle, sunNoon.declination) / 15.0
            maghribUTC = refineEvent(maghribAngleApprox, params.maghribAngle, false)
        }

        // Isha
        var ishaUTC = if (params.ishaInterval != null) {
            maghribUTC + params.ishaInterval / 60.0
        } else {
            val ishaApprox = transitApprox + hourAngleFor(params.ishaAngle, sunNoon.declination) / 15.0
            refineEvent(ishaApprox, params.ishaAngle, false)
        }

        // Duha: sun at 4.5° elevation after sunrise
        val duhaHA = hourAngleFor(-4.5, sunNoon.declination)
        val duhaUTC = if (!duhaHA.isNaN()) transit - duhaHA / 15.0 else sunriseUTC + 15.0 / 60.0

        // Asr end (Karaha): sun at 5° before sunset
        val asrEndHA = hourAngleFor(-5.0, sunNoon.declination)
        val asrEndUTC = if (!asrEndHA.isNaN()) transit + asrEndHA / 15.0 else maghribUTC - 15.0 / 60.0

        // Forbidden zenith: 5 min before dhuhr
        val forbiddenZenithUTC = dhuhrUTC - 5.0 / 60.0

        // Next day Fajr
        val jdNext = JulianDate.atNoon(date.year, date.monthNumber, date.dayOfMonth + 1)
        val sunNext = SunPosition.compute(jdNext)
        val transitNext = 12.0 + (-coords.longitude / 15.0) - (sunNext.equationOfTime / 60.0)
        val fajrHANext = run {
            val latR = coords.latitude.toRadians()
            val decR = sunNext.declination.toRadians()
            val cosHA = (sin((-params.fajrAngle).toRadians()) - sin(latR) * sin(decR)) /
                    (cos(latR) * cos(decR))
            if (cosHA > 1.0 || cosHA < -1.0) Double.NaN
            else acos(cosHA.coerceIn(-1.0, 1.0)).toDegrees()
        }
        val nextFajrUTC = transitNext - fajrHANext / 15.0 + 24.0

        // Night calculations
        val nightDuration = nextFajrUTC - maghribUTC
        val midnightUTC = maghribUTC + nightDuration / 2.0
        val qiyamUTC = maghribUTC + nightDuration * 2.0 / 3.0

        // High latitude adjustments for Fajr/Isha
        val nightForHighLat = sunriseUTC + 24.0 - maghribUTC
        if (fajrUTC.isNaN()) {
            fajrUTC = applyHighLatRule(highLatRule, sunriseUTC, nightForHighLat, params.fajrAngle, true)
        }
        if (ishaUTC.isNaN()) {
            ishaUTC = applyHighLatRule(highLatRule, maghribUTC, nightForHighLat, params.ishaAngle, false)
        }

        // Suhur
        val suhurUTC = fajrUTC - suhurOffsetMinutes / 60.0

        // Collect all times
        val timesUTC = mutableMapOf(
            PrayerEvent.SUHUR to suhurUTC,
            PrayerEvent.FAJR to fajrUTC,
            PrayerEvent.SUNRISE to sunriseUTC,
            PrayerEvent.DUHA to duhaUTC,
            PrayerEvent.FORBIDDEN_ZENITH to forbiddenZenithUTC,
            PrayerEvent.DHUHR to dhuhrUTC,
            PrayerEvent.ASR to asrUTC,
            PrayerEvent.ASR_HANAFI to asrHanafiUTC,
            PrayerEvent.ASR_END to asrEndUTC,
            PrayerEvent.MAGHRIB to maghribUTC,
            PrayerEvent.ISHA to ishaUTC,
            PrayerEvent.ISLAMIC_MIDNIGHT to midnightUTC,
            PrayerEvent.QIYAM to qiyamUTC,
            PrayerEvent.NEXT_FAJR to nextFajrUTC,
        )

        // Apply method adjustments + user adjustments
        for ((event, minutes) in params.adjustments) {
            timesUTC[event] = (timesUTC[event] ?: continue) + minutes / 60.0
        }
        for ((event, minutes) in userAdjustments) {
            timesUTC[event] = (timesUTC[event] ?: continue) + minutes / 60.0
        }

        // Convert UTC hours to Instant
        val startOfDay = date.atStartOfDayIn(TimeZone.UTC)
        val times = timesUTC.mapValues { (_, utcHours) ->
            val totalSeconds = (utcHours * 3600).toLong()
            startOfDay.plus(totalSeconds, DateTimeUnit.SECOND)
        }

        return PrayerDay(
            date = date,
            times = times,
            method = CalculationMethod.OTHER, // method set by caller
            location = coords,
        )
    }

    private fun applyHighLatRule(
        rule: HighLatitudeRule,
        baseTimeUTC: Double,
        nightDuration: Double,
        angle: Double,
        isFajr: Boolean,
    ): Double {
        val portion = when (rule) {
            HighLatitudeRule.NONE -> return Double.NaN
            HighLatitudeRule.MIDDLE_OF_NIGHT -> 0.5
            HighLatitudeRule.SEVENTH_OF_NIGHT -> 1.0 / 7.0
            HighLatitudeRule.TWILIGHT_ANGLE -> angle / 60.0
        }
        val adjustment = portion * nightDuration
        return if (isFajr) baseTimeUTC - adjustment else baseTimeUTC + adjustment
    }
}
