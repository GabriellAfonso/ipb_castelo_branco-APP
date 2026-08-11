# Phase 0 Research: Hymnal View History Collection

**Feature**: `002-hymnal-view-history` | **Date**: 2026-08-08 | **Spec**: [spec.md](./spec.md)

This document resolves the open technical questions before design. Every decision below is grounded in code that already exists in this repository — file paths are given so the plan can be checked against reality rather than assumed.

---

## R-01: Where does the view timer live?

**Decision**: A pure, Android-free state holder `HymnViewTimer` in `features/hymnal/domain/`, driven by a dedicated `HymnViewTrackingViewModel` scoped to the *detail destination's* `NavBackStackEntry`.

**Rationale**:

The spec requires the count to reset when the member leaves and returns to a hymn (FR-004), and to survive rotation. Scoping the tracking ViewModel to the detail `NavBackStackEntry` gives exactly that for free: a new navigation to `hymn_detail/{id}` creates a new entry and therefore a new ViewModel, while a configuration change reuses the existing one.

Note this is a deliberate exception to Pitfall #1 in `CLAUDE.md`, which says to use `hiltViewModel(graphEntry)` inside existing graphs. That rule exists so `HymnalViewModel` — which holds the shared hymn list and search query — is shared across the graph. Here the requirement is the opposite: per-visit state that *must not* be shared. So `HymnalViewModel` stays graph-scoped via `hiltViewModel(graphEntry)` exactly as today, and the new tracking ViewModel uses a bare `hiltViewModel()` in the detail `composable`, whose default owner is that destination's back stack entry. Both ViewModels coexist in the same composable.

The timer logic itself is separated out into a plain class with an injected monotonic clock so it can be unit-tested with virtual time, satisfying the mandated coverage of "the timer pausing and resuming across backgrounding".

**Alternatives considered**:

- *Track inside `HymnalViewModel`.* Rejected: it is graph-scoped (`app/src/main/java/com/ipb/castelobranco/features/hymnal/presentation/navigation/HymnalNavGraph.kt:45`), so it survives list↔detail navigation and would need manual per-hymn reset bookkeeping — reimplementing by hand what the back stack already gives.
- *`DisposableEffect` + a raw coroutine inside the composable.* Rejected: violates the project's "composables are dumb" rule and would be untestable without instrumentation tests, which this project does not use.
- *A `LifecycleObserver` registered from the composable.* Partially adopted — see R-02 — but only as the *signal source*; the state lives in the ViewModel.

---

## R-02: How is "foreground" detected, and how is elapsed time measured?

**Decision**: `LifecycleResumeEffect` in the detail composable reports `onVisible()` / `onHidden()` to the tracking ViewModel. Elapsed time is measured with `SystemClock.elapsedRealtime()` behind an injectable `MonotonicClock` interface. The threshold fires via a single `delay(remaining)` coroutine, not a per-second tick.

**Rationale**:

`RESUMED` is precisely the state the spec means by "on screen in the foreground": it is left when the app is backgrounded, when the screen turns off, and when another activity or dialog covers the screen. `STARTED` would keep counting behind the lock screen on some devices.

`elapsedRealtime()` is monotonic and unaffected by the user changing the wall clock — important because the accumulated duration must never come out negative (FR-008). Wall-clock time is still needed for `viewed_at`, but only at the single instant the event fires, where `OffsetDateTime.now()` gives both the instant and the device's UTC offset in one call (FR-007).

A single `delay(remaining)` rather than a ticking loop means zero work while the hymn sits open, which matters for FR-031 (no degradation of reading). On pause the job is cancelled and the elapsed slice is folded into the accumulator; on resume a fresh job is launched with the recomputed remainder. Because the job runs in `viewModelScope` on an injected dispatcher, `runTest`'s virtual time drives it deterministically in unit tests.

**Alternatives considered**:

- *`ProcessLifecycleOwner`.* Rejected: it observes the whole process, so it cannot tell that the member navigated from the hymn to another in-app screen.
- *`System.currentTimeMillis()` deltas.* Rejected: a clock adjustment mid-visit could produce a negative or absurd duration.
- *A 1-second ticking coroutine.* Rejected: needless wakeups for a threshold that only needs to be observed once.

---

## R-03: How does the hymnal feature learn whether the user is signed in, without importing another feature?

**Decision**: Add a core-level interface `AuthStatusProvider` in `core/domain/auth/`, implemented in `core/data/auth/` by delegating to the existing `AuthSession`. The hymnal feature depends only on the interface.

**Rationale**:

`AuthSession` lives at `app/src/main/java/com/ipb/castelobranco/features/auth/data/local/AuthSession.kt` — inside the *auth feature*. `CLAUDE.md` forbids features importing each other, so `features/hymnal` cannot reference it directly. Core, however, already depends on the auth feature (`core/network/AuthInterceptor.kt:3`, `core/presentation/viewmodel/CoreViewModel.kt`), so placing the adapter in core introduces no new coupling direction and makes the abstraction reusable by any future feature with the same need.

**Alternatives considered**:

- *Move `AuthSession` into core.* Cleaner in the abstract, but it touches the auth feature, the interceptor, the authenticator, and `CoreViewModel` — far outside this feature's blast radius. Worth doing one day; not here.
- *Inject `TokenStorage` directly.* Same feature-import violation, and it leaks token representation into telemetry code.

---

## R-04: How is the authenticated client used without risking a silent logout?

**Decision**: Select the client on `hasValidAccessToken()` — not on `isLoggedIn()`. Use `@AuthedRetrofit` only when a stored access token exists *and* its JWT `exp` is still in the future; otherwise use `@AuthLessRetrofit`.

**Rationale**:

This is the sharpest risk in the feature, and the spec's FR-021 names it. The mechanism:

`AuthInterceptor` attaches whatever access token is stored, valid or not (`core/network/AuthInterceptor.kt:22`). `TokenAuthenticator` reacts to a 401 by attempting a refresh, and **clears the token store when that refresh returns 401 or 400** (`core/network/TokenAuthenticator.kt:66-69`) — which signs the member out. So a background telemetry upload firing on a stale token could log a member out while the app is not even open.

`AuthSession.isLoggedIn()` only checks that the token strings are non-blank — it would happily select the authed client with a long-expired token. `hasValidAccessToken()` (`AuthSession.kt:18`) decodes the JWT and compares `exp` against now, which is exactly the check needed. The ingest endpoint is `AllowAny`, so falling back to the anonymous client costs nothing but attribution.

A residual window remains — a token that expires between the check and the request — but that is the ordinary race every authenticated call in the app already runs, and in that case the refresh is legitimate.

**Alternatives considered**:

- *A third OkHttp client with the interceptor but no authenticator.* The most airtight option, and worth revisiting if the residual race ever bites. Rejected for now because the spec explicitly directs reuse of the two existing qualifiers, and adding a client duplicates timeout/logging configuration.
- *Always anonymous.* Rejected: loses the per-member attribution the church asked for.

---

## R-05: How is a mutable queue built on a whole-blob snapshot store?

**Decision**: A `HymnViewQueueStore` that reads the whole list, mutates it in memory, and writes the whole list back — every operation guarded by a single `Mutex` held for the entire read-modify-write. Backed by the existing `SnapshotStorage` (`JsonSnapshotStorage`) under key `hymn_view_queue`.

**Rationale**:

`JsonSnapshotStorage` (`core/data/local/JsonSnapshotStorage.kt`) is a plain "serialize this object to one JSON file" store. It has no notion of partial update, so append and remove must be expressed as full rewrites. That is entirely acceptable at this scale: the queue is capped at 2000 small records, roughly 400 KB of JSON worst case, rewritten only when a view is recorded (at most once every 30 seconds of reading) or when a sync completes.

The `Mutex` is what FR-011 demands. Without it, two coroutines could each read the same list, each append their own event, and the second write would silently discard the first. Holding the lock across the *whole* read-modify-write — not just the write — is the part that matters.

`SnapshotCacheFactory`/`LocalSnapshotCache` are deliberately **not** reused: they model a replace-whole-blob remote cache with ETag support, and expose a `SnapshotState` flow the queue has no use for. The queue talks to `SnapshotStorage` directly.

**Alternatives considered**:

- *Room.* Explicitly excluded by the spec, and it would be the only Room usage in the app.
- *One file per event.* Avoids rewrites but multiplies file handles and makes ordering and capping awkward.
- *`DataStore<Preferences>`.* Not suited to a growing list; the same whole-blob rewrite with worse ergonomics.

---

## R-06: How is the queue capped and ordered?

**Decision**: Store as a `List<QueuedHymnViewEvent>` in insertion order, oldest first. On append, if size would exceed 2000, drop from the front until it fits. Removal is by `clientEventId` set membership.

**Rationale**: Insertion order is the natural arrival order and needs no timestamp sort. `takeLast(MAX - 1) + newEvent` is a one-line, obviously-correct implementation of "drop the oldest", and its behaviour under the mandated 2500-event test is trivially predictable.

---

## R-07: How does the worker avoid duplicate jobs while still draining a growing queue?

**Decision**: `enqueueUniqueWork("hymn_view_sync", ExistingWorkPolicy.KEEP, request)`, with the worker draining in a loop until the queue is empty, and re-enqueueing itself if anything remains. Also enqueued once on app start.

**Rationale**:

`KEEP` is what FR-015 asks for — a second enqueue while one is pending or running is a no-op, so a burst of recorded views cannot spawn a burst of jobs. Its known gap: an event recorded in the instant between the worker reading the queue and the worker finishing is not covered by that run, and the `KEEP` enqueue it triggered was absorbed by the still-running job.

Three cheap mitigations close it: the worker re-reads the queue after each chunk and keeps going while non-empty; if it exits with a non-empty queue (because a chunk failed) it returns `Result.retry()`; and `MyApp.onCreate` enqueues once so a leftover event is picked up on the next launch at the latest. For telemetry whose value is aggregate, a worst case of "delivered on next app open" is entirely acceptable.

`APPEND_OR_REPLACE` was rejected: it builds a chain, which is both duplicate work and harder to reason about on failure.

Backoff: `BackoffPolicy.EXPONENTIAL` with a 30-second initial interval, well above WorkManager's 10-second floor and unobtrusive for background telemetry.

---

## R-08: What does the worker do with each HTTP outcome?

**Decision**:

| Outcome | Queue | Worker result |
|---------|-------|---------------|
| `201` | Remove every id submitted in that chunk — accepted, rejected, *and* unmentioned | continue to next chunk |
| `400` `VALIDATION_ERROR` | Remove every id in the chunk | continue (the chunk is unfixable; retrying loops forever) |
| `429` | Keep everything | `Result.retry()` |
| `5xx` | Keep everything | `Result.retry()` |
| `IOException` | Keep everything | `Result.retry()` |
| Any other exception | Keep everything | `Result.retry()` |

**Rationale**:

Removing unmentioned ids (FR-017) is the subtle one. The service silently drops an event whose `client_event_id` is not a parseable UUID, so it appears in neither response list. The app always generates a real `UUID.randomUUID()`, so this should never happen — but "should never happen" is exactly how queues come to leak forever. Reconciling against *the set submitted* rather than the set answered makes leakage structurally impossible.

`400 VALIDATION_ERROR` normally means the chunk exceeded `max_batch_size`, which chunking already prevents. If it happens anyway the payload is malformed in some way the app cannot fix by waiting, so discarding it is the only exit that does not retry forever — the same principle behind FR-018's "no event retries forever".

`Result.retry()` rather than `Result.failure()` everywhere else: `failure()` would abandon the queued views permanently.

Nothing in this table can surface to the user (FR-022): the worker returns `retry`/`success` and logs through Timber, and no UI observes it.

---

## R-09: Where do collection settings come from and how are they cached?

**Decision**: Fetch `GET hymnal-history/settings/` via the existing `Refreshable` startup multibinding. Persist `minSecondsToCount` and `maxBatchSize` to the `@SettingsPrefs` DataStore. Read with a three-level fallback: cached → built-in default.

**Rationale**:

`PreloadDataUseCase` (`core/domain/usecase/PreloadDataUseCase.kt`) already collects `Set<Refreshable>` and runs them at startup; `HymnalModule.kt:37` shows the exact binding idiom. Reusing it means no new startup hook, and a failure there is already handled without user-visible effect.

DataStore rather than the snapshot store: two scalars, read on every hymn open, with no list semantics. `ThemePreferences` is the established pattern for exactly this.

Defaults are `MIN_SECONDS_TO_COUNT = 30` (the service's own default, per the contract) and `MAX_BATCH_SIZE = 50` — the spec's "conservative" value, chosen to sit comfortably below any plausible server limit at the cost of a few extra requests in the rare fresh-install-with-backlog case.

**Alternatives considered**: fetching on entry to the hymnal instead of at startup — rejected as it delays the first hymn open for no benefit, since the value is only needed 30 seconds later.

---

## R-10: How is the "unknown fields are forbidden" contract guaranteed?

**Decision**: Two distinct types. `QueuedHymnViewEvent` for persistence, `HymnViewEventDto` for the wire — the latter declaring exactly the seven contract fields and nothing else.

**Rationale**:

The shared `Json` instance sets `encodeDefaults = true` (`core/di/SerializationModule.kt`), so `appVersion` and `platform` are always emitted rather than omitted — which is what we want, and which also means any property added to the DTO is *guaranteed* to reach the wire and break parsing server-side.

Keeping the wire DTO separate means the day someone adds a local-only field to the queued record — a retry counter, an enqueue timestamp — the compiler forces them through the mapper, and the wire shape is untouched. Merging the two types would make that a silent, feature-breaking change. The two currently carry identical fields; that is fine, and the mapper is four lines.

`ignoreUnknownKeys = true` on the same `Json` instance covers the response side, so a future field added to the ingest response cannot crash the worker.

---

## R-11: How does `id` reach the domain, and what happens without it?

**Decision**: `HymnDto.id: Int? = null` and `Hymn.id: Int?`. `RecordHymnViewUseCase` returns early when `id` is null, logging at debug level.

**Rationale**:

Nullable with a default is what makes this safe against the cached snapshot problem. `ignoreUnknownKeys = true` handles *extra* keys; a *missing* key on a non-nullable field without a default throws `MissingFieldException` — which would make every existing installation fail to deserialize its cached hymnal and lose the offline hymnal entirely on upgrade. Nullable-with-default degrades to `null` instead, exactly the "skip silently, next refresh fixes it" behaviour FR-009 requires.

`number` stays the navigation key. Changing `HymnalRoutes.detailRoute()` to use `id` would break existing deep links and gain nothing — the tracking ViewModel resolves the hymn by `number` and reads `id` off it.

**Blocking**: this is D-001. Until the backend adds `id` to the hymnal payload, every view is skipped and the feature is a no-op. Everything else is buildable and unit-testable in the meantime.

---

## R-12: Device identifier

**Decision**: `UUID.randomUUID().toString()` (36 chars), stored in the `@SettingsPrefs` DataStore under `device_id`, generated lazily on first read inside a `dataStore.edit {}` block.

**Rationale**: Satisfies FR-026 to FR-029 with no permission and no platform identifier. Generating inside `edit {}` makes first-read atomic, so two concurrent first calls cannot mint two different ids. 36 characters is comfortably inside the 64-character contract limit. DataStore lives in the app's private data directory, so it is cleared on uninstall — which is the specified and acceptable reset behaviour.

Placed in `core/data/local/DeviceIdProvider.kt` per the spec's directive that it is not hymnal-specific.

---

## Resolved unknowns summary

| # | Question | Resolution |
|---|----------|-----------|
| R-01 | Timer ownership | Pure timer class + destination-scoped tracking ViewModel |
| R-02 | Foreground detection | `LifecycleResumeEffect` + `elapsedRealtime()` + single `delay` |
| R-03 | Auth state without feature coupling | `AuthStatusProvider` interface in core |
| R-04 | Client selection | `hasValidAccessToken()`, not `isLoggedIn()` |
| R-05 | Mutable queue on blob storage | Mutex-guarded whole-list read-modify-write |
| R-06 | Cap and ordering | Insertion-ordered list, drop from front at 2000 |
| R-07 | Duplicate jobs | Unique work `KEEP` + drain loop + startup enqueue |
| R-08 | HTTP outcome handling | Table above; retry never loops forever |
| R-09 | Settings source | `Refreshable` multibinding + DataStore, defaults 30 / 50 |
| R-10 | Forbidden unknown fields | Separate persistence and wire types |
| R-11 | Hymn `id` propagation | Nullable with default; skip when absent |
| R-12 | Device identifier | Random UUID in DataStore |

No `NEEDS CLARIFICATION` items remain.
