# Data Model: Hymnal History Admin Reports

**Feature**: `003-hymnal-history-reports` | **Phase**: 1 | **Date**: 2026-08-14

Three layers, kept deliberately separate: **wire** types mirror the service exactly, **domain** types
are what the use cases compute over, and **UI state** is what a screen renders. Nothing persists —
these surfaces have no snapshot (research [R-13](./research.md#r-13--no-offline-snapshot-for-these-surfaces)).

---

## 1. Wire types

`features/admin/reports/hymnal/data/dto/HymnalReportDtos.kt` — `@Serializable`, names matching the
service. See [contracts/](./contracts/) for the full payloads.

```kotlin
@Serializable data class OccurrencesResponseDto(
    val from: String, val to: String, val groupBy: String,
    val occurrences: List<OccurrenceDto>,
)

@Serializable data class OccurrenceDto(
    val hymnNumber: String, val hymnTitle: String, val occurredOn: String,
    val serviceWindowId: Int?, val serviceWindowName: String?,
    val bucket: String, val deviceCount: Int,
)

@Serializable data class TopHymnsResponseDto(
    val from: String?, val to: String?, val hymns: List<TopHymnDto>,
)

@Serializable data class TopHymnDto(
    val hymnNumber: String, val hymnTitle: String, val occurrenceCount: Int,
)

@Serializable data class CollectionSettingsDto(
    val minSecondsToCount: Int, val collapseWindowMinutes: Int, val maxBatchSize: Int,
    val maxPastDays: Int, val futureToleranceMinutes: Int, val windowGraceMinutes: Int,
)

@Serializable data class CollectionSettingsPatchDto(
    val minSecondsToCount: Int? = null, val collapseWindowMinutes: Int? = null,
    val maxBatchSize: Int? = null, val maxPastDays: Int? = null,
    val futureToleranceMinutes: Int? = null, val windowGraceMinutes: Int? = null,
)

@Serializable data class ServiceWindowDto(
    val id: Int, val name: String, val weekday: Int,
    val startTime: String, val endTime: String, val active: Boolean,
)

@Serializable data class ServiceWindowListDto(val serviceWindows: List<ServiceWindowDto>)

@Serializable data class ServiceWindowWriteDto(
    val name: String, val weekday: Int,
    val startTime: String, val endTime: String, val active: Boolean = true,
)
```

**`@SerialName` is mandatory on every field** — the service uses `snake_case` and the project's
JSON configuration does not rename automatically. The shared `Json` in `SerializationModule` is
built with **`explicitNulls = false`**, so a null field of `CollectionSettingsPatchDto` is omitted
from the body rather than sent as `null`; that is what makes FR-045 ("send only what changed") true
on the wire rather than only in intent.

`weekday` on the wire is `0 = Monday … 6 = Sunday`.

---

## 2. Domain types

`features/admin/reports/hymnal/domain/model/`.

### 2.1 The loaded data

```kotlin
data class HymnOccurrence(
    val hymnNumber: String,
    val hymnTitle: String,
    val occurredOn: LocalDate,
    val serviceWindowId: Int?,     // null = outside every active service
    val serviceWindowName: String?,
    val bucket: String,
    val deviceCount: Int,
)

data class OccurrenceReport(
    val range: DateRange,
    val granularity: BucketGranularity,   // DAY | WEEK | MONTH | SERVICE
    val occurrences: List<HymnOccurrence>,
)

data class TopHymn(val number: String, val title: String, val occurrenceCount: Int)

data class DateRange(val from: LocalDate, val to: LocalDate) {   // both inclusive
    val days: Int get() = ChronoUnit.DAYS.between(from, to).toInt() + 1
}
```

`serviceWindowId == null` is meaning, not absence: it marks use outside any service and is the
basis of the "Fora do culto" slice and of the in-versus-outside reading.

### 2.2 The administrator's three choices

```kotlin
sealed interface ReportPeriod {
    data object ThisWeek : ReportPeriod
    data object ThisMonth : ReportPeriod
    data object ThisYear : ReportPeriod
    data class Custom(val from: LocalDate, val to: LocalDate) : ReportPeriod
}

sealed interface ReportSlice {
    data object All : ReportSlice
    data class Service(val id: Int, val name: String) : ReportSlice
    data object OutsideService : ReportSlice
    data class Weekday(val day: DayOfWeek) : ReportSlice
}

enum class ReportReading { HIGHLIGHTS, RANKING, EVOLUTION, SERVICES, CALENDAR, IN_VS_OUTSIDE, COVERAGE }
```

`ReportPeriod` resolves to a `DateRange` plus a `BucketGranularity` only through
`ResolveReportPeriodUseCase`, which takes today as a parameter — never `LocalDate.now()`
(research [R-07](./research.md#r-07--date-arithmetic-time-zone-and-testability)).

### 2.3 What the readings produce

```kotlin
data class BarPoint(
    val label: String,             // "50 · Grandioso És Tu"  |  "09/08"  |  "ago"
    val value: Int,                // occurrence count — the primary metric
    val secondaryLabel: String,    // "27 aparelhos" — reach, never a length
    val fraction: Float,           // 0f..1f, floored at MIN_BAR_FRACTION
    val hymnNumber: String? = null // set on rankings so a bar can be tapped
)

data class HymnRanking(val bars: List<BarPoint>, val totalHymns: Int, val shown: Int)

data class EvolutionSeries(val bars: List<BarPoint>, val granularity: BucketGranularity)

data class ServiceBulletin(
    val date: LocalDate,
    val serviceName: String?,      // null = outside any service, rendered as such
    val hymns: List<BulletinHymn>,
    val totalReach: Int,
)
data class BulletinHymn(val number: String, val title: String, val deviceCount: Int)

data class CalendarMonth(val month: YearMonth, val days: List<CalendarDay>)
data class CalendarDay(val date: LocalDate, val occurrenceCount: Int, val intensity: Float)

data class InVsOutside(
    val inService: HymnRanking,
    val outsideService: HymnRanking,
    val onlyInService: List<String>,     // hymn numbers exclusive to each side
    val onlyOutsideService: List<String>,
)

data class Highlight(
    val label: String,             // "Hino mais cantado"
    val value: String,             // "50 · Grandioso És Tu"
    val delta: HighlightDelta?,    // null when the preceding period has no data
)
data class HighlightDelta(val direction: Direction, val text: String)  // UP | DOWN | FLAT

data class HymnalCoverage(
    val catalogSize: Int,
    val everSungCount: Int,
    val proportionText: String,          // "A igreja já cantou 84 dos 640 hinos (13%)"
    val neverSung: List<CatalogHymn>,
    val forgotten: List<ForgottenHymn>,
)
data class CatalogHymn(val number: String, val title: String)
data class ForgottenHymn(
    val number: String, val title: String,
    val lastSung: LocalDate?,            // null = older than the visible window
    val text: String,                    // "Cantado pela última vez em 12/09/2025"
                                         // or "Não é cantado há mais de um ano"
)

data class HymnProfile(
    val number: String,
    val title: String,
    val allTimeCount: Int,                 // labelled "em todo o histórico"
    val firstSeen: LocalDate?,             // labelled "no último ano"
    val lastSeen: LocalDate?,
    val services: List<ServiceShare>,      // where it usually appears
    val typicalReach: Int,
    val recurrence: HymnRecurrence?,
)
data class ServiceShare(val serviceName: String?, val occurrenceCount: Int)
data class HymnRecurrence(
    val serviceName: String,
    val text: String,                      // "Cantado em 6 dos últimos 8 domingos à noite"
    val marks: List<Boolean>,              // one per instance, most recent last
)
```

Every `text`, `proportionText` and `label` above is **produced in the domain** (FR-051). The view
layer renders strings it is given and never assembles one from numbers.

### 2.4 Emptiness

```kotlin
sealed interface ReportEmptyReason {
    data object NoCollectionAtAll : ReportEmptyReason
    data object NoRecordsInPeriod : ReportEmptyReason
    data object NoRecordsInSlice : ReportEmptyReason
    data class ServiceInactiveOrAbsentInPeriod(val serviceName: String) : ReportEmptyReason
    data class ServiceWithoutRecords(val serviceName: String) : ReportEmptyReason
    data object CatalogUnavailable : ReportEmptyReason
}
```

`ServiceWithoutRecords` renders both possibilities and asserts neither: *"Nenhum hino foi aberto
neste culto. Pode ser que o culto não tenha acontecido, ou que ninguém tenha usado o hinário."* The
app collects hymn views only, so it has no evidence a service happened (spec D-5).

### 2.5 Administration

```kotlin
data class ServiceWindow(
    val id: Int,
    val name: String,
    val weekday: DayOfWeek,        // domain uses java.time; the wire integer never escapes data/
    val startTime: LocalTime,
    val endTime: LocalTime,
    val active: Boolean,
)

data class ServiceWindowDraft(          // create and edit share one draft
    val id: Int?,                       // null = create
    val name: String,
    val weekday: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val active: Boolean,
)

data class CollectionSettings(
    val minSecondsToCount: Int,
    val collapseWindowMinutes: Int,
    val maxBatchSize: Int,
    val maxPastDays: Int,
    val futureToleranceMinutes: Int,
    val windowGraceMinutes: Int,
)

enum class SettingField(val wireName: String, val min: Int, val max: Int, val label: String) {
    MIN_SECONDS_TO_COUNT("min_seconds_to_count", 1, 3600, "Tempo mínimo de leitura (s)"),
    COLLAPSE_WINDOW_MINUTES("collapse_window_minutes", 1, 1440, "Janela de agrupamento (min)"),
    MAX_BATCH_SIZE("max_batch_size", 1, 1000, "Tamanho máximo do lote"),
    MAX_PAST_DAYS("max_past_days", 1, 3650, "Idade máxima do evento (dias)"),
    FUTURE_TOLERANCE_MINUTES("future_tolerance_minutes", 1, 1440, "Tolerância de futuro (min)"),
    WINDOW_GRACE_MINUTES("window_grace_minutes", 1, 1440, "Tolerância após o culto (min)"),
}
```

`SettingField.wireName` is what maps a `field_errors` key back to the field on screen
(research [R-05](./research.md#r-05--how-do-field_errors-reach-the-right-form-field)). It is the
single source for both the local range check and the server-error routing, so the two cannot drift.

### 2.6 Validation rules

| Rule | Where | Message |
|---|---|---|
| Custom range: `from <= to` | `ValidateReportPeriodUseCase` | "A data inicial não pode ser depois da final." |
| Custom range: ≤ 366 days inclusive | same | "O período não pode passar de 366 dias." |
| Setting within `[min, max]` | `ValidateCollectionSettingsUseCase` | "Informe um número entre {min} e {max}." — on the field |
| Setting is a whole number | same | "Informe um número inteiro." — on the field |
| Window name non-empty, ≤ 100 chars | `ValidateServiceWindowUseCase` | on the name field |
| `endTime > startTime`, strictly | same | on the time fields |
| Weekday in 0..6 | same | structurally impossible from a `DayOfWeek` picker; asserted anyway |

---

## 3. Mapping

`features/admin/reports/hymnal/data/mapper/`:

- `OccurrenceMapper` — `OccurrenceDto → HymnOccurrence`; `occurredOn` parsed as `LocalDate`.
- `TopHymnMapper` — trivial.
- `CollectionSettingsMapper` — `Dto ↔ domain`, plus `CollectionSettings.diff(other): CollectionSettingsPatchDto` which is what makes the PATCH partial.
- `ServiceWindowMapper` — `weekday: Int ↔ DayOfWeek` via `DayOfWeek.of(weekday + 1)` / `dayOfWeek.value - 1`, and `LocalTime` parsing tolerant of both `"19:00"` and `"19:00:00"` (the service returns seconds, its examples accept both on write).

`WeekdayLabels` (presentation) holds the Portuguese labels. The conversion and the labels are
covered by a round-trip test over all seven days — the feature's most inviting off-by-one
(research [R-11](./research.md#r-11--translating-the-weekday)).

---

## 4. Core additions

```kotlin
// core/domain/model/HymnCatalogEntry.kt
data class HymnCatalogEntry(val number: String, val title: String)

// core/domain/repository/HymnCatalogRepository.kt
interface HymnCatalogRepository {
    fun observeHymnCatalog(): Flow<SnapshotState<List<HymnCatalogEntry>>>
}

// core/domain/util/DateProvider.kt
fun interface DateProvider { fun today(): LocalDate }

// core/domain/error/AppError.kt  — Server gains one field
class Server(
    val code: Int,
    message: String? = "Erro no servidor ($code)",
    cause: Throwable? = null,
    val errorCode: String? = null,
    val fieldErrors: Map<String, List<String>>? = null,   // NEW
    userMessage: String? = null,
) : AppError(message, cause, userMessage)
```

`HymnalRepository` extends `HymnCatalogRepository`; `HymnalRepositoryImpl` maps its loaded
`List<Hymn>` to `List<HymnCatalogEntry>`; `HymnalModule` binds the impl to the core interface. This
mirrors `SongsRepository : AllSongsRepository` exactly
(research [R-04](./research.md#r-04--how-does-the-report-read-the-hymnal-catalogue-without-importing-the-hymnal-feature)).

---

## 5. UI state

One contract file per surface, in `presentation/state/`, all following the project's
flag-carrying `data class` convention.

```kotlin
data class HymnalReportUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val period: ReportPeriod = ReportPeriod.ThisMonth,
    val slice: ReportSlice = ReportSlice.All,
    val reading: ReportReading = ReportReading.HIGHLIGHTS,
    val availableServices: List<ReportSlice.Service> = emptyList(),  // active windows only
    val highlights: List<Highlight> = emptyList(),
    val ranking: HymnRanking? = null,
    val evolution: EvolutionSeries? = null,
    val bulletins: List<ServiceBulletin> = emptyList(),
    val calendar: List<CalendarMonth> = emptyList(),
    val inVsOutside: InVsOutside? = null,
    val coverage: HymnalCoverage? = null,
    val coverageLoading: Boolean = false,
    val emptyReason: ReportEmptyReason? = null,
    val rangeError: String? = null,          // custom-range validation, on the picker
)

data class CollectionSettingsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val values: Map<SettingField, String> = emptyMap(),      // raw text, so a typo survives
    val fieldErrors: Map<SettingField, String> = emptyMap(),
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
)

data class ServiceWindowsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val windows: List<ServiceWindow> = emptyList(),
    val editing: ServiceWindowDraft? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val pendingDelete: ServiceWindow? = null,
    val isSaving: Boolean = false,
)
```

One-shot outcomes (saved, deleted, failed) go through `SharedFlow<HymnalReportEvent>` per the
project's convention for events, not through the state.

The reports hub has **no** UI state: it is a static list, like `AdminScreen`
(research [R-02](./research.md#r-02--how-does-the-hub-accept-new-areas-without-generic-infrastructure)).
