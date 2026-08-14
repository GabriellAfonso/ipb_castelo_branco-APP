# Contract: Hymnal History Reporting API (client side)

**Feature**: `003-hymnal-history-reports` | **Base URL**: `https://gabrielafonso.com.br/ipbcb/`

The service is built and deployed. This is the app's binding record of what it may send and what it
must tolerate. Nothing here is negotiable from the client side. Source of truth:
`backend/specs/006-hymnal-view-history/contracts/reporting-endpoints.md`.

Both endpoints require an **administrator** session and therefore go through `@AuthedRetrofit` —
including in this feature the settings read, which is public but is used here as an administrative
reading (FR-004).

Endpoint constants extend the existing
`features/hymnal/data/api/HymnalHistoryEndpoints.kt` style but live in the administration feature's
own `HymnalReportEndpoints.kt`, built on `ApiConstants.BASE_PATH` (`"api/"`).

---

## 1. `GET api/hymnal-history/occurrences/`

The pliable one. One period, no pagination, pivoted locally.

### Query

| Param | Type | Sent by the app | Notes |
|---|---|---|---|
| `from` | `YYYY-MM-DD` | always | inclusive, church local day |
| `to` | `YYYY-MM-DD` | always | inclusive |
| `group_by` | `service` \| `day` \| `week` \| `month` | always | chosen by the period, never by the user (research R-08) |

The app always sends `from` and `to` explicitly rather than relying on the service's 30-day default,
so what is on screen and what was requested cannot drift.

### Response `200`

```json
{
  "from": "2026-08-01",
  "to": "2026-08-31",
  "group_by": "service",
  "occurrences": [
    {
      "hymn_number": "50",
      "hymn_title": "Grandioso És Tu",
      "occurred_on": "2026-08-09",
      "service_window_id": 3,
      "service_window_name": "Culto de Domingo à Noite",
      "bucket": "2026-08-09:3",
      "device_count": 27
    },
    {
      "hymn_number": "120",
      "hymn_title": "Saudosa Lembrança",
      "occurred_on": "2026-08-12",
      "service_window_id": null,
      "service_window_name": null,
      "bucket": "2026-08-12:none",
      "device_count": 2
    }
  ]
}
```

### What the app must hold to

- **An occurrence is a hymn sung once by the congregation**, not once per person. `device_count` is
  reach, never a count of singings. It is a secondary label and never a bar length (FR-015).
- **`service_window_id: null` is meaning, not missing data**: the views fell outside every active
  window and collapsed by calendar day. It is the "Fora do culto" slice and the right-hand side of
  the in-versus-outside reading.
- **`group_by` changes only `bucket`.** The same range always returns the same number of
  occurrences. No reading may imply that regrouping changed a total.
- **Ordering is stable** — `occurred_on` ascending, then service start time (nulls last), then hymn
  number. The evolution chart renders in the order received and must not re-sort it.
- **No pagination**, and the range is capped at 366 days. Both facts are why the whole period is
  loaded once and pivoted in memory.
- `bucket` formats: `service → "{date}:{window_id}"` or `"{date}:none"`; `day → 2026-08-09`;
  `week → 2026-W32` (ISO); `month → 2026-08`.

### Errors

| Status | `error_code` | App behaviour |
|---|---|---|
| `400` | `VALIDATION_ERROR` | `from` after `to`, span > 366 days, unparseable date, bad `group_by`. The app validates the first two locally and must never rely on this (research R-17). Shown via `AppError.toUserMessage()`. |
| `401` | `NOT_AUTHENTICATED` | standard auth error path |
| `403` | `PERMISSION_DENIED` | `AppError.Auth` → standard authorisation text, not an empty report |

---

## 2. `GET api/hymnal-history/top-hymns/`

The all-time one. No date parameters are sent by this feature.

### Query

| Param | Sent by the app |
|---|---|
| `from` | never |
| `to` | never |

Omitting both covers all recorded history, and the 366-day cap does not apply. That is the only
reason this endpoint exists in the app: it is the sole source for "all history".

### Response `200`

```json
{
  "from": null,
  "to": null,
  "hymns": [
    { "hymn_number": "50",  "hymn_title": "Grandioso És Tu",     "occurrence_count": 42 },
    { "hymn_number": "12",  "hymn_title": "Firme nas Promessas", "occurrence_count": 31 }
  ]
}
```

### What the app must hold to

- **Hymns with no occurrence are absent.** Filling the gap is the client's job — that is the whole
  never-sung reading, and it is why the local catalogue is needed (research R-04, R-18).
- Already ordered by count descending, hymn number ascending as tie-break. The app does not re-sort.
- It **cannot** filter by service window or weekday. Every sliced reading therefore comes from
  `occurrences`, never from here (research R-03).
- It returns **no dates**. Any "last sung" statement it could support would be invented; the app
  says "não é cantado há mais de um ano" instead (FR-030).

### Errors

Same as above, minus `group_by`.

---

## 3. Fetch plan

| Trigger | Requests |
|---|---|
| Report opened / period changed | `occurrences` for the period **and** `occurrences` for the preceding period of equal length, concurrently (research R-12) |
| Coverage reading opened, first time | `top-hymns` (all time) **and** `occurrences` for the last 366 days, concurrently |
| Slice changed | none |
| Reading changed | none, except the first opening of coverage |
| Hymn card opened | none — it reads data already loaded |

The preceding-period request failing does not fail the report: the highlights simply render without
deltas.
