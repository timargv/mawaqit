package io.mawaqit.lib.model

/**
 * Prayer time calculation methods used worldwide.
 *
 * Each method defines specific sun depression angles for Fajr and Isha,
 * and optionally per-prayer minute adjustments. Access the underlying
 * angles via the [parameters] property.
 *
 * ## Usage
 * ```kotlin
 * // Use a preset method
 * val day = Mawaqit.calculate(date, coords, method = CalculationMethod.DUM_RF)
 *
 * // Inspect method parameters
 * val params = CalculationMethod.EGYPT.parameters
 * println(params.fajrAngle) // 19.5
 *
 * // Custom method — use OTHER and pass MethodParameters directly
 * val day = Mawaqit.calculate(date, coords,
 *     parameters = MethodParameters(fajrAngle = 19.0, ishaAngle = 16.0))
 * ```
 *
 * @property displayName human-readable method name
 */
enum class CalculationMethod(val displayName: String) {

    /** Muslim World League — widely used default. */
    MWL("Muslim World League"),

    /** Islamic Society of North America. */
    ISNA("ISNA"),

    /** Egyptian General Authority of Survey. */
    EGYPT("Egyptian General Authority"),

    /** Umm al-Qura University, Makkah — used in Saudi Arabia. Isha = 90 min after Maghrib. */
    UMM_AL_QURA("Umm al-Qura"),

    /** University of Islamic Sciences, Karachi. */
    KARACHI("Karachi"),

    /** Institute of Geophysics, University of Tehran. */
    TEHRAN("Tehran"),

    /** Shia Ithna-Ashari (Jafari). */
    JAFARI("Jafari"),

    /** Духовное управление мусульман Республики Татарстан. */
    DUM_RT("ДУМ РТ (Татарстан)"),

    /** Духовное управление мусульман Российской Федерации. */
    DUM_RF("ДУМ РФ (Россия)"),

    /** Духовное управление мусульман Центральной России. */
    DUM_CR("ДУМ ЦР (Центр. Россия)"),

    /** Presidency of Religious Affairs, Turkey. */
    DIYANET("Diyanet (Turkey)"),

    /** Jabatan Kemajuan Islam Malaysia. */
    JAKIM("JAKIM (Malaysia)"),

    /** Kementerian Agama, Indonesia. */
    KEMENAG("Kemenag (Indonesia)"),

    /** Муфтият Республики Дагестан. */
    DAGESTAN("Дагестан"),

    /**
     * Иджтихад — метод с запасом безопасности для поста.
     * Углы MWL + ~0.33° предосторожность на горизонтные события.
     * Магриб наступает на ~2-3 мин позже стандарта, Фаджр на ~2-3 мин раньше.
     */
    IJTIHAD("Иджтихад"),

    /** Custom parameters — use [Mawaqit.calculate] with explicit [MethodParameters]. */
    OTHER("Custom");

    /** Default parameters for this method. */
    val parameters: MethodParameters get() = when (this) {
        MWL -> MethodParameters(fajrAngle = 18.0, ishaAngle = 17.0)
        ISNA -> MethodParameters(fajrAngle = 15.0, ishaAngle = 15.0)
        EGYPT -> MethodParameters(fajrAngle = 19.5, ishaAngle = 17.5)
        UMM_AL_QURA -> MethodParameters(fajrAngle = 18.5, ishaAngle = 0.0, ishaInterval = 90)
        KARACHI -> MethodParameters(fajrAngle = 18.0, ishaAngle = 18.0)
        TEHRAN -> MethodParameters(fajrAngle = 17.7, ishaAngle = 14.0, maghribAngle = 4.5)
        JAFARI -> MethodParameters(fajrAngle = 16.0, ishaAngle = 14.0, maghribAngle = 4.0)
        DUM_RT -> MethodParameters(
            fajrAngle = 18.0, ishaAngle = 17.0,
            adjustments = mapOf(PrayerEvent.FAJR to -3, PrayerEvent.ISHA to 3),
        )
        DUM_RF -> MethodParameters(
            fajrAngle = 18.0, ishaAngle = 17.0,
            adjustments = mapOf(PrayerEvent.FAJR to -5, PrayerEvent.ISHA to 5),
        )
        DUM_CR -> MethodParameters(
            fajrAngle = 18.0, ishaAngle = 17.0,
            adjustments = mapOf(PrayerEvent.FAJR to -4, PrayerEvent.ISHA to 4),
        )
        DIYANET -> MethodParameters(
            fajrAngle = 18.0, ishaAngle = 17.0,
            adjustments = mapOf(
                PrayerEvent.SUNRISE to -7,
                PrayerEvent.DHUHR to 5,
                PrayerEvent.ASR to 4,
                PrayerEvent.MAGHRIB to 7,
            ),
        )
        JAKIM -> MethodParameters(fajrAngle = 20.0, ishaAngle = 18.0)
        KEMENAG -> MethodParameters(
            fajrAngle = 20.0, ishaAngle = 18.0,
            adjustments = mapOf(PrayerEvent.DHUHR to 2),
        )
        DAGESTAN -> MethodParameters(
            fajrAngle = 18.0, ishaAngle = 17.0,
            adjustments = mapOf(PrayerEvent.FAJR to -3, PrayerEvent.ISHA to 2),
        )
        IJTIHAD -> MethodParameters(
            fajrAngle = 18.33,
            ishaAngle = 17.10,
            maghribAngle = 1.20,
        )
        OTHER -> MethodParameters(fajrAngle = 18.0, ishaAngle = 17.0)
    }
}
