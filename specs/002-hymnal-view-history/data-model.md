# Phase 1 Data Model: Hymnal View History Collection

**Feature**: `002-hymnal-view-history` | **Date**: 2026-08-08 | **Spec**: [spec.md](./spec.md)

Three layers of types, deliberately kept separate: domain models the rest of the app reasons about, persistence records written to disk, and wire DTOs that must match the service byte for byte.

---

## 1. Domain models

`features/hymnal/domain/model/HymnViewHistory.kt`

### HymnViewEvent

One qualifying view. Created by `RecordHymnViewUseCase` at the moment the threshold is crossed.

| Field | Type | Rule |
|-------|------|------|
| `clientEventId` | `String` | `UUID.randomUUID().toString()`. Unique per event; the reconciliation key. |
| `hymnId` | `Int` | The service's hymn identifier. Non-null here — an event cannot exist without it (FR-009 filters earlier). |
| `deviceId` | `String` | Non-blank, ≤ 64 chars. |
| `viewedAt` | `OffsetDateTime` | The instant the threshold was crossed, carrying the device's UTC offset. Never naive (FR-007). |
| `durationSeconds` | `Long` | Accumulated foreground seconds at that instant. `>= 0` (FR-008). |
| `appVersion` | `String` | `BuildConfig.VERSION_NAME`. |
| `platform` | `String` | Constant `"android"`. |

### HymnViewCollectionSettings

The two service-controlled values the app consumes.

| Field | Type | Default | Rule |
|-------|------|---------|------|
| `minSecondsToCount` | `Int` | `30` | `> 0`; values `<= 0` from the service are ignored in favour of the default. |
| `maxBatchSize` | `Int` | `50` | `> 0`; clamped to `1..500` defensively. |

Companion holds `DEFAULT` for the fresh-install-offline case (FR-024).

### RejectionReason

Sealed representation of the service's stable reason codes, so the four known values are exhaustive at the call site and anything new is still loggable.

```
UnknownHymn | ViewedAtInFuture | ViewedAtTooOld | InvalidEvent | Unrecognised(raw: String)
```

Parsed from `unknown_hymn`, `viewed_at_in_future`, `viewed_at_too_old`, `invalid_event`.

### SyncOutcome

What one submission attempt produced, returned by the repository to the worker.

```
Delivered(removedIds: Set<String>)   — the chunk was reconciled; those ids are gone
Discarded(removedIds: Set<String>)   — unfixable chunk (400); ids dropped, keep going
Deferred(error: AppError)            — nothing removed; retry later
```

`Deferred` covers throttling, server errors, and connectivity failures alike — the worker treats all three identically, so distinguishing them in the type would be noise.

---

## 2. Persistence records

`features/hymnal/data/local/`

### QueuedHymnViewEvent

`@Serializable`. The on-disk form of `HymnViewEvent`. Currently field-identical, but a distinct type on purpose — see [research R-10](./research.md#r-10-how-is-the-unknown-fields-are-forbidden-contract-guaranteed). Any local-only field added here must not reach the wire, and the mapper is the enforcement point.

`viewedAt` is stored as an ISO-8601 offset `String` rather than a temporal type, because `kotlinx.serialization` has no built-in `OffsetDateTime` serializer and a string round-trips losslessly through `OffsetDateTime.parse`/`toString`.

| Field | Type |
|-------|------|
| `clientEventId` | `String` |
| `hymnId` | `Int` |
| `deviceId` | `String` |
| `viewedAt` | `String` (ISO-8601 with offset) |
| `durationSeconds` | `Long` |
| `appVersion` | `String` |
| `platform` | `String` |

### The queue file

- Storage: `SnapshotStorage` (the existing `JsonSnapshotStorage`), key `hymn_view_queue` → `filesDir/snapshots/hymn_view_queue.json`.
- Shape: a JSON array of `QueuedHymnViewEvent`, **oldest first**.
- Invariants:
  - Size ≤ `MAX_QUEUE_SIZE = 2000` (FR-012).
  - `clientEventId` unique within the file.
  - Every read-modify-write happens under one `Mutex` (FR-011).
- Lifetime: survives restart and update; removed on uninstall. Not covered by `clearAll()` on logout — telemetry is not user data in the session sense, and dropping it on logout would lose views the service would have accepted anonymously.

### Settings and device id in DataStore

Both in the existing `@SettingsPrefs` store (`settings_prefs`), alongside theme and font size.

| Key | Type | Owner |
|-----|------|-------|
| `hymn_history_min_seconds` | `Int` | `HymnViewSettingsStore` (hymnal) |
| `hymn_history_max_batch` | `Int` | `HymnViewSettingsStore` (hymnal) |
| `device_id` | `String` | `DeviceIdProvider` (core) |

Absent keys mean "never fetched" and fall through to defaults.

---

## 3. Wire DTOs

`features/hymnal/data/dto/HymnalHistoryDtos.kt`. These mirror the service exactly; see [contracts/hymnal-history-api.md](./contracts/hymnal-history-api.md).

### HymnViewEventDto — exactly seven fields, no more

```
client_event_id, hymn_id, device_id, viewed_at, duration_seconds, app_version, platform
```

Any eighth property makes the service reject the event as `invalid_event`. This type is the single place that constraint is expressed.

### IngestRequestDto

`{ "events": [HymnViewEventDto, ...] }` — at most `maxBatchSize` entries.

### IngestResponseDto

`{ "accepted": [String], "rejected": [RejectedEventDto] }`

### RejectedEventDto

`{ "client_event_id": String, "reason": String }`

### HistorySettingsDto

All six service fields are declared with defaults so a payload change cannot break parsing; only `min_seconds_to_count` and `max_batch_size` are mapped into the domain. The other four (`collapse_window_minutes`, `max_past_days`, `future_tolerance_minutes`, `window_grace_minutes`) are parsed and discarded — declaring them documents the contract and costs nothing.

---

## 4. Changes to existing types

### HymnDto — `features/hymnal/data/dto/HymnalDtos.kt`

```diff
 @Serializable
 data class HymnDto(
+    val id: Int? = null,
     val number: String,
     val title: String,
     val lyrics: List<HymnLyricDto>
 )
```

**Nullable with a default is load-bearing.** Every existing installation has a cached `hymnal.json` written before this field existed. A non-null field without a default would throw `MissingFieldException` on that cache, and the offline hymnal would break on upgrade for every user. See [research R-11](./research.md#r-11-how-does-id-reach-the-domain-and-what-happens-without-it).

### Hymn — `features/hymnal/domain/model/Hymnal.kt`

```diff
 data class Hymn(
+    val id: Int?,
     val number: String,
     val title: String,
     val lyrics: List<HymnLyric>
 )
```

`number` remains the navigation and display identifier; `HymnalRoutes.detailRoute()` is unchanged. `id` is carried solely for reporting.

### HymnMapper — `features/hymnal/data/mapper/HymnMapper.kt`

Passes `id` through. Sort order (by numeric `number`) is untouched.

---

## 5. State transitions

An event's life:

```
        threshold crossed
              │
              ▼
     ┌──────────────────┐   hymn.id == null
     │  not created     │◄────────────────────  (FR-009: skipped silently)
     └──────────────────┘
              │ hymn.id != null
              ▼
     ┌──────────────────┐
     │     QUEUED       │──── queue at 2000, this is the oldest ──► DROPPED
     └──────────────────┘
              │ worker submits its chunk
              ▼
     ┌──────────────────┐
     │    SUBMITTED     │
     └──────────────────┘
       │        │       │
   201 │    400 │       │ 429 / 5xx / IO
       ▼        ▼       ▼
   REMOVED  REMOVED   QUEUED (unchanged, retry with backoff)
```

There is no `SENDING` flag persisted on individual records. A crash mid-submission leaves the whole chunk queued and it is simply submitted again; the service deduplicates on `client_event_id` and answers `accepted`, so at-least-once delivery is safe by construction. This is why no local "in flight" bookkeeping is needed — a deliberate simplification the service's idempotency pays for.

---

## 6. Validation rules and their sources

| Rule | Enforced where | Requirement |
|------|---------------|-------------|
| `clientEventId` is a real UUID | `RecordHymnViewUseCase` (only creation site) | FR-006 |
| `viewedAt` carries an offset | `OffsetDateTime` type + ISO-8601 round-trip | FR-007 |
| `durationSeconds >= 0` | Monotonic clock; accumulator never decreases | FR-008 |
| `deviceId` non-blank, ≤ 64 | `DeviceIdProvider` (UUID is 36) | FR-027 |
| Queue ≤ 2000, oldest dropped | `HymnViewQueueStore.append` | FR-012 |
| Chunk ≤ `maxBatchSize` | `SyncHymnViewsUseCase` chunking | FR-016 |
| Exactly seven wire fields | `HymnViewEventDto` declaration | External contract |
| One event per visit | `HymnViewTimer` latches after firing | FR-003 |
