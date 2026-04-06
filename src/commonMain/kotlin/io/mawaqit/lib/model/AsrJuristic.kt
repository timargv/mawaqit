package io.mawaqit.lib.model

/**
 * Juristic method for Asr prayer time calculation.
 * Determines when Asr begins based on shadow length ratio.
 */
enum class AsrJuristic(val shadowFactor: Double) {
    /** Shafi'i, Maliki, Hanbali — shadow equals object length + noon shadow. */
    STANDARD(1.0),

    /** Hanafi — shadow equals twice object length + noon shadow. */
    HANAFI(2.0),
}
