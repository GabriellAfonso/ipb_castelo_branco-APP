# Quickstart & Validation: Hymnal View History Collection

**Feature**: `002-hymnal-view-history` | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

How to prove this feature works. The feature is invisible, so "it looks fine" proves nothing — every check below is either an assertion in a unit test or an inspection of a file on the device.

---

## Prerequisites

- Android Studio with the project's SDK at `/home/node/.local/android-sdk` (`local.properties`)
- A device or emulator with `adb`
- Backend reachable at `https://gabrielafonso.com.br/ipbcb/`
- **D-001**: `GET api/hymnal/` returning `id` per [contracts/hymnal-history-api.md §3](./contracts/hymnal-history-api.md#3-prerequisite-change-to-get-apihymnal--d-001-blocking). Without it every view is skipped and only the unit suite is meaningful.

---

## Running the tests

```bash
./gradlew :app:testDebugUnitTest --tests "com.ipb.castelobranco.features.hymnal.*"
./gradlew :app:testDebugUnitTest --tests "com.ipb.castelobranco.core.data.local.DeviceIdProviderTest"
```

Full suite:

```bash
./gradlew :app:testDebugUnitTest
```

> **Never** prefix with `clean`, `--rerun-tasks`, or `--no-daemon` — it destroys the KSP cache and turns a ~6-second run into 15+ minutes (`CLAUDE.md`). Use `clean` only for build errors you cannot otherwise explain.

---

## Test plan

The five scenarios the spec names as mandatory are marked **[REQUIRED]**. Each is an assertion, not an observation.

### `HymnViewTimerTest`

| # | Scenario | Assertion |
|---|---|---|
| 1 | **[REQUIRED]** Pause and resume across backgrounding | 20 s foreground → background 10 min → foreground 15 s (threshold 30) fires once, at the 30 s mark of *foreground* time |
| 2 | **[REQUIRED]** Fires exactly once per visit | Held 10× the threshold → exactly one emission |
| 3 | Below threshold | 29 s then hidden → zero emissions |
| 4 | Never negative | Duration reported is `>= 0` and equals accumulated foreground time |
| 5 | Backgrounded before threshold, never returns | Zero emissions |

Driven by `runTest` virtual time with a fake `MonotonicClock`; no real waiting.

### `HymnViewQueueStoreTest`

| # | Scenario | Assertion |
|---|---|---|
| 1 | **[REQUIRED]** Survives restart | Append 3, construct a new store over the same fake `SnapshotStorage`, read → the same 3, same order |
| 2 | **[REQUIRED]** Cap drops the oldest | Append 2500 → size 2000, and the first 500 ids are gone while the last 2000 remain in order |
| 3 | Concurrent appends | 100 parallel appends via `async` → 100 distinct events stored, none lost |
| 4 | Remove by id set | Remove 2 of 5 → the other 3 remain, order preserved |
| 5 | Empty store | Reading a store that was never written → empty list, no throw |
| 6 | Corrupt JSON | Malformed file → empty list, no throw (a corrupt queue must not brick the app) |

### `RecordHymnViewUseCaseTest`

| # | Scenario | Assertion |
|---|---|---|
| 1 | Happy path | Event queued with a parseable UUID, the device id, and a `viewedAt` carrying an offset |
| 2 | Hymn without `id` (FR-009) | Nothing queued, nothing thrown, no scheduler call |
| 3 | Queue write fails | Swallowed; no exception escapes to the caller |

### `SyncHymnViewsUseCaseTest` / `HymnViewHistoryRepositoryImplTest`

| # | Scenario | Assertion |
|---|---|---|
| 1 | **[REQUIRED]** Deletes exactly what was submitted | 3 submitted, service accepts 1 and rejects 1 and ignores 1 → all 3 removed; a 4th, never submitted, remains |
| 2 | Rejection reasons logged | Each `rejected` entry produces a log line naming its reason |
| 3 | Chunking | 120 queued with `maxBatchSize` 50 → 3 requests of 50/50/20 |
| 4 | `429` | Nothing removed; `SyncOutcome.Deferred` |
| 5 | `5xx` | Nothing removed; `Deferred` |
| 6 | `IOException` | Nothing removed; `Deferred` |
| 7 | `400 VALIDATION_ERROR` | The chunk is removed; sync continues |
| 8 | Valid token → authed client | The authed API is called |
| 9 | **Expired token → anonymous client** | The auth-less API is called — the guard against silent logout ([research R-04](./research.md#r-04-how-is-the-authenticated-client-used-without-risking-a-silent-logout)) |
| 10 | Empty queue | No request issued |

### `HymnViewTrackingViewModelTest`

| # | Scenario | Assertion |
|---|---|---|
| 1 | Threshold → record | `RecordHymnViewUseCase` invoked once with the right hymn |
| 2 | Fresh instance, fresh count | A new ViewModel starts from zero accumulated time |
| 3 | Settings failure | Falls back to the 30 s default and still records |

### `DeviceIdProviderTest`

| # | Scenario | Assertion |
|---|---|---|
| 1 | Generated once | Two calls return the same value |
| 2 | Shape | Parses as a UUID; non-blank; `length <= 64` |
| 3 | Persisted | A new provider over the same DataStore returns the same value |

### Serialization tests

| # | Scenario | Assertion |
|---|---|---|
| 1 | **Exactly seven wire keys** | `Json.encodeToJsonElement(dto).jsonObject.keys` equals exactly the seven contract names — the guard against `invalid_event` |
| 2 | Snapshot without `id` | A pre-feature `hymnal.json` payload deserializes with `id == null` and does **not** throw `MissingFieldException` |
| 3 | Response with an extra field | Parses without throwing (`ignoreUnknownKeys`) |
| 4 | `viewedAt` round-trip | `OffsetDateTime` → string → `OffsetDateTime` preserves instant and offset |

---

## Manual validation on a device

Unit tests cannot prove WorkManager scheduling or real lifecycle behaviour. These five checks close that gap.

### V-1 — A view is recorded

1. Install a debug build, open **Hinário**, open any hymn.
2. Leave it on screen 35 seconds.
3. Read the queue:

```bash
adb shell run-as com.ipb.castelobranco cat files/snapshots/hymn_view_queue.json
```

**Expect**: one event, with a UUID `clientEventId`, the correct `hymnId`, a `viewedAt` ending in an offset such as `-03:00`, and `durationSeconds` around 30.

**If empty**: check `id` is present in `files/snapshots/hymnal.json`. If it is null, D-001 has not shipped and everything is behaving correctly (FR-009).

### V-2 — Foreground-only counting

1. Open a hymn, wait 15 seconds, press Home.
2. Wait 2 minutes. Return to the app.
3. After roughly 15 more seconds on the hymn, check the queue.

**Expect**: exactly one event, `durationSeconds` around 30 — not 135. The background gap must not count.

### V-3 — Offline queue, then delivery

1. Enable airplane mode. View three different hymns past the threshold.
2. Confirm three events in the queue file.
3. Force-stop the app (`adb shell am force-stop com.ipb.castelobranco`), then reboot the device.
4. Confirm the three events are still there.
5. Disable airplane mode and wait for connectivity.

**Expect**: the queue file drains to `[]` without reopening the app. Confirm with:

```bash
adb shell dumpsys jobscheduler | grep -i castelobranco
```

### V-4 — Invisibility

Repeat V-3 with the backend unreachable (airplane mode on, or a bad base URL in a scratch build), and use the app normally for several minutes.

**Expect**: no snackbar, no dialog, no error state anywhere, no crash. Nothing in the UI acknowledges the feature exists.

### V-5 — Device id

```bash
adb shell run-as com.ipb.castelobranco cat files/datastore/settings_prefs.preferences_pb | strings | grep -A1 device_id
```

**Expect**: a UUID. Reinstall the app → a different UUID. Update the app in place (`adb install -r`) → the same UUID.

---

## Verifying the round trip on the backend

With `id` shipped and a device reporting, an administrator can confirm collection end to end:

- `GET api/hymnal-history/occurrences/` — recent views, including the hymn and device
- `GET api/hymnal-history/top-hymns/` — the aggregate the church actually wanted

Both are `IsAdminUser` and have **no app UI** — that is a separate feature. Check them with `curl` or the Django admin.

---

## Definition of done

- [ ] `./gradlew :app:testDebugUnitTest` passes, including all five **[REQUIRED]** scenarios
- [ ] V-1 through V-5 pass on a real device
- [ ] No new lint warning; no line over 120 characters
- [ ] No new permission in `AndroidManifest.xml`
- [ ] No user-visible string, screen, or control added
- [ ] `specs/hymnal/spec.md` created and describing this addition, committed with the code (`CLAUDE.md` §7.2)
- [ ] D-001 confirmed shipped, or its absence explicitly accepted for this release
