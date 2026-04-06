package io.mawaqit.lib.model

/**
 * A date in the Islamic (Hijri) calendar.
 *
 * ## Usage
 * ```kotlin
 * val hijri = Mawaqit.toHijri(2026, 4, 6)
 * println(hijri.formatted)    // "18 Shawwal 1447 AH"
 * println(hijri.monthNameAr)  // "شوال"
 * println(hijri.month == 9)   // false (Ramadan check)
 * ```
 *
 * @property day day of the Hijri month (1-30)
 * @property month Hijri month number (1 = Muharram, 9 = Ramadan, 12 = Dhul-Hijjah)
 * @property year Hijri year
 */
data class HijriDate(
    val day: Int,
    val month: Int,
    val year: Int,
) {
    /** Arabic month name. */
    val monthNameAr: String get() = MONTH_NAMES_AR.getOrElse(month - 1) { "" }

    /** English month name. */
    val monthNameEn: String get() = MONTH_NAMES_EN.getOrElse(month - 1) { "" }

    /** Formatted as "day MonthName year AH". */
    val formatted: String get() = "$day $monthNameEn $year AH"

    companion object {
        private val MONTH_NAMES_AR = listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة",
        )
        private val MONTH_NAMES_EN = listOf(
            "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani",
            "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhul-Qi'dah", "Dhul-Hijjah",
        )
    }
}
