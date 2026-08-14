# Research: Hymnal History Admin Reports

**Feature**: `003-hymnal-history-reports` | **Phase**: 0 | **Date**: 2026-08-14

Every open question the plan needed answered before design. Each entry records the decision, why it
was taken, and what was rejected. Nothing here is left as NEEDS CLARIFICATION.

---

## R-01 — Where does the feature live, and how is it navigated?

**Decision**: `features/admin/reports/`, a new sub-area of the administration feature alongside
`panel/`, `register/` and `schedule/`. Inside it, `hub/` holds the reports hub and `hymnal/` holds
the hymnal-history surfaces.

Navigation adds a nested `reportsGraph()` inside `adminGraph`, with its own `ReportsRoutes` object,
exactly as `worshipHubGraph` nests `chordChartsGraph` and `lyricsGraph` (Pitfall #8). The nesting
earns its place: the occurrences report and the hymn card must share one loaded period, and a graph
is what gives them a shared `ViewModelStoreOwner`.

**Rationale**: `CLAUDE.md` places top-level routes in `AppRoutes` and feature-internal routes in a
local `XRoutes`; the administration area already owns `AdminRoutes`. Adding a second top-level graph
for reports would put an administrative sub-area on the same footing as `adminGraph` itself.

**Rejected**: putting the report under `features/hymnal/` — forbidden by the spec and by the
feature-isolation rule; a flat set of destinations inside `adminGraph` — then report and hymn card
cannot share loaded data without refetching, which FR-007 forbids.

---

## R-02 — How does the hub accept new areas without generic infrastructure?

**Decision**: the hub is a screen with **no ViewModel and no data layer**, built exactly like
`AdminScreen`: a `List<ReportArea>` declared in the screen, one item composable, one destination per
area. `ReportArea` carries a title, a one-line description, an icon, an accent colour and an
`onClick`.

**Rationale**: `AdminScreen` already proves the pattern — nine actions, one list, one card
composable, and adding the tenth is one list entry. Nothing about the hymnal is special-cased in the
hub, its state, or its item composable, which satisfies FR-002; and no registry, no plugin
interface and no shared "report" abstraction is built, which satisfies FR-003.

**Rejected**: a `ReportRegistry` with per-area providers — infrastructure for a set of size one, and
explicitly out of scope.

---

## R-03 — Which source feeds which reading?

**Decision**:

| Reading | Source | Range |
|---|---|---|
| Highlights | occurrences | chosen period (+ preceding period of equal length) |
| Ranking | occurrences | chosen period |
| Evolution | occurrences | chosen period |
| Services (bulletins) | occurrences | chosen period |
| Calendar | occurrences | chosen period |
| In-service vs outside | occurrences | chosen period |
| Coverage — never sung | top-hymns (all time) + local catalogue | all history |
| Coverage — forgotten | top-hymns (all time) + occurrences of the last 366 days | see R-18 |
| Hymn card — all-time total | top-hymns (all time) | all history |
| Hymn card — dates, services, reach | occurrences | last 366 days |

**Rationale**: `top-hymns` cannot filter by service window or weekday, so every sliced reading must
come from `occurrences`. `occurrences` is capped at 366 days, so every all-time statement must come
from `top-hymns`. The mapping is written down because a screen that quietly asks both for the same
quantity is exactly how two numbers that disagree reach the leadership.

**Rejected**: feeding the unsliced ranking from `top-hymns` "because it is already sorted" — the
same bar chart would then change its numbers when the slice changed from "all" to a service, and the
two would not be comparable.

---

## R-04 — How does the report read the hymnal catalogue without importing the hymnal feature?

**Decision**: mirror `AllSongsRepository`. Declare in core:

- `core/domain/model/HymnCatalogEntry.kt` — `number: String`, `title: String`.
- `core/domain/repository/HymnCatalogRepository.kt` — `fun observeHymnCatalog(): Flow<SnapshotState<List<HymnCatalogEntry>>>`.

`features/hymnal`'s `HymnalRepository` extends `HymnCatalogRepository`, `HymnalRepositoryImpl`
implements the extra method by mapping its already-loaded `List<Hymn>`, and `HymnalModule` binds the
impl to the core interface. The administration feature injects only the core interface.

**Rationale**: the project already does exactly this — `SongsRepository : AllSongsRepository`,
bound in `features/worshiphub/tables/di/SongsTable.kt`, injected by
`features/admin/register/domain/usecase/ObserveSongsUseCase.kt`. Reusing that shape adds one
interface and one model to core and keeps the feature-isolation rule intact, with no duplicated
catalogue, no snapshot moved, and no second fetch: the hymnal snapshot is already preloaded at
startup.

**Rejected**: moving the whole hymnal repository to core — far outside this feature and it drags
the reader UI's concerns with it. Fetching `api/hymnal/` again from the administration feature — a
second copy of the catalogue and a second cache, for data already in memory. Exposing `Hymn`
itself from core — that leaks lyrics and the nullable server id into a contract that needs neither.

---

## R-05 — How do `field_errors` reach the right form field?

**Decision**: carry them on the existing error type. `core/network/error/ApiErrorParser.kt` already
parses `field_errors` into `ApiErrorBody.fieldErrors`, but `ResponseExt.toAppError()` drops them.
Add `fieldErrors: Map<String, List<String>>? = null` to `AppError.Server` and fill it in
`ResponseExt.toAppError()`.

**Rationale**: the constitution says `ResponseExt.kt` is the only place that reads `errorBody()`, so
the feature must not parse the body itself. The parser already produces the data; the only thing
missing is the last hop into the error type. One nullable field on one existing subclass, no new
error hierarchy, and every other caller is unaffected because the parameter has a default.

**Rejected**: a feature-local error parser — a direct constitution violation. A separate
`ValidationError` subclass of `AppError` — a new branch every existing `when` over `AppError` would
have to handle, for information that is an attribute of a server error, not a new category.

---

## R-06 — Where does aggregation happen, and how is refetching avoided?

**Decision**: the ViewModel holds one loaded `OccurrenceReport` for the current period plus the
preceding period. Slice and reading are `StateFlow`s; the rendered state is derived from
`combine(report, slice, reading)` through **pure domain use cases**. Only a period change calls the
repository.

**Rationale**: FR-007 and FR-050. Keeping the raw report in the ViewModel and deriving everything
downstream makes every slice/reading change a recomposition over data already in memory, and makes
each aggregation a function with no Android and no coroutine dependency — the shape FR-056's tests
need.

**Rejected**: aggregating in the repository — then each slice would be a repository call and the
temptation to refetch is one refactor away. Aggregating in the composable — forbidden by the layer
rules and untestable without instrumentation.

---

## R-07 — Date arithmetic, time zone and testability

**Decision**: `java.time`, which the project already uses widely (`SundayPlaysMapper`,
`AdminScheduleContract`, `HymnViewHistoryMapper`) and which is safe at `minSdk 24` because
`isCoreLibraryDesugaringEnabled = true` and `desugar_jdk_libs` is on the classpath.

"Today" comes from a new `core/domain/util/DateProvider.kt` — a `fun interface` returning
`LocalDate`, sitting beside the existing `MonotonicClock` and bound in `AppInfoModule`. Every
period resolution and every "how long ago" statement takes it as a parameter.

The zone is `America/Sao_Paulo`, the church's zone and the one the collection service uses for its
inclusive day boundaries. It is a single constant, not a device-locale lookup, so a leader
travelling does not silently shift the report by a day.

**Rejected**: `LocalDate.now()` inside the use cases — makes every period and every "não é cantado
há mais de um ano" assertion untestable without freezing the system clock.

---

## R-08 — Which `group_by` does each period ask for?

**Decision**: the period decides, and the app never lets the user pick it directly.

| Period | `group_by` | Evolution bars |
|---|---|---|
| Esta semana | `day` | one per day |
| Este mês | `day` | one per day |
| Este ano | `month` | one per month |
| Personalizado ≤ 31 days | `day` | one per day |
| Personalizado ≤ 120 days | `week` | one per ISO week |
| Personalizado > 120 days | `month` | one per month |

**Rationale**: `group_by` changes only the bucket label, never the number of occurrences, so it is
purely a granularity choice for the evolution chart. Exposing it as a fourth selector would give the
administrator a knob whose only effect is the width of a bar, and would invite the reading that
different groupings mean different totals.

Note that the services reading does **not** depend on `group_by`: it groups by
`occurred_on + service_window_id`, which the response always carries.

---

## R-09 — Drawing the charts by hand

**Decision**: two composables in the feature's `presentation/components/`:

- `HorizontalBarChart` — a `Column` of rows; each bar is a `Box` with `fillMaxWidth(fraction)`. No
  `Canvas` needed, and the hymn number/title labels stay real text, selectable and accessible.
- `VerticalBarChart` — a `Canvas` for the bars plus a `Row` of labels beneath, because vertical bars
  need shared baseline arithmetic that layout weights express badly.

Both take `List<BarPoint>` — `label`, `value`, `secondaryLabel`, `fraction` — already computed by
the domain. Colours come from the project theme (`BrandColors`, `MaterialTheme.colorScheme`).

**Rationale**: FR-023 and FR-024. Bars proportional to a value are the whole requirement; a charting
library would be a new dependency on a toolchain already pinned to very recent AGP/Kotlin/Compose
versions, for arithmetic that is one division. Keeping the fraction in the domain means swapping the
drawing later touches only the two composables.

A minimum bar fraction (2%) is applied in the domain so a value of 1 next to a value of 400 is still
visible — the edge case the spec names.

**Rejected**: Vico, YCharts and every other charting library — excluded by the spec.

---

## R-10 — Occurrence count versus device reach

**Decision**: `occurrence_count` is the primary metric everywhere: it orders rankings and sets
`BarPoint.value`. Device reach is `deviceCount` summed over the same occurrences and appears only as
`BarPoint.secondaryLabel` ("27 aparelhos"). No sort, no bar, no proportion uses reach.

**Rationale**: FR-014/FR-015, and the requester's own reading of the two metrics. Encoding it in the
`BarPoint` shape — one `value`, one `secondaryLabel` — makes the rule hard to break by accident.

---

## R-11 — Translating the weekday

**Decision**: the wire uses Python's convention, `0 = Monday … 6 = Sunday`. `java.time.DayOfWeek`
uses `1 = Monday … 7 = Sunday`. The mapping is therefore `DayOfWeek.of(weekday + 1)` outward and
`dayOfWeek.value - 1` inward, isolated in one mapper with the Portuguese labels beside it
(`"Segunda-feira" … "Domingo"`), and covered by a round-trip test over all seven values.

**Rationale**: this is the single most inviting off-by-one in the feature — treating `0` as Sunday
rotates the whole week and mislabels every service. Keeping the conversion in one file with an
exhaustive test is cheaper than finding it in a bulletin.

---

## R-12 — The comparison against the preceding period

**Decision**: on a period change the repository is asked for two ranges — the chosen one and the
immediately preceding one of identical length — issued concurrently. If the preceding range fails or
is empty, the highlights render without deltas; the report itself is not failed by it.

**Rationale**: FR-020 needs the previous period's numbers and the app has no other way to get them.
Doing it as part of the period change keeps FR-007 intact: no slice or reading change ever refetches.
Degrading gracefully matters because the preceding range is the more likely of the two to be empty
on a church that just installed collection.

---

## R-13 — No offline snapshot for these surfaces

**Decision**: these screens fetch on demand and show the standard loading/error states. They are not
registered as `Preloadable` or `Refreshable`, and nothing is written to `filesDir/snapshots/`.

**Rationale**: the snapshot pattern exists for member-facing content that must work on a bus. These
are occasional administrative readings by a handful of people; caching a year of occurrences per
period per slice would add a cache-invalidation problem for no user benefit, and stale report numbers
are worse than an error state.

---

## R-14 — Keeping a wide ranking usable

**Decision**: the ranking renders the top 25 by default with a "Ver todos (N)" action that expands
to the full list. The cap is applied in the domain, which also reports the total, so the view never
decides what to hide.

**Rationale**: a year of a healthy church can put hundreds of hymns in the ranking; hundreds of bars
is neither readable nor cheap to compose. Twenty-five is roughly one screen of scrolling and covers
the question the ranking is asked.

---

## R-15 — Empty states as domain output

**Decision**: a sealed `ReportEmptyReason` produced by the domain — `NoCollectionAtAll`,
`NoRecordsInPeriod`, `NoRecordsInSlice`, `ServiceInactiveOrAbsentInPeriod(serviceName)`,
`ServiceWithoutRecords(serviceName)`, `CatalogUnavailable` — with the Portuguese sentence chosen in
the domain and handed to the view ready to render.

`ServiceWithoutRecords` states both possibilities and asserts neither, per the spec's D-5: the app
collects hymn views only and has no evidence a service happened.

**Rationale**: FR-035/FR-036/FR-037 and FR-051. A sealed type makes the compiler check that a new
reading handles every emptiness, which a nullable string would not.

`NoCollectionAtAll` is distinguished from `NoRecordsInPeriod` by the all-time ranking being empty —
which the coverage reading already fetches — not by guessing from an empty period.

---

## R-16 — The settings screen does not update the collection cache on this device

**Decision**: saving settings updates the server. The device's own cached collection settings
(`HymnViewSettingsStore`, `@SettingsPrefs`) are **not** written by this screen; they refresh at the
next app start through the existing `Refreshable` binding.

**Rationale**: that store lives in `features/hymnal` and the administration feature may not import
it. Routing it through core would mean promoting a collection-internal cache into a shared contract
for a benefit measured in hours on exactly one device — the administrator's. The screen therefore
says so plainly, and the limitation is recorded here rather than papered over.

**Rejected**: a core `CollectionSettingsCache` interface — a new shared abstraction whose only
consumer is a convenience.

---

## R-17 — Validating the custom range locally

**Decision**: the app refuses, before any request: start after end, and a span over 366 days
(inclusive count). The message names the rule broken. The service enforces the same rules; the local
check exists so the administrator gets the answer immediately and no doomed request is issued.

---

## R-18 — What the forgotten list can honestly say

**Decision**: the coverage reading loads two things — the all-time ranking, and the occurrences of
the last 366 days. Then:

- catalogue ∖ all-time ranking → **never sung** (no date, none exists).
- in the all-time ranking **and** in the 366-day window → **forgotten**, with the exact date of its
  most recent occurrence.
- in the all-time ranking but **not** in the 366-day window → **forgotten**, labelled
  "não é cantado há mais de um ano", with no date.

Ordering: the undated group first (oldest known to be older than a year), then dated entries by date
ascending.

This is two sources feeding two different facts, not one number — the never-sung set comes from the
ranking, the dates come from the window, and neither is summed into the other. FR-018 holds.

**Rationale**: `top-hymns` returns no dates and `occurrences` cannot reach past 366 days. Any date
older than that is genuinely unknown to the app, and the interface says exactly that instead of
inventing precision.

---

## R-19 — What the hymn card shows, and from where

**Decision**: the card is a destination inside the reports graph. It reads:

- all-time total → the all-time ranking (labelled "em todo o histórico");
- first and last recorded date, the services it appears in, typical reach, and the recurrence
  sentence → the occurrences of the last 366 days (labelled "no último ano").

The recurrence sentence — "cantado em 6 dos últimos 8 domingos à noite" — counts the instances of
the hymn's most frequent service window within the window, and is produced in the domain together
with the band of marks (one mark per instance, filled or empty).

A hymn opened from the never-sung list renders a card that states it has never been recorded, with
no dates and no counts.

---

## R-20 — Errors and permissions

**Decision**: every repository maps through `runCatching { … }.mapError()` and
`Response.toAppError()`, as `AdminScheduleRepositoryImpl` does. The screens render
`AppError.toUserMessage()`. A `403` therefore reaches the screen as `AppError.Auth` and shows the
project's standard authorisation text — not an empty report.

The Admin Panel entry remains the app-side filter (`isLoggedIn && isAdmin`); the service is the
authority on every request.
