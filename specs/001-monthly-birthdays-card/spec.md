# Feature Specification: Monthly Birthdays Highlight Card

**Feature Branch**: `001-monthly-birthdays-card`

**Created**: 2026-06-20

**Status**: Implemented

**Input**: User description: "Monthly Birthdays highlight card on the home screen (CoreScreen). Fetches monthly birthdays from API, displays in existing Highlight carousel, shows member name and birth day ordered by day ascending."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Current Month Birthdays (Priority: P1)

As a church member, I want to see who has birthdays this month on the home screen so I can congratulate them and feel connected to the community.

**Why this priority**: Core value of the feature — without this, the feature has no purpose.

**Independent Test**: Can be fully tested by opening the app home screen and verifying birthday cards appear in the highlight carousel with correct names and days for the current month.

**Acceptance Scenarios**:

1. **Given** the current month has birthdays registered, **When** the user opens the home screen, **Then** a birthday highlight card displays all members with birthdays this month, showing each member's name and day of birth, ordered by day ascending.
2. **Given** the current month has no birthdays registered, **When** the user opens the home screen, **Then** the birthday highlight card shows an empty state message indicating no birthdays this month.
3. **Given** the user is not authenticated, **When** they open the home screen, **Then** the birthday card is not shown (feature requires authentication).

---

### User Story 2 - Offline Birthday Access (Priority: P2)

As a church member with unreliable connectivity, I want to see birthdays even when offline so the information is always available.

**Why this priority**: Enhances reliability but the feature is still valuable without offline support on first use.

**Independent Test**: Can be tested by loading birthdays once with connectivity, then enabling airplane mode and reopening the app to verify cached birthdays still appear.

**Acceptance Scenarios**:

1. **Given** birthdays were previously loaded and cached, **When** the user opens the app without internet, **Then** the cached birthdays for the current month are displayed.
2. **Given** no cached birthdays exist and the user is offline, **When** they open the app, **Then** the birthday card shows the empty state placeholder.

---

### Edge Cases

- What happens when the API returns an error (500, timeout)? The card shows cached data if available, otherwise shows the empty state — no error is surfaced to the user for this non-critical feature.
- What happens on the 1st of a new month? The cache from the previous month is stale; a fresh API call is made. If it fails, the old cache is shown until a successful refresh.
- What happens when a member's name is very long? The name is truncated with ellipsis to maintain card layout.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST fetch birthdays for the current month from the API on home screen load.
- **FR-002**: System MUST display each birthday entry with the member's name and day of the month.
- **FR-003**: System MUST order birthday entries by day ascending (earliest day first).
- **FR-004**: System MUST show an empty state with a placeholder message when no birthdays exist for the current month.
- **FR-005**: System MUST cache birthday data locally for offline access following the existing snapshot cache pattern.
- **FR-006**: System MUST require user authentication to fetch birthday data.
- **FR-007**: System MUST display the birthday card within the existing highlight carousel on the home screen, replacing the current static placeholder.

### Key Entities

- **Birthday**: Represents a member's birthday entry — contains the member's name (text) and birth day of the month (integer 1-31).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users see current month birthdays on the home screen within 2 seconds of opening the app (with network).
- **SC-002**: Birthday information is available offline after at least one successful load.
- **SC-003**: All birthdays for the current month are displayed in correct day-ascending order.
- **SC-004**: Empty months display a clear, friendly placeholder message.

## Assumptions

- Users are authenticated before viewing the home screen (birthday data requires JWT auth).
- The API endpoint `GET /ipbcb/members/birthdays/?month={1-12}` is available and returns the documented response format.
- The existing highlight carousel on CoreScreen supports dynamic content cards.
- Birthday data is read-only — no CRUD operations needed.
- Only the current month is displayed — no month navigation or selector.
- No admin panel integration or member detail screens are in scope.
