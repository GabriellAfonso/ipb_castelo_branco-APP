# Core Module — Structural & Test Readiness Analysis

> Base package: `com.gabrielafonso.ipb.castelobranco`
> Analysis date: 2026-03-04
> Scope: `app/src/main/java/.../core/**`

---

## 1. Executive Summary

The core module is **architecturally sound but unevenly mature**. Its domain layer — sealed result types, interfaces for cache/fetch/logging, the `AppError` hierarchy, and the `AuthEventBus` — is clean, dependency-free, and ready for unit testing immediately. The data layer is well-abstracted behind interfaces, making it testable through mocking with minimal ceremony.

The presentation layer (`CoreActivity`, `CoreViewModel`, `BaseScreen`, `AppNavHost`) is deeply coupled to the Android framework, Jetpack Compose, and multiple feature modules simultaneously. It is not suitable for unit testing in its current form and is not expected to be in the short term.

Two structural problems require attention before testing can scale safely:

1. **`logTime()` — a top-level Android-coupled utility — lives inside the domain file `BaseSnapshotRepository.kt`**, importing `android.os.SystemClock` and `android.util.Log` into the domain layer. This is a layering violation.
2. **`DiskCache` (core/utils/)** uses Gson while the rest of the codebase uses kotlinx.serialization. It predates the snapshot infrastructure and is not used by it. It is de facto legacy code.

**Overall testing readiness: Medium-High** for the domain and data layers. **Low** for the presentation layer.

---

## 2. Stability Classification

### 2.1 `core/domain/snapshot/` — Domain Contracts & Base Logic

---

#### `NetworkResult<T>`
| Field | Value |
|---|---|
| **File** | `core/domain/snapshot/NetworkResult.kt` |
| **Responsibility** | Models the three outcomes of a network fetch: `Success<T>` (body + etag), `NotModified`, `Failure` (throwable). |
| **Dependencies** | None (pure Kotlin sealed class). |
| **Volatility** | Low — the three outcomes are exhaustive and stable by design. |
| **Test recommendation** | Unit — as a supporting type used in assertions, not as a direct test subject. |
| **Rationale** | No logic to test. Value is as a type constraint verified at compile time. Verify instantiation and exhaustive `when` matching in consuming tests. |

---

#### `RefreshResult`
| Field | Value |
|---|---|
| **File** | `core/domain/snapshot/RefreshResult.kt` |
| **Responsibility** | Models the four outcomes of a cache refresh: `Updated`, `NotModified`, `CacheUsed`, `Error`. |
| **Dependencies** | None (pure Kotlin sealed class). |
| **Volatility** | Low. |
| **Test recommendation** | Unit — as assertion type in `BaseSnapshotRepository` tests. |
| **Rationale** | Same as `NetworkResult`: no standalone logic, value is as a discriminated result in consumer tests. |

---

#### `SnapshotState<T>`
| Field | Value |
|---|---|
| **File** | `core/domain/snapshot/SnapshotState.kt` |
| **Responsibility** | Models the three observable states of a data snapshot: `Loading`, `Data<T>`, `Error`. |
| **Dependencies** | None. |
| **Volatility** | Low. |
| **Test recommendation** | Unit — as assertion type. |
| **Rationale** | Sealed class with data-only subtypes. No standalone logic to exercise. |

---

#### `SnapshotCache<Dto>` / `SnapshotFetcher<Dto>` / `Logger`
| Field | Value |
|---|---|
| **Files** | `SnapshotCache.kt`, `SnapshotFetcher.kt`, `Logger.kt` |
| **Responsibility** | Domain contracts (interfaces) for the cache layer, network fetch layer, and logging respectively. `Logger.Noop` is a built-in stub. |
| **Dependencies** | None (pure interfaces). |
| **Volatility** | Low — these are stable contracts; the `Logger.Noop` idiom eliminates the need for a mock logger in tests. |
| **Test recommendation** | Not directly testable — these are contracts. Their implementations are the test subjects. |
| **Rationale** | Serve as mock targets in collaborator tests. |

---

#### `BaseSnapshotRepository<Dto, Domain>`
| Field | Value |
|---|---|
| **File** | `core/domain/snapshot/BaseSnapshotRepository.kt` |
| **Responsibility** | Generic abstract repository implementing the snapshot pattern: preload from disk cache, refresh from network, expose state as `StateFlow<SnapshotState<Domain>>`. Encapsulates the cache-first + network-fallback algorithm. |
| **Dependencies** | `SnapshotCache<Dto>` (interface), `SnapshotFetcher<Dto>` (interface), `mapper: (Dto) -> Domain` (function), `Logger` (interface), `kotlinx.coroutines.Dispatchers`. Also co-located with the `logTime()` top-level function that imports `android.os.SystemClock` and `android.util.Log`. |
| **Volatility** | Low — the core algorithm (preload → refresh → fallback) is stable. |
| **Test recommendation** | **Unit — high priority.** |
| **Rationale** | Contains the most business-critical logic in the core module: the cache-first + network-refresh algorithm, the ETag handling, and the graceful degradation to cache on failure. All dependencies are interfaces and can be mocked trivially. The hardcoded `Dispatchers.IO` requires `StandardTestDispatcher` / `UnconfinedTestDispatcher` via `kotlinx-coroutines-test`. The Android imports (`SystemClock`, `Log`) belong to `logTime()`, a package-level function not called by the class itself — the class is Android-free and testable on the JVM. |

**Blocking issue:** `logTime()` is defined in the same file as `BaseSnapshotRepository` and imports Android types. This does not affect the class at runtime but it imports Android symbols into a file marked as domain code. If running unit tests that compile this file, the Android imports must be satisfied. This works because `android.jar` is provided on the test classpath in Android projects, but it is a layering smell. `logTime()` should be moved to `core/data/logger/` or removed.

---

### 2.2 `core/domain/error/` — Error Hierarchy

---

#### `AppError` + `Throwable.toAppError()` + `Result<T>.mapError()`
| Field | Value |
|---|---|
| **File** | `core/domain/error/AppError.kt` |
| **Responsibility** | Defines the canonical domain error hierarchy (`Network`, `Auth`, `Server`, `Unknown`) and two mapping extension functions that convert raw `Throwable`s into `AppError` subtypes. |
| **Dependencies** | `java.io.IOException` only. Zero Android, zero Hilt, zero coroutines. |
| **Volatility** | Low — the four categories cover all expected failure modes. New categories would be additive. |
| **Test recommendation** | **Unit — high priority.** |
| **Rationale** | Contains real branching logic in `toAppError()`: it must correctly distinguish `AppError` pass-through, `IOException` mapping, and fallback to `Unknown`. `mapError()` wraps `recoverCatching` — verify that a successful `Result` is unchanged and a failed one is mapped. These are pure functions. Zero setup required. |

---

### 2.3 `core/domain/auth/` — Cross-Feature Event Bus

---

#### `AuthEventBus`
| Field | Value |
|---|---|
| **File** | `core/domain/auth/AuthEventBus.kt` |
| **Responsibility** | Singleton Kotlin `SharedFlow`-based event bus for auth lifecycle events. Emits `LoginSuccess`. |
| **Dependencies** | `kotlinx.coroutines.flow.MutableSharedFlow`, `javax.inject` annotations (compile-time only). |
| **Volatility** | Low — the event set is minimal and additive by nature. |
| **Test recommendation** | **Unit — medium priority.** |
| **Rationale** | Verify that `emit()` delivers events to collectors and that `extraBufferCapacity = 1` prevents silent drops on `tryEmit`. Use `turbine` or `kotlinx-coroutines-test` `collect` with a `launch`. Hilt annotations (`@Singleton`, `@Inject`) do not affect testability. |

---

### 2.4 `core/domain/usecase/` — Application Use Cases

---

#### `PreloadDataUseCase`
| Field | Value |
|---|---|
| **File** | `core/domain/usecase/PreloadDataUseCase.kt` |
| **Responsibility** | Orchestrates the full app boot sequence: preload caches from disk, then refresh all feature data in parallel from the network. |
| **Dependencies** | `SongsRepository`, `HymnalRepository`, `ScheduleRepository`, `GalleryRepository` — all cross-feature repository interfaces. |
| **Volatility** | **High** — the commented-out lines (`songsRepository.preload()`, `hymnalRepository.preload()`, `profileRepository.preload()`) are direct evidence of ongoing instability. The set of preloaded features changes as features mature. |
| **Test recommendation** | Not yet recommended. |
| **Rationale** | The use case is essentially a sequencing script with no branching logic of its own. Testing it would only verify that it calls each dependency in order — a test of the wiring, not of any business rule. Given the commented-out code and ongoing changes, any test written now will have high maintenance cost. Write tests once the set of preloaded repositories stabilizes. |

---

### 2.5 `core/domain/repository/` — Repository Contract

---

#### `SnapshotRepository<T>`
| Field | Value |
|---|---|
| **File** | `core/domain/repository/SnapshotRepository.kt` |
| **Responsibility** | Generic repository contract: `observe(): Flow<T?>` and `refresh(): Boolean`. |
| **Dependencies** | None. |
| **Volatility** | Medium — note the **contract mismatch** with `BaseSnapshotRepository`: this interface returns `Flow<T?>` (raw nullable), while `BaseSnapshotRepository` exposes `StateFlow<SnapshotState<Domain>>` (wrapped sealed state). The two are not aligned. |
| **Test recommendation** | Not directly — contract only. Investigate whether this interface is actively used or is vestigial. |
| **Rationale** | If unused, it is dead code and should be removed before writing tests that depend on it. |

---

### 2.6 `core/data/snapshot/` — Data Layer Implementations

---

#### `SnapshotCodec<T>` / `JsonSnapshotCodec<T>`
| Field | Value |
|---|---|
| **Files** | `SnapshotCodec.kt`, `JsonSnapshotCodec.kt` |
| **Responsibility** | Interface for encode/decode of a DTO to/from a `String`. `JsonSnapshotCodec` implements it using `kotlinx.serialization.Json`. |
| **Dependencies** | `JsonSnapshotCodec` depends on `kotlinx.serialization.Json` and `KSerializer<T>`. No Android. |
| **Volatility** | Low. |
| **Test recommendation** | **Unit — high priority.** |
| **Rationale** | `JsonSnapshotCodec` has deterministic, round-trip behavior: `decode(encode(value)) == value`. This is directly verifiable with a real `Json` instance and a simple serializable DTO. No mocking needed. Tests guard against serialization regressions when DTO fields change. |

---

#### `LocalSnapshotCache<T>`
| Field | Value |
|---|---|
| **File** | `core/data/snapshot/LocalSnapshotCache.kt` |
| **Responsibility** | Implements `SnapshotCache<T>` by composing `SnapshotStorage` (interface) and `SnapshotCodec<T>` (interface). Delegates encode/decode and file I/O entirely to collaborators. |
| **Dependencies** | `SnapshotStorage` (interface), `SnapshotCodec<T>` (interface). Zero Android, zero framework. |
| **Volatility** | Low. |
| **Test recommendation** | **Unit — high priority.** |
| **Rationale** | This is the cache coordination layer. Tests should verify: `load()` returns `null` when storage has nothing, returns decoded value when present; `save()` calls codec encode then storage save and optionally saveETag; `loadETag()` delegates to storage; `clear()` delegates to storage. All dependencies are interfaces — mock with Mockk in 10 lines. |

---

#### `RetrofitSnapshotFetcher<T>`
| Field | Value |
|---|---|
| **File** | `core/data/snapshot/RetrofitSnapshotFetcher.kt` |
| **Responsibility** | Implements `SnapshotFetcher<T>`. Translates `Retrofit Response<T>` to `NetworkResult<T>`: maps 304 to `NotModified`, 2xx with body to `Success`, 2xx with null body or non-2xx to `Failure`, exceptions to `Failure`. |
| **Dependencies** | `retrofit2.Response<T>` (data class, no network call), a suspend lambda `(etag: String?) -> Response<T>`. |
| **Volatility** | Low. |
| **Test recommendation** | **Unit — high priority.** |
| **Rationale** | Contains four distinct branches (304, 2xx+body, 2xx+null body, error code, exception). Each branch is a separate test case. The lambda can be replaced with a simple suspend function that returns a mocked `Response.success()`, `Response.error()`, or throws — no `MockWebServer` required. |

---

#### `SnapshotCacheFactory`
| Field | Value |
|---|---|
| **File** | `core/data/snapshot/SnapshotCacheFactory.kt` |
| **Responsibility** | Factory that composes `LocalSnapshotCache` + `JsonSnapshotCodec` for a given key and serializer. |
| **Dependencies** | `SnapshotStorage` (interface), `kotlinx.serialization.Json`. |
| **Volatility** | Low. |
| **Test recommendation** | Unit — low priority (the factory itself has no logic beyond object construction). |
| **Rationale** | Integration-test via `LocalSnapshotCache` tests which transitively exercise the factory composition. A standalone test adds little value unless the factory grows conditional logic. |

---

### 2.7 `core/data/local/` — Local Storage

---

#### `SnapshotStorage` (interface)
| Field | Value |
|---|---|
| **File** | `core/data/local/SnapshotStorage.kt` |
| **Responsibility** | Contract for raw string-based key-value file storage with ETag support. |
| **Dependencies** | None. |
| **Volatility** | Low. |
| **Test recommendation** | Not directly — serves as mock target. |

---

#### `JsonSnapshotStorage`
| Field | Value |
|---|---|
| **File** | `core/data/local/JsonSnapshotStorage.kt` |
| **Responsibility** | Implements `SnapshotStorage` using the Android `filesDir`. Sanitizes keys, manages `.json` and `_etag.txt` file pairs. |
| **Dependencies** | `android.content.Context` (filesDir access), Hilt `@ApplicationContext`, `kotlinx.coroutines.Dispatchers`. |
| **Volatility** | Low — stable file I/O logic with clear single responsibility. |
| **Test recommendation** | Integration (Robolectric or Android instrumented). |
| **Rationale** | Pure file system operations. Cannot be meaningfully tested without a real or simulated file system. Robolectric provides a working `filesDir` equivalent. Key tests: `safeKey()` sanitization, round-trip save/load, ETag save/load, clear deletes both files, `loadOrNull` returns null when file absent. |

---

#### `ThemePreferences`
| Field | Value |
|---|---|
| **File** | `core/data/local/ThemePreferences.kt` |
| **Responsibility** | Maps `ThemeMode` enum to/from `DataStore<Preferences>` using an int preference key. |
| **Dependencies** | `DataStore<Preferences>` (Jetpack), `ThemeMode` (features/settings/domain/model). |
| **Volatility** | Low — the mapping table (0=FOLLOW_SYSTEM, 1=LIGHT, 2=DARK) is stable by convention. |
| **Test recommendation** | Integration — requires a real or in-memory DataStore. |
| **Rationale** | The int↔ThemeMode mapping contains branching logic worth testing. An in-memory `DataStore` can be constructed with `PreferenceDataStoreFactory.create(produceFile = { tempFile })` in a coroutine test. |

---

#### `StorageDirConstants`
| Field | Value |
|---|---|
| **File** | `core/data/local/StorageDirConstants.kt` |
| **Responsibility** | String constants for storage directory names. |
| **Dependencies** | None. |
| **Volatility** | Low. |
| **Test recommendation** | None needed. |

---

### 2.8 `core/data/logger/` — Logging

---

#### `AndroidLogger`
| Field | Value |
|---|---|
| **File** | `core/data/logger/AndroidLogger.kt` |
| **Responsibility** | Implements `Logger` interface by delegating to `android.util.Log.w`. |
| **Dependencies** | `android.util.Log`. |
| **Volatility** | Low. |
| **Test recommendation** | None recommended. |
| **Rationale** | Trivial delegation. `Logger.Noop` exists precisely to avoid the need to test this in isolation. |

---

### 2.9 `core/network/` — HTTP Infrastructure

---

#### `ApiConstants`
| Field | Value |
|---|---|
| **File** | `core/network/ApiConstants.kt` |
| **Responsibility** | Holds the `BASE_PATH` string constant. |
| **Dependencies** | None. |
| **Volatility** | Low. |
| **Test recommendation** | None. |

---

#### `AuthInterceptor`
| Field | Value |
|---|---|
| **File** | `core/network/AuthInterceptor.kt` |
| **Responsibility** | OkHttp `Interceptor` that attaches `Authorization: Bearer <token>` to outgoing requests if a token exists. Skips if the header is already present. |
| **Dependencies** | `TokenStorage` (features/auth/data/local), OkHttp `Interceptor.Chain`. |
| **Volatility** | Low — the logic (skip if header present, attach if token available, pass through if none) is stable. |
| **Test recommendation** | Unit — medium priority. |
| **Rationale** | Three branches: header already present (pass-through), token available (attach header), no token (pass-through unmodified). Can be tested by mocking `TokenStorage` with Mockk and constructing a real OkHttp `Request` + a fake `Chain`. No `MockWebServer` needed. Cross-feature dependency on `TokenStorage` is the only coupling cost. |

---

#### `TokenAuthenticator`
| Field | Value |
|---|---|
| **File** | `core/network/TokenAuthenticator.kt` |
| **Responsibility** | OkHttp `Authenticator` handling 401 responses. Uses a `Mutex` to prevent concurrent refresh races. Refreshes the token via `AuthApi`, saves new tokens, retries with the new access token. Clears storage on 401/400 refresh failure. |
| **Dependencies** | `AuthApi` (features/auth/data/api), `TokenStorage` (features/auth/data/local), OkHttp `Authenticator`. Uses `runBlocking` + `Mutex`. |
| **Volatility** | Medium — token refresh logic is stable, but the `runBlocking(Dispatchers.IO)` and mutex pattern are complex to test correctly. |
| **Test recommendation** | Integration — medium priority. |
| **Rationale** | The mutex-based concurrent refresh guard and the `responseCount` retry limiter are non-trivial logic paths worth testing. However, the `runBlocking` within an OkHttp callback is hard to exercise cleanly in isolation. Recommended approach: use a real `MockWebServer` with a test `OkHttpClient` and simulate 401 → refresh → retry sequences. Unit-mocking `AuthApi` and `TokenStorage` is possible but requires careful coroutine handling of `runBlocking`. |

---

### 2.10 `core/utils/` — General Utilities

---

#### `DiskCache`
| Field | Value |
|---|---|
| **File** | `core/utils/DiskCache.kt` |
| **Responsibility** | Legacy Android file-based cache using Gson with timestamp-based TTL validation (by day and by minute window). |
| **Dependencies** | `android.content.Context`, `com.google.gson.Gson`, `java.util.Calendar`. |
| **Volatility** | **High** — this is legacy code predating the snapshot infrastructure. Uses Gson (inconsistent with the codebase's kotlinx.serialization standard). The `isCacheValidToday` and `isCacheValidWithinMinutes` methods depend on `System.currentTimeMillis()` (non-deterministic side effect). |
| **Test recommendation** | Not recommended. Candidate for deletion. |
| **Rationale** | The snapshot infrastructure (`LocalSnapshotCache`, `JsonSnapshotStorage`, `SnapshotCacheFactory`) supersedes `DiskCache` entirely. Any feature still using `DiskCache` should be migrated. Testing a component marked for removal is wasted investment. |

---

### 2.11 `core/presentation/` — Presentation Layer

---

#### `AppRoutes`
| Field | Value |
|---|---|
| **File** | `core/presentation/navigation/AppRoutes.kt` |
| **Responsibility** | Route string constants for the navigation graph. |
| **Dependencies** | None. |
| **Volatility** | Medium — route strings change as features are added/removed. |
| **Test recommendation** | None. |

---

#### `AppNavExtensions` (`safePopBackStack()`)
| Field | Value |
|---|---|
| **File** | `core/presentation/navigation/AppNavExtensions.kt` |
| **Responsibility** | Extension on `NavHostController` that prevents popping the root route. |
| **Dependencies** | `NavHostController`, `AppRoutes`. |
| **Volatility** | Low. |
| **Test recommendation** | None — logic too thin and tightly coupled to navigation runtime. |

---

#### `LocalAppNavigator` / `AppNavigator`
| Field | Value |
|---|---|
| **File** | `core/presentation/navigation/LocalAppNavigator.kt` |
| **Responsibility** | `CompositionLocal` holder for navigation lambdas (navigate to profile, auth). |
| **Dependencies** | Compose runtime. |
| **Volatility** | Low. |
| **Test recommendation** | None — pure wiring, no logic. |

---

#### `CoreActivity`
| Field | Value |
|---|---|
| **File** | `core/presentation/CoreActivity.kt` |
| **Responsibility** | Single `ComponentActivity` entry point. Sets Compose content with `IPBCasteloBrancoTheme` and `AppNavHost`. Provides `newRootIntent()` factory. |
| **Dependencies** | Android `ComponentActivity`, Hilt `@AndroidEntryPoint`, Compose. |
| **Volatility** | Low — boilerplate entry point. |
| **Test recommendation** | Not recommended for unit tests. |
| **Rationale** | No business logic. Tests for navigation flows belong at the integration/UI test layer with Espresso or Compose test APIs. |

---

#### `BaseActivity`
| Field | Value |
|---|---|
| **File** | `core/presentation/base/BaseActivity.kt` |
| **Responsibility** | Abstract `ComponentActivity` template applying edge-to-edge and theme. Used by no subclasses after the single-activity migration. |
| **Dependencies** | Android `ComponentActivity`, Compose. |
| **Volatility** | **High** — this class is effectively dead code after the single-activity migration to `CoreActivity`. |
| **Test recommendation** | None. Candidate for deletion. |
| **Rationale** | `CoreActivity` does not extend `BaseActivity`. This class has no subclasses and no active role. |

---

#### `AppNavHost`
| Field | Value |
|---|---|
| **File** | `core/presentation/navigation/AppNavHost.kt` |
| **Responsibility** | Root composable `NavHost` wiring all feature graphs and single-screen composables into the navigation backstack. |
| **Dependencies** | All feature navigation graphs, `NavHostController`, Compose, `LocalContext`. |
| **Volatility** | Medium — grows when new features are added. |
| **Test recommendation** | Not yet recommended. |
| **Rationale** | Navigation graph correctness is best verified with Compose UI tests (`TestNavHostController`). The orchestration logic here is wiring, not business rules. |

---

#### `CoreViewModel`
| Field | Value |
|---|---|
| **File** | `core/presentation/viewmodel/CoreViewModel.kt` |
| **Responsibility** | Orchestrates app initialization: preloads data, observes login state, reacts to auth events, handles logout, triggers profile refresh after login. |
| **Dependencies** | `PreloadDataUseCase`, `AuthSession` (features/auth), `ProfileRepository` (features/profile), `AuthEventBus`, `LogoutUseCase` (features/auth) — **five cross-feature dependencies**. |
| **Volatility** | Medium — the initialization sequence (`startAppInitialization`) and profile refresh logic have evolved alongside feature completions and are still subject to change. |
| **Test recommendation** | Unit — low priority now; medium priority once `PreloadDataUseCase` stabilizes. |
| **Rationale** | The ViewModel contains testable state transitions (`isPreloading`, `isLoggedIn`, `CoreEvent.LogoutSuccess`), but testing them requires mocking five collaborators across multiple features. Tests written now will have high coupling to collaborator interfaces that are themselves still in flux. The `initialize()` side-effect dispatch pattern (three concurrent coroutines launched unconditionally) also requires careful sequencing in tests. |

---

#### `BaseScreen`
| Field | Value |
|---|---|
| **File** | `core/presentation/base/BaseScreen.kt` |
| **Responsibility** | Shared screen scaffold: wraps content in `Scaffold` with a `TopBar`, resolves the account image from the file system, listens to `ProfilePhotoBus`, manages `TopBarProfileViewModel` scoped to the `Activity`. |
| **Dependencies** | `AuthSession` (via Hilt `EntryPointAccessors`), `ProfilePhotoBus` (features/profile), Android `Context`, `ComponentActivity`, Coil `AsyncImage`, `LocalAppNavigator`. |
| **Volatility** | Medium — the profile photo resolution logic and `ProfilePhotoBus` integration have been added incrementally. |
| **Test recommendation** | Not recommended. |
| **Rationale** | `BaseScreen` uses `EntryPointAccessors.fromApplication()` to manually resolve `AuthSession` outside the standard Hilt injection path, constructs a custom `ViewModelProvider.Factory`, and performs file system reads in a `remember` block. These patterns make isolated testing impractical without an instrumented test environment. |

---

#### `CoreScreen` / `CoreView`
| Field | Value |
|---|---|
| **File** | `core/presentation/screens/CoreScreen.kt` |
| **Responsibility** | Root screen composable: shows the navigation drawer, highlights carousel, and feature button grid. `CoreView` wires ViewModels; `CoreScreen` is stateless. |
| **Dependencies** | `CoreViewModel`, `ProfileViewModel`, `ScheduleViewModel` (features/schedule), Compose. |
| **Volatility** | Medium — button grid has placeholder "In Dev" entries; feature set will grow. |
| **Test recommendation** | Not recommended for unit tests. Compose UI tests are appropriate for `CoreScreen` (stateless variant). |
| **Rationale** | `CoreScreen` is a pure rendering composable and could be snapshot-tested with `ComposeTestRule`. `CoreView` requires a full Hilt environment. |

---

#### Presentation Components (`TopBar`, `CustomButton`, etc.)
| Field | Value |
|---|---|
| **Files** | `components/TopBar.kt`, `components/CustomButton.kt`, `components/Highlight.kt`, `components/ThemeToggle.kt`, `components/InDevelopmentScreen.kt`, `components/DateFieldWithPicker.kt` |
| **Responsibility** | Reusable Compose UI components. |
| **Dependencies** | Compose, Material3, Coil (TopBar only). |
| **Volatility** | Low to Medium. |
| **Test recommendation** | Compose UI tests (screenshot or semantic verification) — low priority. |
| **Rationale** | Pure rendering. No business logic. Not worth unit testing. Compose screenshot tests would catch visual regressions but are infrastructure-heavy to set up. |

---

#### Theme (`Color.kt`, `Type.kt`, `Theme.kt`)
| Field | Value |
|---|---|
| **Files** | `core/presentation/theme/` |
| **Responsibility** | Material3 color schemes and typography. `IPBCasteloBrancoTheme` composable handles dark/light mode resolution via `AppCompatDelegate`. |
| **Dependencies** | Compose Material3, `AppCompatDelegate`. |
| **Volatility** | Low. |
| **Test recommendation** | None. |

---

### 2.12 `core/di/` — Hilt Modules

All DI modules (`ApiModule`, `DataStoreModule`, `HttpClientModule`, `RetrofitModule`, `SerializationModule`, `SnapshotCoreModule`, `NetworkQualifiers`) are pure wiring. They contain no business logic and are not test subjects. Their correctness is verified by Hilt's compile-time validation and by integration tests that verify the dependency graph resolves. No unit tests recommended.

---

## 3. High-Priority Test Targets

The following components should be tested first, in order of risk-adjusted value:

### Priority 1 — Zero setup, maximum value

| # | Component | File | Justification |
|---|---|---|---|
| 1 | `AppError.toAppError()` | `core/domain/error/AppError.kt` | Pure function, 3 branches, no mocks. Guards against error mapping regressions across the entire app. |
| 2 | `AppError.mapError()` | `core/domain/error/AppError.kt` | Pure function, used at every repository boundary. 2 cases. |
| 3 | `JsonSnapshotCodec` | `core/data/snapshot/JsonSnapshotCodec.kt` | Round-trip encode/decode with a real `Json` instance. Guards DTO serialization regressions. Zero mocking. |

### Priority 2 — Light mocking, high value

| # | Component | File | Justification |
|---|---|---|---|
| 4 | `LocalSnapshotCache` | `core/data/snapshot/LocalSnapshotCache.kt` | Core cache coordination logic. 2 interface mocks (Mockk). 5 distinct behaviors to verify. |
| 5 | `RetrofitSnapshotFetcher` | `core/data/snapshot/RetrofitSnapshotFetcher.kt` | 4 branches (304, success, null body, failure, exception). Lambda mock. No network. |
| 6 | `BaseSnapshotRepository` | `core/domain/snapshot/BaseSnapshotRepository.kt` | Central cache-first + refresh algorithm. Mock `SnapshotCache` + `SnapshotFetcher`. Use `UnconfinedTestDispatcher`. |

### Priority 3 — Coroutine infrastructure needed

| # | Component | File | Justification |
|---|---|---|---|
| 7 | `AuthEventBus` | `core/domain/auth/AuthEventBus.kt` | Singleton event bus. Verify delivery and non-drop behavior. Needs `runTest`. |
| 8 | `AuthInterceptor` | `core/network/AuthInterceptor.kt` | Three header-injection branches. Mock `TokenStorage`. Construct real OkHttp request. |

---

## 4. Risk Areas

### 4.1 Layering Violation — `logTime()` in domain code

**Location:** `core/domain/snapshot/BaseSnapshotRepository.kt`, lines 13–15

`logTime()` is a package-level inline function co-located with `BaseSnapshotRepository`. It imports `android.os.SystemClock` and `android.util.Log`. These are Android framework types in a domain-layer file. The function is not called by the class itself and serves no purpose beyond development debugging.

**Risk:** Any future static analysis or strict layering enforcement will flag this file. It also creates the false impression that the domain class has Android dependencies.

**Recommended action:** Delete `logTime()` or move it to `core/data/logger/`. The `Logger` interface already covers this concern.

---

### 4.2 Legacy Artifact — `DiskCache`

**Location:** `core/utils/DiskCache.kt`

Uses `com.google.gson.Gson` while the entire rest of the codebase uses `kotlinx.serialization.Json`. Uses `System.currentTimeMillis()` and `Calendar` for TTL, making tests time-dependent. Provides no ETag support. Superseded entirely by the snapshot infrastructure.

**Risk:** Any feature that still calls `DiskCache` is bypassing the canonical caching layer and will not benefit from ETag-based conditional fetching or the `BaseSnapshotRepository` state model.

**Recommended action:** Audit which features (if any) still call `DiskCache`. Migrate them to the snapshot infrastructure and delete `DiskCache`.

---

### 4.3 Interface Mismatch — `SnapshotRepository<T>` vs `BaseSnapshotRepository`

**Location:** `core/domain/repository/SnapshotRepository.kt`

The `SnapshotRepository<T>` interface declares `observe(): Flow<T?>` and `refresh(): Boolean`. `BaseSnapshotRepository<Dto, Domain>` declares `observe(): StateFlow<SnapshotState<Domain>>` and `refresh(): RefreshResult`. The signatures are incompatible: different return types for both methods.

**Risk:** If any code declares a variable as `SnapshotRepository<T>` expecting to call `observe()` and get a `StateFlow<SnapshotState<Domain>>`, it will fail at runtime or compile time. The interface appears to be either unused or represents an alternative contract for a different set of repositories.

**Recommended action:** Audit usages of `SnapshotRepository<T>`. If unused, delete it. If used, align its contract with `BaseSnapshotRepository` or document the intentional divergence.

---

### 4.4 Cross-Feature Coupling in `core/domain/`

**Location:** `core/domain/usecase/PreloadDataUseCase.kt` and `core/network/AuthInterceptor.kt`, `TokenAuthenticator.kt`

`PreloadDataUseCase` imports 4 feature repository interfaces. `AuthInterceptor` and `TokenAuthenticator` import `features.auth.data.*`. These are domain and network components in `core` that have compile-time dependencies on feature modules.

**Risk for PreloadDataUseCase:** High test fragility. Any change to a feature repository interface forces a change here. The commented-out lines are evidence this is already happening.

**Risk for AuthInterceptor/TokenAuthenticator:** Lower concern — auth infrastructure is a cross-cutting concern and coupling to `features.auth` in `core.network` is a pragmatic and stable choice. This is acceptable.

---

### 4.5 `BaseScreen` — Non-standard Hilt EntryPoint usage

**Location:** `core/presentation/base/BaseScreen.kt`, lines 48–108

`BaseScreen` uses `EntryPointAccessors.fromApplication()` to manually resolve `AuthSession` from a composable, bypassing the standard `hiltViewModel()` injection path. It then constructs `TopBarProfileViewModel` with a custom `ViewModelProvider.Factory` that captures the manually-resolved `AuthSession`. It also reads the file system directly inside a `remember` block.

**Risk:** Difficult to test. The manual `EntryPointAccessors` call requires a Hilt component to be installed, which means this composable cannot be tested without a Hilt-enabled Android test environment. The file system read in `remember` has no abstraction layer.

**Recommended action:** Not urgent, but for future testability, extract the profile photo file resolution into an injectable use case or repository method. Consider replacing the manual `EntryPointAccessors` approach with a proper `@HiltViewModel` if the activity-scoping constraint allows it.

---

### 4.6 `BaseActivity` — Dead Code After Single-Activity Migration

**Location:** `core/presentation/base/BaseActivity.kt`

`CoreActivity` extends `ComponentActivity` directly, not `BaseActivity`. `BaseActivity` has no remaining subclasses.

**Risk:** Dead code adds confusion and maintenance surface. New developers may inherit from `BaseActivity` unaware that it is obsolete.

**Recommended action:** Delete `BaseActivity`.

---

### 4.7 `BaseSnapshotRepository` — Hardcoded `Dispatchers.IO`

**Location:** `core/domain/snapshot/BaseSnapshotRepository.kt`, lines 30, 42, 49, 55

The dispatcher is hardcoded, not injected. This forces tests to use `Dispatchers.setMain(UnconfinedTestDispatcher())` or `runTest` with the coroutine test framework to avoid actual IO thread creation.

**Risk:** Minor test complexity, not a blocking issue. Standard pattern in Android coroutine testing.

**Recommended action:** Acceptable as-is. Document in the test setup that `UnconfinedTestDispatcher` is required.

---

## 5. Suggested Testing Strategy

### 5.1 Recommended order

**Phase 1 — Pure domain logic (no test infrastructure needed beyond JUnit4)**

1. `AppError` + `toAppError()` + `mapError()` — JUnit4, no mocking
2. `JsonSnapshotCodec` with a real `Json` instance — JUnit4, no mocking
3. `NetworkResult`, `RefreshResult`, `SnapshotState` — verified transitively in Phase 2

**Phase 2 — Interface mocking with Mockk (add `mockk` and `kotlinx-coroutines-test`)**

4. `LocalSnapshotCache` — mock `SnapshotStorage` and `SnapshotCodec`
5. `RetrofitSnapshotFetcher` — provide a lambda returning fake `Response<T>` objects
6. `BaseSnapshotRepository` (via a concrete anonymous subclass) — mock `SnapshotCache` + `SnapshotFetcher`, use `UnconfinedTestDispatcher`
7. `AuthEventBus` — use `runTest` + Turbine or manual `launch { collect {} }`

**Phase 3 — Android-adjacent (add Robolectric or use instrumented tests)**

8. `JsonSnapshotStorage` — Robolectric with real `Context` / `filesDir`
9. `ThemePreferences` — in-memory `DataStore` via `PreferenceDataStoreFactory.create(tempFile)`
10. `AuthInterceptor` — mock `TokenStorage`, construct real OkHttp `Request`

**Phase 4 — Integration / network layer**

11. `TokenAuthenticator` — `MockWebServer` with a scripted 401 → refresh → 200 sequence

---

### 5.2 Mocking strategy

| Collaborator | Strategy |
|---|---|
| `SnapshotCache<T>` | Mockk `mockk<SnapshotCache<T>>()` |
| `SnapshotFetcher<T>` | Mockk or plain suspend lambda |
| `SnapshotStorage` | Mockk |
| `SnapshotCodec<T>` | Mockk or real `JsonSnapshotCodec` |
| `TokenStorage` | Mockk |
| `AuthApi` | Mockk (for `TokenAuthenticator`), MockWebServer (for integration) |
| `kotlinx.coroutines.Dispatchers` | `Dispatchers.setMain(UnconfinedTestDispatcher())` in `@Before` / `@After` |
| `android.content.Context` | Robolectric `ApplicationProvider.getApplicationContext()` |

**Do not mock** `Json` (kotlinx.serialization) — use a real instance configured identically to `SerializationModule.provideJson()`. Mocking a serialization library hides real encoding bugs.

---

### 5.3 Refactoring prerequisites for testing

The following changes should be made **before** writing tests for the indicated components, to avoid writing tests that immediately need rework:

| Refactoring | Affected test target | Priority |
|---|---|---|
| Delete `logTime()` from `BaseSnapshotRepository.kt` | `BaseSnapshotRepository` | High |
| Resolve or delete `SnapshotRepository<T>` interface | Any repository using it | High |
| Delete `DiskCache` (or migrate remaining callers) | Any feature using `DiskCache` | Medium |
| Delete `BaseActivity` | None | Low |
| Inject `CoroutineDispatcher` into `BaseSnapshotRepository` (optional) | `BaseSnapshotRepository` | Low — `UnconfinedTestDispatcher` is sufficient |

---

*Analysis based on full source inspection of all 50 files in `core/`. No inferences made beyond what is directly observable in the code.*
