---

description: "Task list for Hymnal History Admin Reports"
---

# Tasks: Hymnal History Admin Reports

**Input**: Design documents from `specs/003-hymnal-history-reports/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md),
[data-model.md](./data-model.md), [contracts/](./contracts/), [quickstart.md](./quickstart.md)

**Tests**: Included. FR-056 and FR-057 make them part of the specification, not an option.

**Organization**: grouped by user story so each is independently implementable and testable.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: can run in parallel (different files, no dependency on an incomplete task)
- **[Story]**: the user story the task serves (US1–US8)
- Every task names its exact file path

## Path Conventions

- Main: `app/src/main/java/com/ipb/castelobranco/`
- Tests: `app/src/test/java/com/ipb/castelobranco/`
- Feature root: `app/src/main/java/com/ipb/castelobranco/features/admin/reports/`
- Hymnal surfaces root: `<feature root>/hymnal/`

**Phase order note**: US1 and US2 are both P1. US1 is sequenced first because it builds the
navigation and the screen shell that US2 fills with readings. Within the P2 tier the order is US3,
US4, US5, US7; the P3 tier is US6 then US8.

**Never run** `clean`, `--rerun-tasks` or `--no-daemon` before the test task in any phase — it
destroys the KSP cache (`CLAUDE.md`). Use `./gradlew :app:testDebugUnitTest`.

---

## Phase 1: Setup

**Purpose**: package skeleton and the constants everything else references. No dependency is added
to `gradle/libs.versions.toml` in this feature — FR-023 forbids it.

- [X] T001 Create the package directories for the feature under `app/src/main/java/com/ipb/castelobranco/features/admin/reports/`: `presentation/navigation/`, `hub/presentation/model/`, `hub/presentation/screens/`, `hymnal/data/{api,dto,mapper,repository}/`, `hymnal/di/`, `hymnal/domain/{model,repository,usecase}/`, `hymnal/presentation/{components,screens,state,util,viewmodel}/`
- [X] T002 [P] Add the endpoint path constants in `features/admin/reports/hymnal/data/api/HymnalReportEndpoints.kt`, built on `ApiConstants.BASE_PATH`, for `hymnal-history/occurrences/`, `hymnal-history/top-hymns/`, `hymnal-history/settings/`, `hymnal-history/service-windows/` and `hymnal-history/service-windows/{id}/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: the core crossings, the domain vocabulary and the whole data layer. Every user story
depends on this phase.

**⚠️ CRITICAL**: no user story work can begin until this phase is complete.

### Core additions

- [X] T003 [P] Create `core/domain/model/HymnCatalogEntry.kt` with `number: String` and `title: String` only — no lyrics, no server id
- [X] T004 [P] Create `core/domain/util/DateProvider.kt` as a `fun interface` returning `LocalDate`, beside the existing `MonotonicClock.kt`
- [X] T005 Create `core/domain/repository/HymnCatalogRepository.kt` exposing `fun observeHymnCatalog(): Flow<SnapshotState<List<HymnCatalogEntry>>>` (depends on T003)
- [X] T006 Bind `DateProvider` to a `LocalDate.now(ZoneId.of("America/Sao_Paulo"))` implementation in `core/di/AppInfoModule.kt` (depends on T004)
- [X] T007 Add `val fieldErrors: Map<String, List<String>>? = null` to `AppError.Server` in `core/domain/error/AppError.kt`, defaulted so no existing caller changes
- [X] T008 Fill `fieldErrors` from `parseApiError(...)` in `core/network/error/ResponseExt.kt`, keeping it the only place that reads `errorBody()` (depends on T007)
- [X] T009 [P] Add `core/network/error/ResponseExtFieldErrorsTest.kt`: a body with `field_errors` produces an `AppError.Server` carrying them; a body without them produces the same error as before; a malformed body still yields `HTTP <code>`

### Hymnal catalogue binding (no behaviour change)

- [X] T010 Make `HymnalRepository` extend `HymnCatalogRepository` in `features/hymnal/domain/repository/HymnalRepository.kt` (depends on T005)
- [X] T011 Implement `observeHymnCatalog()` in `features/hymnal/data/repository/HymnalRepositoryImpl.kt` by mapping the already-observed `List<Hymn>` to `List<HymnCatalogEntry>` (depends on T010)
- [X] T012 Bind `HymnalRepositoryImpl` to `HymnCatalogRepository` in `features/hymnal/di/HymnalModule.kt`, mirroring `SongsTable.kt`'s `AllSongsRepository` binding (depends on T011)

### Domain vocabulary

- [X] T013 [P] Create `features/admin/reports/hymnal/domain/model/ReportSelection.kt` with `ReportPeriod`, `ReportSlice` and `ReportReading` per data-model §2.2
- [X] T014 [P] Create `.../domain/model/OccurrenceReport.kt` with `HymnOccurrence`, `OccurrenceReport`, `TopHymn`, `DateRange` and `BucketGranularity` per data-model §2.1
- [X] T015 [P] Create `.../domain/model/ReportReadings.kt` with `BarPoint`, `HymnRanking`, `EvolutionSeries`, `ServiceBulletin`, `BulletinHymn`, `CalendarMonth`, `CalendarDay`, `InVsOutside`, `Highlight`, `HighlightDelta`, `HymnalCoverage`, `CatalogHymn`, `ForgottenHymn`, `HymnProfile`, `ServiceShare`, `HymnRecurrence` per data-model §2.3
- [X] T016 [P] Create `.../domain/model/HymnalAdminModels.kt` with `ServiceWindow`, `ServiceWindowDraft`, `CollectionSettings` and the `SettingField` enum carrying `wireName`, `min`, `max` and the Portuguese label per data-model §2.5
- [X] T017 [P] Create `.../domain/model/ReportEmptyReason.kt` as the sealed emptiness type with all six branches per data-model §2.4
- [X] T018 Create `.../domain/repository/HymnalReportRepository.kt` declaring `getOccurrences(range, granularity)` and `getAllTimeTopHymns()`, both returning `Result<…>` (depends on T014)
- [X] T019 Create `.../domain/repository/HymnalHistoryAdminRepository.kt` declaring the settings read/patch and the service-window list/create/update/delete, all returning `Result<…>` (depends on T016)

### Data layer

- [X] T020 Create `.../data/dto/HymnalReportDtos.kt` with every DTO from data-model §1, `@SerialName` on **every** field, and `CollectionSettingsPatchDto` all-nullable so the shared `Json` (`explicitNulls = false`) omits unchanged keys
- [X] T021 [P] Create `.../data/api/HymnalReportApi.kt` with `GET occurrences` (`from`, `to`, `group_by`) and `GET top-hymns` (no date params), returning `Response<…>` (depends on T002, T020)
- [X] T022 [P] Create `.../data/api/HymnalHistoryAdminApi.kt` with `GET`/`PATCH settings`, `GET`/`POST service-windows`, `PATCH`/`DELETE service-windows/{id}` (depends on T002, T020)
- [X] T023 [P] Create `.../data/mapper/OccurrenceMapper.kt` mapping `OccurrenceDto → HymnOccurrence`, parsing `occurred_on` to `LocalDate` and preserving `service_window_id == null` as meaning (depends on T014, T020)
- [X] T024 [P] Create `.../data/mapper/ServiceWindowMapper.kt` converting `weekday` with `DayOfWeek.of(weekday + 1)` outward and `dayOfWeek.value - 1` inward, and parsing both `"19:00"` and `"19:00:00"` (depends on T016, T020)
- [X] T025 [P] Create `.../data/mapper/CollectionSettingsMapper.kt` with `Dto ↔ domain` plus `CollectionSettings.diff(other): CollectionSettingsPatchDto` returning only changed fields (depends on T016, T020)
- [X] T026 Create `.../data/repository/HymnalReportRepositoryImpl.kt` using `runCatching { … }.mapError()` and `Response.toAppError()`, as `AdminScheduleRepositoryImpl` does (depends on T018, T021, T023)
- [X] T027 Create `.../data/repository/HymnalHistoryAdminRepositoryImpl.kt` with the same error mapping, sending the partial patch from `diff()` (depends on T019, T022, T024, T025)
- [X] T028 Create `.../di/HymnalReportModule.kt` providing both APIs from **`@AuthedRetrofit`** — including the settings read (FR-004) — and binding both repository implementations (depends on T026, T027)
- [X] T029 [P] Create `.../presentation/util/WeekdayLabels.kt` mapping `DayOfWeek` to `"Segunda-feira" … "Domingo"` (depends on T016)

### Foundational tests and fakes

- [X] T030 [P] Create the test fakes in `features/admin/reports/hymnal/` test sources: `FakeHymnalReportRepository`, `FakeHymnalHistoryAdminRepository`, `FakeHymnCatalogRepository` and a fixed `DateProvider`, all returning canned data so every date assertion is deterministic (depends on T018, T019)
- [X] T031 [P] Add `.../data/mapper/ServiceWindowMapperTest.kt`: round-trip all seven weekdays in both directions, asserting explicitly that wire `6` is `DOMINGO` and wire `0` is `SEGUNDA-FEIRA`; plus both time formats (depends on T024)
- [X] T032 [P] Add `.../data/mapper/CollectionSettingsDiffTest.kt`: a one-field change produces a patch whose serialized body contains exactly that key; no change produces an empty body (depends on T025)
- [X] T033 [P] Add `.../data/repository/HymnalReportRepositoryImplTest.kt`: happy path maps to domain; an HTTP failure maps to `AppError`; `403` arrives as `AppError.Auth` (depends on T026)
- [X] T034 [P] Add `.../data/repository/HymnalHistoryAdminRepositoryImplTest.kt`: happy path per operation; a `400` with `field_errors` reaches the caller as `AppError.Server` carrying them; `404` on delete maps cleanly (depends on T027)

**Checkpoint**: the data layer is complete and tested; every user story can now start.

---

## Phase 3: User Story 1 — An administrator reaches the reports area (Priority: P1) 🎯 MVP

**Goal**: the Admin Panel's "Relatórios" card opens a hub that lists report areas, and the hymnal
entry opens the report screen with its loading, error and empty states working against the real
service.

**Independent Test**: sign in as an administrator, tap Relatórios, confirm the hub lists the hymnal
entry, tap it, confirm the report opens on the default period and shows a real loading state
followed by data or a real error. A non-administrator never sees the panel entry.

### Tests for User Story 1

- [X] T035 [P] [US1] Add `.../domain/usecase/ResolveReportPeriodUseCaseTest.kt`: each preset resolves to the right inclusive range against a fixed today; each maps to the `group_by` from research R-08; a custom range longer than 366 days and a reversed range are both refused with the rule named (depends on T030)

### Implementation for User Story 1

- [X] T036 [US1] Create `.../domain/usecase/ResolveReportPeriodUseCase.kt` taking today as a parameter, returning the `DateRange`, the `BucketGranularity`, the preceding range of equal length, and the local validation result (depends on T013, T014)
- [X] T037 [US1] Create `.../domain/usecase/GetOccurrenceReportUseCase.kt` fetching the chosen and the preceding range concurrently, degrading to no preceding data on failure without failing the report (depends on T018, T036)
- [X] T038 [P] [US1] Add `.../domain/usecase/GetOccurrenceReportUseCaseTest.kt`: both ranges succeed; the preceding range failing still yields the chosen one; the chosen range failing yields the error (depends on T030, T037)
- [X] T039 [US1] Create `.../presentation/state/HymnalReportContract.kt` with `HymnalReportUiState` per data-model §5 and a `HymnalReportEvent` `SharedFlow` type for one-shot outcomes (depends on T013, T015, T017)
- [X] T040 [US1] Create `.../presentation/viewmodel/HymnalReportViewModel.kt` holding the loaded report, exposing `StateFlow<HymnalReportUiState>`, fetching on init and on period change only, and running aggregation on `@DefaultDispatcher` (depends on T037, T039)
- [X] T041 [US1] Create `.../presentation/components/EmptyReading.kt` rendering a `ReportEmptyReason` as its explanatory sentence — never a blank chart (depends on T017)
- [X] T042 [US1] Create `.../presentation/screens/HymnalReportScreen.kt` as a `BaseScreen` collector plus a pure content composable, rendering loading, error via `AppError.toUserMessage()`, and the empty state (depends on T040, T041)
- [X] T043 [P] [US1] Create `features/admin/reports/hub/presentation/model/ReportArea.kt` with title, description, icon, accent colour and `onClick` — nothing hymnal-specific
- [X] T044 [US1] Create `features/admin/reports/hub/presentation/screens/ReportsHubScreen.kt` as a static `List<ReportArea>` with one item composable and a `@Preview`, built like `AdminScreen` — no ViewModel, no data layer, no assumption that the list has one entry (depends on T043)
- [X] T045 [US1] Create `features/admin/reports/presentation/navigation/ReportsNavGraph.kt` with `ReportsRoutes`, a `ReportsNav` data class and `NavGraphBuilder.reportsGraph(navController)`, using `safePopBackStack()` and scoping `HymnalReportViewModel` with `hiltViewModel(graphEntry)` (depends on T042, T044)
- [X] T046 [US1] Nest `reportsGraph()` inside `adminGraph` and add `reports: () -> Unit` to `AdminNav` in `features/admin/panel/presentation/navigation/AdminNavGraph.kt` (depends on T045)
- [X] T047 [US1] Remove `enabled = false` from the "Relatórios" action and point `onClick` at `nav.reports` in `features/admin/panel/presentation/screens/AdminScreen.kt` — the Indigo accent already declared becomes live on its own (depends on T046)

**Checkpoint**: the hub is reachable, the report opens, and its three states are real.

---

## Phase 4: User Story 2 — One period, many readings, without refetching (Priority: P1)

**Goal**: period, slice and reading are three independent selections; only the period fetches. The
highlights, ranking and evolution readings render over the loaded period.

**Independent Test**: load a period, disable the network, then cycle slice and reading through every
combination — all render correct numbers with no request. Re-enable and change the period; exactly
one pair of requests is issued.

### Tests for User Story 2

- [X] T048 [P] [US2] Add `.../domain/usecase/FilterOccurrencesUseCaseTest.kt`: `All`; a service slice keeps only that `serviceWindowId`; `OutsideService` keeps only the nulls; a weekday slice derives the day from `occurredOn` across services (depends on T030)
- [X] T049 [P] [US2] Add `.../domain/usecase/BuildHymnRankingUseCaseTest.kt`: ordering by occurrence count descending with hymn number as tie-break; reach as secondary label only, never as order or fraction; a value of 1 beside 400 still gets the minimum bar fraction; the top-25 cap reports the true total (depends on T030)
- [X] T050 [P] [US2] Add `.../domain/usecase/BuildEvolutionSeriesUseCaseTest.kt`: one bar per bucket in the order the service returned, with no client re-sorting (depends on T030)
- [X] T051 [P] [US2] Add `.../domain/usecase/BuildHighlightsUseCaseTest.kt`: the four statements; deltas present when the preceding period has data; **no delta invented** when it is empty (depends on T030)
- [X] T052 [P] [US2] Add `.../domain/usecase/ResolveEmptyReasonUseCaseTest.kt`: all six branches, including a service slice whose window was inactive or absent during the period (depends on T030)
- [X] T053 [P] [US2] Add `.../presentation/viewmodel/HymnalReportViewModelTest.kt`: changing slice or reading issues **zero** repository calls; changing the period issues exactly one chosen-plus-preceding pair; a custom range over 366 days issues none and surfaces `rangeError` (depends on T030, T040)

### Implementation for User Story 2

- [X] T054 [P] [US2] Create `.../domain/usecase/FilterOccurrencesUseCase.kt` — the single slice implementation every reading reuses (depends on T013, T014)
- [X] T055 [P] [US2] Create `.../domain/usecase/BuildHymnRankingUseCase.kt` producing `BarPoint`s with the occurrence count as `value`, reach as `secondaryLabel`, the fraction floored at the minimum, and the top-25 cap plus the true total (depends on T015, T054)
- [X] T056 [P] [US2] Create `.../domain/usecase/BuildEvolutionSeriesUseCase.kt` producing one `BarPoint` per bucket in received order (depends on T015, T054)
- [X] T057 [P] [US2] Create `.../domain/usecase/BuildHighlightsUseCase.kt` producing the four `Highlight`s with their Portuguese label, value and optional delta — all text assembled here, none in the view (depends on T015, T054)
- [X] T058 [P] [US2] Create `.../domain/usecase/ResolveEmptyReasonUseCase.kt` choosing the branch and its Portuguese sentence, with `ServiceWithoutRecords` naming both possibilities and asserting neither (depends on T017, T054)
- [X] T059 [P] [US2] Create `.../domain/usecase/GetServiceWindowsUseCase.kt` returning the windows for the slice selector (depends on T019)
- [X] T060 [P] [US2] Create `.../presentation/components/HorizontalBarChart.kt` as a `Column` of rows with `fillMaxWidth(fraction)` bars, real text labels, theme colours, and a `@Preview` with fake data (depends on T015)
- [X] T061 [P] [US2] Create `.../presentation/components/VerticalBarChart.kt` with a `Canvas` for the bars plus a label row, theme colours, and a `@Preview` (depends on T015)
- [X] T062 [P] [US2] Create `.../presentation/components/PeriodSelector.kt` offering esta semana / este mês / este ano / personalizado, with a range picker that surfaces `rangeError` on the picker itself (depends on T013)
- [X] T063 [P] [US2] Create `.../presentation/components/SliceSelector.kt` offering Todas, each **active** service window, Fora do culto, and a weekday (depends on T013, T029)
- [X] T064 [P] [US2] Create `.../presentation/components/ReadingSelector.kt` offering the seven readings (depends on T013)
- [X] T065 [US2] Extend `HymnalReportViewModel` with `period`/`slice`/`reading` flows combined against the loaded report through the pure use cases, loading the service windows once, and refetching **only** on a period change (depends on T054–T059, T040)
- [X] T066 [US2] Wire the selectors and the highlights, ranking and evolution readings into `HymnalReportScreen.kt`, each falling back to `EmptyReading` (depends on T060–T065)

**Checkpoint**: the four acceptance questions in quickstart §3.2 are answerable, offline after the first load.

---

## Phase 5: User Story 3 — What we sang last Sunday (Priority: P2)

**Goal**: the services reading as bulletins, and the month calendar with intensity.

**Independent Test**: load a period with at least two services; confirm one card per service, newest
first, each listing its hymns; confirm the calendar shades days by volume and a tap opens that day's
bulletin.

### Tests for User Story 3

- [X] T067 [P] [US3] Add `.../domain/usecase/BuildServiceBulletinsUseCaseTest.kt`: reverse-chronological order; one card per date + service group; occurrences with no service grouped by calendar day and flagged as outside-service (depends on T030)
- [X] T068 [P] [US3] Add `.../domain/usecase/BuildCalendarUseCaseTest.kt`: one `CalendarMonth` per month of the period (twelve for a year); intensity scales to the busiest day; days with no occurrence have intensity zero (depends on T030)

### Implementation for User Story 3

- [X] T069 [P] [US3] Create `.../domain/usecase/BuildServiceBulletinsUseCase.kt` (depends on T015, T054)
- [X] T070 [P] [US3] Create `.../domain/usecase/BuildCalendarUseCase.kt` (depends on T015, T054)
- [X] T071 [P] [US3] Create `.../presentation/components/BulletinCard.kt` showing date, service name (or the outside-service marking), and each hymn with number, title and reach, with a `@Preview` (depends on T015)
- [X] T072 [P] [US3] Create `.../presentation/components/CalendarGrid.kt` as a grid of day boxes with variable opacity and an `onDayClick`, with a `@Preview` (depends on T015)
- [X] T073 [US3] Add the services and calendar readings to `HymnalReportScreen.kt`, with tapping a calendar day scrolling to or opening that day's bulletin (depends on T069–T072, T065)

**Checkpoint**: "o que cantamos no domingo passado" is answered in four taps.

---

## Phase 6: User Story 4 — Together versus alone (Priority: P2)

**Goal**: in-service and outside-service rankings side by side, with the exclusive hymns marked.

**Independent Test**: load a period containing both kinds of occurrence and confirm two rankings and
the exclusive callouts on each side; empty one side and confirm it names which side is empty while
the other still renders.

### Tests for User Story 4

- [X] T074 [P] [US4] Add `.../domain/usecase/BuildInVsOutsideUseCaseTest.kt`: both rankings; exclusives computed on both sides; one side empty leaves the other intact and yields the side-specific empty reason (depends on T030)

### Implementation for User Story 4

- [X] T075 [US4] Create `.../domain/usecase/BuildInVsOutsideUseCase.kt` reusing `BuildHymnRankingUseCase` for each side (depends on T055)
- [X] T076 [US4] Add the in-versus-outside reading to `HymnalReportScreen.kt`, side by side, with the exclusive hymns marked and a per-side empty state (depends on T075, T065)

**Checkpoint**: the outside-service signal has a reading of its own, not a hidden filter.

---

## Phase 7: User Story 5 — What the church never sings, and what it has forgotten (Priority: P2)

**Goal**: the coverage reading — proportion, never-sung list, forgotten list — from the all-time
ranking crossed with the local catalogue and the last 366 days.

**Independent Test**: with a known catalogue size and an all-time ranking covering a subset, confirm
the proportion, the never-sung list, and a forgotten hymn whose last occurrence falls outside the
366-day window showing **no date** and the "mais de um ano" text.

### Tests for User Story 5

- [X] T077 [P] [US5] Add `.../domain/usecase/BuildHymnalCoverageUseCaseTest.kt`: never-sung is catalogue minus the all-time ranking; an empty ranking yields the whole catalogue; a hymn last sung inside the window shows the exact date; **a hymn absent from the window shows no date, the "não é cantado há mais de um ano" text, and sorts ahead of every dated entry**; an unavailable catalogue yields `CatalogUnavailable` (depends on T030)

### Implementation for User Story 5

- [X] T078 [P] [US5] Create `.../domain/usecase/GetAllTimeTopHymnsUseCase.kt` calling `top-hymns` with **no** date parameters (depends on T018)
- [X] T079 [US5] Create `.../domain/usecase/BuildHymnalCoverageUseCase.kt` taking the catalogue, the all-time ranking and the last-366-days occurrences, producing the proportion sentence, the never-sung list and the forgotten list with their text already assembled (depends on T005, T015, T078)
- [X] T080 [US5] Load the coverage sources lazily in `HymnalReportViewModel` — the all-time ranking and a 366-day occurrences fetch, concurrently, on the first opening of the reading only — and observe the catalogue through `HymnCatalogRepository` (depends on T079, T065)
- [X] T081 [US5] Add the coverage reading to `HymnalReportScreen.kt`, labelled "todo o histórico", with the period and slice selectors hidden on it and both lists navigable (depends on T080)

**Checkpoint**: the never-sung and forgotten questions are answered, with no invented precision.

---

## Phase 8: User Story 7 — Managing the services the report groups by (Priority: P2)

**Goal**: list, create, edit, activate/deactivate and delete service windows, with the delete
confirmation that states no history is lost.

**Independent Test**: create a service and see it in the list and in the report's slice selector;
deactivate it and see it leave the selector while staying listed; note a past period's occurrence
total, delete a window, and confirm the total is unchanged.

### Tests for User Story 7

- [X] T082 [P] [US7] Add `.../domain/usecase/ValidateServiceWindowUseCaseTest.kt`: end equal to and before start both refused on the time fields; empty name and a 101-character name refused on the name field; a valid draft passes (depends on T030)
- [X] T083 [P] [US7] Add `.../presentation/viewmodel/ServiceWindowsViewModelTest.kt`: a local validation failure issues no request; a server `field_errors` response lands on the named field; a successful create refreshes the list (depends on T030)

### Implementation for User Story 7

- [X] T084 [P] [US7] Create `.../domain/usecase/ValidateServiceWindowUseCase.kt` returning per-field errors per data-model §2.6 (depends on T016)
- [X] T085 [P] [US7] Create `.../domain/usecase/SaveServiceWindowUseCase.kt` (create when the draft id is null, patch otherwise) and `.../domain/usecase/DeleteServiceWindowUseCase.kt` (depends on T019)
- [X] T086 [P] [US7] Create `.../presentation/state/ServiceWindowsContract.kt` with `ServiceWindowsUiState` per data-model §5 (depends on T016)
- [X] T087 [US7] Create `.../presentation/viewmodel/ServiceWindowsViewModel.kt` validating locally before any request and routing server `field_errors` to fields by wire name (depends on T084, T085, T086)
- [X] T088 [P] [US7] Create `.../presentation/components/ServiceWindowRow.kt` showing name, Portuguese weekday, times, an active toggle and edit/delete actions, with a `@Preview` (depends on T029)
- [X] T089 [P] [US7] Create `.../presentation/components/ServiceWindowFormDialog.kt` for create and edit, with per-field errors and a weekday picker that cannot produce an out-of-range value (depends on T029, T086)
- [X] T090 [US7] Create `.../presentation/screens/ServiceWindowsScreen.kt` with its three states, and a delete confirmation stating that no history is deleted, that occurrences regroup by calendar day, and that deactivating is the milder option (depends on T087, T088, T089)
- [X] T091 [US7] Add the screen to `ReportsNavGraph.kt` and reach it from the report's top bar via `BaseScreen`'s `extraActions`; refresh the report's slice selector when the windows change (depends on T090, T045, T065)

**Checkpoint**: every service-based slice, bulletin and recurrence statement now has a managed source.

---

## Phase 9: User Story 6 — Everything about one hymn (Priority: P3)

**Goal**: the hymn card, reached from any list in the feature, including the recurrence sentence.

**Independent Test**: tap a hymn in the ranking and confirm the all-time total labelled "em todo o
histórico", the dated facts labelled "no último ano", and the recurrence sentence with its matching
band of marks. A hymn opened from the never-sung list shows no dates and no counts.

### Tests for User Story 6

- [X] T092 [P] [US6] Add `.../domain/usecase/BuildHymnProfileUseCaseTest.kt`: all-time total from the all-time source and dated facts from the window; the recurrence sentence counts instances of the hymn's most frequent service and its marks match; a never-sung hymn yields no dates and no counts (depends on T030)

### Implementation for User Story 6

- [X] T093 [US6] Create `.../domain/usecase/BuildHymnProfileUseCase.kt` producing `HymnProfile` with every label and sentence assembled in the domain (depends on T015, T054, T078)
- [X] T094 [US6] Create `.../presentation/screens/HymnCardScreen.kt` rendering the profile, the recurrence band, and the never-recorded variant, with its three states (depends on T093)
- [X] T095 [US6] Add the hymn card destination to `ReportsNavGraph.kt` keyed by hymn number, resolving `HymnalReportViewModel` with `hiltViewModel(graphEntry)` so no refetch occurs (depends on T094, T045)
- [X] T096 [US6] Make every hymn list in the feature tappable — ranking bars, bulletin hymns, never-sung and forgotten entries, both sides of the in-versus-outside reading (depends on T095, T066, T073, T076, T081)

**Checkpoint**: every list item in the feature has a destination.

---

## Phase 10: User Story 8 — Tuning the collection (Priority: P3)

**Goal**: the six collection parameters, editable, validated locally, with server field errors
landing on the right field and the three effect explanations visible.

**Independent Test**: change a value, save, reopen and confirm it; submit an out-of-range value and
confirm the error lands on that field with no request issued.

### Tests for User Story 8

- [X] T097 [P] [US8] Add `.../domain/usecase/ValidateCollectionSettingsUseCaseTest.kt`: for each of the six fields, `min − 1`, `min`, `max`, `max + 1` and a non-integer, asserting the message names the accepted range (depends on T030)
- [X] T098 [P] [US8] Add `.../presentation/viewmodel/CollectionSettingsViewModelTest.kt`: a local failure issues no request; only changed values are sent; a server `field_errors` key is routed by `SettingField.wireName`; an **unrecognised** key falls back to the generic error text rather than vanishing (depends on T030)

### Implementation for User Story 8

- [X] T099 [P] [US8] Create `.../domain/usecase/ValidateCollectionSettingsUseCase.kt` checking each value against its own `SettingField` range (depends on T016)
- [X] T100 [P] [US8] Create `.../domain/usecase/GetCollectionSettingsUseCase.kt` and `.../domain/usecase/UpdateCollectionSettingsUseCase.kt`, the latter sending the diff (depends on T019, T025)
- [X] T101 [P] [US8] Create `.../presentation/state/CollectionSettingsContract.kt` with `CollectionSettingsUiState` holding raw text per field so a typo survives (depends on T016)
- [X] T102 [US8] Create `.../presentation/viewmodel/CollectionSettingsViewModel.kt` (depends on T099, T100, T101)
- [X] T103 [P] [US8] Create `.../presentation/components/SettingNumberField.kt` showing the label, the accepted range, the per-field error and the effect explanation, with a `@Preview` (depends on T016)
- [X] T104 [US8] Create `.../presentation/screens/CollectionSettingsScreen.kt` with its three states and the three effect explanations from contracts §2 — grace re-interprets stored history, collapse window is ingest-only, minimum duration is future-only — plus the note that this device's own collection settings refresh at the next app start (depends on T102, T103)
- [X] T105 [US8] Add the screen to `ReportsNavGraph.kt` and reach it from the report's top bar `extraActions` beside the service windows entry (depends on T104, T091)

**Checkpoint**: all four surfaces exist and every user story is independently demonstrable.

---

## Phase 11: Polish & Cross-Cutting Concerns

- [X] T106 [P] Update `specs/hymnal/spec.md`: §4 "Fora de escopo" no longer declares these screens non-existent; §3.6 notes that `PATCH` now has an in-app surface owned by `features/admin/reports/` (FR-054)
- [X] T107 [P] Update `specs/admin/spec.md`: add the reports hub and the three hymnal surfaces to §1 Telas, move "Relatórios" out of "Sem implementação" in §2.1, and record in §5 that these screens do make network calls (FR-055)
- [X] T108 [P] Verify no entry was added to `gradle/libs.versions.toml` and no charting library appears anywhere in `app/build.gradle.kts` (FR-023)
- [X] T109 Review every new file for the 120-character line limit, `official` Kotlin style, hardcoded Portuguese user-facing strings, and no magic strings (`CLAUDE.md`)
- [X] T110 Confirm no view-layer file computes a proportion, a delta or a temporal sentence — every such string comes from a domain use case (FR-051)
- [X] T111 Run `./gradlew :app:testDebugUnitTest` and confirm green
- [ ] T112 Walk the whole of [quickstart.md](./quickstart.md) §3 on a device against the real backend, including §3.0 (empty church) and §3.9 (non-administrator) — **the only task not done: it needs a device, an administrator account and the live backend.** Note that `app/build.gradle.kts` currently points the debug `API_BASE_URL` at a local address, so restore it before walking §3

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: no dependencies
- **Foundational (Phase 2)**: depends on Setup — **blocks every user story**
- **US1 (Phase 3)**: depends on Foundational
- **US2 (Phase 4)**: depends on US1 — it fills the screen and ViewModel US1 created
- **US3, US4, US5 (Phases 5–7)**: depend on US2's `FilterOccurrencesUseCase` and ViewModel derivation; independent of each other
- **US7 (Phase 8)**: depends on Foundational only; its wiring task T091 touches the report screen, so it is easiest after US2
- **US6 (Phase 9)**: depends on whichever lists exist — T096 links every list built so far
- **US8 (Phase 10)**: depends on Foundational only; T105 shares the top bar with T091
- **Polish (Phase 11)**: depends on every story intended for the release

### User Story Independence

- **US1** is self-contained once the data layer exists.
- **US2** needs US1's shell; every later reading needs US2's filter. This is the one genuine chain.
- **US3, US4, US5** are mutually independent — three developers can take one each after US2.
- **US7** and **US8** touch a different repository and different screens; both can be built in
  parallel with the reading phases. Note that **US2 does not depend on US7's screen**: the slice
  selector reads the windows through `GetServiceWindowsUseCase`, which exists in Foundational.
- **US6** is last because it is the destination of the lists the other stories build.

### Parallel Opportunities

- Phase 2: T003–T004, T013–T017, T021–T025 and T031–T034 are each parallel within their group.
- Phase 4: T048–T053 (all tests) and T054–T064 (use cases and components) are parallel; only T065
  and T066 serialise.
- Phases 5, 6, 7, 8 and 10 can run concurrently once Phase 4 is done, on different files.

---

## Parallel Example: User Story 2

```bash
# All US2 tests together:
Task: "FilterOccurrencesUseCaseTest"      # T048
Task: "BuildHymnRankingUseCaseTest"       # T049
Task: "BuildEvolutionSeriesUseCaseTest"   # T050
Task: "BuildHighlightsUseCaseTest"        # T051
Task: "ResolveEmptyReasonUseCaseTest"     # T052

# All US2 pure use cases together:
Task: "FilterOccurrencesUseCase"          # T054
Task: "BuildHymnRankingUseCase"           # T055
Task: "BuildEvolutionSeriesUseCase"       # T056
Task: "BuildHighlightsUseCase"            # T057

# All US2 components together:
Task: "HorizontalBarChart"                # T060
Task: "VerticalBarChart"                  # T061
Task: "PeriodSelector"                    # T062
Task: "SliceSelector"                     # T063
Task: "ReadingSelector"                   # T064
```

---

## Implementation Strategy

### MVP

Phases 1–4 — Setup, Foundational, US1 and US2. That is the smallest thing worth showing: the hub
opens, the report loads a period, and the administrator can pivot it by slice and reading without
refetching. Stop there and validate quickstart §3.1 and §3.2 before going further.

US1 alone is not shippable, and deliberately so: `specs/admin/spec.md` §5 forbids an enabled card
that leads to an empty screen. T047 — enabling the card — is the last task of Phase 3 for that
reason, and the commit that includes it should include Phase 4.

### Incremental Delivery

1. Phases 1–2 → the data layer is tested and nothing is user-visible yet.
2. Phase 3 + 4 → **MVP**: hub, report, three readings.
3. Phase 5 → bulletins and calendar, the weekly question answered.
4. Phase 8 → service windows, so the slices stop depending on whatever the server already had.
5. Phases 6 and 7 → the two comparative readings.
6. Phases 9 and 10 → the hymn card and settings tuning.
7. Phase 11 → specs updated in the same commit as the code they describe.

---

## Notes

- `[P]` means different files with no incomplete dependency.
- Every user-facing string is hardcoded Portuguese; every derived sentence comes from `domain/`.
- Commit after each task or logical group; the spec updates (T106, T107) ride with the code.
- Stop at any checkpoint to validate a story on its own.
