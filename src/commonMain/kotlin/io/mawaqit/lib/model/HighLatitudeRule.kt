package io.mawaqit.lib.model

/**
 * Rules for adjusting Fajr and Isha at high latitudes (above ~48°N/S)
 * where the sun may not reach the required depression angle during summer.
 *
 * These rules only activate when the standard astronomical calculation
 * cannot determine a valid time (the hour angle equation has no solution).
 *
 * ## Usage
 * ```kotlin
 * val day = Mawaqit.calculate(
 *     date = LocalDate(2026, 6, 21),
 *     coordinates = Coordinates(59.93, 30.34), // Saint Petersburg
 *     highLatitudeRule = HighLatitudeRule.SEVENTH_OF_NIGHT,
 * )
 * ```
 */
enum class HighLatitudeRule {
    /** No adjustment — may produce invalid times at extreme latitudes. */
    NONE,

    /** Fajr/Isha = sunrise/sunset ± half of the night duration. */
    MIDDLE_OF_NIGHT,

    /** Fajr/Isha = sunrise/sunset ± 1/7 of the night duration. */
    SEVENTH_OF_NIGHT,

    /** Interpolate based on the twilight angle relative to the horizon angle. */
    TWILIGHT_ANGLE,
}
