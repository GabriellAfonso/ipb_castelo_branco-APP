# Feature Specification: Hymnal History Admin Reports

**Feature Branch**: `003-hymnal-history-reports`

**Created**: 2026-08-14

**Status**: Draft

**Input**: User description: "Add the administrative surface for the hymnal view history to the Android app. Collection
already exists and is complete (spec `002-hymnal-view-history`); what does not exist is any way for the church to
**see** that data inside the app. This feature is the administrative consumption of that data: a reports hub and
three surfaces, all admin-only, reachable from the Admin Panel."

## Overview

Since `002-hymnal-view-history` the app silently records which hymns the congregation opens and delivers those
records to the church's collection service. Nobody in the church can read the result: the only way to consult it
today is the server's own admin site or calling the API by hand.

This feature gives the church leadership three surfaces, all restricted to administrators and all reached from the
Admin Panel:

1. **A hymnal history report** — many readings of one period of data.
2. **A collection settings screen** — the six numbers that govern how views are collected.
3. **A service windows screen** — the weekly services the collection service uses to group views.

Between the Admin Panel and those surfaces sits a **reports hub**, because "Relatórios" is an area of the panel and
the hymnal is only the first area that has a report.

### The central idea: one fetch, many readings

The collection service does not expose raw views. It exposes **occurrences**. An occurrence is *a hymn sung once by
the congregation*, not once per person: twenty phones opening hymn 50 during Sunday service form **one** occurrence
whose reach is 20 devices, not twenty occurrences. Views collapse by hymn + service, falling back to hymn + calendar
day when the view lands outside every active service.

The occurrences reading is deliberately pliable: no pagination, a whole period (up to a year) in a single response.
It is therefore not "the occurrences chart" — it is **the source the app pivots locally**. The administrator picks a
period, a slice and a reading, and only the period costs a round trip. Slice and reading recompute on data already
in hand.

### The two metrics, and which is which

- **Occurrence count** answers *how many times a hymn was sung*. It is the **primary metric**: it orders every
  ranking and sets the length of every bar.
- **Device reach** (the sum of the per-occurrence device counts) answers *how many devices followed along*. It is
  **secondary**: it appears as a label beside the primary metric, never as a bar length and never as sort order.

No reading may present the two as interchangeable, and no screen may sort by one while labelling the other.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - An administrator reaches the reports area (Priority: P1)

An administrator opens the Admin Panel and taps "Relatórios", which is currently inert and grey. A hub opens listing
the areas of church life that have a report. Today the hub lists one — the hymnal — and tapping it opens the hymnal
history report.

**Why this priority**: Without the entry point nothing else in this feature is reachable. It is also the piece that
decides whether the next report (schedule, attendance, members, gallery) is an addition or a rewrite.

**Independent Test**: Open the Admin Panel as an administrator, tap "Relatórios", confirm the hub opens with the
hymnal entry, tap it, and confirm the hymnal report opens. A non-administrator never sees the Admin Panel entry at
all.

**Acceptance Scenarios**:

1. **Given** an administrator on the Admin Panel, **When** they tap "Relatórios", **Then** the reports hub opens.
2. **Given** the reports hub is open, **When** it renders, **Then** it lists the hymnal report as a selectable item,
   presented as one of several possible areas rather than as the screen's only content.
3. **Given** the reports hub is open, **When** the administrator taps the hymnal entry, **Then** the hymnal history
   report opens on its default period and default reading.
4. **Given** a signed-in member who is not an administrator, **When** they use the app, **Then** neither the Admin
   Panel nor the reports hub is reachable.
5. **Given** the hub is open, **When** the administrator goes back, **Then** they return to the Admin Panel without
   the app crashing or skipping a level.

---

### User Story 2 - One period, many readings, without refetching (Priority: P1)

An administrator opens the hymnal report. It opens on a period, showing a few short statements about that period.
They switch the slice to "Culto de Domingo à Noite" and the reading to the ranking: the ranking now covers only that
service. They switch the slice to "Fora do culto" and the ranking changes again. None of these changes waits on the
network. Only when they change the period does the report fetch again.

**Why this priority**: This is the feature. It is the reading the church has never had, and everything else in this
spec either feeds it or administers it.

**Independent Test**: Load the report for a period, then change slice and reading repeatedly with the network
disabled after the first load; every combination renders correct numbers from the already-loaded period.

**Acceptance Scenarios**:

1. **Given** the report is loaded for a period, **When** the administrator changes the slice or the reading,
   **Then** the numbers recompute without any network request.
2. **Given** the report is loaded, **When** the administrator changes the period, **Then** the report fetches the new
   period, shows its loading state, and recomputes every reading over the new data.
3. **Given** the administrator asks "which hymns were most sung this month", **When** they select the month period,
   the "todas as ocorrências" slice and the ranking reading, **Then** the hymns appear ordered by occurrence count
   descending, each labelled with its number, its title and its device reach.
4. **Given** the administrator asks "which hymns were most sung on Sunday evenings this year", **When** they select
   the year period, the "Culto de Domingo à Noite" slice and the ranking reading, **Then** only occurrences of that
   service are counted.
5. **Given** the administrator asks "what does the congregation open during the week, outside services", **When**
   they select the "Fora do culto" slice, **Then** only occurrences with no service attached are counted.
6. **Given** the administrator picks a weekday slice, **When** the ranking renders, **Then** only occurrences whose
   date falls on that weekday are counted, regardless of which service they belong to.
7. **Given** any slice, **When** the evolution reading is selected, **Then** one vertical bar per period bucket is
   shown in chronological order, at the granularity the period implies.
8. **Given** the report is loaded, **When** the highlights reading renders, **Then** it states the most sung hymn,
   how many distinct hymns the church used, how many services had any record, and the average reach per occurrence.
9. **Given** the previous period of the same length also has data, **When** the highlights render, **Then** each
   statement carries its change against that previous period; **When** it has no data, **Then** no change is shown
   and no zero-based comparison is invented.
10. **Given** the administrator picks a custom range longer than the maximum the service accepts, **When** they
    confirm it, **Then** the app refuses it locally, explains the limit, and does not issue the request.

---

### User Story 3 - What we sang last Sunday (Priority: P2)

An administrator wants the plainest answer in the feature: the repertoire of a given service. They select the
services reading and see one card per service, most recent first, each reading like that Sunday's bulletin — the
date, the service's name, and the hymns sung with number and title.

**Why this priority**: It answers a question the leadership asks weekly, needs no chart, and is the reading most
likely to be used the day after a service.

**Independent Test**: Load a period containing at least two services and confirm one card per service in reverse
chronological order, each listing its hymns.

**Acceptance Scenarios**:

1. **Given** a period with services that have records, **When** the services reading renders, **Then** there is one
   card per service occurrence group, ordered most recent first.
2. **Given** a card, **When** it renders, **Then** it shows the date, the service name and every hymn of that
   service with number, title and device reach.
3. **Given** occurrences that belong to no service, **When** the services reading renders, **Then** they appear
   grouped by calendar day and are visibly marked as outside-service use, not disguised as a service.
4. **Given** the calendar reading, **When** it renders, **Then** each day of each month within the period is a cell
   shaded by how many occurrences fell on it, and days with none are visibly empty.
5. **Given** the calendar reading, **When** the administrator taps a day, **Then** that day's bulletin opens.

---

### User Story 4 - Together versus alone (Priority: P2)

An administrator wants to compare what the congregation sings together with what it opens on its own during the
week. They select that reading and see the two side by side, each with its own ranking, with the hymns that appear
on one side and not the other called out.

**Why this priority**: Outside-service use is the genuinely new signal the collection produced; it is pastorally
distinct from congregational singing and deserves a reading of its own rather than being hidden inside a filter.

**Independent Test**: Load a period containing both in-service and outside-service occurrences and confirm two
rankings and the exclusive-hymn callouts on each side.

**Acceptance Scenarios**:

1. **Given** a period with both kinds of occurrence, **When** the reading renders, **Then** in-service and
   outside-service rankings are shown side by side, each ordered by occurrence count.
2. **Given** a hymn that appears only in services, **When** the reading renders, **Then** it is marked as exclusive
   to that side; the same holds for a hymn that appears only outside services.
3. **Given** a period where one of the two sides is empty, **When** the reading renders, **Then** that side shows an
   empty state naming which side is empty, and the other side still renders.

---

### User Story 5 - What the church never sings, and what it has forgotten (Priority: P2)

An administrator wants to widen the repertoire. They open the coverage reading and see what proportion of the
hymnal the church has ever used, a navigable list of hymns never opened by anyone, and a list of hymns that were
sung once but not for a long time, most forgotten first.

**Why this priority**: Absence is the reading neither the catalogue nor the report can give on its own, and it is
the most directly useful one for whoever chooses the repertoire.

**Independent Test**: With a catalogue of known size and an all-time ranking covering a subset of it, confirm the
proportion, the never-sung list and the forgotten list, including a hymn whose last occurrence falls outside the
last year.

**Acceptance Scenarios**:

1. **Given** the local hymnal catalogue and the all-time ranking, **When** the coverage reading renders, **Then** it
   states the proportion of the catalogue that has ever been sung and lists the hymns that never were.
2. **Given** the coverage reading, **When** it renders, **Then** it is explicitly labelled as covering all recorded
   history, and the report's period and slice selectors visibly do not apply to it.
3. **Given** a hymn whose most recent occurrence is inside the last year, **When** the forgotten list renders,
   **Then** it shows the exact date it was last sung.
4. **Given** a hymn that has been sung at some point but whose most recent occurrence is older than the app can
   see, **When** the forgotten list renders, **Then** it says the hymn has not been sung for more than a year and
   states no date, rather than implying a date it does not have.
5. **Given** the forgotten list, **When** it renders, **Then** hymns are ordered from most forgotten to least, with
   the undated group ordered ahead of every dated one.
6. **Given** the catalogue is unavailable, **When** the coverage reading is selected, **Then** it explains that the
   hymnal catalogue could not be read, and the rest of the report keeps working.

---

### User Story 6 - Everything about one hymn (Priority: P3)

Tapping a hymn in any list of this feature opens its card: number, title, how many times it has been sung, when it
was first and last recorded, which services it usually appears in, its typical reach, and a sentence about how
regularly it recurs in the service it belongs to.

**Why this priority**: It is the natural destination of every list item in the feature and the place the other
readings converge for a single hymn — valuable, but only after there are lists to tap.

**Independent Test**: Tap a hymn from the ranking and confirm the card shows the all-time total, the dated facts
limited to what the app can see, and the recurrence sentence.

**Acceptance Scenarios**:

1. **Given** any hymn list in this feature, **When** the administrator taps an item, **Then** that hymn's card
   opens.
2. **Given** a hymn card, **When** it renders, **Then** the all-time total is labelled as covering all history and
   the dated facts are labelled as covering only the visible window.
3. **Given** a hymn that appears in a recurring service, **When** the card renders, **Then** it states in how many
   of the recent instances of that service the hymn appeared, alongside a band of marks for those instances.
4. **Given** a hymn with no occurrence at all, **When** its card opens from the never-sung list, **Then** the card
   states it has never been recorded and shows no invented dates or counts.

---

### User Story 7 - Managing the services the report groups by (Priority: P2)

An administrator manages the weekly services: they list them, create one, edit it, deactivate it, or delete it. The
screen makes clear that none of these actions deletes history, and that deactivating is the gentler choice when a
service simply stops happening.

**Why this priority**: Every service-based slice, bulletin and recurrence statement depends on these windows being
right. A wrong or missing window silently degrades every reading above.

**Independent Test**: Create a service, see it in the list and in the report's slice selector; deactivate it;
delete it and confirm the report still shows the same occurrences, regrouped by calendar day.

**Acceptance Scenarios**:

1. **Given** the service windows screen, **When** it loads, **Then** every service is listed with its name, its
   weekday in Portuguese, its start and end time, and whether it is active.
2. **Given** the create form, **When** the administrator submits a valid service, **Then** it is created and appears
   in the list.
3. **Given** the create or edit form, **When** the end time is not strictly later than the start time, **Then** the
   app refuses locally and says so on the time fields, without issuing the request.
4. **Given** a name longer than the accepted length or an empty name, **When** the administrator submits, **Then**
   the app refuses locally and says so on the name field.
5. **Given** an existing service, **When** the administrator toggles it inactive, **Then** it stays in the list,
   visibly inactive, and stops being offered as a slice in the report.
6. **Given** an existing service, **When** the administrator asks to delete it, **Then** the app asks to confirm and
   states plainly that no history is deleted, that past occurrences will regroup by calendar day, and that
   deactivating is the milder alternative.
7. **Given** a Sunday service, **When** it is shown anywhere in the app, **Then** it reads "Domingo" — the weekday
   convention of the collection service is translated without rotating the week.

---

### User Story 8 - Tuning the collection (Priority: P3)

An administrator opens the collection settings screen, sees the six numbers that govern collection with their
accepted ranges, changes one, and saves. The screen explains, next to each number, what changing it does — in
particular which changes only affect future collection and which change how existing history is read.

**Why this priority**: The defaults work; this screen is for the rare tuning session. It ranks last because getting
it wrong is recoverable and nothing else in the feature depends on it.

**Independent Test**: Open the screen, change one value, save, reopen, and confirm the new value; then submit an
out-of-range value and confirm the error lands on that field.

**Acceptance Scenarios**:

1. **Given** the settings screen, **When** it loads, **Then** the six current values are shown, each with its
   accepted range.
2. **Given** a value outside its range, **When** the administrator tries to save, **Then** the app refuses locally
   and shows the message on that field, without issuing the request.
3. **Given** valid changes, **When** the administrator saves, **Then** only the changed values are sent and the
   screen shows the saved result.
4. **Given** the service refuses a value the app accepted, **When** the response arrives, **Then** the message is
   shown on the field the service named, not in a generic alert.
5. **Given** the settings screen, **When** it renders, **Then** it states that the grace period re-interprets stored
   history on the next reading, that the collapse window affects only newly collected views, and that the minimum
   duration applies only from now on.

---

### Edge Cases

- **A church that just installed collection.** Every reading is empty. Each reading says so in its own words rather
  than drawing an empty chart, and the report as a whole says the collection has not produced anything yet.
- **A period with no records.** The reading says the chosen period has no records and points at changing the period,
  distinct from "collection has produced nothing at all".
- **A slice that is empty because the service did not exist yet.** When the selected service was created after, or
  deactivated before, part of the chosen period, the empty state says so instead of implying nobody sang.
- **A service with no occurrences.** The app cannot know whether the service did not happen or happened and nobody
  opened the hymnal — only hymn views are collected. The empty state must state both possibilities and claim
  neither.
- **A custom range longer than the accepted maximum**, or with the start after the end: refused locally with the
  reason, before any request.
- **A period whose previous period has no data**: highlights show the value with no change indicator.
- **A hymn present in the report but absent from the local catalogue** (or the reverse): both readings degrade to
  what they can say; the report never drops an occurrence because the catalogue does not know the hymn.
- **The administrator's session is not, or is no longer, an administrator**: the surfaces show the standard
  authorisation error rather than an empty report.
- **A very wide ranking** (hundreds of hymns in a year): the ranking stays usable — bars are capped to a readable
  count with the remainder reachable, rather than rendering hundreds of bars at once.
- **All occurrences in a bucket have the same count**: bars still render at a readable minimum length instead of
  collapsing to zero width.
- **Device reach of a single-device occurrence**: reach is shown as-is; no reading rounds a reach of 1 away.
- **Rotation and process death** on any surface: the chosen period, slice and reading survive; changing orientation
  never refetches.

## Requirements *(mandatory)*

### Functional Requirements

#### Access and entry

- **FR-001**: The Admin Panel's "Relatórios" action MUST become active and MUST open the reports hub.
- **FR-002**: The reports hub MUST present the available report areas as a list of interchangeable entries, so a new
  area can be added without altering the hub, its state, or its item presentation.
- **FR-003**: The reports hub MUST contain exactly one area in this feature — the hymnal history — and MUST NOT
  introduce generic reporting infrastructure for areas that do not exist.
- **FR-004**: Every surface in this feature MUST be reachable only by administrators and MUST use the authenticated
  network path for every request, including reading collection settings.
- **FR-005**: Every surface MUST render a loading, a success and an error state.

#### Period, slice and reading

- **FR-006**: The report MUST offer the periods "esta semana", "este mês", "este ano" and a custom range.
- **FR-007**: Changing the period MUST be the only interaction that issues a report request. Changing the slice or
  the reading MUST recompute from data already loaded.
- **FR-008**: The app MUST refuse locally, before any request, a custom range whose start is after its end or whose
  span exceeds the maximum the collection service accepts, and MUST explain which rule was broken.
- **FR-009**: The report MUST offer the slices: all occurrences, one specific service, outside any service, and one
  specific weekday. Service slice and weekday slice are independent filters and both MUST be applied on already
  loaded data.
- **FR-010**: The service slice MUST list only active services.
- **FR-011**: The readings MUST be: highlights, ranking, evolution, services (bulletins), calendar, in-service
  versus outside-service, and hymnal coverage.
- **FR-012**: The report MUST open on the highlights reading of the default period.
- **FR-013**: The chosen period, slice and reading MUST survive configuration changes without refetching.

#### Metrics and data sourcing

- **FR-014**: Occurrence count MUST be the primary metric — it orders every ranking and sets every bar length.
- **FR-015**: Device reach MUST appear only as a secondary label and MUST never determine ordering or bar length.
- **FR-016**: Every period-bounded reading — highlights, ranking, evolution, services, calendar, in-service versus
  outside-service — MUST be computed from the period's occurrence data and from nothing else.
- **FR-017**: The all-time ranking MUST be the only source for the coverage reading and for a hymn's all-time total,
  and both MUST be visibly labelled as covering all recorded history.
- **FR-018**: No single reading may mix the two sources into one number.
- **FR-019**: The highlights reading MUST state, for the chosen period and slice: the most sung hymn, the number of
  distinct hymns used, the number of services with any record, and the average device reach per occurrence.
- **FR-020**: Each highlight MUST carry its change against the immediately preceding period of the same length when
  that period has data, and MUST omit the change otherwise. Fetching that preceding period is permitted only as part
  of a period change.

#### Charts

- **FR-021**: The ranking MUST be horizontal bars, one per hymn, length proportional to occurrence count, each
  labelled with hymn number and title.
- **FR-022**: The evolution MUST be vertical bars, one per bucket, in the chronological order the collection service
  returns, at the granularity the chosen period implies.
- **FR-023**: Charts MUST be drawn with the platform's own drawing and layout primitives and the project's theme
  colours. No charting library may be added.
- **FR-024**: The view layer MUST receive chart points already aggregated, so replacing the drawing later touches
  only the drawing.

#### Non-chart readings

- **FR-025**: The services reading MUST show one card per service group, most recent first, each with the date, the
  service name, and the hymns with number, title and reach.
- **FR-026**: Occurrence groups with no service MUST appear in the services reading grouped by calendar day and
  visibly marked as outside-service use.
- **FR-027**: The calendar reading MUST render each month of the period as a grid of days shaded by occurrence
  count, and tapping a day MUST open that day's bulletin.
- **FR-028**: The in-service versus outside-service reading MUST show both rankings side by side and MUST mark the
  hymns exclusive to each side.
- **FR-029**: The coverage reading MUST state the proportion of the catalogue ever sung and MUST list the never-sung
  hymns, navigably.
- **FR-030**: The coverage reading MUST list forgotten hymns — sung at some point but not recently — ordered most
  forgotten first, showing the exact last-sung date when it falls inside the visible window and stating "não é
  cantado há mais de um ano" when it does not, with no invented date.
- **FR-031**: Undated forgotten hymns MUST sort ahead of every dated one.
- **FR-032**: Tapping a hymn in any list of this feature MUST open that hymn's card.
- **FR-033**: The hymn card MUST show number, title, all-time occurrence total, first and last recorded dates within
  the visible window, the services it usually appears in, its typical reach, and a recurrence sentence with a band
  of marks for the recent instances of the service it belongs to.
- **FR-034**: Every statement in the hymn card MUST be labelled with the extent of history it covers.

#### Empty states

- **FR-035**: Each reading MUST have its own empty state explaining the reason for the emptiness rather than
  rendering a blank chart.
- **FR-036**: The empty states MUST distinguish: collection has produced nothing at all; the chosen period has no
  records; the chosen slice has no records; and the selected service did not exist or was inactive during the
  chosen period.
- **FR-037**: An empty service MUST state that the app cannot tell whether the service did not happen or happened
  without anyone opening the hymnal, and MUST NOT assert either.

#### Service windows

- **FR-038**: The service windows screen MUST list, create, edit, activate/deactivate and delete services.
- **FR-039**: The weekday MUST be translated between the collection service's convention (Monday first, Sunday
  last) and the Portuguese label, in both directions, without rotating the week.
- **FR-040**: The app MUST validate locally, before any request: non-empty name within the accepted length, weekday
  within range, and end time strictly later than start time — each error on its own field.
- **FR-041**: Deleting a service MUST require confirmation that states that no history is deleted, that past
  occurrences regroup by calendar day, and that deactivating is the milder alternative.
- **FR-042**: Creating, editing, activating, deactivating or deleting a service MUST be reflected in the report's
  slice selector without restarting the app.

#### Collection settings

- **FR-043**: The settings screen MUST show and edit the six collection parameters, each with its accepted range
  visible.
- **FR-044**: The app MUST validate each value against its own range locally, before any request, and show the
  failure on that field.
- **FR-045**: Saving MUST send only the values that changed.
- **FR-046**: Field-level errors returned by the collection service MUST be mapped to the corresponding form field;
  a field-level error MUST NOT be shown as a generic alert.
- **FR-047**: The settings screen MUST explain, at the point of editing, that changing the grace period
  re-interprets stored history on the next reading, that changing the collapse window affects only newly collected
  views, and that changing the minimum duration applies only from now on.

#### Architecture and layering

- **FR-048**: This feature MUST live under the administration area. It MUST NOT import from the hymnal feature area;
  anything shared MUST be exposed through the shared core.
- **FR-049**: Reading the hymnal catalogue for the coverage reading MUST go through a shared-core capability, not
  through the hymnal feature area. (See "Decision D-1".)
- **FR-050**: Filtering, grouping and aggregation of occurrences MUST happen in the domain layer, in pure use cases
  testable without the Android platform — not in the view layer and not in the repository.
- **FR-051**: Every derived sentence, proportion and temporal statement MUST be produced in the domain layer and
  arrive at the view layer ready to display. The view layer MUST NOT compute or choose that text.
- **FR-052**: User-visible text MUST be Portuguese, hardcoded, as elsewhere in the project.
- **FR-053**: Errors crossing into the view layer MUST use the project's single error hierarchy.

#### Documentation

- **FR-054**: The hymnal domain spec MUST be updated in the same commit: its "Fora de escopo" section currently
  declares these screens as non-existent.
- **FR-055**: The administration domain spec MUST be updated in the same commit to include the reports hub and the
  new surfaces, and to move "Relatórios" out of the unimplemented list.

#### Tests

- **FR-056**: Test coverage MUST include: aggregating occurrences into a ranking with and without a service slice
  and with and without a weekday slice; the never-sung and forgotten calculations including a hymn whose most
  recent occurrence falls outside the visible window; the weekday translation in both directions; the range
  validation of all six settings; and the mapping of field-level errors to the right form field.
- **FR-057**: Every use case MUST have at least a happy path and one error path, using the project's existing test
  stack, with fakes preferred over mocks.

### Key Entities

- **Occurrence**: one hymn sung once by the congregation within a period. Carries the hymn's number and title, the
  date it happened, the service it belongs to (which may be absent), a bucket label for the chosen granularity, and
  the number of devices that contributed. Its absence of a service is meaningful: it marks use outside any service.
- **Hymn ranking entry**: a hymn and how many occurrences it has, within a range or across all history. Hymns with
  no occurrence in the range are absent — filling those gaps is the app's work, not the service's.
- **Service window**: a recurring weekly service, with a name, a weekday, a start time, an end time and an active
  flag. It is the grouping key for occurrences and the definition of a report slice.
- **Collection settings**: six positive whole numbers governing collection, each with its own accepted range. Some
  affect only future collection; one changes how stored history is read.
- **Report area**: an entry in the reports hub — a name, a short description and a destination. The hymnal is the
  only instance in this feature.
- **Hymnal catalogue entry**: a hymn's number and title as the app already holds them locally. It is the complement
  the ranking cannot supply, and the only way to know what has never been sung.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: An administrator can answer "which hymns were most sung this month" in at most three taps from the
  Admin Panel.
- **SC-002**: An administrator can answer "what did we sing last Sunday" in at most four taps from the Admin Panel.
- **SC-003**: After a period has loaded, switching slice or reading produces the new numbers with no network
  request and no perceptible wait, including with the network fully disabled.
- **SC-004**: A whole year of history renders every reading without the report becoming unresponsive.
- **SC-005**: Every reading, in every empty situation described in this spec, shows a sentence explaining why it is
  empty; no reading ever displays a blank chart.
- **SC-006**: Every number a reading shows can be traced to exactly one of the two data sources, and no two
  readings on the same screen disagree about the same quantity.
- **SC-007**: An out-of-range collection setting is reported on its own field in 100% of cases, whether it was
  caught locally or by the collection service.
- **SC-008**: A Sunday service reads "Domingo" everywhere it appears, and each of the seven weekdays maps to exactly
  one label, verified by test.
- **SC-009**: A hymn last sung outside the visible window is never shown with a date, in any reading.
- **SC-010**: Deleting a service window leaves the occurrence total for any past period unchanged.

## Assumptions

### Decisions taken

- **D-1 — Reading the hymnal catalogue.** The coverage reading needs both the report and the local hymn catalogue,
  but the catalogue lives in the hymnal feature area and feature areas may not import one another. **Decision**: the
  catalogue is exposed as a read-only capability in the shared core — number and title only — with the hymnal
  feature area continuing to own the reader experience. The project already does exactly this for songs shared
  between the worship hub and administration, so this is an existing pattern rather than a new abstraction, and it
  keeps the layering rule intact without duplicating the catalogue or moving the hymnal.
- **D-2 — Which source feeds which reading.** Period-bounded readings come from the occurrences source; coverage and
  all-time totals come from the all-time ranking source. This is stated as a hard rule (FR-016 to FR-018) precisely
  so no screen queries both and shows numbers that disagree.
- **D-3 — Recurrence is folded into the hymn card.** A recurrence statement is only meaningful for one named hymn,
  and the only way to name a hymn is to tap it. Building it as a separate top-level reading would need its own hymn
  picker for no gain, so it is a section of the hymn card instead of the eighth reading.
- **D-4 — The calendar covers each month of the period.** For a week or a month it is one grid; for a year it is
  twelve, scrollable. It is not restricted to the current month, and it does not attempt a year-long single grid.
- **D-5 — "No service" versus "no one opened the hymnal" cannot be distinguished.** Only hymn views are collected,
  so the app has no evidence a service happened. The empty state names both possibilities and asserts neither. This
  is the honest form of the requirement that the difference be clear.
- **D-6 — Highlights may cost a second fetch.** The change-versus-previous-period figure needs the preceding period.
  That fetch happens as part of a period change, which is the one interaction already allowed to hit the network.

### Assumed

- The collection service is already fully implemented and its contract is fixed. The app conforms to it; this
  feature proposes no change to it.
- The administrator is online when opening the report. There is no offline snapshot for these surfaces — they are
  occasional administrative readings, not a member-facing screen that must work on a bus.
- The existing admin gate (the panel appearing only for administrators) is the app-side filter; the collection
  service remains the authority on every request.
- The hymnal catalogue is already available locally from the existing offline snapshot; the coverage reading does
  not fetch it.
- Dates and periods are interpreted in the church's local time, matching the collection service.
- The report's default period on open is the current month.

## Out of Scope

- Any change to collection itself. It is finished and must not be touched.
- Any surface for the ordinary member. Everything here is administrator-only.
- Reports for areas other than the hymnal. The hub must accommodate them; none is built now.
- Exporting reports (CSV, PDF, sharing).
- Automatic repertoire suggestion, or any recommendation of what to sing. These readings inform whoever chooses;
  they do not choose.
- Collection or reporting of anything that is not a hymn — lyrics, chord charts, studies and Bible remain
  uninstrumented.
- Adding a charting library, now or as an alternative.

## Dependencies

- The collection service's reporting, settings and service-window contracts, already implemented and documented in
  the backend specification `006-hymnal-view-history`.
- The authenticated network path and the existing administrator session.
- The existing local hymnal catalogue, reached through the shared core per Decision D-1.
- The Admin Panel, whose "Relatórios" action becomes the entry point.
