# Implementation Plan: Hymnal View History Collection

**Branch**: `002-hymnal-view-history` | **Date**: 2026-08-08 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/002-hymnal-view-history/spec.md`

## Summary

Record a hymn view when a member keeps `HymnDetailScreen` in the foreground past a service-configured threshold, hold those views in a durable local queue, and upload them in the background whenever the network allows — with no screen, no control, and no way for a failure to reach the member.

The approach reuses what the app already has rather than adding infrastructure: `JsonSnapshotStorage` for the queue, the `@SettingsPrefs` DataStore for the device id and cached settings, the `Refreshable` startup multibinding for the settings fetch, WorkManager + Hilt-Work for upload, and the existing `@AuthedRetrofit` / `@AuthLessRetrofit` pair for transport. Three genuinely new pieces carry the feature: a pure, clock-injected `HymnViewTimer`; a mutex-guarded `HymnViewQueueStore` that turns a whole-blob store into an append-and-remove queue; and a `HymnViewSyncWorker` that reconciles against the ids it submitted rather than the ids the service answered for.

Two decisions deserve to be read before implementation starts, because getting them wrong is silent and expensive:

- **Client selection is on `hasValidAccessToken()`, not `isLoggedIn()`.** Sending an expired token to this endpoint can make `TokenAuthenticator` clear the token store and sign the member out from a background job (`core/network/TokenAuthenticator.kt:66-69`). See [research R-04](./research.md#r-04-how-is-the-authenticated-client-used-without-risking-a-silent-logout).
- **`HymnDto.id` is `Int? = null`.** Non-nullable without a default would throw `MissingFieldException` on every pre-existing cached snapshot and break the offline hymnal on upgrade. See [research R-11](./research.md#r-11-how-does-id-reach-the-domain-and-what-happens-without-it).

## Technical Context

**Language/Version**: Kotlin 2.3.10, JVM target 17

**Primary Dependencies**: Jetpack Compose (BOM 2026.02.00), Hilt + Hilt-Work 1.2.0, WorkManager 2.10.0, Retrofit + `kotlinx.serialization`, DataStore Preferences, Timber

**Storage**: `JsonSnapshotStorage` (files under `filesDir/snapshots/`) for the event queue; `@SettingsPrefs` DataStore for the device id and cached settings. **No Room** — explicitly excluded.

**Testing**: JUnit4 + MockK + `kotlinx-coroutines-test` + Turbine, unit tests only (`app/src/test/`), fakes preferred over mocks

**Target Platform**: Android, `minSdk` 24 / `targetSdk` 36 / `compileSdk` 36

**Project Type**: Single-module Android app (`:app`), feature-based MVVM + Clean

**Performance Goals**: No measurable effect on hymn open or scroll. The timer does no work while idle — one `delay`, not a tick loop. The queue is rewritten at most once per recorded view (≤ once per 30 s of reading) and once per sync.

**Constraints**: Fully offline-capable; queue capped at 2000 events; zero user-visible surface; no new runtime permission; no new third-party dependency.

**Scale/Scope**: ~20 new files, ~2 modified. Queue worst case ≈ 400 KB of JSON. Upload volume per device on the order of tens of events per week.

## Constitution Check

*GATE: must pass before Phase 0, re-checked after Phase 1.*

`.specify/memory/constitution.md` is still the unmodified Spec Kit template — every principle is a `[PRINCIPLE_N_NAME]` placeholder. There are no ratified constitutional gates to evaluate.

In its absence the binding rules are `CLAUDE.md`, and the plan is checked against them explicitly:

| Rule (`CLAUDE.md`) | Status | Note |
|---|---|---|
| `UI → ViewModel → UseCase → Repository (interface) → Repository (impl)` | PASS | Full chain; the ViewModel touches only use cases |
| Features never import each other | PASS | Needs a new core `AuthStatusProvider` — see below |
| `domain/` has no Android knowledge | PASS | `HymnViewTimer` takes an injected clock, not `SystemClock` |
| `presentation/` never touches repos | PASS | |
| Errors as sealed classes or `Result<T>`; no raw HTTP above the repository | PASS | Existing `AppError` + new `SyncOutcome` |
| Every screen has `UiState` with Loading/Success/Error | N/A | The feature adds no screen |
| `@Preview` on every content composable | N/A | No new composable |
| Graph-scoped `hiltViewModel(graphEntry)` in existing graphs (Pitfall #1) | DEVIATION | Justified below and in Complexity Tracking |
| No magic strings; 120-char lines; official Kotlin style | PASS | Constants for work name, storage keys, defaults |
| Tests: happy path + 1 error per use case | PASS | Test plan in [quickstart.md](./quickstart.md) |
| No unnecessary abstractions | PASS | Two new interfaces, each removing a concrete rule violation |
| Spec and code in the same commit (§7.2) | PASS | Includes creating `specs/hymnal/spec.md` |

**One deviation, recorded**: the tracking ViewModel uses bare `hiltViewModel()` rather than `hiltViewModel(graphEntry)`. Pitfall #1 exists so `HymnalViewModel` — holding the shared hymn list and search query — is shared across the hymnal graph. This feature needs the exact opposite: state that resets on every fresh visit to the detail destination (FR-004). Destination scoping delivers that from the back stack instead of hand-rolled reset bookkeeping. `HymnalViewModel` itself remains graph-scoped and untouched. See Complexity Tracking.

**Post-Phase 1 re-check**: unchanged. The design added no further deviations; `AuthStatusProvider` and `DeviceIdProvider` both exist specifically to *keep* the layering rules rather than bend them.

## Project Structure

### Documentation (this feature)

```text
specs/002-hymnal-view-history/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0 — 12 resolved decisions
├── data-model.md        # Phase 1 — domain / persistence / wire types
├── quickstart.md        # Phase 1 — validation and test plan
├── contracts/
│   └── hymnal-history-api.md
├── checklists/
│   └── requirements.md
└── tasks.md             # Phase 2 — created by /speckit-tasks, NOT here
```

### Source code

```text
app/src/main/java/com/ipb/castelobranco/
├── core/
│   ├── data/
│   │   ├── auth/
│   │   │   └── AuthSessionStatusProvider.kt      NEW  delegates to AuthSession
│   │   └── local/
│   │       └── DeviceIdProvider.kt               NEW  interface + DataStore impl
│   ├── di/
│   │   ├── AuthModule.kt                         MOD  bind AuthStatusProvider
│   │   └── DeviceIdModule.kt                     NEW  bind DeviceIdProvider
│   └── domain/auth/
│       └── AuthStatusProvider.kt                 NEW  interface (no feature import)
│
├── features/hymnal/
│   ├── data/
│   │   ├── api/
│   │   │   ├── HymnalHistoryApi.kt               NEW
│   │   │   └── HymnalHistoryEndpoints.kt         NEW
│   │   ├── dto/
│   │   │   ├── HymnalDtos.kt                     MOD  + id: Int? = null
│   │   │   └── HymnalHistoryDtos.kt              NEW  wire types, exactly 7 fields
│   │   ├── local/
│   │   │   ├── HymnViewQueueStore.kt             NEW  mutex-guarded queue
│   │   │   ├── QueuedHymnViewEvent.kt            NEW  persistence record
│   │   │   └── HymnViewSettingsStore.kt          NEW  cached settings
│   │   ├── mapper/
│   │   │   ├── HymnMapper.kt                     MOD  pass id through
│   │   │   └── HymnViewHistoryMapper.kt          NEW  domain ↔ persistence ↔ wire
│   │   ├── repository/
│   │   │   └── HymnViewHistoryRepositoryImpl.kt  NEW
│   │   └── work/
│   │       ├── HymnViewSyncWorker.kt             NEW  @HiltWorker
│   │       └── HymnViewSyncScheduler.kt          NEW  unique-work enqueue
│   ├── di/
│   │   └── HymnalHistoryModule.kt                NEW  APIs, repo, Refreshable
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Hymnal.kt                         MOD  Hymn + id: Int?
│   │   │   └── HymnViewHistory.kt                NEW  event, settings, reasons
│   │   ├── repository/
│   │   │   └── HymnViewHistoryRepository.kt      NEW
│   │   ├── timer/
│   │   │   └── HymnViewTimer.kt                  NEW  pure, clock-injected
│   │   └── usecase/
│   │       ├── RecordHymnViewUseCase.kt          NEW
│   │       ├── SyncHymnViewsUseCase.kt           NEW
│   │       └── GetHymnViewSettingsUseCase.kt     NEW
│   └── presentation/
│       ├── navigation/HymnalNavGraph.kt          MOD  wire tracking VM
│       ├── screens/HymnDetailScreen.kt           MOD  lifecycle signals only
│       └── viewmodel/
│           └── HymnViewTrackingViewModel.kt      NEW  destination-scoped
│
└── MyApp.kt                                      MOD  enqueue sync on start

app/src/test/java/com/ipb/castelobranco/
├── core/data/local/DeviceIdProviderTest.kt       NEW
└── features/hymnal/
    ├── data/local/HymnViewQueueStoreTest.kt      NEW
    ├── data/repository/
    │   └── HymnViewHistoryRepositoryImplTest.kt  NEW
    ├── domain/timer/HymnViewTimerTest.kt         NEW
    ├── domain/usecase/
    │   ├── RecordHymnViewUseCaseTest.kt          NEW
    │   └── SyncHymnViewsUseCaseTest.kt           NEW
    └── presentation/viewmodel/
        └── HymnViewTrackingViewModelTest.kt      NEW
```

**Structure Decision**: Single-module feature-based layout, unchanged. Everything lives under `features/hymnal/` except the device id and auth-status abstraction, which the spec places in `core/` because neither is hymnal-specific. `MonotonicClock` is injected via the existing `DispatcherModule` style so `domain/` stays free of Android types.

## Implementation Order

Sequenced so each step is independently testable and nothing is blocked on D-001 until the very end.

**Step 1 — Core primitives.** `DeviceIdProvider` (interface + DataStore impl + Hilt binding), `AuthStatusProvider` (interface + `AuthSession` adapter + binding). Both fully unit-testable now. Nothing else depends on ordering here.

**Step 2 — Domain types.** `HymnViewEvent`, `HymnViewCollectionSettings`, `RejectionReason`, `SyncOutcome`, `HymnViewHistoryRepository`. Pure declarations, no tests of their own.

**Step 3 — The queue.** `QueuedHymnViewEvent`, `HymnViewQueueStore` over `SnapshotStorage` with a `Mutex`. Tests: append/read round-trip, survives a store restart, cap drops the oldest, concurrent appends both land. This is the piece most worth getting right first — everything downstream trusts it.

**Step 4 — The timer.** `HymnViewTimer` with an injected clock. Tests: fires at the threshold; pause/resume accumulates across a gap; fires exactly once no matter how long the visit runs; never fires below the threshold.

**Step 5 — Network and settings.** `HymnalHistoryApi` (two qualified instances), the DTOs, `HymnViewSettingsStore`, `refreshSettings()`, and the `Refreshable` binding. Tests: settings cached and read back; a fetch failure falls through to cache then to defaults.

**Step 6 — Repository and sync use case.** `HymnViewHistoryRepositoryImpl` including client selection on `hasValidAccessToken()`, chunking at `maxBatchSize`, and the submitted-set reconciliation. Tests: the outcome table from [research R-08](./research.md#r-08-what-does-the-worker-do-with-each-http-outcome), and the mandated "deletes exactly the ids the server answered for" — plus the id it *didn't* answer for.

**Step 7 — Worker and scheduling.** `HymnViewSyncWorker`, `HymnViewSyncScheduler` (unique work, `KEEP`, network constraint, exponential backoff), enqueue on record and in `MyApp.onCreate`.

**Step 8 — Presentation.** `HymnViewTrackingViewModel`, `LifecycleResumeEffect` in `HymnDetailScreen`, wiring in `HymnalNavGraph`. Tests: threshold fires once per visit; a new ViewModel instance starts a fresh count.

**Step 9 — `id` propagation.** `HymnDto`, `Hymn`, `HymnMapper`. Small and last on purpose: until D-001 lands the field is always null, so doing it early would only produce dead code. Test: a snapshot JSON with no `id` still deserializes and yields `id == null`.

**Step 10 — Domain spec.** Create `specs/hymnal/spec.md` (it does not exist — see Risks) covering the current hymnal domain plus this addition, committed with the code per `CLAUDE.md` §7.2.

## Risks

| Risk | Impact | Handling |
|---|---|---|
| **D-001 not shipped** — backend does not expose `id` | Feature is a silent no-op end to end | Everything is buildable and unit-testable without it; FR-009 makes the no-op safe rather than broken. Coordinate before release, and verify with the quickstart's D-001 check. |
| **Silent logout via background upload** | A member is signed out without touching the app — severe and hard to attribute | Gate on `hasValidAccessToken()`. Covered by a repository test asserting the anonymous client is chosen for an expired token. |
| **`MissingFieldException` on cached snapshots** | Offline hymnal breaks for every existing user on upgrade | `id: Int? = null`, plus a test deserializing a pre-feature snapshot payload. |
| **Queue leaks events forever** | Unbounded growth, repeated uploads | Reconcile against the submitted set, not the answered set; hard cap at 2000; `400` discards rather than retries. |
| **Lost event on concurrent append** | Undercounting, invisible | Single `Mutex` across the whole read-modify-write; concurrency test. |
| **A view recorded in the worker's blind spot** (R-07) | Delivery deferred to the next app start | Accepted. Drain loop plus startup enqueue bound the delay; aggregate telemetry does not need promptness. |
| **Sends an eighth field by accident** | Every event rejected `invalid_event`, silently | Separate wire DTO whose only job is that constraint; a serialization test asserts the exact key set. |

## Complexity Tracking

| Deviation | Why needed | Simpler alternative rejected because |
|---|---|---|
| Bare `hiltViewModel()` for the tracking ViewModel, against Pitfall #1 | FR-004 requires per-visit state that resets when the member leaves and returns; destination scoping gives that from the back stack | Graph-scoped `HymnalViewModel` survives list↔detail navigation, so it would need manual per-hymn reset tracking — more code, and a state-leak bug waiting to happen |
| New `AuthStatusProvider` interface in core | `AuthSession` lives in `features/auth`, which `features/hymnal` may not import | Importing it directly breaks the feature-isolation rule; moving `AuthSession` into core touches the interceptor, authenticator, and `CoreViewModel` — far outside this feature's scope |
| Separate persistence and wire types with identical fields today | The service rejects any event carrying an unknown field; one shared type makes a future local-only field a silent, feature-breaking change | A single type is smaller but removes the compiler from the loop on the one constraint that fails invisibly |
