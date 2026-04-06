package io.mawaqit.lib.model

/**
 * Angle and timing parameters for a prayer calculation method.
 * Every [CalculationMethod] resolves to a [MethodParameters] instance.
 * The prayer engine reads these parameters — it never branches on method identity.
 */
data class MethodParameters(
    /** Sun depression angle for Fajr (degrees below horizon). */
    val fajrAngle: Double,

    /** Sun depression angle for Isha (degrees below horizon). Ignored if [ishaInterval] is set. */
    val ishaAngle: Double,

    /** Fixed interval after Maghrib for Isha (minutes). Overrides [ishaAngle] when non-null. */
    val ishaInterval: Int? = null,

    /** Sun depression angle for Maghrib (standard 0.833°). Some methods use higher values. */
    val maghribAngle: Double = 0.833,

    /** Per-event minute adjustments applied by the method itself. */
    val adjustments: Map<PrayerEvent, Int> = emptyMap(),
)
