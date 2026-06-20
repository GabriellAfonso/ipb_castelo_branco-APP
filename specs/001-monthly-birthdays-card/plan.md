# Implementation Plan: Monthly Birthdays Highlight Card

**Branch**: `001-monthly-birthdays-card` | **Date**: 2026-06-20 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/001-monthly-birthdays-card/spec.md`

## Summary

Display monthly birthdays in the home screen highlight carousel by fetching from `GET /ipbcb/members/birthdays/?month={1-12}`, following the existing snapshot-cached MVVM architecture. Replaces the static `HighlightBirthdays()` placeholder with a dynamic, visually polished card.

## Technical Context

**Language/Version**: Kotlin 1.9+, targeting Android API 26+

**Primary Dependencies**: Jetpack Compose, Hilt, Retrofit + kotlinx.serialization, Coroutines/Flow

**Storage**: JsonSnapshotStorage (file-based JSON cache, existing pattern)

**Testing**: JUnit4 + MockK + kotlinx-coroutines-test + Turbine

**Target Platform**: Android (mobile-app)

**Project Type**: mobile-app (single `:app` module, feature-based MVVM + Clean)

**Performance Goals**: Birthdays visible within 2 seconds of app open (cached from disk immediately, network refresh in background)

**Constraints**: Offline-capable via snapshot cache; JWT auth required

**Scale/Scope**: Single highlight card, single API endpoint, ~5 new files + modifications to 3 existing files

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Constitution is not yet customized for this project (template placeholders only). Default architecture rules from CLAUDE.md apply:

- **Feature isolation**: Feature lives in `core/` per spec (small, shared feature) — no cross-feature imports needed. PASS.
- **Domain purity**: Domain model and use case have no Android knowledge. PASS.
- **Presentation boundary**: UI only talks to ViewModel. PASS.
- **Error handling**: Errors as sealed classes (`SnapshotState`). PASS.
- **No raw HTTP exceptions in ViewModel**: Handled by `BaseSnapshotRepository`. PASS.

All gates pass. No violations.

## Project Structure

### Documentation (this feature)

```text
specs/001-monthly-birthdays-card/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── birthdays-api.md
└── tasks.md             # Phase 2 output (/speckit-tasks)
```

### Source Code (repository root)

```text
app/src/main/java/com/ipb/castelobranco/
├── core/
│   ├── data/
│   │   ├── api/
│   │   │   └── MembersApi.kt                    # NEW — Retrofit interface
│   │   ├── dto/
│   │   │   └── BirthdayDtos.kt                  # NEW — BirthdaysResponseDto, BirthdayDto
│   │   ├── mapper/
│   │   │   └── BirthdayMapper.kt                # NEW — DTO → Domain mapping
│   │   ├── repository/
│   │   │   └── MembersRepositoryImpl.kt         # NEW — BaseSnapshotRepository impl
│   │   └── snapshot/
│   │       └── BirthdaySnapshotFetcher.kt       # NEW — RetrofitSnapshotFetcher
│   ├── di/
│   │   ├── MembersModule.kt                     # NEW — API + repo binding + preload/refresh
│   │   └── BirthdaySnapshotModule.kt            # NEW — SnapshotCache + SnapshotFetcher
│   ├── domain/
│   │   ├── model/
│   │   │   └── Birthday.kt                      # NEW — Domain model
│   │   ├── repository/
│   │   │   └── MembersRepository.kt             # NEW — Repository interface
│   │   └── usecase/
│   │       └── GetMonthlyBirthdaysUseCase.kt    # NEW — Observe + refresh
│   └── presentation/
│       ├── viewmodel/
│       │   └── CoreViewModel.kt                 # MODIFY — Add birthday state
│       ├── screens/
│       │   └── CoreScreen.kt                    # MODIFY — Pass birthdays to highlight
│       └── components/
│           └── Highlight.kt                     # MODIFY — Dynamic HighlightBirthdays
└── (tests)
    └── core/
        ├── domain/usecase/
        │   └── GetMonthlyBirthdaysUseCaseTest.kt
        └── data/repository/
            └── MembersRepositoryImplTest.kt
```

**Structure Decision**: All new code in `core/` following existing patterns. Feature is too small and shared (home screen) for a separate feature module. Follows same layered structure as schedule feature but scoped to core.
