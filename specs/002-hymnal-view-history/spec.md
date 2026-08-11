# Feature Specification: Hymnal View History Collection

**Feature Branch**: `002-hymnal-view-history`

**Created**: 2026-08-08

**Status**: Draft

**Input**: User description: "Add hymnal usage collection to the Android app. When a member opens a hymn and stays on it long enough, the app records that view locally and syncs it to the backend, so the church can see which hymns the congregation actually sings — during the week and on Sundays. The backend for this already exists and is implemented. This spec is the client side only."

## Overview

The church has no way of knowing which hymns its congregation actually uses. Attendance at a service tells them which hymns were *led*; nothing tells them which hymns members open on their own during the week, or follow along with on Sunday.

This feature makes the app report that usage. When a member opens a hymn and stays on it long enough to be genuinely reading it, the app records a view, holds it locally, and delivers it to the church's collection service when the network allows.

**The feature is invisible.** It adds no screen, no setting, no indicator, and no message. A member using the hymnal must not be able to tell it exists, and a failure to collect or deliver must never interrupt them.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - A genuine hymn view is recorded (Priority: P1)

A member opens a hymn from the hymnal and reads it. Once they have had it on screen — actually on screen, awake, in front of them — for the minimum qualifying time, the app records one view of that hymn.

**Why this priority**: This is the measurement itself. Without it there is no data, and every other story in this feature has nothing to operate on. It is the minimum viable slice: even with no delivery at all, a correct local record is the thing the rest of the feature merely transports.

**Independent Test**: Open a hymn, leave it on screen past the qualifying duration, and inspect the local record store. Exactly one view exists, naming that hymn, timestamped at the moment the threshold was reached, and carrying the elapsed on-screen time.

**Acceptance Scenarios**:

1. **Given** a member opens a hymn, **When** it has been on screen and in the foreground for the qualifying duration, **Then** exactly one view is recorded for that hymn at that moment.
2. **Given** a member opens a hymn and leaves it before the qualifying duration elapses, **When** they navigate away, **Then** no view is recorded.
3. **Given** a hymn has been on screen for less than the qualifying duration, **When** the member backgrounds the app or the screen turns off, **Then** counting pauses; **When** they return to the hymn, **Then** counting resumes from the accumulated time rather than restarting.
4. **Given** a hymn has already produced a view in this visit, **When** the member keeps it on screen for several more minutes, **Then** no second view is recorded for that visit.
5. **Given** a member leaves a hymn after a view was recorded, **When** they open the same hymn again later, **Then** a fresh count begins and may produce a second view — this is expected and correct.
6. **Given** a member opens a hymn whose record in the local hymnal catalogue predates this feature and therefore lacks the identifier the collection service requires, **When** the qualifying duration elapses, **Then** nothing is recorded, nothing is shown to the member, and no failure occurs.

---

### User Story 2 - Recorded views reach the church, eventually and exactly once (Priority: P1)

Views recorded on the device are delivered to the church's collection service. The member may be offline, on a plane, or out of signal for days; the views wait and are delivered when connectivity returns. Once the service has confirmed the outcome of a view — whether it stored it, deduplicated it, or refused it — the device stops holding it.

**Why this priority**: A record that never leaves the device is worthless to the church. Together with Story 1 this forms the complete feature; the two are jointly the MVP.

**Independent Test**: Record several views with the network disabled, confirm they persist; restore the network, and confirm they are delivered and the local store drains to empty, with no view delivered twice and none left behind.

**Acceptance Scenarios**:

1. **Given** views are waiting locally and the device has connectivity, **When** delivery runs, **Then** the views are submitted and every view the service answered for is removed from the local store.
2. **Given** the service accepts some views and refuses others, **When** the response is processed, **Then** both the accepted and the refused views are removed — a refused view is never retried — and each refusal reason is written to the diagnostic log.
3. **Given** a view was submitted but appears in neither the accepted nor the refused part of the response, **When** the response is processed, **Then** that view is removed as well, so nothing can accumulate indefinitely.
4. **Given** the device has no connectivity, **When** views are recorded, **Then** they remain stored and delivery is deferred until connectivity is available.
5. **Given** delivery fails for any reason — network error, service error, or the service reporting that the device is sending too often — **When** the failure occurs, **Then** the waiting views are kept, delivery is retried later with progressively longer waits, and the member sees nothing: no message, no error state, no interruption.
6. **Given** views are waiting and the member force-closes the app or reboots the device, **When** the device next has connectivity, **Then** delivery still happens without the member reopening the app.
7. **Given** more views are waiting than the service will accept in a single submission, **When** delivery runs, **Then** they are submitted in several smaller submissions, each within the permitted size.
8. **Given** several views are recorded in quick succession while a delivery is already running, **When** delivery completes, **Then** no waiting view is lost and no duplicate delivery job is started.

---

### User Story 3 - Collection behaves as the church configures it (Priority: P2)

The church can change how long a member must stay on a hymn for it to count, and how much the app may submit at once. The app reads that configuration from the service and honours it, and keeps working sensibly when it cannot reach the service.

**Why this priority**: The feature is fully functional on its built-in defaults; remote configuration lets the church tune the signal without shipping an app update. Valuable, but not required for the first useful version.

**Independent Test**: Change the qualifying duration on the service, restart the app, and confirm the timer uses the new value. Then disable the network, restart, and confirm the last known value is still used.

**Acceptance Scenarios**:

1. **Given** the app starts with connectivity, **When** it reads the collection configuration, **Then** the qualifying duration and the permitted submission size are stored for later use.
2. **Given** the app starts without connectivity and configuration was read at some earlier point, **When** a hymn is opened, **Then** the most recently known values are used.
3. **Given** the app starts on a fresh install without connectivity, **When** a hymn is opened, **Then** built-in defaults are used — a thirty-second qualifying duration and a conservative submission size — and collection works normally.
4. **Given** reading the configuration fails, **When** the failure occurs, **Then** nothing is shown to the member and the app continues with the values it already has.

---

### User Story 4 - Collection identifies a device, never a person (Priority: P2)

So the church can distinguish "twenty members opened this hymn" from "one member opened it twenty times", each device carries an identifier. That identifier is randomly generated by the app, reveals nothing about the device or its owner, and requires no permission.

**Why this priority**: Without it the counts are still collected but are harder to interpret. It is a quality-of-data concern, and a privacy commitment that must be honoured from the first release rather than retrofitted.

**Independent Test**: Install the app fresh and inspect the identifier: it is a random value, matching no hardware or platform identifier. Update the app and confirm the identifier is unchanged. Uninstall, reinstall, and confirm a new one is generated.

**Acceptance Scenarios**:

1. **Given** a fresh install, **When** the app first needs the device identifier, **Then** a random identifier is generated and stored.
2. **Given** an identifier already exists, **When** the app restarts or is updated, **Then** the same identifier is reused.
3. **Given** the app is uninstalled and reinstalled, **When** the identifier is next needed, **Then** a new random one is generated — the only consequence is that the service can no longer tell the new install is the same device, which is acceptable.
4. **Given** the feature is active, **When** any collected data is examined, **Then** it contains no hardware identifier, no advertising identifier, and no platform-assigned device identifier, and the app requests no additional permission for this feature.

---

### Edge Cases

- **A hymn opened and immediately closed.** No view is recorded. Counting is tied to the hymn being on screen; anything below the qualifying duration produces nothing.
- **A hymn left open overnight with the screen off.** The screen being off is not viewing. Counting is paused whenever the app is not in the foreground, so an overnight idle produces at most the foreground time actually accumulated.
- **The member reopens the same hymn a minute later.** A second view is recorded. This is intentional: the app deliberately does not try to decide whether two nearby views are "really" the same reading. The collection service collapses views of the same hymn from the same device within its own window and reports the collapsed one as accepted, at which point the device discards it.
- **Views accumulate faster than they can be delivered — a long offline period.** The local store holds a fixed maximum of two thousand views. On overflow the oldest is discarded first. Nothing of value is lost: the service refuses views older than its retention window in any case.
- **The device clock is wrong.** The recorded moment carries the device's own time and offset. The service refuses views dated in the future or too far in the past, and the app discards them on that answer rather than retrying forever.
- **The service is unreachable for days.** Delivery keeps being retried at increasing intervals. Views wait, subject to the store's maximum. The member is never told.
- **The service reports that the device is submitting too often.** Delivery stops for that attempt, the waiting views are kept intact, and delivery is retried later.
- **The member is signed in, then signs out, while views are waiting.** Delivery continues either way. Views submitted while signed in are attributed to that member by the service; views submitted while signed out are anonymous. Neither case may cause a sign-in prompt, a token refresh loop, or an involuntary sign-out.
- **The app is updated while views are waiting.** The waiting views survive the update and are delivered afterwards.
- **The hymnal catalogue cached before this feature existed.** Hymns in it lack the identifier the service requires. Those views are silently skipped until the catalogue is next refreshed, after which collection works normally for them.
- **Two views are recorded at almost the same instant.** Both are stored. Writes to the local store are serialised so one cannot overwrite the other.

---

## Requirements *(mandatory)*

### Functional Requirements

#### Recording a view

- **FR-001**: The app MUST measure, for each visit to a hymn's detail view, the cumulative time that hymn is displayed while the app is in the foreground.
- **FR-002**: Measurement MUST pause when the app leaves the foreground or the device screen turns off, and MUST resume — from the accumulated total, not from zero — when the hymn is displayed in the foreground again.
- **FR-003**: When the cumulative foreground time first reaches the configured qualifying duration, the app MUST record exactly one view for that visit, and MUST NOT record further views for the same visit however long it continues.
- **FR-004**: Leaving the hymn's detail view and returning to it MUST begin a new visit with a new cumulative count, which may produce a further view.
- **FR-005**: The app MUST NOT attempt any local deduplication or collapsing of nearby views; deciding whether two views represent one reading belongs to the collection service.
- **FR-006**: Each recorded view MUST carry: a unique identifier generated by the app, the identifier of the hymn viewed, the device identifier, the moment the qualifying duration was reached, the cumulative foreground seconds counted at that moment, the app's version, and the platform name.
- **FR-007**: The recorded moment MUST include the device's UTC offset. A moment without an offset MUST never be recorded or submitted.
- **FR-008**: The recorded duration MUST never be negative.
- **FR-009**: When the hymn being viewed has no identifier of the kind the collection service requires — as is the case for catalogue data cached before this feature — the app MUST record nothing, MUST NOT display anything, and MUST NOT fail.

#### Holding views locally

- **FR-010**: Recorded views MUST be stored on the device and MUST survive the app being closed, force-stopped, restarted, and updated.
- **FR-011**: The local store MUST support appending single views and removing arbitrary views, and MUST serialise concurrent writes so that two views recorded at nearly the same instant are both retained.
- **FR-012**: The local store MUST hold at most 2000 views. When a new view would exceed that, the oldest view MUST be discarded to make room.
- **FR-013**: A view MUST be removed from the local store only after the collection service has answered for it, or because it was discarded under FR-012.

#### Delivering views

- **FR-014**: The app MUST submit waiting views to the collection service whenever the device has network connectivity, without requiring the member to have the app open.
- **FR-015**: Delivery MUST be scheduled when a new view is recorded, MUST survive the app being closed and the device rebooting, and MUST NOT allow more than one delivery job for this feature to be pending or running at a time.
- **FR-016**: A single submission MUST contain no more views than the configured permitted submission size; a larger backlog MUST be split across several submissions.
- **FR-017**: On a successful submission, the app MUST remove every view the service reported as accepted, every view the service reported as refused, and every view it submitted that the service mentioned in neither list.
- **FR-018**: For each refused view, the app MUST write the refusal reason to the diagnostic log. Recognised reasons are: unknown hymn, moment in the future, moment too old, and malformed view.
- **FR-019**: On a failed submission — network failure, service error, or the service reporting the device is submitting too often — the app MUST retain all waiting views intact and MUST retry later, with the interval between retries growing after each successive failure.
- **FR-020**: The app MUST submit views with the member's credentials attached when a member is signed in, so the service can attribute the view, and without credentials when no member is signed in.
- **FR-021**: A submission made without credentials MUST NOT be able to trigger a credential-refresh attempt or sign the member out.
- **FR-022**: No outcome of delivery — success, refusal, throttling, or failure — may produce any user-visible effect: no message, no indicator, no error state, no interruption, no crash.

#### Configuration

- **FR-023**: The app MUST read the collection configuration from the service at startup and store the qualifying duration and the permitted submission size for later use.
- **FR-024**: When the configuration cannot be read, the app MUST use the most recently stored values; on a fresh install with no stored values, it MUST use built-in defaults of thirty seconds and a conservative submission size.
- **FR-025**: Failure to read the configuration MUST NOT produce any user-visible effect and MUST NOT prevent views from being recorded or delivered.

#### Device identity

- **FR-026**: The app MUST generate a random device identifier on first need and store it on the device.
- **FR-027**: The device identifier MUST persist across app restarts and app updates, and MUST be non-empty and no longer than 64 characters.
- **FR-028**: The device identifier MUST NOT be derived from any hardware identifier, advertising identifier, or platform-assigned device identifier, and obtaining it MUST NOT require any permission.
- **FR-029**: Regeneration of the identifier after a reinstall is acceptable and MUST NOT be treated as an error condition.

#### Invisibility

- **FR-030**: The feature MUST add no screen, no navigation destination, no setting, and no visible control.
- **FR-031**: No part of this feature may block, delay, or otherwise degrade the responsiveness of opening or reading a hymn.

### Key Entities

- **Hymn View**: One occasion on which a member had a hymn on screen long enough for it to count. Carries its own unique identifier, the hymn it refers to, the device that produced it, the moment it qualified (with UTC offset), how many foreground seconds had accumulated at that moment, the app version, and the platform. Once the collection service has answered for it, it has no further use on the device.
- **View Queue**: The device-local holding area for hymn views awaiting delivery. Ordered oldest-first, capped at 2000, appended to and removed from individually, and durable across restarts and updates.
- **Device Identity**: A single random value identifying this installation. Anonymous, permissionless, stable across updates, regenerated on reinstall.
- **Collection Settings**: The church-controlled parameters governing collection. The app consumes two — the qualifying duration and the permitted submission size — and falls back to the last known values, then to built-in defaults.
- **Hymn Identifier**: The stable identifier the collection service uses to name a hymn. Distinct from the hymn *number* shown to members and currently used for navigation. Absent from catalogue data cached before this feature, in which case views for that hymn cannot be reported.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Across 20 trial visits in which a hymn is held on screen past the qualifying duration, exactly 20 views are recorded — one per visit, never zero, never two.
- **SC-002**: Across 20 trial visits shorter than the qualifying duration, zero views are recorded.
- **SC-003**: A visit interrupted by backgrounding the app produces a view once the *combined* foreground time reaches the qualifying duration, with the recorded duration within one second of the true foreground total.
- **SC-004**: 100% of views recorded while offline are still present after the app is force-closed, the device is rebooted, and the app is updated.
- **SC-005**: Once connectivity is restored, all waiting views are delivered and the local store drains to empty within two minutes.
- **SC-006**: After any successful submission, the number of views remaining locally equals the number never submitted — every submitted view is removed regardless of whether the service accepted it, refused it, or ignored it. Zero views are delivered twice.
- **SC-007**: The local store never exceeds 2000 views, verified by recording 2500 views with delivery disabled and confirming exactly the 500 oldest were discarded.
- **SC-008**: Over a week of ordinary use with the collection service unreachable, the feature produces zero user-visible messages, zero error states, and zero crashes.
- **SC-009**: A change to the qualifying duration made by the church takes effect on a device within one app restart.
- **SC-010**: Data collected about any member contains no hardware identifier, no advertising identifier, and no platform-assigned device identifier; the app's permission set is unchanged by this feature.
- **SC-011**: The church can determine, for any given week, which hymns were opened and how many distinct devices opened each — the outcome that justifies the feature.

---

## Out of Scope

- Any administrative or reporting screen in the app. The collection service exposes view occurrences, top-hymn rankings, settings editing, and service-window management, all restricted to administrators. Surfacing any of these is a separate feature.
- Editing the collection settings from the app. The app reads them only.
- Collecting usage of anything other than hymn detail views — song lyrics, chord charts, studies, and the Bible are untouched.
- Any member-facing control to enable, disable, or inspect collection.
- Local deduplication or collapsing of nearby views.

---

## Dependencies

- **D-001 (blocking, external): the hymnal catalogue must expose the service's hymn identifier.** The catalogue endpoint currently returns each hymn's number, title, and lyrics, with no stable primary key; the app's hymn model and its detail navigation both use the hymn *number* as the identifier. The collection service, however, names hymns by an integer identifier. The backend project must therefore add that identifier to the hymnal payload — an additive, non-breaking change — and the app must carry it alongside the number through the catalogue transfer object, the domain model, and the cached catalogue. Until this lands, the feature records nothing (FR-009). This change belongs to the backend project and its songs domain spec, not to this feature's implementation.
- **D-002 (satisfied): the collection service exists and is implemented.** Its ingest and settings endpoints are live, and this feature must match their contract exactly rather than negotiate it. See *External Contract* below.
- **D-003 (satisfied): background scheduling and persistent local storage are already available** in the app and are to be reused rather than replaced.

---

## External Contract *(given — the service is already built; the app conforms to it)*

This section records constraints imposed on the app by an existing, unmodifiable service. It is requirement, not design.

### Submitting views — `POST /ipbcb/api/hymnal-history/events/`

Requires no authentication, but accepts it. Rate-limited to 600 requests per hour per client address.

Each submitted view carries exactly seven fields, and no others:

| Field | Rule |
|-------|------|
| `client_event_id` | Must be a parseable UUID. A view with an unparseable one is silently dropped by the service and appears in neither response list. |
| `hymn_id` | The service's integer identifier for the hymn (see D-001). |
| `device_id` | Non-blank, at most 64 characters. |
| `viewed_at` | Must carry a UTC offset. A moment without one is refused. |
| `duration_seconds` | Must be zero or greater. |
| `app_version` | Optional; defaults to empty. |
| `platform` | Optional; defaults to empty. |

**Unknown fields are forbidden.** Any extra key makes that view fail parsing and come back refused as `invalid_event`. Exactly these seven fields are to be sent.

A successful submission returns the identifiers it accepted and the identifiers it refused, each refusal carrying one of four stable reasons: `unknown_hymn`, `viewed_at_in_future`, `viewed_at_too_old`, `invalid_event`. Acceptance means stored, deduplicated, or collapsed — all three mean the device may forget the view. Refusal also means the device may forget it; the reason exists precisely so that no view retries forever.

Submitting more views than the permitted submission size fails the whole submission. Exceeding the rate limit fails the submission without loss.

Errors follow the project-wide shape: an error code, a detail message, and optional field errors.

### Reading configuration — `GET /ipbcb/api/hymnal-history/settings/`

Public read. Returns the qualifying duration (default 30 seconds), the collapse window, the permitted submission size, the retention window, the future tolerance, and the service-window grace. The app consumes only the first and the third. Editing these settings is administrator-only and out of scope.

---

## Implementation Constraints *(directed by the requester — inputs to `/speckit-plan`)*

These are decisions the requester has already made. They are recorded here so planning does not revisit them; they are deliberately kept out of the functional requirements above.

- The feature lives under the hymnal feature area, except the device identifier, which belongs to the shared core since it is not hymnal-specific. Feature areas must not import one another.
- The layering convention applies unchanged: UI → ViewModel → UseCase → Repository interface → Repository implementation, with dependency injection throughout. Errors surface as sealed types or results; no raw HTTP exception may rise above the repository.
- The view queue reuses the existing JSON snapshot storage mechanism in the shared core — the same one backing the hymnal and schedule caches. **No new database is to be introduced.** Unlike those caches, this queue is a mutable append-and-remove structure rather than a whole-blob replacement, so writes must be serialised through a mutex or a single-writer coroutine.
- The device identifier is stored in the existing preference datastore.
- Delivery uses the existing background work scheduler with a network constraint, unique work to prevent duplicate jobs, and exponential backoff.
- Submissions use the authenticated network client when a member is signed in and the unauthenticated client otherwise — never the authenticated client anonymously, which would risk driving the token authenticator into a refresh-or-sign-out cycle.
- Testing uses the project's existing stack, with fakes preferred over mocks, and a happy path plus at least one error path per use case. Coverage must at minimum include: the timer pausing and resuming across backgrounding; the threshold firing exactly once per visit; the queue surviving a restart; the queue cap discarding the oldest; and delivery removing exactly the views the service answered for.
- The hymnal domain spec at `specs/hymnal/spec.md` must be updated to describe this addition, in the same commit as the code, per the project's spec-driven workflow.

---

## Assumptions

- **Sunday and weekday usage are distinguished by the service, not the app.** The app reports when a view happened; deciding whether that falls inside a service window is the collection service's job. The app has no notion of service windows.
- **The qualifying duration in force for a visit is the one known when the hymn was opened.** A configuration change arriving mid-visit does not retroactively alter a count already under way.
- **"Conservative submission size" on a fresh offline install means 50 views per submission** — comfortably below any plausible server limit, at the cost of a few extra submissions in the rare case that a large backlog is delivered before configuration is ever read.
- **Waiting views are not expired locally by age.** The 2000-view cap is the only bound. Stale views are refused by the service on submission and discarded on that answer, which is simpler and equally effective.
- **The platform value is the constant `android`, and the app version is the version name of the running build.**
- **No member-facing opt-out is provided**, as directed. This is defensible because what is collected is anonymous by construction: a random per-install identifier, a hymn, a moment, and a duration — no name, no contact detail, no location, and no device or hardware identifier. Should the church later require an opt-out, it is an additive change.
- **The member's identity, when signed in, is attached by the service from the credentials on the request** — the app never places a member identifier in the view itself.
- **Diagnostic logging of refusal reasons carries no personal data** and is subject to the project's existing rule against logging personal data in release builds.
- **Views recorded before D-001 lands are simply not produced**, rather than produced-and-quarantined. There is no backfill: usage before the catalogue exposes hymn identifiers is not recoverable, which is acceptable for a feature whose value is ongoing rather than historical.
