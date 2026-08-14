# Contract: Collection Settings & Service Windows API (client side)

**Feature**: `003-hymnal-history-reports` | **Base URL**: `https://gabrielafonso.com.br/ipbcb/`

Source of truth:
`backend/specs/006-hymnal-view-history/contracts/settings-and-windows-endpoints.md`. The app
conforms; it proposes nothing.

All calls in this feature go through `@AuthedRetrofit`. The settings **read** is public and the
collection path in `features/hymnal` deliberately uses the unauthenticated client for it
(`specs/hymnal/spec.md` §3.6) — that stays exactly as it is. This feature is a different caller with
a different client, and the two do not share a Retrofit interface.

---

## 1. `GET api/hymnal-history/settings/`

### Response `200`

```json
{
  "min_seconds_to_count": 30,
  "collapse_window_minutes": 10,
  "max_batch_size": 200,
  "max_past_days": 90,
  "future_tolerance_minutes": 5,
  "window_grace_minutes": 30
}
```

Never 404s: defaults are materialised on first read.

---

## 2. `PATCH api/hymnal-history/settings/`

Admin-only. **Partial — send only what changed** (FR-045).

```json
{ "min_seconds_to_count": 45 }
```

This is enforced structurally: `CollectionSettingsPatchDto` has all-nullable fields and the shared
`Json` is built with `explicitNulls = false`, so an unchanged field is absent from the body rather
than sent as `null`.

### Ranges

| Field | Min | Max |
|---|---|---|
| `min_seconds_to_count` | 1 | 3600 |
| `collapse_window_minutes` | 1 | 1440 |
| `max_batch_size` | 1 | 1000 |
| `max_past_days` | 1 | 3650 |
| `future_tolerance_minutes` | 1 | 1440 |
| `window_grace_minutes` | 1 | 1440 |

Mirrored locally in `SettingField` (see [data-model.md](../data-model.md#25-administration)), which
is also what routes a server error back to a field — one table, so the two cannot drift.

### Response `200`

The full updated settings object.

### Errors

```json
{
  "error_code": "VALIDATION_ERROR",
  "detail": "Validation failed.",
  "field_errors": {
    "min_seconds_to_count": ["Value 0 is out of range. Expected an integer between 1 and 3600."]
  }
}
```

`field_errors` reaches the screen on `AppError.Server.fieldErrors`, filled in
`core/network/error/ResponseExt.kt` — the constitution's single parsing point — and mapped to a
field by `SettingField.wireName`. **A field-level error is never shown as a generic alert**
(FR-046). An unrecognised key falls back to the generic error text rather than being dropped.

### Effects the screen must state (FR-047)

| Change | Effect | Copy shown next to the field |
|---|---|---|
| `window_grace_minutes` | **Re-interprets stored history** at read time | "Altera como o histórico já gravado é lido. Nenhum evento é apagado." |
| `collapse_window_minutes` | **Ingest only** — existing reports do not change | "Vale só para as visualizações coletadas a partir de agora." |
| `min_seconds_to_count` | Future only | "Vale só para as visualizações coletadas a partir de agora." |

**Known limitation** (research R-16): saving here does not update this device's own cached
collection settings. They refresh at the next app start, through the existing startup binding in
`features/hymnal`, which the administration feature may not import.

---

## 3. `GET` / `POST api/hymnal-history/service-windows/`

### List `200`

```json
{
  "service_windows": [
    { "id": 3, "name": "Culto de Domingo à Noite", "weekday": 6,
      "start_time": "19:00:00", "end_time": "21:00:00", "active": true }
  ]
}
```

Ordered by `weekday`, then `start_time` — the same order that decides which window wins when two
overlap. The app renders in the order received.

> **`weekday` is `0 = Monday … 6 = Sunday`. Sunday is `6`, not `0`.**
> Converted once, in `ServiceWindowMapper`: `DayOfWeek.of(weekday + 1)` outward,
> `dayOfWeek.value - 1` inward, with a round-trip test over all seven values (research R-11).

### Create `POST` → `201`

```json
{ "name": "Culto de Oração", "weekday": 2, "start_time": "19:30", "end_time": "21:00", "active": true }
```

`active` optional, defaults to `true`; everything else required.

### Validation

| Rule | Enforced locally before sending |
|---|---|
| `end_time` strictly after `start_time` | yes — on the time fields |
| `weekday` in 0–6 | structurally, by the picker |
| `name` non-empty, ≤ 100 chars | yes — on the name field |

Server-side `field_errors` are routed to the same fields by wire name, exactly as for settings.

---

## 4. `GET` / `PATCH` / `DELETE api/hymnal-history/service-windows/{id}/`

| Method | Response |
|---|---|
| `GET` | `200` with the window — not used by the app, which edits from the loaded list |
| `PATCH` | `200` with the updated window; partial, same validation |
| `DELETE` | `204`, empty body |

`404` with `error_code: NOT_FOUND` when the id is gone — the app reloads the list and says the
service no longer exists rather than leaving a phantom row.

Activating and deactivating are a `PATCH` of `active` alone.

### Effect on history — what the confirmation must say (FR-041)

Creating, editing, deactivating or deleting a window **never touches a stored event**. Occurrences
are derived at read time, so the next report simply reflects the new configuration. Deleting a
window that past occurrences were grouped under makes those events regroup by calendar day: the
history is intact, only the interpretation changed.

Deactivating is the gentler option and the screen presents it as preferable when a service merely
stops happening. The delete confirmation reads, in substance:

> "Apagar 'Culto de Domingo à Noite'? Nenhum registro do histórico é apagado. As ocorrências que
> estavam agrupadas neste culto passam a se agrupar por dia. Se o culto apenas deixou de acontecer,
> prefira desativá-lo."
