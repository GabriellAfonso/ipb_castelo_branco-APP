# Quickstart: Hymnal History Admin Reports

**Feature**: `003-hymnal-history-reports` | **Phase**: 1 | **Date**: 2026-08-14

How to run, validate and test this feature. Types are in [data-model.md](./data-model.md); payloads
are in [contracts/](./contracts/); the reasoning is in [research.md](./research.md).

---

## 1. Prerequisites

- An account whose profile is an **administrator** — every endpoint here is `IsAdminUser`, and the
  Admin Panel entry only appears for `authState.isLoggedIn && authState.isAdmin`.
- A backend with `006-hymnal-view-history` deployed (it is).
- Some collected history. Without it every screen shows an empty state — which is itself worth
  validating first, see §3.0.
- At least one **active service window** covering a time when views were recorded; otherwise every
  occurrence collapses by calendar day and the service slices are all empty.

## 2. Build and test

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:testDebugUnitTest --tests "com.ipb.castelobranco.features.admin.reports.*"
./gradlew :app:testDebugUnitTest --tests "com.ipb.castelobranco.core.network.error.*"
```

Never pass `clean`, `--rerun-tasks` or `--no-daemon` first — it destroys the KSP cache and turns
seconds into 15+ minutes (`CLAUDE.md`).

## 3. Manual validation

Each scenario names the user story it proves.

### 3.0 The empty church (Edge cases)

Point the app at an account with no collected history.

- The report opens and says the collection has not produced anything yet — not a blank chart.
- Every reading has its own sentence. Coverage says the church has sung 0 of N hymns and lists the
  catalogue as never sung.
- **Expected**: no reading renders an empty chart; `NoCollectionAtAll` copy appears, distinct from
  `NoRecordsInPeriod`.

### 3.1 Reaching the reports (US1)

1. Sign in as an administrator, open the menu → **Painel Admin**.
2. The **Relatórios** card is coloured, not grey, and tappable.
3. Tap it → the hub opens, listing **Histórico do hinário**.
4. Tap that → the report opens on **este mês**, on the **Destaques** reading.
5. Back returns to the panel, then to the home screen, with no crash.

**Expected**: three taps from the panel to the report (SC-001 counts from here).

### 3.2 One period, many readings (US2)

1. With the report loaded, **turn off the network**.
2. Switch the slice: Todas → Culto de Domingo à Noite → Fora do culto → Domingo (weekday).
3. Switch the reading through all seven.

**Expected**: every combination renders real numbers with no error and no spinner. Turning the
network back on and changing the period shows a loading state and new numbers.

Check the four acceptance questions:

- "Hinos mais cantados neste mês" — Este mês + Todas + Ranking.
- "Mais cantados aos domingos à noite neste ano" — Este ano + that service + Ranking.
- "O que a congregação abre durante a semana" — Fora do culto + Ranking.
- "O que cantamos no domingo passado" — Cultos, first card.

Also: pick a custom range of 400 days. **Expected**: refused immediately, message names the
366-day limit, no request issued (check with the network log or Timber output).

### 3.3 The bulletin and the calendar (US3)

1. Reading → **Cultos**. One card per service, most recent first, each with date, service name and
   its hymns.
2. Occurrences outside a service appear grouped by day and visibly marked as such.
3. Reading → **Calendário**. Sundays are visibly darker than midweek days. Tap a dense day.

**Expected**: the day's bulletin opens.

### 3.4 Together versus alone (US4)

Reading → **Culto × fora do culto**. Two rankings side by side; hymns exclusive to one side are
marked. If one side is empty, it says which side and the other still renders.

### 3.5 Coverage and forgotten hymns (US5)

Reading → **Cobertura**.

- The proportion sentence names both numbers ("a igreja já cantou X dos Y hinos").
- The reading is labelled **todo o histórico**, and the period/slice selectors are not shown on it.
- The forgotten list shows exact dates for hymns sung within the last year, and
  "não é cantado há mais de um ano" — with **no date** — for the rest, which sort first.

### 3.6 The hymn card (US6)

Tap any hymn in any list. **Expected**: the all-time total is labelled "em todo o histórico"; the
dates, services and reach are labelled "no último ano"; the recurrence sentence reads like
"cantado em 6 dos últimos 8 domingos à noite" with a matching band of marks. A hymn opened from the
never-sung list shows no dates and no counts.

### 3.7 Service windows (US7)

1. Create "Culto de Oração", Wednesday 19:30–21:00. It appears in the list **and** in the report's
   slice selector without restarting the app.
2. Try end 19:00 with start 19:30. **Expected**: refused on the time fields, no request.
3. Try a 120-character name. **Expected**: refused on the name field.
4. Deactivate it. **Expected**: still listed, visibly inactive, gone from the slice selector.
5. Note the occurrence total for a past period. Delete a window. **Expected**: the confirmation says
   no history is deleted and that deactivating is milder; after deleting, the same period's
   occurrence total is **unchanged** (SC-010), with those occurrences now grouped by day.
6. A Sunday service reads **Domingo** everywhere (SC-008). If it reads "Segunda", the weekday
   conversion is inverted — see [research R-11](./research.md#r-11--translating-the-weekday).

### 3.8 Collection settings (US8)

1. Open the settings screen. Six fields, each showing its accepted range.
2. Enter `0` in *Tempo mínimo de leitura*. **Expected**: refused on that field, no request.
3. Enter a valid value and save. **Expected**: only that key is sent (verify in the network log);
   the screen shows the saved result.
4. Each of the three effect explanations is visible: grace re-interprets stored history, collapse
   window affects only new views, minimum duration applies from now on.

### 3.9 Not an administrator

Sign in as an ordinary member. **Expected**: no Admin Panel entry at all. If an admin session loses
its privilege mid-use, the surfaces show the standard authorisation message — not an empty report.

## 4. Test plan

Every item FR-056 names, plus the happy/error pair FR-057 requires per use case.

### Explicitly mandated by FR-056

| What | Where | Cases |
|---|---|---|
| Occurrences → ranking, no slice | `BuildHymnRankingUseCaseTest` | ordering by count desc, tie-break, reach as secondary, bar fraction, top-25 cap |
| Ranking with a service slice | `FilterOccurrencesUseCaseTest` + ranking test | only that `service_window_id` counted |
| Ranking with a weekday slice | same | derived from `occurred_on`, across services |
| Ranking, outside-service slice | same | only `service_window_id == null` |
| Never-sung calculation | `BuildHymnalCoverageUseCaseTest` | catalogue ∖ all-time ranking; empty ranking → whole catalogue |
| Forgotten, last occurrence inside the window | same | exact date, correct text |
| **Forgotten, last occurrence outside 366 days** | same | **no date**, "mais de um ano" text, sorts first |
| Weekday translation | `ServiceWindowMapperTest` | all seven values, both directions, round-trip; asserts `6 → DOMINGO` |
| Six setting ranges | `ValidateCollectionSettingsUseCaseTest` | min−1, min, max, max+1, non-integer, per field |
| `field_errors` → form field | `CollectionSettingsViewModelTest` | known key routed by `wireName`; unknown key falls back to generic |

### Additional

| What | Where |
|---|---|
| `AppError.Server` carries `fieldErrors`; body without them unchanged | `ResponseExtFieldErrorsTest` |
| Partial patch contains only changed keys | `CollectionSettingsDiffTest` |
| Period resolution + `group_by` per period; custom range validation | `ResolveReportPeriodUseCaseTest` |
| Highlights with and without a preceding period | `BuildHighlightsUseCaseTest` |
| Evolution preserves server order | `BuildEvolutionSeriesUseCaseTest` |
| Bulletins reverse-chronological; no-service groups marked | `BuildServiceBulletinsUseCaseTest` |
| Calendar: one month per month of the period, intensity scaling | `BuildCalendarUseCaseTest` |
| In-vs-outside exclusives on both sides; one side empty | `BuildInVsOutsideUseCaseTest` |
| Hymn profile labels; never-sung hymn yields no dates | `BuildHymnProfileUseCaseTest` |
| Empty reasons, all six branches | `ResolveEmptyReasonUseCaseTest` |
| Window validation: end ≤ start, empty name, 101 chars | `ValidateServiceWindowUseCaseTest` |
| Repositories map failures to `AppError`; `403` → `AppError.Auth` | both `…RepositoryImplTest` |
| **Slice/reading changes issue zero repository calls**; period change issues one pair | `HymnalReportViewModelTest` |
| Catalogue unavailable → `CatalogUnavailable`, rest of the report still works | `HymnalReportViewModelTest` |

Fakes over mocks: a `FakeHymnalReportRepository` returning canned occurrence lists and a
`FakeHymnCatalogRepository` returning a small catalogue cover most of the above, with a fixed
`DateProvider` so every date assertion is deterministic.

## 5. Definition of done

- All of §3 passes on a device against the real backend.
- `./gradlew :app:testDebugUnitTest` is green.
- `specs/hymnal/spec.md` §4 no longer declares these screens out of scope, and
  `specs/admin/spec.md` covers the hub and the new surfaces — both in the same commit as the code
  (FR-054, FR-055).
- No new entry in `gradle/libs.versions.toml`.
