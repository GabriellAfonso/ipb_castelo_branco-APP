# CLAUDE.md — IPB Castelo Branco (Android)

## Overview

Android app for the Igreja Presbiteriana de Castelo Branco (local church). Consumes a Django REST API located at `../backend` relative to the workspace root. Features include a photo gallery, worship song tracking, monthly schedule, and more.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Dependency Injection:** Hilt
- **Networking:** Retrofit
- **Async:** Coroutines + Flow
- **Build:** Gradle with Kotlin DSL (`.kts`)
- **Build:** Done manually by the developer via Android Studio

## Architecture

The project follows **Clean Architecture** organized by **features**, with MVVM in the presentation layer.

### Feature layer structure

```
features/<name>/
├── data/
│   ├── api/          # Retrofit interfaces
│   ├── dto/          # Data Transfer Objects (API response)
│   ├── mapper/       # DTO → Domain Model conversion
│   ├── local/        # Local cache (when applicable)
│   ├── repository/   # Repository implementations
│   └── snapshot/     # Offline/snapshot data (when applicable)
├── di/               # Hilt modules for the feature
├── domain/
│   ├── model/        # Domain entities
│   ├── repository/   # Repository interfaces
│   └── usecase/      # Use cases (when applicable)
└── presentation/
    ├── screens/      # Screen composables
    ├── viewmodel/    # ViewModels
    ├── components/   # Feature-scoped reusable composables
    ├── state/        # UI State classes
    └── navigation/   # Feature navigation routes (when applicable)
```

### Core structure

```
core/
├── data/
│   ├── local/        # Shared local storage
│   ├── logger/       # Logging
│   ├── repository/   # Shared repository base/utilities
│   └── snapshot/     # Shared snapshot logic
├── di/               # Global Hilt modules
├── domain/
│   ├── repository/   # Global repository contracts
│   └── snapshot/
├── network/          # Retrofit/OkHttp setup
├── ui/
│   ├── base/         # Base UI classes
│   ├── components/   # Shared composables
│   └── theme/        # App theme (colors, typography, shapes)
└── utils/            # General utilities
```

## Features

| Feature | Description |
|---|---|
| `auth` | User authentication |
| `gallery` | Church photo gallery |
| `hymnal` | Hymnal / songs |
| `schedule` | Monthly schedule |
| `admin/register` | Register songs played during worship |
| `admin/schedule` | Admin schedule management |
| `admin/panel` | Admin panel |
| `profile` | User profile |
| `settings` | App settings |
| `worshiphub` | Worship hub / service tables |
| `main` | Root screen / main navigation |

## Backend (Django)

The API lives in the same workspace at `../backend`. Before implementing any network calls or new endpoints, **check the Django views directly** to understand the API contracts (fields, types, behavior).

## Testing

The project has no tests yet. When writing tests:

- Use **JUnit4** + **Mockk** for unit tests
- Focus on: **ViewModels**, **Use Cases**, and **Mappers**
- Name tests using `given_when_then` or `should_when` patterns
- Place tests in `src/test/java/...` mirroring the source package
- Use `UnconfinedTestDispatcher` for testing coroutines/Flow in ViewModels

## Code Conventions

- **UI State:** `<Feature>UiState` (e.g. `ScheduleUiState`)
- **ViewModel:** `<Feature>ViewModel` (e.g. `HymnalViewModel`)
- **DTOs:** `Dto` suffix (e.g. `SongDto`)
- **Domain Models:** no suffix (e.g. `Song`)
- **Mappers:** extension function `fun XDto.toDomain(): X` or class `XMapper`
- **Feature entry points:** `entry/` folder with the feature's nav graph registration

## Base Package

```
com.gabrielafonso.ipb.castelobranco
```
