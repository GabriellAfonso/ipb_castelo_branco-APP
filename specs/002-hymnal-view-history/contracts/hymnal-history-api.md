# Contract: Hymnal View History API (client side)

**Feature**: `002-hymnal-view-history` | **Base URL**: `https://gabrielafonso.com.br/ipbcb/`

The service is already built and deployed. This document is the app's binding record of what it must send and what it must tolerate. Nothing here is negotiable from the client side.

Retrofit endpoint constants live in `features/hymnal/data/api/HymnalHistoryEndpoints.kt`, built on `ApiConstants.BASE_PATH` (`"api/"`) in the same style as `HymnalEndpoints.kt`.

---

## 1. `POST api/hymnal-history/events/`

Submit a batch of recorded views.

**Auth**: optional. Send a bearer token when one is valid so the service can attribute the views; otherwise send none. See [research R-04](../research.md#r-04-how-is-the-authenticated-client-used-without-risking-a-silent-logout) — sending an *expired* token here can silently sign the member out.

**Throttle**: 600 requests/hour per client address.

### Request

```json
{
  "events": [
    {
      "client_event_id": "0b7f2c1e-6a3d-4f89-9b21-4c0f5e6d7a88",
      "hymn_id": 42,
      "device_id": "8f1c9e40-2b77-4d3a-9a5e-6f0b1c2d3e4f",
      "viewed_at": "2026-08-09T19:34:12-03:00",
      "duration_seconds": 47,
      "app_version": "0.9.6",
      "platform": "android"
    }
  ]
}
```

### Field rules

| Field | Type | Rule | Failure mode |
|-------|------|------|--------------|
| `client_event_id` | string | Must parse as a UUID | **Silently dropped** — appears in neither response list |
| `hymn_id` | int | Must exist server-side | Rejected `unknown_hymn` |
| `device_id` | string | Non-blank, ≤ 64 chars | Rejected `invalid_event` |
| `viewed_at` | string | ISO-8601 **with UTC offset** | Naive timestamp → rejected `invalid_event` |
| `duration_seconds` | int | `>= 0` | Rejected `invalid_event` |
| `app_version` | string | Optional, defaults `""` | — |
| `platform` | string | Optional, defaults `""` | — |

> **Unknown fields are forbidden.** Any eighth key makes that event fail parsing and return as `invalid_event`. `HymnViewEventDto` must declare exactly these seven properties. The shared `Json` has `encodeDefaults = true`, so every declared property *is* emitted — there is no accidental omission to hide behind.

Batch size must not exceed the `max_batch_size` from §2, or the **whole request** fails.

### Response `201`

```json
{
  "accepted": ["0b7f2c1e-...", "1c8e3d2f-..."],
  "rejected": [
    { "client_event_id": "2d9f4e3a-...", "reason": "unknown_hymn" }
  ]
}
```

`accepted` means stored, deduplicated, **or collapsed** — all three mean the device may forget the event.

### Reason codes (stable)

| Code | Meaning |
|------|---------|
| `unknown_hymn` | No hymn with that `hymn_id` |
| `viewed_at_in_future` | Beyond the service's future tolerance |
| `viewed_at_too_old` | Older than `max_past_days` |
| `invalid_event` | Failed DTO parsing — bad field, or an unknown one |

### Client reconciliation rule

> Remove **every `client_event_id` in the submitted chunk**, not just those the service listed.

Accepted → remove. Rejected → remove and log the reason. Listed in neither → remove as well. The third case is what stops a silently-dropped event living in the queue forever. Reconciling against the submitted set rather than the answered set makes the leak structurally impossible rather than merely unlikely.

### Error responses

Project-wide shape, parsed by the existing `ApiErrorBody` / `Response.toAppError()`:

```json
{ "error_code": "VALIDATION_ERROR", "detail": "…", "field_errors": { } }
```

| Status | Meaning | Client action |
|--------|---------|---------------|
| `400` | Batch too large or malformed | Discard the chunk, continue — it cannot succeed by waiting |
| `429` | Throttled | Keep the queue, `Result.retry()` |
| `5xx` | Server error | Keep the queue, `Result.retry()` |
| network/IO | Offline or timeout | Keep the queue, `Result.retry()` |

No outcome may reach the UI (FR-022).

---

## 2. `GET api/hymnal-history/settings/`

Read the collection configuration. Public; no auth needed.

### Response `200`

```json
{
  "min_seconds_to_count": 30,
  "collapse_window_minutes": 10,
  "max_batch_size": 200,
  "max_past_days": 30,
  "future_tolerance_minutes": 5,
  "window_grace_minutes": 15
}
```

The app consumes **two**: `min_seconds_to_count` (the view threshold) and `max_batch_size` (the chunk size). The other four are declared in the DTO with defaults for documentation and forward-compatibility, then discarded.

On any failure — offline, 5xx, malformed — use the last cached values, and on a fresh install with none, `30` seconds and a batch of `50`. Never surface the failure (FR-025).

`PATCH` on this endpoint is admin-only and **out of scope**.

---

## 3. Prerequisite change to `GET api/hymnal/` — D-001, blocking

The hymnal payload must additionally expose the primary key:

```diff
 {
+  "id": 42,
   "number": "42",
   "title": "Firme nas Promessas",
   "lyrics": [ … ]
 }
```

Additive and backwards-compatible; owned by the backend project (`DjangoHymnalRepository.values()` and its songs domain spec), not by this feature.

**Client tolerance is mandatory regardless.** `HymnDto.id` is `Int? = null`. Existing cached snapshots have no `id`, and a non-nullable field without a default would throw `MissingFieldException` on that cache — breaking the offline hymnal for every existing user on upgrade. With the nullable default, those hymns simply report nothing until the next snapshot refresh (FR-009).

---

## 4. Internal contracts introduced by this feature

Not HTTP, but boundaries other code depends on.

### `AuthStatusProvider` — `core/domain/auth/`

```kotlin
interface AuthStatusProvider {
    suspend fun hasValidAccessToken(): Boolean
}
```

Lets `features/hymnal` choose its HTTP client without importing `features/auth`, which `CLAUDE.md` forbids. Implemented in `core/data/auth/` by delegating to `AuthSession.hasValidAccessToken()`.

### `DeviceIdProvider` — `core/data/local/`

```kotlin
interface DeviceIdProvider {
    suspend fun get(): String
}
```

Returns a stable random UUID, generating and persisting it on first call. No hardware identifier, no permission (FR-028).

### `HymnViewHistoryRepository` — `features/hymnal/domain/repository/`

```kotlin
interface HymnViewHistoryRepository {
    suspend fun record(event: HymnViewEvent)
    suspend fun queuedCount(): Int
    suspend fun syncOnce(): SyncOutcome
    suspend fun refreshSettings()
    suspend fun currentSettings(): HymnViewCollectionSettings
}
```

The only type crossing this boundary is domain-level; no `Response`, no `HttpException`, no DTO. Failures come back as `SyncOutcome.Deferred(AppError)` or as `Result` — never as a thrown HTTP exception (`CLAUDE.md`).
