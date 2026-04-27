# CLAUDE.md — IPB Castelo Branco

## Project

- **Package:** `com.ipb.castelobranco`
- **API base:** `https://gabrielafonso.com.br/ipbcb/`
- **Single module:** `:app` — all code under `app/src/main/java/`

## Architecture — Feature-Based MVVM + Clean

**Single Activity:** `CoreActivity` is the ONLY `@AndroidEntryPoint`. UI 100% Compose.

Flow: `UI → ViewModel → UseCase → Repository (interface) → Repository (impl)`

```
core/
├── data/          — DataStore, repositories, snapshot cache
├── di/            — Hilt modules
├── domain/        — Interfaces, use cases, AuthEventBus
├── network/       — AuthInterceptor, TokenAuthenticator
└── presentation/
    ├── CoreActivity.kt
    ├── viewmodel/CoreViewModel.kt
    ├── screens/CoreView.kt
    └── navigation/  — AppRoutes, AppNavHost, LocalAppNavigator
features/
├── auth/ | profile/ | schedule/ | settings/
└── gallery/ | hymnal/ | worshiphub/ | admin/
```

**Rules:**
- Features never import each other — share via `core/`.
- `domain/` has no Android knowledge (no `Context`, `ViewModel`, Compose).
- `presentation/` only knows the ViewModel — never accesses repo/use case directly.
- Errors are sealed classes or `Result<T>` — raw HTTP exceptions never reach the ViewModel.

## UI — Jetpack Compose

- Every screen has `sealed class UiState` with `Loading`, `Success`, `Error` — always handle all three.
- ViewModel exposes `StateFlow<UiState>`; Screen collects with `collectAsStateWithLifecycle()`.
- Composables are **dumb**: receive state, emit events via lambdas. Zero logic.
- Separate Screen (VM collector) from content Composable (pure data) — enables previews.
- `@Preview` with explicit fake data on every content Composable.
- Naming: PascalCase, no prefix (`ScheduleScreen`).

## ViewModel

- Exposes only `StateFlow`/`SharedFlow`. Never `LiveData`.
- Use `viewModelScope`. Never `GlobalScope` or manual `CoroutineScope`.
- One-time events (snackbar, dialog, nav) via `SharedFlow<UiEvent>`.
- No `Context` in ViewModel — use parameter or `UiText`.
- Multi-screen graphs: use graph-scoped `hiltViewModel(graphEntry)` (see Pitfalls #1).

## Navigation

- Multiple screens → `NavGraphBuilder.xGraph()` in `XNavGraph.kt`.
- Single screen → inline `composable {}` in `AppNavHost`.
- Existing graphs: `authGraph`, `adminGraph`, `worshipHubGraph`, `hymnalGraph`, `galleryGraph`.
- Register routes in `AppRoutes` before use.

## DI — Custom Qualifiers

```kotlin
@AuthedRetrofit / @AuthLessRetrofit   // protected / public APIs
@Client / @AuthLessClient             // authenticated / unauthenticated OkHttpClient
@AuthPrefs / @SettingsPrefs           // DataStores
@ApiBaseUrl                           // base URL string
```

Wrong qualifier on protected API → silent 401.

## Security

- Never hardcode keys/secrets — use `local.properties` + `BuildConfig`.
- Auth tokens: pending migration to `EncryptedSharedPreferences` (currently plain DataStore).
- No `Log.d` with PII in production. ProGuard/R8 active in release.

## Tests

Stack: `JUnit4` + `MockK` + `kotlinx-coroutines-test` + `turbine`. Minimum: happy path + 1 error per use case. Prefer fakes over mocks.

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:testDebugUnitTest --tests "com.ipb.castelobranco.core.*"
```

**Gradle cache — never use** `clean`, `--rerun-tasks`, or `--no-daemon` before running tests (destroys KSP cache, turns seconds into 15+ min). Use `clean` only for unexplainable build errors.

`sdk.dir` in `local.properties` → `/home/node/.local/android-sdk`

## Code

- All code in English. User-visible strings in Portuguese (hardcoded, no `strings.xml`).
- Line limit: 120 chars. Kotlin code style: `official`. No magic strings — use constants.
- No unnecessary abstractions. Check compatibility before adding libs.

## Git

- Conventional Commits: `feat`, `fix`, `refactor`, `chore`, `test`, `docs`.
- Version bump in separate commit: `chore(release): bump version to X.Y.Z`.

## Pitfalls

1. **Graph-scoped VM:** In existing graphs (hymnal, gallery, worshiphub, admin), use `hiltViewModel(graphEntry)` — not bare `hiltViewModel()`.
2. **Single screen vs. graph:** Check `AppNavHost.kt` first — single screen = inline, multiple = graph.
3. **Snapshot cache:** Offline features follow the `JsonSnapshotStorage` pattern (`HymnalSnapshotModule`, `ScheduleSnapshotModule`...).
4. **UCropActivity** in Manifest — do not remove (profile photo upload).
5. **Theme change:** `context.findActivity()?.recreate()` — called from Screen, never from ViewModel.
6. **Auth/Logout:** Auth success via lambda `onAuthSuccess()` in `AppNavHost → authGraph`. Logout via `popUpTo(MAIN) { inclusive = true }`.
7. **Share intents:** `navController.context.startActivity(Intent.createChooser(...))`.
