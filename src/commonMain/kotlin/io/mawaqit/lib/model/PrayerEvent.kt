package io.mawaqit.lib.model

/**
 * All prayer-related events throughout an Islamic day.
 */
enum class PrayerEvent {
    /** Pre-dawn meal time (Fajr minus configurable offset). */
    SUHUR,

    /** Dawn prayer — sun at fajrAngle° below horizon. */
    FAJR,

    /** Sunrise — sun at ~0.833° below horizon (with refraction). */
    SUNRISE,

    /** Forenoon prayer — sun at 4.5° above horizon after sunrise. */
    DUHA,

    /** Forbidden time near solar zenith (~5 min before Dhuhr). */
    FORBIDDEN_ZENITH,

    /** Noon prayer — shortly after solar transit. */
    DHUHR,

    /** Afternoon prayer (Shafi/Maliki/Hanbali shadow ratio). */
    ASR,

    /** Afternoon prayer (Hanafi shadow ratio). */
    ASR_HANAFI,

    /** End of Asr / start of makruh time — sun at ~5° above horizon before sunset. */
    ASR_END,

    /** Sunset prayer — sun at ~0.833° below horizon. */
    MAGHRIB,

    /** Night prayer — sun at ishaAngle° below horizon. */
    ISHA,

    /** Islamic midnight — midpoint between Maghrib and next Fajr. */
    ISLAMIC_MIDNIGHT,

    /** Last third of the night — time for Qiyam/Tahajjud. */
    QIYAM,

    /** Next day's Fajr — for night duration calculations. */
    NEXT_FAJR,
}
