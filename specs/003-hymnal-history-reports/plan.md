# Implementation Plan: Hymnal History Admin Reports

**Branch**: `003-hymnal-history-reports` | **Date**: 2026-08-14 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/003-hymnal-history-reports/spec.md`

## Summary

Give the church leadership four screens it has never had: a **reports hub** reached from the Admin
Panel's currently inert "Relatórios" card, a **hymnal history report** with seven readings over one
loaded period, a **collection settings** form, and a **service windows** manager.

The whole feature turns on one idea: the occurrences endpoint returns a whole period unpaginated, so
the app fetches once per period and **pivots locally**. Period, slice and reading are three
independent selections; only the period touches the network. Everything the screen shows — every
bar length, every proportion, every sentence like "não é cantado há mais de um ano" — is computed by
pure use cases in `domain/` and arrives at the composable ready to render.

Three decisions are worth reading before writing code, because each is cheap now and expensive later:

- **The catalogue crosses features through core, not through an import.** Never-sung hymns need the
  hymnal catalogue, which lives in `features/hymnal`. The project already solved this shape once:
  `SongsRepository : AllSongsRepository`, bound in worshiphub, injected by admin. This feature adds
  `HymnCatalogRepository` in core the same way. See [research R-04](./research.md#r-04--how-does-the-report-read-the-hymnal-catalogue-without-importing-the-hymnal-feature).
- **`field_errors` reach the form through `AppError.Server`, not through a feature-local parser.**
  `ApiErrorParser` already parses them; `ResponseExt.toAppError()` drops them. One nullable field on
  one existing class closes the gap without violating the constitution's single-parsing-point rule.
  See [research R-05](./research.md#r-05--how-do-field_errors-reach-the-right-form-field).
- **Which endpoint feeds which reading is a hard rule, written down.** Sliced and period-bounded
  readings come from `occurrences`; all-time statements come from `top-hymns`. A screen that quietly
  asks both for the same quantity is how two disagreeing numbers reach the leadership. See
  [research R-03](./research.md#r-03--which-source-feeds-which-reading).

## Technical Context

**Language/Version**: Kotlin 2.3.10, JVM target 17

**Primary Dependencies**: Jetpack Compose (BOM 2026.02.00), Hilt 2.59.1, Navigation Compose 2.9.7,
Retrofit 3.0.0 + `kotlinx.serialization` 1.10.0, `java.time` via core library desugaring. **No new
dependency** — in particular no charting library (FR-023).

**Storage**: none. These surfaces fetch on demand; nothing is snapshotted or cached to disk
([research R-13](./research.md#r-13--no-offline-snapshot-for-these-surfaces)).

**Testing**: JUnit4 + MockK + `kotlinx-coroutines-test` + Turbine, unit tests only
(`app/src/test/`), fakes preferred over mocks.

**Target Platform**: Android, `minSdk` 24 / `targetSdk` 36 / `compileSdk` 36. `java.time` is safe at
API 24 because `isCoreLibraryDesugaringEnabled = true` and `desugar_jdk_libs` is on the classpath.

**Project Type**: Single-module Android app (`:app`), feature-based MVVM + Clean.

**Performance Goals**: switching slice or reading recomputes in memory, with zero network calls and
no perceptible wait (SC-003). A year of occurrences renders every reading without the report
becoming unresponsive (SC-004) — aggregation runs on the default dispatcher, not the main thread.

**Constraints**: admin-only; `@AuthedRetrofit` for every call including the settings read; only a
period change may fetch; all aggregation in pure domain use cases; charts hand-drawn with Compose
primitives; user-visible text in hardcoded Portuguese.

**Scale/Scope**: ~60 new files, ~7 modified, 4 new screens. Worst-case payload is one year of
occurrences — on the order of a few thousand rows, comfortably in memory.

## Constitution Check

*GATE: must pass before Phase 0, re-checked after Phase 1.*

`.specify/memory/constitution.md` is still the unmodified Spec Kit template — every principle is a
`[PRINCIPLE_N_NAME]` placeholder — so there are no ratified Spec Kit gates. The project's own
constitution is `specs/constitution.md`, which currently covers error handling only. Both it and
`CLAUDE.md` are checked explicitly.

### `specs/constitution.md` — error handling

| Rule | Status | Note |
|---|---|---|
| `AppError` is the only hierarchy crossing data → domain → presentation | PASS | Repositories use `runCatching { … }.mapError()` + `Response.toAppError()`, as `AdminScheduleRepositoryImpl` does |
| No raw `Throwable` reaches the ViewModel | PASS | |
| `message` technical, `userMessage` authored | PASS | `field_errors` are routed to fields, not into `userMessage` |
| Presentation decides displayed text via `AppError.toUserMessage()` | PASS | |
| `ResponseExt.kt` is the only place that reads `errorBody()` | PASS | The feature never parses a body; `ResponseExt` is *extended* to carry `fieldErrors` onward |
| Portuguese strings hardcoded in Kotlin | PASS | |

### `CLAUDE.md`

| Rule | Status | Note |
|---|---|---|
| `UI → ViewModel → UseCase → Repository (interface) → Repository (impl)` | PASS | Full chain on all four screens |
| Features never import each other | PASS | Catalogue crosses via core `HymnCatalogRepository` (R-04) |
| `domain/` has no Android knowledge | PASS | `DateProvider` returns `LocalDate`; no `Context`, no Compose |
| `presentation/` never touches repositories | PASS | |
| Three states per screen | PASS | `isLoading` / `error` flags on every UI state, plus a richer `ReportEmptyReason` |
| `@Preview` required on shared `core/presentation/components/` | N/A | No new core component; previews on the two chart composables are optional and planned anyway |
| Screen split from content composable | PASS | Every screen is a VM collector + a pure content composable |
| ViewModel exposes only `StateFlow`/`SharedFlow`, uses `viewModelScope` | PASS | |
| No `Context` in ViewModel | PASS | |
| Graph-scoped `hiltViewModel(graphEntry)` (Pitfall #1) | PASS | `HymnalReportViewModel` is graph-scoped so the hymn card reuses the loaded period; the settings and windows ViewModels are single-screen and use plain `hiltViewModel()`, exactly as `AdminScheduleViewModel` and `MusicRegistrationViewModel` already do in this graph |
| Multiple screens → `NavGraphBuilder.xGraph()` (Pitfall #8) | PASS | New `reportsGraph()` nested in `adminGraph`, as worshiphub nests its sub-graphs |
| `safePopBackStack()`, never raw `popBackStack()` | PASS | |
| Correct DI qualifier (`@AuthedRetrofit`) | PASS | Wrong qualifier here means a silent 401 |
| No magic strings; 120-char lines; official Kotlin style | PASS | Endpoint constants, `SettingField` table, route constants |
| No unnecessary abstractions; check compatibility before adding libs | PASS | No library added; new abstractions are three core declarations, each removing a rule violation |
| Tests: happy path + ≥1 error per use case, fakes over mocks | PASS | Test plan in [quickstart.md](./quickstart.md) |
| Spec and code in the same commit (§7.2) | PASS | `specs/hymnal/spec.md` and `specs/admin/spec.md` updated with the code (FR-054, FR-055) |

**Post-Phase 1 re-check**: unchanged. Phase 1 added no deviation. The three core additions
(`HymnCatalogRepository`, `DateProvider`, `AppError.Server.fieldErrors`) all exist to *keep* rules
that would otherwise have to be bent — see Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/003-hymnal-history-reports/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0 — 20 resolved decisions
├── data-model.md        # Phase 1 — wire / domain / UI-state types
├── quickstart.md        # Phase 1 — validation and test plan
├── contracts/
│   ├── reporting-api.md
│   └── settings-and-windows-api.md
├── checklists/
│   └── requirements.md
└── tasks.md             # Phase 2 — created by /speckit-tasks, NOT here
```

### Source code

```text
app/src/main/java/com/ipb/castelobranco/
├── core/
│   ├── di/AppInfoModule.kt                            MOD  bind DateProvider
│   ├── domain/
│   │   ├── error/AppError.kt                          MOD  Server + fieldErrors
│   │   ├── model/HymnCatalogEntry.kt                  NEW  number + title only
│   │   ├── repository/HymnCatalogRepository.kt        NEW  cross-feature catalogue read
│   │   └── util/DateProvider.kt                       NEW  fun interface, beside MonotonicClock
│   └── network/error/ResponseExt.kt                   MOD  carry fieldErrors onward
│
├── features/hymnal/                                   (3 small edits, no behaviour change)
│   ├── data/repository/HymnalRepositoryImpl.kt        MOD  observeHymnCatalog()
│   ├── di/HymnalModule.kt                             MOD  bind HymnCatalogRepository
│   └── domain/repository/HymnalRepository.kt          MOD  : HymnCatalogRepository
│
├── features/admin/panel/presentation/
│   ├── navigation/AdminNavGraph.kt                    MOD  nest reportsGraph, AdminNav.reports
│   └── screens/AdminScreen.kt                         MOD  "Relatórios" enabled + onClick
│
└── features/admin/reports/
    ├── presentation/navigation/ReportsNavGraph.kt     NEW  reportsGraph, ReportsRoutes, ReportsNav
    ├── hub/presentation/
    │   ├── model/ReportArea.kt                        NEW  title, description, icon, accent, onClick
    │   └── screens/ReportsHubScreen.kt                NEW  static list — no ViewModel, like AdminScreen
    └── hymnal/
        ├── data/
        │   ├── api/HymnalReportApi.kt                 NEW  occurrences, top-hymns
        │   ├── api/HymnalHistoryAdminApi.kt           NEW  settings, service windows
        │   ├── api/HymnalReportEndpoints.kt           NEW  paths on ApiConstants.BASE_PATH
        │   ├── dto/HymnalReportDtos.kt                NEW  @SerialName on every field
        │   ├── mapper/OccurrenceMapper.kt             NEW
        │   ├── mapper/CollectionSettingsMapper.kt     NEW  + diff() → partial patch
        │   ├── mapper/ServiceWindowMapper.kt          NEW  weekday 0=Mon ↔ DayOfWeek
        │   ├── repository/HymnalReportRepositoryImpl.kt        NEW
        │   └── repository/HymnalHistoryAdminRepositoryImpl.kt  NEW
        ├── di/HymnalReportModule.kt                   NEW  @AuthedRetrofit APIs + repo bindings
        ├── domain/
        │   ├── model/ReportSelection.kt               NEW  ReportPeriod, ReportSlice, ReportReading
        │   ├── model/OccurrenceReport.kt              NEW  HymnOccurrence, DateRange, granularity
        │   ├── model/ReportReadings.kt                NEW  BarPoint, rankings, bulletins, calendar…
        │   ├── model/HymnalAdminModels.kt             NEW  ServiceWindow, CollectionSettings, SettingField
        │   ├── model/ReportEmptyReason.kt             NEW  sealed emptiness
        │   ├── repository/HymnalReportRepository.kt          NEW
        │   ├── repository/HymnalHistoryAdminRepository.kt    NEW
        │   └── usecase/                               NEW  see "Use cases" below
        └── presentation/
            ├── components/                            NEW  HorizontalBarChart, VerticalBarChart,
            │                                               CalendarGrid, BulletinCard, PeriodSelector,
            │                                               SliceSelector, ReadingSelector, EmptyReading,
            │                                               SettingNumberField, ServiceWindowRow,
            │                                               ServiceWindowFormDialog
            ├── screens/HymnalReportScreen.kt          NEW  the seven readings
            ├── screens/HymnCardScreen.kt              NEW  ficha do hino
            ├── screens/CollectionSettingsScreen.kt    NEW
            ├── screens/ServiceWindowsScreen.kt        NEW
            ├── state/HymnalReportContract.kt          NEW
            ├── state/CollectionSettingsContract.kt    NEW
            ├── state/ServiceWindowsContract.kt        NEW
            ├── util/WeekdayLabels.kt                  NEW  Portuguese labels
            └── viewmodel/HymnalReportViewModel.kt     NEW  graph-scoped
                          CollectionSettingsViewModel.kt   NEW
                          ServiceWindowsViewModel.kt       NEW

app/src/test/java/com/ipb/castelobranco/
├── core/network/error/ResponseExtFieldErrorsTest.kt   NEW
└── features/admin/reports/hymnal/
    ├── data/mapper/ServiceWindowMapperTest.kt         NEW  weekday round-trip, all seven days
    ├── data/mapper/CollectionSettingsDiffTest.kt      NEW  partial patch contains only changes
    ├── data/repository/HymnalReportRepositoryImplTest.kt        NEW
    ├── data/repository/HymnalHistoryAdminRepositoryImplTest.kt  NEW
    ├── domain/usecase/…                               NEW  one per use case
    └── presentation/viewmodel/HymnalReportViewModelTest.kt      NEW  slice/reading never refetch
```

**Structure Decision**: single-module, feature-based layout, unchanged. The feature is a new
sub-area of the administration feature — `features/admin/reports/` beside `panel/`, `register/` and
`schedule/` — with the hub in `hub/` and the hymnal surfaces in `hymnal/`, so a second report area
is a sibling directory and a list entry, not a refactor. The only things outside it are three core
declarations and three one-line edits in `features/hymnal` that bind the existing repository to the
new core interface.

### Use cases

All in `features/admin/reports/hymnal/domain/usecase/`, all pure except the four that call a
repository. The count is high because FR-050 and FR-051 put every aggregation *and* every derived
sentence in the domain, and FR-056 requires each to be testable on its own.

| Use case | Kind | Produces |
|---|---|---|
| `ResolveReportPeriodUseCase` | pure | `DateRange` + `BucketGranularity` + the preceding range; validates a custom range (R-08, R-17) |
| `FilterOccurrencesUseCase` | pure | the slice — the single implementation every reading below uses |
| `BuildHighlightsUseCase` | pure | four `Highlight`s with deltas when the preceding period has data |
| `BuildHymnRankingUseCase` | pure | `HymnRanking` — bar fractions, top-25 cap, total |
| `BuildEvolutionSeriesUseCase` | pure | `EvolutionSeries` in the order the service returned |
| `BuildServiceBulletinsUseCase` | pure | `ServiceBulletin`s, reverse chronological |
| `BuildCalendarUseCase` | pure | one `CalendarMonth` per month of the period |
| `BuildInVsOutsideUseCase` | pure | both rankings plus the exclusive-hymn sets |
| `BuildHymnalCoverageUseCase` | pure | proportion, never-sung, forgotten (R-18) |
| `BuildHymnProfileUseCase` | pure | the hymn card, including the recurrence sentence and marks |
| `ResolveEmptyReasonUseCase` | pure | which `ReportEmptyReason` applies, if any |
| `ValidateCollectionSettingsUseCase` | pure | per-field range errors |
| `ValidateServiceWindowUseCase` | pure | per-field name/time errors |
| `GetOccurrenceReportUseCase` | suspend | period + preceding period, concurrently (R-12) |
| `GetAllTimeTopHymnsUseCase` | suspend | the all-time ranking |
| `GetCollectionSettingsUseCase` / `UpdateCollectionSettingsUseCase` | suspend | settings read / partial patch |
| `GetServiceWindowsUseCase` / `SaveServiceWindowUseCase` / `DeleteServiceWindowUseCase` | suspend | windows CRUD |

## Implementation Order

Sequenced so every step is independently testable and the two riskiest pieces — the layering
crossings — land first, while they are still cheap to change.

**Step 1 — Core additions.** `HymnCatalogEntry`, `HymnCatalogRepository`, `DateProvider` + its
`AppInfoModule` binding, `AppError.Server.fieldErrors`, `ResponseExt` filling it. Then the three
`features/hymnal` edits that bind the existing repository to the new interface. Test: an error body
carrying `field_errors` produces an `AppError.Server` that carries them; a body without them still
produces the same error as before.

**Step 2 — Domain types.** All five model files plus the two repository interfaces. Pure
declarations; no tests of their own.

**Step 3 — Network and mapping.** Both API interfaces, the DTOs, the three mappers, both repository
implementations, the Hilt module with `@AuthedRetrofit`. Tests: the weekday round-trip over all
seven days; `diff()` producing a patch with only the changed keys; an HTTP failure mapping to
`AppError`; a `403` arriving as `AppError.Auth`.

**Step 4 — Period, slice and the ranking.** `ResolveReportPeriodUseCase`,
`FilterOccurrencesUseCase`, `BuildHymnRankingUseCase`. This is the spine: it is what FR-056 names
first, and every later reading reuses the filter. Tests: ranking with no slice, with a service
slice, with a weekday slice, with the outside-service slice; a custom range over 366 days refused
before any call.

**Step 5 — The remaining period-bounded readings.** Highlights (with and without a preceding
period), evolution, bulletins, calendar, in-versus-outside, and `ResolveEmptyReasonUseCase`.

**Step 6 — The report screen.** `HymnalReportViewModel` (graph-scoped), the contract, the selectors,
the two chart composables, the empty-state composable, `HymnalReportScreen`. Test: changing slice or
reading issues no repository call; changing period issues exactly one pair of calls.

**Step 7 — Coverage and the hymn card.** `BuildHymnalCoverageUseCase` over the catalogue, the
all-time ranking and the 366-day window; `BuildHymnProfileUseCase`; `HymnCardScreen`. Tests: a hymn
whose most recent occurrence falls outside the window yields no date and the
"mais de um ano" text; the never-sung set is the catalogue minus the ranking; a missing catalogue
yields `CatalogUnavailable` without failing the rest of the report.

**Step 8 — Service windows.** Validation use case, CRUD use cases, `ServiceWindowsViewModel`, the
screen, the form and the delete confirmation with its history-preserving copy. Tests: end before
start refused locally; a name over 100 characters refused; `field_errors` from the service landing on
the right field.

**Step 9 — Collection settings.** Validation use case, ViewModel, screen with the per-field range
hints and the three effect explanations. Tests: each of the six ranges; a server `field_errors`
response routed by wire name; an unrecognised key falling back to the generic message.

**Step 10 — Hub, navigation and specs.** `ReportArea`, `ReportsHubScreen`, `reportsGraph` nested in
`adminGraph`, `AdminScreen`'s "Relatórios" card enabled, and the entry from the report's top bar
(`extraActions`) to the two administration screens. Then update `specs/hymnal/spec.md` §4 and
`specs/admin/spec.md` §1/§2.1/§5 in the same commit (FR-054, FR-055).

The hub is last on purpose: until there is something to open, an enabled card is a card that leads
to an empty screen — exactly what `specs/admin/spec.md` §5 forbids.

## Risks

| Risk | Impact | Handling |
|---|---|---|
| **Weekday off-by-one** (`0 = Monday`, not Sunday) | Every bulletin, slice and service label is wrong by one day, and it looks plausible | One conversion point in `ServiceWindowMapper`, round-trip test over all seven values (R-11) |
| **A screen quietly reads both sources for one number** | Two numbers on screen disagree; leadership loses trust in the report | The source table in R-03 is normative; coverage is visually separated and labelled "todo o histórico"; the period/slice selectors are hidden on it |
| **Slice change triggers a refetch** through a careless `LaunchedEffect(key)` | Breaks FR-007, and it degrades silently — just slower | ViewModel test asserting zero repository calls across slice and reading changes |
| **Wrong Retrofit qualifier** | Silent 401 on every admin call | `@AuthedRetrofit` in one module; the settings read here is deliberately *not* the public one used by collection |
| **The patch sends unchanged fields** | A partial patch becomes a full overwrite | All-nullable patch DTO plus the shared `Json`'s `explicitNulls = false`, with a serialization test asserting the exact key set |
| **A year of occurrences aggregated on the main thread** | Jank on period change; SC-004 fails | Aggregation runs on `@DefaultDispatcher`; the ViewModel derives with `combine(...).flowOn(default)` |
| **Settings saved here do not apply on this device until restart** | Administrator changes a threshold and sees no local effect | Accepted and documented (R-16); the screen says so |
| **`field_errors` key the app does not know** | Error vanishes, form looks fine | Unknown keys fall back to the generic error text; test covers it |
| **Hymn present in the report but absent from the catalogue** | A dropped occurrence, invisibly | Coverage crosses catalogue → ranking only for absence; no reading filters occurrences by catalogue membership |
| **The hub grows a second area later and needs a rewrite** | The whole point of FR-002 lost | Hub is a `List<ReportArea>` + one item composable, with no hymnal special-casing anywhere in it |

## Complexity Tracking

| Deviation | Why needed | Simpler alternative rejected because |
|---|---|---|
| New `HymnCatalogRepository` + `HymnCatalogEntry` in core | The never-sung reading needs the hymnal catalogue, and `features/admin` may not import `features/hymnal` | Importing directly breaks the feature-isolation rule; refetching `api/hymnal/` duplicates a catalogue already preloaded in memory; the project already uses this exact shape for `AllSongsRepository` |
| New field on `AppError.Server` | `field_errors` are already parsed but stop at `ResponseExt`; FR-046 requires them on the form field | A feature-local parser violates the constitution's single-parsing-point rule; a new `AppError` subclass forces every existing `when` over `AppError` to grow a branch for what is an attribute, not a category |
| New `DateProvider` in core | Every period resolution and every "há mais de um ano" statement must be testable without freezing the system clock | `LocalDate.now()` inside the use cases makes the whole of FR-056 untestable; a feature-local clock would be a second abstraction for something `MonotonicClock` already establishes as a core concern |
| A graph nested three levels deep (`adminGraph → reportsGraph → report destinations`) | The hymn card must reuse the period the report already loaded, which needs a shared `ViewModelStoreOwner` | Flat destinations in `adminGraph` mean the hymn card refetches, breaking FR-007; worshiphub already nests sub-graphs (Pitfall #8) |
| ~18 use cases for one feature | FR-050 and FR-051 put every aggregation and every derived sentence in `domain/`, and FR-056 requires each testable alone | Fewer, larger use cases would bundle unrelated readings behind one entry point, making the mandated per-reading tests reach through irrelevant setup |
