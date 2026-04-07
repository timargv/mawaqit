# مواقيت Mawaqit

**High-precision Islamic prayer time calculation library for Kotlin Multiplatform.**

Mawaqit computes all prayer times using astronomical algorithms from Jean Meeus' *Astronomical Algorithms*, with IAU 1980 nutation model and atmospheric refraction correction.

```
┌─────────────────────────────────────────────────────┐
│                    Mawaqit API                      │
│  Mawaqit.calculate(date, coordinates, method)       │
│  Mawaqit.qiblaDirection(coordinates)                │
│  Mawaqit.toHijri(date)                              │
└─────────────────┬───────────────────────────────────┘
                  │
     ┌────────────┴────────────┐
     │     Prayer Engine       │
     │  14 events × 15 methods │
     │  High latitude rules    │
     │  Per-prayer adjustments │
     └────────────┬────────────┘
                  │
     ┌────────────┴────────────┐
     │   Astronomy Engine      │
     │  Julian Date            │
     │  Nutation (IAU 1980)    │
     │  Obliquity (IAU 2006)   │
     │  Sun Position (Meeus)   │
     │  ΔT estimation          │
     │  Atmospheric refraction │
     └─────────────────────────┘
```

## Features

- **14 prayer events** — Suhur, Fajr, Sunrise, Duha, Forbidden Zenith, Dhuhr, Asr, Asr Hanafi, Asr End (Karaha), Maghrib, Isha, Islamic Midnight, Qiyam, Next Fajr
- **15+ calculation methods** — MWL, ISNA, Egypt, Umm al-Qura, Karachi, Tehran, Jafari, Diyanet, JAKIM, Kemenag, and Russian methods (DUM RT, DUM RF, DUM CR, Dagestan)
- **High latitude support** — Middle of Night, Seventh of Night, Twilight Angle rules
- **Qibla direction** — True bearing and distance to the Kaaba
- **Hijri calendar** — Gregorian to Islamic date conversion
- **Kotlin Multiplatform** — Android, iOS, JVM, JS
- **Zero dependencies** — Only `kotlinx-datetime`
- **Instant-based output** — Timezone-safe, no ambiguity
- **High precision** — RA-based Equation of Time (±2 sec), iterative sun position refinement
- **Testable** — `PrayerCalculator` interface for dependency injection and mocking

## Installation

```kotlin
// settings.gradle.kts
include(":mawaqit")

// build.gradle.kts (your module)
dependencies {
    implementation(project(":mawaqit"))
}
```

## Quick Start

```kotlin
import io.mawaqit.lib.Mawaqit
import io.mawaqit.lib.model.*
import kotlinx.datetime.*

// Calculate prayer times for Mecca
val day = Mawaqit.calculate(
    date = LocalDate(2026, 4, 6),
    coordinates = Coordinates(21.4225, 39.8262),
)

// Access times as Instant (UTC)
val fajr: Instant = day.fajr()
val maghrib: Instant = day.maghrib()

// Convert to local time
val fajrLocal = day.toLocalTime(PrayerEvent.FAJR, TimeZone.of("Asia/Riyadh"))

// Access all events
day.times.forEach { (event, instant) ->
    println("$event: $instant")
}
```

## Calculation Methods

```kotlin
// Use a specific method
val day = Mawaqit.calculate(
    date = today,
    coordinates = Coordinates(55.7558, 37.6173), // Moscow
    method = CalculationMethod.DUM_RF,
)

// Use Hanafi Asr calculation
val day = Mawaqit.calculate(
    date = today,
    coordinates = coords,
    method = CalculationMethod.KARACHI,
    asrJuristic = AsrJuristic.HANAFI,
)

// Custom adjustments (minutes)
val day = Mawaqit.calculate(
    date = today,
    coordinates = coords,
    adjustments = mapOf(
        PrayerEvent.FAJR to -2,   // 2 minutes earlier
        PrayerEvent.ISHA to 3,    // 3 minutes later
    ),
)
```

### Available Methods

| Method | Fajr | Isha | Region |
|--------|------|------|--------|
| MWL | 18° | 17° | Worldwide (default) |
| ISNA | 15° | 15° | North America |
| EGYPT | 19.5° | 17.5° | Egypt |
| UMM_AL_QURA | 18.5° | 90 min | Saudi Arabia |
| KARACHI | 18° | 18° | Pakistan |
| TEHRAN | 17.7° | 14° | Iran |
| JAFARI | 16° | 14° | Shia |
| DUM_RT | 18° | 17° (+3) | Tatarstan, Russia |
| DUM_RF | 18° | 17° (+5) | Russia (Federal) |
| DUM_CR | 18° | 17° (+4) | Central Russia |
| DIYANET | 18° | 17° | Turkey |
| JAKIM | 20° | 18° | Malaysia |
| KEMENAG | 20° | 18° | Indonesia |
| DAGESTAN | 18° | 17° (+2) | Dagestan, Russia |
| **IJTIHAD** | **18.33°** | **17.10°** | **Safety-first (Maghrib 1.20°)** |

### Ijtihad Method

The Ijtihad method uses slightly deeper angles than MWL, providing a built-in safety margin (~2-3 minutes) for fasting-critical times (Fajr/Maghrib). This ensures the sun has fully set before breaking fast and Fajr begins with certainty.

```kotlin
val day = Mawaqit.calculate(
    date = today,
    coordinates = coords,
    method = CalculationMethod.IJTIHAD,
)
```

### Custom Parameters

Combine any base method with custom angles or adjustments:

```kotlin
// MWL angles + safety adjustments for fasting
val params = CalculationMethod.MWL.parameters.copy(
    adjustments = mapOf(
        PrayerEvent.FAJR to -2,     // 2 min earlier
        PrayerEvent.MAGHRIB to 2,   // 2 min later
        PrayerEvent.DHUHR to 2,     // 2 min later (past zenith)
    ),
)
val day = Mawaqit.calculate(date, coords, parameters = params)

// Ijtihad angles with custom Maghrib
val params = CalculationMethod.IJTIHAD.parameters.copy(
    maghribAngle = 1.50, // even deeper angle
)
val day = Mawaqit.calculate(date, coords, parameters = params)
```

## High Latitudes

At extreme latitudes (above ~48°N/S), the sun may not reach the required depression angle for Fajr or Isha. Use `HighLatitudeRule` to handle this:

```kotlin
val day = Mawaqit.calculate(
    date = today,
    coordinates = Coordinates(59.9343, 30.3351), // Saint Petersburg
    method = CalculationMethod.DUM_RF,
    highLatitudeRule = HighLatitudeRule.SEVENTH_OF_NIGHT,
)
```

| Rule | Description |
|------|-------------|
| `MIDDLE_OF_NIGHT` | Fajr/Isha at ± half the night duration from sunrise/sunset |
| `SEVENTH_OF_NIGHT` | Fajr/Isha at ± 1/7 of the night duration |
| `TWILIGHT_ANGLE` | Proportional interpolation based on angle |
| `NONE` | No adjustment (may return invalid times) |

## Qibla & Hijri

```kotlin
// Qibla direction (degrees from North)
val bearing = Mawaqit.qiblaDirection(Coordinates(55.7558, 37.6173))
// → ~168.3° for Moscow

// Distance to Kaaba (km)
val distance = Mawaqit.distanceToKaaba(Coordinates(55.7558, 37.6173))
// → ~3,612 km

// Hijri date
val hijri = Mawaqit.toHijri(LocalDate(2026, 4, 6))
// → HijriDate(day=18, month=10, year=1447) = 18 Shawwal 1447 AH
```

## Architecture

```
io.mawaqit.lib/
├── Mawaqit.kt                    ← Public API (single entry point)
├── PrayerCalculator.kt           ← Interface for testability/DI
├── model/
│   ├── Coordinates.kt            ← Geographic position
│   ├── PrayerDay.kt              ← Result: Map<PrayerEvent, Instant>
│   ├── PrayerEvent.kt            ← 14 prayer events enum
│   ├── CalculationMethod.kt      ← 15+ methods with parameters
│   ├── MethodParameters.kt       ← Angles and adjustments
│   ├── AsrJuristic.kt            ← Standard / Hanafi
│   ├── HighLatitudeRule.kt       ← High latitude strategies
│   └── HijriDate.kt              ← Islamic calendar date
└── internal/
    ├── astronomy/                 ← Meeus algorithms
    │   ├── JulianDate.kt
    │   ├── DeltaT.kt
    │   ├── Nutation.kt            ← IAU 1980 (63 terms)
    │   ├── Obliquity.kt           ← Capitaine 2003
    │   ├── SunPosition.kt         ← RA, Dec, EoT (RA-based, ±2 sec precision)
    │   ├── SolarCoordinates.kt    ← Az/El for observer
    │   └── Refraction.kt          ← Bennett's formula
    └── prayer/
        ├── PrayerEngine.kt        ← Core computation (2-pass iterative refinement)
        ├── QiblaCalculator.kt     ← Kaaba bearing/distance
        └── HijriConverter.kt      ← Gregorian → Hijri
```

## Platforms

| Platform | Status |
|----------|--------|
| Android | ✅ |
| iOS (arm64, simulator) | ✅ |
| JVM | ✅ |
| JS | Planned |

## License

Mawaqit is **dual-licensed**:

- **AGPL-3.0** — free for open-source and personal use. If you use Mawaqit in a commercial application, you must open-source your entire codebase. See [LICENSE](LICENSE).
- **Commercial License** — use in closed-source commercial applications without opening your code. See [LICENSE-COMMERCIAL.md](LICENSE-COMMERCIAL.md) for details.

| Use Case | License | Cost |
|----------|---------|------|
| Personal projects | AGPL-3.0 | Free |
| Education / academic | AGPL-3.0 | Free |
| Open-source projects | AGPL-3.0 | Free |
| Closed-source commercial | Commercial | Paid |

## Accuracy

Mawaqit uses a two-pass iterative computation: sun position is calculated at the approximate event time, then refined. Combined with RA-based Equation of Time, this achieves high precision:

| Source | Typical Error |
|--------|--------------|
| Equation of Time (RA-based) | ±2 seconds |
| Iterative refinement | ±5 seconds |
| Nutation (IAU 1980, 63 terms) | ±0.01 seconds |
| **Total algorithmic** | **±10 seconds** |
| Atmospheric refraction (fixed model) | ±30-60 seconds |

The dominant error source is atmospheric refraction, which varies with temperature and pressure. For fasting-critical times, use the **IJTIHAD** method or apply safety adjustments through `MethodParameters.adjustments`.

## References

- Jean Meeus, *Astronomical Algorithms*, 2nd Edition (1998)
- IAU 1980 Nutation Theory
- Capitaine et al. (2003) — Obliquity of the ecliptic
- Espenak & Meeus — ΔT polynomial expressions
- PrayTimes.org — Prayer angle reference data
