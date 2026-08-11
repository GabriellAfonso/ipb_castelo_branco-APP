---

description: "Task list for Hymnal View History Collection"
---

# Tasks: Hymnal View History Collection

**Input**: Design documents from `specs/002-hymnal-view-history/`

**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/hymnal-history-api.md](./contracts/hymnal-history-api.md), [quickstart.md](./quickstart.md)

**Tests**: Included. The spec mandates them under *Implementation Constraints*, naming five scenarios that must be covered. Test tasks marked **[REQUIRED]** are those five.

**Organization**: Grouped by user story so each is independently implementable and testable.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependency on incomplete work)
- **[Story]**: `[US1]`–`[US4]`, mapping to the user stories in [spec.md](./spec.md)
- All paths are repository-relative. Main source root is `app/src/main/java/com/ipb/castelobranco/`, test root `app/src/test/java/com/ipb/castelobranco/`.

---

## Phase 1: Setup

**Purpose**: Confirm the ground is ready. This feature adds no dependency and no permission — verifying that is the setup.

- [X] T001 Verify WorkManager and Hilt-Work are already wired: `androidx.work.runtime.ktx` and `androidx.hilt.work` in `app/build.gradle.kts` (lines ~162-164), `HiltWorkerFactory` provided by `MyApp` in `app/src/main/java/com/ipb/castelobranco/MyApp.kt`. No new dependency is to be added.
- [X] T002 [P] Create the empty package directories for the feature under `app/src/main/java/com/ipb/castelobranco/features/hymnal/`: `data/local/`, `data/work/`, `domain/timer/`, and under `core/`: `data/auth/`, `domain/auth/`.
- [X] T003 [P] Confirm `app/src/main/AndroidManifest.xml` needs no change — this feature adds no permission, no component, and no exported surface. Record the confirmation; a diff here later is a red flag.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared primitives every story needs. Nothing below is story-specific, and no story can start until this is done.

**⚠️ CRITICAL**: `DeviceIdProvider` (T010-T012) is implemented here rather than in its own story phase (US4) because an event cannot be constructed without a device id — US1 and US2 both depend on it. US4's phase therefore holds the *verification* of its privacy and persistence properties, not its construction. This is a deliberate, recorded departure from strict story independence.

### Hymn identifier propagation (unblocks the whole feature — see D-001)

- [X] T004 Add `id: Int? = null` as the **last** property of `HymnDto` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/data/dto/HymnalDtos.kt`. **Nullable with a default is mandatory** — a non-null field without a default throws `MissingFieldException` on every pre-existing cached snapshot and breaks the offline hymnal on upgrade (see [research R-11](./research.md#r-11-how-does-id-reach-the-domain-and-what-happens-without-it)).
- [X] T005 Add `id: Int?` to `Hymn` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/domain/model/Hymnal.kt`. Leave `number` as the display and navigation key; do not touch `HymnalRoutes.detailRoute()`.
- [X] T006 Pass `id` through in `HymnDto.toDomain()` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/data/mapper/HymnMapper.kt`. Leave the existing sort by numeric `number` unchanged.
- [X] T007 [P] Write `HymnDtoBackwardCompatibilityTest` in `app/src/test/java/com/ipb/castelobranco/features/hymnal/data/dto/HymnDtoBackwardCompatibilityTest.kt`: a pre-feature snapshot payload (`{"number":"42","title":"…","lyrics":[]}`) deserializes without throwing and yields `id == null`; a payload with `"id":42` yields `id == 42`.

### Domain types

- [X] T008 [P] Create `app/src/main/java/com/ipb/castelobranco/features/hymnal/domain/model/HymnViewHistory.kt` with `HymnViewEvent` (7 fields per [data-model §1](./data-model.md#1-domain-models)), `HymnViewCollectionSettings` (with `DEFAULT` = 30 s / 50), `RejectionReason` (sealed: the four codes plus `Unrecognised`), and `SyncOutcome` (sealed: `Delivered`, `Discarded`, `Deferred`).
- [X] T009 [P] Create the `HymnViewHistoryRepository` interface in `app/src/main/java/com/ipb/castelobranco/features/hymnal/domain/repository/HymnViewHistoryRepository.kt` per [contracts §4](./contracts/hymnal-history-api.md#4-internal-contracts-introduced-by-this-feature). Domain types only — no `Response`, no DTO, no `HttpException`.

### Core primitives

- [X] T010 [P] Create the `DeviceIdProvider` interface in `app/src/main/java/com/ipb/castelobranco/core/data/local/DeviceIdProvider.kt` (`suspend fun get(): String`) and its `DataStoreDeviceIdProvider` implementation over the `@SettingsPrefs` DataStore under key `device_id`. Generate `UUID.randomUUID().toString()` lazily **inside** `dataStore.edit {}` so two concurrent first calls cannot mint different ids.
- [X] T011 [P] Create the `AuthStatusProvider` interface in `app/src/main/java/com/ipb/castelobranco/core/domain/auth/AuthStatusProvider.kt` (`suspend fun hasValidAccessToken(): Boolean`) and `AuthSessionStatusProvider` in `app/src/main/java/com/ipb/castelobranco/core/data/auth/AuthSessionStatusProvider.kt` delegating to `AuthSession.hasValidAccessToken()`. This exists so `features/hymnal` never imports `features/auth` (see [research R-03](./research.md#r-03-how-does-the-hymnal-feature-learn-whether-the-user-is-signed-in-without-importing-another-feature)).
- [X] T012 Bind both providers `@Singleton` in a new `app/src/main/java/com/ipb/castelobranco/core/di/AppInfoModule.kt`, which also supplies the clock and the app-version/platform values. One module rather than two: these are all ambient app facts with the same lifetime, and splitting them would add files without adding a boundary.
- [X] T013 [P] Create `MonotonicClock` as a `fun interface` (`fun elapsedMillis(): Long`) in `app/src/main/java/com/ipb/castelobranco/core/domain/util/MonotonicClock.kt`, provided in `AppInfoModule` as a lambda over `SystemClock.elapsedRealtime()` — no separate implementation file needed. Keeps `domain/` free of Android types and makes the timer testable with virtual time.

### Settings storage (no network yet)

- [X] T014 Create `HymnViewSettingsStore` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/data/local/HymnViewSettingsStore.kt` over the `@SettingsPrefs` DataStore, keys `hymn_history_min_seconds` and `hymn_history_max_batch`. Reads fall back to `HymnViewCollectionSettings.DEFAULT` when absent. Clamp on read: `minSecondsToCount > 0`, `maxBatchSize` into `1..500`.
- [X] T015 [P] Create `GetHymnViewSettingsUseCase` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/domain/usecase/GetHymnViewSettingsUseCase.kt`, returning cached-or-default. No network here — US3 adds that behind the same use case.

**Checkpoint**: Domain types, device id, auth status, clock, and settings defaults all exist and compile. Story work can begin.

---

## Phase 3: User Story 1 - A genuine hymn view is recorded (Priority: P1) 🎯 MVP

**Goal**: A hymn kept in the foreground past the threshold produces exactly one durable local record — correct even with no network and no backend.

**Independent Test**: Open a hymn, leave it on screen 35 seconds, then `adb shell run-as com.ipb.castelobranco cat files/snapshots/hymn_view_queue.json` — exactly one event, correct hymn, `viewedAt` carrying an offset, duration ≈ 30.

### Tests for User Story 1

> Write these first and watch them fail.

- [X] T016 [P] [US1] **[REQUIRED]** `HymnViewTimerTest` in `app/src/test/java/com/ipb/castelobranco/features/hymnal/domain/timer/HymnViewTimerTest.kt` — covers *pause and resume across backgrounding* and *fires exactly once per visit*. Cases: 20 s foreground → 10 min background → 15 s foreground fires once at the 30 s foreground mark; held 10× the threshold emits once; 29 s then hidden emits zero; reported duration is never negative; backgrounded before the threshold and never returning emits zero. Drive with `runTest` virtual time and a fake `MonotonicClock` — no real waiting.
- [X] T017 [P] [US1] **[REQUIRED]** `HymnViewQueueStoreTest` in `app/src/test/java/com/ipb/castelobranco/features/hymnal/data/local/HymnViewQueueStoreTest.kt` — covers *survives a restart* and *cap drops the oldest*. Cases: append 3, rebuild the store over the same fake `SnapshotStorage`, read back the same 3 in order; append 2500 → size is 2000 with the first 500 gone and the last 2000 in order; 100 parallel `async` appends all land; remove a 2-id set leaves the other 3 in order; never-written store reads empty; malformed JSON reads empty without throwing.
- [X] T018 [P] [US1] `RecordHymnViewUseCaseTest` in `app/src/test/java/com/ipb/castelobranco/features/hymnal/domain/usecase/RecordHymnViewUseCaseTest.kt` — happy path queues an event with a parseable UUID, the device id, and an offset-carrying `viewedAt`; a hymn with `id == null` queues nothing and throws nothing (FR-009); a queue write failure is swallowed.
- [X] T019 [P] [US1] `HymnViewTrackingViewModelTest` in `app/src/test/java/com/ipb/castelobranco/features/hymnal/presentation/viewmodel/HymnViewTrackingViewModelTest.kt` — reaching the threshold invokes `RecordHymnViewUseCase` once with the right hymn; a fresh ViewModel instance starts from zero accumulated time; a settings failure falls back to 30 s and still records.

### Implementation for User Story 1

- [X] T020 [P] [US1] Create `HymnViewTimer` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/domain/timer/HymnViewTimer.kt`: pure Kotlin, injected `MonotonicClock`, `onVisible()` / `onHidden()` / accumulated-millis accessor, and a latch so it reports the threshold crossing at most once. No Android imports, no coroutines inside — the ViewModel owns scheduling.
- [X] T021 [P] [US1] Create `QueuedHymnViewEvent` (`@Serializable`, the 7 fields, `viewedAt` as an ISO-8601 `String`) in `app/src/main/java/com/ipb/castelobranco/features/hymnal/data/local/QueuedHymnViewEvent.kt`.
- [X] T022 [US1] Create `HymnViewQueueStore` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/data/local/HymnViewQueueStore.kt` over the existing `SnapshotStorage`, key `hymn_view_queue`. `append`, `readAll`, `remove(ids: Set<String>)`, `count`. **A single `Mutex` must be held across the entire read-modify-write**, not just the write — that is what stops two near-simultaneous events losing one another (FR-011). Cap at `MAX_QUEUE_SIZE = 2000`, dropping from the front. A corrupt or absent file reads as an empty list.
- [X] T023 [US1] Create the domain↔persistence half of `HymnViewHistoryMapper` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/data/mapper/HymnViewHistoryMapper.kt` (`HymnViewEvent` ↔ `QueuedHymnViewEvent`, `OffsetDateTime` ↔ ISO-8601 string, round-trip lossless).
- [X] T024 [US1] Create `RecordHymnViewUseCase` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/domain/usecase/RecordHymnViewUseCase.kt`: takes the hymn's `id` (nullable) and the accumulated duration; returns silently when `id == null` (FR-009); otherwise builds a `HymnViewEvent` with `UUID.randomUUID()`, the `DeviceIdProvider` value, `OffsetDateTime.now()`, `BuildConfig.VERSION_NAME`, and `platform = "android"`, and hands it to the repository. Never throws to the caller.
- [X] T025 [US1] Create `HymnViewHistoryRepositoryImpl` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/data/repository/HymnViewHistoryRepositoryImpl.kt` implementing `record`, `queuedCount`, and `currentSettings` only. Leave `syncOnce` and `refreshSettings` as `TODO()` — US2 and US3 fill them.
- [X] T026 [US1] Create `HymnalHistoryModule` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/di/HymnalHistoryModule.kt` binding the repository and the queue store. Follow the `@Module @InstallIn(SingletonComponent::class)` style of the existing `HymnalModule.kt`.
- [X] T027 [US1] Create `HymnViewTrackingViewModel` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/presentation/viewmodel/HymnViewTrackingViewModel.kt`: `@HiltViewModel`, holds a `HymnViewTimer`, exposes `onHymnVisible(hymn)` / `onHymnHidden()`. On visible, launch a single `delay(remaining)` job in `viewModelScope`; on hidden, cancel it and fold the elapsed slice into the accumulator. On the delay completing, call `RecordHymnViewUseCase`. Threshold read once per visit from `GetHymnViewSettingsUseCase`. No `Context`, no user-visible state.
- [X] T028 [US1] Wire the tracking ViewModel into `app/src/main/java/com/ipb/castelobranco/features/hymnal/presentation/navigation/HymnalNavGraph.kt`: inside the existing `hymn_detail/{hymnId}` `composable`, obtain it with **bare `hiltViewModel()`** so it scopes to that destination's `NavBackStackEntry` and resets on each fresh visit (FR-004). `HymnalViewModel` keeps its `hiltViewModel(graphEntry)` exactly as today — see the deviation recorded in [plan.md](./plan.md#complexity-tracking).
- [X] T029 [US1] Add lifecycle signalling to `app/src/main/java/com/ipb/castelobranco/features/hymnal/presentation/screens/HymnDetailScreen.kt`: a `LifecycleResumeEffect(hymn)` calling `onHymnVisible(hymn)` on resume and `onHymnHidden()` on pause. `RESUMED`, not `STARTED` — `STARTED` keeps counting behind the lock screen on some devices. Signals only; `HymnDetailContent` stays pure and its previews unchanged.

**Checkpoint**: Views are recorded and survive restarts, with no network involved. This alone is a shippable increment — the data is being collected, just not yet delivered.

---

## Phase 4: User Story 2 - Recorded views reach the church, exactly once (Priority: P1)

**Goal**: Queued views are uploaded whenever connectivity allows, survive reboots, and are removed exactly once — with no failure ever reaching the member.

**Independent Test**: Record three views in airplane mode, force-stop the app, reboot, confirm they persist, then restore the network — the queue file drains to `[]` without the app being reopened.

### Tests for User Story 2

- [X] T030 [P] [US2] `HymnViewEventDtoSerializationTest` in `app/src/test/java/com/ipb/castelobranco/features/hymnal/data/dto/HymnViewEventDtoSerializationTest.kt` — assert `Json.encodeToJsonElement(dto).jsonObject.keys` equals **exactly** the seven contract names. This is the guard against every event coming back `invalid_event`; the shared `Json` sets `encodeDefaults = true`, so any property added to the DTO does reach the wire. Also assert an ingest response carrying an unknown extra field parses without throwing.
- [X] T031 [P] [US2] **[REQUIRED]** `HymnViewHistoryRepositoryImplTest` in `app/src/test/java/com/ipb/castelobranco/features/hymnal/data/repository/HymnViewHistoryRepositoryImplTest.kt` — covers *sync deletes exactly the ids the server answered for*. Cases: 3 submitted, service accepts 1 / rejects 1 / **ignores 1** → all 3 removed while a 4th never-submitted event remains; each rejection reason is logged; 120 queued at `maxBatchSize` 50 → requests of 50/50/20; `429`, `5xx`, and `IOException` each remove nothing and return `Deferred`; `400 VALIDATION_ERROR` removes that chunk and continues; an empty queue issues no request.
- [X] T032 [P] [US2] `HymnViewHistoryClientSelectionTest` in `app/src/test/java/com/ipb/castelobranco/features/hymnal/data/repository/HymnViewHistoryClientSelectionTest.kt` — a valid token selects the authed API; an **expired** token selects the auth-less API. This is the guard against a background upload signing a member out via `TokenAuthenticator` clearing the token store (see [research R-04](./research.md#r-04-how-is-the-authenticated-client-used-without-risking-a-silent-logout)).
- [X] T033 [P] [US2] `SyncHymnViewsUseCaseTest` in `app/src/test/java/com/ipb/castelobranco/features/hymnal/domain/usecase/SyncHymnViewsUseCaseTest.kt` — drains a multi-chunk queue to empty on success; stops and reports `Deferred` on the first deferred chunk, leaving the remainder queued; never throws.

### Implementation for User Story 2

- [X] T034 [P] [US2] Create the wire DTOs in `app/src/main/java/com/ipb/castelobranco/features/hymnal/data/dto/HymnalHistoryDtos.kt`: `HymnViewEventDto` (**exactly** the seven `@SerialName` fields — no eighth, ever), `IngestRequestDto`, `IngestResponseDto`, `RejectedEventDto`. Keep this type separate from `QueuedHymnViewEvent` so a future local-only field cannot silently reach the wire ([research R-10](./research.md#r-10-how-is-the-unknown-fields-are-forbidden-contract-guaranteed)).
- [X] T035 [P] [US2] Create `HymnalHistoryEndpoints` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/data/api/HymnalHistoryEndpoints.kt` built on `ApiConstants.BASE_PATH`, in the style of `HymnalEndpoints.kt`: `EVENTS_PATH = "${ApiConstants.BASE_PATH}hymnal-history/events/"`.
- [X] T036 [US2] Create `HymnalHistoryApi` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/data/api/HymnalHistoryApi.kt` with `@POST` returning `Response<IngestResponseDto>`.
- [X] T037 [US2] Provide **two** qualified `HymnalHistoryApi` instances in `HymnalHistoryModule.kt` — one from `@AuthedRetrofit`, one from `@AuthLessRetrofit` — behind new local qualifiers (e.g. `@AuthedHistoryApi` / `@AuthLessHistoryApi`). Wrong qualifier here means a silent 401 or a silent logout, so name them unambiguously.
- [X] T038 [US2] Add the wire mappings (`QueuedHymnViewEvent` → `HymnViewEventDto`, `HistorySettingsDto` → domain, `reason` → `RejectionReason`) in a separate `app/src/main/java/com/ipb/castelobranco/features/hymnal/data/mapper/HymnViewHistoryWireMapper.kt`, keeping the wire direction physically apart from the persistence mapper.
- [X] T039 [US2] Implement `syncOnce` in `HymnViewHistoryRepositoryImpl`: pick the API on `AuthStatusProvider.hasValidAccessToken()`; chunk at `maxBatchSize`; on `201` remove **every id in the submitted chunk** — accepted, rejected, and unmentioned alike — and log each rejection reason via Timber; map outcomes per the table in [research R-08](./research.md#r-08-what-does-the-worker-do-with-each-http-outcome). Map failures through the existing `Response.toAppError()` / `Throwable.toAppError()` so no raw HTTP escapes the repository.
- [X] T040 [US2] Create `SyncHymnViewsUseCase` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/domain/usecase/SyncHymnViewsUseCase.kt`: loop `syncOnce` while the queue is non-empty and the outcome is `Delivered` or `Discarded`; stop on `Deferred` and report it.
- [X] T041 [US2] Create `HymnViewSyncWorker` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/data/work/HymnViewSyncWorker.kt`: `@HiltWorker` + `@AssistedInject` in the style of `core/data/worker/BirthdayNotificationWorker.kt`. Run `SyncHymnViewsUseCase`; return `Result.retry()` on `Deferred` or on any unexpected throwable, `Result.success()` otherwise. Wrap the whole body so nothing can crash the process. `WORK_NAME = "hymn_view_sync"`.
- [X] T042 [US2] Create `HymnViewSyncScheduler` in `app/src/main/java/com/ipb/castelobranco/features/hymnal/data/work/HymnViewSyncScheduler.kt`: `OneTimeWorkRequestBuilder` with `NetworkType.CONNECTED`, `BackoffPolicy.EXPONENTIAL` at 30 s, enqueued via `enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)` so a burst of views cannot spawn a burst of jobs (FR-015).
- [X] T043 [US2] Call the scheduler from `RecordHymnViewUseCase` after a successful queue append, so a new view triggers delivery (FR-015).
- [X] T044 [US2] Enqueue the sync once in `MyApp.onCreate()` in `app/src/main/java/com/ipb/castelobranco/MyApp.kt`, beside `scheduleBirthdayNotifications()`. This is what bounds the known `KEEP` blind spot — an event recorded while a job was finishing is picked up at the next launch at the latest ([research R-07](./research.md#r-07-how-does-the-worker-avoid-duplicate-jobs-while-still-draining-a-growing-queue)).

**Checkpoint**: The feature is functionally complete on built-in defaults. Views are recorded, delivered, and reconciled; nothing surfaces to the member. **This is the full MVP.**

---

## Phase 5: User Story 3 - Collection behaves as the church configures it (Priority: P2)

**Goal**: The church can change the qualifying duration and batch size server-side and have devices honour it, while still working offline and on a fresh install.

**Independent Test**: Change `min_seconds_to_count` on the service, restart the app, confirm the timer uses the new value. Then go offline, restart, and confirm the last known value is still used.

### Tests for User Story 3

- [X] T045 [P] [US3] `HymnViewSettingsStoreTest` in `app/src/test/java/com/ipb/castelobranco/features/hymnal/data/local/HymnViewSettingsStoreTest.kt` — fetched values are persisted and read back; an unwritten store returns `DEFAULT` (30 / 50); out-of-range server values (`0`, negative, absurdly large) are clamped rather than propagated.
- [X] T046 [P] [US3] `RefreshHymnViewSettingsTest` in `app/src/test/java/com/ipb/castelobranco/features/hymnal/data/repository/RefreshHymnViewSettingsTest.kt` — a successful fetch updates the store; a network failure leaves the previous values intact and throws nothing; a fresh install with a failing fetch yields the defaults.

### Implementation for User Story 3

- [X] T047 [P] [US3] Add `HistorySettingsDto` to `app/src/main/java/com/ipb/castelobranco/features/hymnal/data/dto/HymnalHistoryDtos.kt` declaring **all six** service fields with defaults — the four unused ones (`collapse_window_minutes`, `max_past_days`, `future_tolerance_minutes`, `window_grace_minutes`) are parsed and discarded, which documents the contract and costs nothing.
- [X] T048 [P] [US3] Add `SETTINGS_PATH = "${ApiConstants.BASE_PATH}hymnal-history/settings/"` to `HymnalHistoryEndpoints.kt` and a `@GET` returning `Response<HistorySettingsDto>` to `HymnalHistoryApi.kt`. Read via the **auth-less** API — the endpoint is a public read and needs no token.
- [X] T049 [US3] Implement `refreshSettings` in `HymnViewHistoryRepositoryImpl`: fetch, map the two consumed fields into `HymnViewCollectionSettings`, persist via `HymnViewSettingsStore`. Swallow every failure (FR-025).
- [X] T050 [US3] Bind the settings refresh into the existing startup multibinding in `HymnalHistoryModule.kt`: `@Provides @IntoSet fun bindHistoryRefreshable(r: HymnViewHistoryRepository): Refreshable = Refreshable { r.refreshSettings() }`, matching `HymnalModule.kt:37`. `PreloadDataUseCase` already runs the set at startup and already tolerates failures.

**Checkpoint**: Collection is remotely tunable, and degrades cleanly to cached values and then to defaults.

---

## Phase 6: User Story 4 - Collection identifies a device, never a person (Priority: P2)

**Goal**: Prove the privacy commitment holds — a random, permissionless identifier that survives updates and resets on reinstall, and telemetry that carries nothing personal.

**Independent Test**: Fresh install → a UUID; update in place → the same UUID; reinstall → a different one; and the app's permission set is unchanged.

> **Note**: `DeviceIdProvider` is *built* in Phase 2 (T010, T012) because US1 and US2 cannot construct an event without it. This phase verifies its required properties and audits the feature's privacy posture. The tasks are real work, but they are assertions rather than construction.

### Tests for User Story 4

- [X] T051 [P] [US4] `DeviceIdProviderTest` in `app/src/test/java/com/ipb/castelobranco/core/data/local/DeviceIdProviderTest.kt` — two calls return the same value; the value parses as a UUID, is non-blank, and is ≤ 64 characters (FR-027); a new provider over the same DataStore returns the same value; concurrent first calls return one identical value, not two.
- [X] T052 [P] [US4] Add a case to `RecordHymnViewUseCaseTest` asserting the constructed event carries **only** the seven contract fields and no member name, email, id, or location — the anonymity claim the spec's no-opt-out decision rests on.

### Implementation / verification for User Story 4

- [X] T053 [US4] Audit `app/src/main/AndroidManifest.xml` against `git diff` — confirm this feature added no permission and no component (FR-028, SC-010).
- [X] T054 [US4] Grep the feature's source for `ANDROID_ID`, `Settings.Secure`, `Build.SERIAL`, `getDeviceId`, and advertising-id APIs; confirm zero hits. Confirm every Timber call in the feature logs reason codes and counts only — never event contents (`CLAUDE.md`: no PII in production logs).
- [X] T055 [US4] Run quickstart **V-5** on a device: read the device id, reinstall to confirm it changes, `adb install -r` to confirm it survives an in-place update.

**Checkpoint**: All four stories complete and independently verified.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [X] T056 Create `specs/hymnal/spec.md` — **it does not exist yet**. Per `CLAUDE.md` §7.2 and §7.4, document the current hymnal domain (list, detail, search, font size, offline snapshot, `GET api/hymnal/`) *and* this view-history addition. Commit it with the code, not separately.
- [X] T057 [P] Verify no line exceeds 120 characters and Kotlin `official` style holds across all new files; run the project's lint.
- [X] T058 Run the full suite: `./gradlew :app:testDebugUnitTest`. **Never** prefix with `clean`, `--rerun-tasks`, or `--no-daemon` — it destroys the KSP cache and turns ~6 seconds into 15+ minutes (`CLAUDE.md`).
- [X] T059 Run quickstart **V-1** through **V-4** on a device: a view is recorded; background time does not count; the offline queue survives a reboot and drains on reconnect; and several minutes of use with the backend unreachable produce no message, no error state, and no crash.
- [X] T060 Confirm **D-001** — that `GET api/hymnal/` now returns `id` — or explicitly accept for this release that the feature ships as a no-op. Check `files/snapshots/hymnal.json` on a device after a refresh. Until this lands, every view is skipped by design (FR-009) and only the unit suite is meaningful.
- [X] T061 Final read-through against the spec: FR-030 (no screen, no control) and FR-022 (no user-visible failure) hold in the finished diff. A new string resource, a new snackbar, or a new nav destination in this diff means something went wrong.

---

## Dependencies & Execution Order

### Phase dependencies

- **Phase 1 (Setup)**: no dependencies.
- **Phase 2 (Foundational)**: depends on Phase 1. **Blocks every story.**
- **Phase 3 (US1)**: depends on Phase 2.
- **Phase 4 (US2)**: depends on Phase 3 — it delivers what US1 records. This is a genuine, unavoidable ordering: there is nothing to upload until something is queued.
- **Phase 5 (US3)** and **Phase 6 (US4)**: depend on Phase 2 only. Both can run in parallel with Phase 4.
- **Phase 7 (Polish)**: depends on everything intended for the release.

### Story dependencies

- **US1 (P1)**: needs Foundational only. First shippable increment.
- **US2 (P1)**: needs US1. The two together are the MVP.
- **US3 (P2)**: independent of US1/US2 in construction — it swaps the source of two numbers US1 and US2 already read from a store with defaults.
- **US4 (P2)**: independent. Its implementation sits in Foundational by necessity (see the Phase 2 note); this phase is verification.

### Within a story

Tests first and failing → domain types → storage → repository → use case → ViewModel/worker → wiring.

### Parallel opportunities

- T002, T003 (Setup).
- T007, T008, T009, T010, T011, T013 (Foundational — distinct files). T012 waits on T010 and T011; T014 waits on T008.
- T016–T019 (all US1 tests, distinct files).
- T020, T021 in parallel; T022 waits on T021; T023 waits on T021; T024 waits on T022 and T023.
- T030–T033 (all US2 tests). T034, T035 in parallel; T036 waits on T034 and T035.
- T045, T046 (US3 tests); T047, T048 in parallel.
- T051, T052 (US4 tests).
- Whole phases: US3 and US4 can proceed alongside US2 once Phase 2 is done.

---

## Parallel Example: User Story 1

```bash
# All four US1 test files at once — distinct files, no shared state:
Task: "HymnViewTimerTest in app/src/test/.../domain/timer/HymnViewTimerTest.kt"
Task: "HymnViewQueueStoreTest in app/src/test/.../data/local/HymnViewQueueStoreTest.kt"
Task: "RecordHymnViewUseCaseTest in app/src/test/.../domain/usecase/RecordHymnViewUseCaseTest.kt"
Task: "HymnViewTrackingViewModelTest in app/src/test/.../presentation/viewmodel/HymnViewTrackingViewModelTest.kt"

# Then the two independent implementation files:
Task: "HymnViewTimer in app/src/main/.../domain/timer/HymnViewTimer.kt"
Task: "QueuedHymnViewEvent in app/src/main/.../data/local/QueuedHymnViewEvent.kt"
```

---

## Implementation Strategy

### MVP

The MVP is **US1 + US2** — recording without delivery collects data nobody can see, and delivery without recording has nothing to send. Neither half is separately useful to the church, though US1 alone is a legitimate, testable checkpoint.

1. Phase 1 → Phase 2 → Phase 3 (US1). **Stop and validate**: quickstart V-1 and V-2.
2. Phase 4 (US2). **Stop and validate**: quickstart V-3 and V-4.
3. Ship. The feature runs on the 30 s / 50 defaults and is fully correct.

### Incremental delivery

4. Phase 5 (US3) — remote tuning. Ship.
5. Phase 6 (US4) — privacy verification. Can precede US3 if the privacy audit is wanted before any release.
6. Phase 7 — polish, domain spec, and the D-001 decision.

### Parallel team strategy

Two developers after Phase 2: one takes US1 then US2 (the critical path), the other takes US3 and US4 in parallel plus T056 (`specs/hymnal/spec.md`), which needs no code.

---

## Notes

- **D-001 gates end-to-end value, not construction.** Every task here is implementable and unit-testable before the backend exposes `id`. Until it does, T060 is the honest checkpoint: the feature is correct and silent.
- Two failure modes in this feature are invisible when they go wrong — an eighth wire field (every event rejected) and an expired token on the authed client (member silently signed out). T030 and T032 exist solely to catch them; do not skip or weaken them.
- Commit per task or per logical group. Any checkpoint is a safe stopping point.
- `[P]` means distinct files with no incomplete dependency.
