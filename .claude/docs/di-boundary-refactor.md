# Plano: Refatoração de Fronteiras de DI

**Motivação:** Core importa de features; features importam umas das outras. O padrão correto é:
- `core/domain/` define os contratos (interfaces)
- Features implementam e/ou consomem esses contratos
- Módulos Hilt de cada feature fazem o binding `interface → impl`
- Nenhum arquivo em `core/` importa de `features/`

Executar as 4 etapas em ordem. Cada etapa é independente e pode virar um PR separado.

---

## Violações Identificadas

### Core → Feature (CRÍTICO — nunca deve ocorrer)

| Arquivo em core/ | Importa de feature/ | Problema |
|---|---|---|
| `core/network/AuthInterceptor.kt` | `features/auth/data/local/TokenStorage` | Core depende de impl concreta de auth |
| `core/network/TokenAuthenticator.kt` | `features/auth/data/local/TokenStorage` | idem |
| `core/network/TokenAuthenticator.kt` | `features/auth/data/api/AuthApi` | Core usa API de feature diretamente |
| `core/network/TokenAuthenticator.kt` | `features/auth/data/dto/RefreshRequest` | Core usa DTO de feature |
| `core/presentation/viewmodel/CoreViewModel.kt` | `features/auth/data/local/AuthSession` | Core usa state local de feature |
| `core/presentation/base/BaseScreen.kt` | `features/auth/data/local/AuthSession` | idem |
| `core/presentation/base/BaseScreen.kt` | `features/profile/data/local/ProfilePhotoBus` | Core usa bus interno de feature |
| `core/data/local/ThemePreferences.kt` | `features/settings/domain/model/ThemeMode` | Core usa modelo de feature |
| `MyApp.kt` | `features/settings/domain/model/ThemeMode` | idem |
| `core/domain/usecase/PreloadDataUseCase.kt` | 6 repositórios de features | Acoplamento para preload — tolerável mas pode ser melhorado |

### Feature → Feature (CRÍTICO — nunca deve ocorrer)

| Feature origem | Importa de feature destino | Arquivo | Motivo |
|---|---|---|---|
| `settings` | `gallery` | `SettingsViewModel.kt` | SettingsViewModel injeta GalleryRepository |
| `hymnal` | `settings` | `HymnalViewModel.kt` | HymnalViewModel injeta SettingsRepository |
| `schedule` | `profile` | `ScheduleSnapshotFetcher.kt` | Snapshot fetcher chama ProfileApi diretamente |
| `hymnal` | `profile` | `HymnalSnapshotFetcher.kt` | idem |
| `admin/schedule` | `schedule` | `AdminScheduleScreen.kt` | Usa formatter de outra feature |
| `admin/schedule` | `schedule` | `AdminScheduleApi.kt` | Usa MonthScheduleDto de outra feature |

---

## Etapa 1 — Contratos de auth/profile/settings para core/domain/

**Objetivo:** Eliminar as violações core→feature das camadas de rede, sessão e UI.

### 1.1 Criar interfaces em `core/domain/`

**`core/domain/auth/ITokenStorage.kt`**
```kotlin
package com.ipb.castelobranco.core.domain.auth

interface ITokenStorage {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    suspend fun clearTokens()
}
```

**`core/domain/auth/IAuthSession.kt`**
```kotlin
package com.ipb.castelobranco.core.domain.auth

interface IAuthSession {
    val isLoggedIn: Boolean
    val userId: Int?
    val userName: String?
    val userRole: String?
}
```

**`core/domain/profile/IProfilePhotoBus.kt`**
```kotlin
package com.ipb.castelobranco.core.domain.profile

import kotlinx.coroutines.flow.Flow

interface IProfilePhotoBus {
    val photoUpdates: Flow<String?>
    suspend fun emit(photoUrl: String?)
}
```

**`core/domain/settings/ThemeMode.kt`**
```kotlin
package com.ipb.castelobranco.core.domain.settings

enum class ThemeMode { LIGHT, DARK, SYSTEM }
```
> Mover de `features/settings/domain/model/ThemeMode.kt` — atualizar todos os imports.

### 1.2 Features implementam as interfaces

- `features/auth/data/local/TokenStorage.kt` → adicionar `: ITokenStorage`
- `features/auth/data/local/AuthSession.kt` → adicionar `: IAuthSession`
- `features/profile/data/local/ProfilePhotoBus.kt` → adicionar `: IProfilePhotoBus`

### 1.3 Core passa a usar interfaces

- `core/network/AuthInterceptor.kt` → injetar `ITokenStorage` em vez de `TokenStorage`
- `core/network/TokenAuthenticator.kt` → injetar `ITokenStorage` em vez de `TokenStorage`
- `core/presentation/viewmodel/CoreViewModel.kt` → injetar `IAuthSession`
- `core/presentation/base/BaseScreen.kt` → injetar `IAuthSession` + `IProfilePhotoBus`

### 1.4 Criar módulos de binding nas features

**`features/auth/di/AuthBindingsModule.kt`**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindingsModule {

    @Binds
    abstract fun bindTokenStorage(impl: TokenStorage): ITokenStorage

    @Binds
    abstract fun bindAuthSession(impl: AuthSession): IAuthSession
}
```

**`features/profile/di/ProfileBindingsModule.kt`**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileBindingsModule {

    @Binds
    abstract fun bindProfilePhotoBus(impl: ProfilePhotoBus): IProfilePhotoBus
}
```

### 1.5 TokenAuthenticator — extrair AuthApi do core

O problema: `TokenAuthenticator` usa `AuthApi` (feature) para fazer refresh do token.

**Opção A (abstração):** Criar `ITokenRefresher` em `core/domain/auth/`:
```kotlin
interface ITokenRefresher {
    suspend fun refresh(refreshToken: String): Result<Pair<String, String>>
    // retorna (accessToken, refreshToken) ou erro
}
```
Feature auth implementa com `AuthApi`. `TokenAuthenticator` injeta a interface.

**Opção B (mover para core):** Mover `AuthApi` e `RefreshRequest`/`RefreshResponse` para `core/data/api/` — eles são usados diretamente pelo core de rede, então semanticamente pertencem ao core.

> **Recomendação:** Opção B é mais simples se o refresh de token for sempre responsabilidade do core de rede.

---

## Etapa 2 — Interfaces de repositório para core/domain/

**Objetivo:** Resolver `PreloadDataUseCase` dependendo de 6 repositórios de features e permitir que features usem contratos de outras sem importar a feature diretamente.

### 2.1 Mover interfaces para core/domain/

Mover apenas as interfaces (não as implementações):

| Interface atual | Nova localização |
|---|---|
| `features/gallery/domain/repository/GalleryRepository.kt` | `core/domain/gallery/GalleryRepository.kt` |
| `features/hymnal/domain/repository/HymnalRepository.kt` | `core/domain/hymnal/HymnalRepository.kt` |
| `features/schedule/domain/repository/ScheduleRepository.kt` | `core/domain/schedule/ScheduleRepository.kt` |
| `features/worshiphub/*/domain/repository/ChordChartRepository.kt` | `core/domain/worshiphub/ChordChartRepository.kt` |
| `features/worshiphub/*/domain/repository/LyricsRepository.kt` | `core/domain/worshiphub/LyricsRepository.kt` |
| `features/worshiphub/*/domain/repository/SongsRepository.kt` | `core/domain/worshiphub/SongsRepository.kt` |
| `features/settings/domain/repository/SettingsRepository.kt` | `core/domain/settings/SettingsRepository.kt` |

**Implementações ficam onde estão** — só o arquivo de interface muda de pacote.

### 2.2 Módulos de binding por feature

Cada feature adiciona binding no seu próprio `di/` module:
```kotlin
// features/gallery/di/GalleryModule.kt
@Binds abstract fun bindGalleryRepo(impl: GalleryRepositoryImpl): GalleryRepository

// features/hymnal/di/HymnalModule.kt
@Binds abstract fun bindHymnalRepo(impl: HymnalRepositoryImpl): HymnalRepository
// ... etc
```

### 2.3 Resultado

- `PreloadDataUseCase` passa a importar de `core/domain/*/` — limpo, sem violação
- `SettingsViewModel` pode injetar `GalleryRepository` via `core/domain/gallery/GalleryRepository`
- `HymnalViewModel` pode injetar `SettingsRepository` via `core/domain/settings/SettingsRepository`
- Nenhuma feature importa diretamente de outra feature

---

## Etapa 3 — Corrigir acesso cross-feature a Profile (snapshot fetchers)

**Problema:** `ScheduleSnapshotFetcher` e `HymnalSnapshotFetcher` importam `ProfileApi` e `MeProfileDto` diretamente da feature profile.

**Solução:** Criar `IProfileService` em `core/domain/profile/`:

**`core/domain/profile/IProfileService.kt`**
```kotlin
package com.ipb.castelobranco.core.domain.profile

interface IProfileService {
    suspend fun getMyProfile(): Result<UserProfile>
}

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val role: String
)
```

- Feature profile implementa `IProfileService` usando `ProfileApi` internamente
- `ScheduleSnapshotFetcher` e `HymnalSnapshotFetcher` injetam `IProfileService`
- Nenhum snapshot fetcher importa DTOs de outra feature

**`features/profile/di/ProfileBindingsModule.kt`** (adicionar ao criado na Etapa 1):
```kotlin
@Binds
abstract fun bindProfileService(impl: ProfileServiceImpl): IProfileService
```

---

## Etapa 4 — Corrigir violações admin→schedule

**Problema 1:** `AdminScheduleScreen` usa `MonthScheduleWhatsappFormatter` da feature schedule.

**Solução:** Mover para `core/domain/schedule/formatter/MonthScheduleWhatsappFormatter.kt`.
- É lógica de formatação genérica, sem dependência de UI
- Tanto `schedule` quanto `admin/schedule` importam de core — sem cruzar features

**Problema 2:** `AdminScheduleApi` usa `MonthScheduleDto` da feature schedule.

- **Opção A:** Mover `MonthScheduleDto` para `core/data/dto/MonthScheduleDto.kt` — DTO é compartilhado
- **Opção B:** Criar `AdminMonthScheduleDto` próprio em admin — correto se os endpoints forem diferentes

> **Recomendação:** Opção A se o endpoint for o mesmo; Opção B se admin tiver campos extras no response.

---

## Ordem de Execução Sugerida

```
Etapa 1a — ThemeMode para core/domain/settings/         (impacto: MyApp, ThemePreferences, settings, hymnal)
Etapa 1b — ITokenStorage + IAuthSession                  (impacto: AuthInterceptor, TokenAuthenticator, CoreViewModel, BaseScreen)
Etapa 1c — IProfilePhotoBus                              (impacto: BaseScreen, ProfilePhotoBus)
Etapa 2  — Interfaces de repositório para core/domain/   (impacto: PreloadDataUseCase, SettingsViewModel, HymnalViewModel)
Etapa 3  — IProfileService                               (impacto: ScheduleSnapshotFetcher, HymnalSnapshotFetcher)
Etapa 4  — Formatter + DTO compartilhados                (impacto: AdminScheduleScreen, AdminScheduleApi)
```

Etapas 1a, 1b, 1c são independentes entre si e podem ser feitas em paralelo.

---

## Estrutura Final de core/domain/ esperada

```
core/domain/
├── auth/
│   ├── AuthEventBus.kt          (já existe)
│   ├── IAuthSession.kt          (NOVO — Etapa 1b)
│   ├── ITokenStorage.kt         (NOVO — Etapa 1b)
│   └── ITokenRefresher.kt       (NOVO — Etapa 1b, se Opção A)
├── gallery/
│   └── GalleryRepository.kt     (MOVER — Etapa 2)
├── hymnal/
│   └── HymnalRepository.kt      (MOVER — Etapa 2)
├── profile/
│   ├── IProfilePhotoBus.kt      (NOVO — Etapa 1c)
│   └── IProfileService.kt       (NOVO — Etapa 3)
├── schedule/
│   ├── ScheduleRepository.kt    (MOVER — Etapa 2)
│   └── formatter/
│       └── MonthScheduleWhatsappFormatter.kt  (MOVER — Etapa 4)
├── settings/
│   ├── ThemeMode.kt             (MOVER — Etapa 1a)
│   └── SettingsRepository.kt    (MOVER — Etapa 2)
├── snapshot/                    (já existe)
│   ├── BaseSnapshotRepository.kt
│   ├── Logger.kt
│   ├── SnapshotCache.kt
│   └── SnapshotFetcher.kt
└── worshiphub/
    ├── ChordChartRepository.kt  (MOVER — Etapa 2)
    ├── LyricsRepository.kt      (MOVER — Etapa 2)
    └── SongsRepository.kt       (MOVER — Etapa 2)
```

---

## Checklist de Validação por Etapa

Após cada etapa, rodar os testes:
```bash
./gradlew :app:testDebugUnitTest
```

Verificar zero violações core→feature (exceto AppNavHost, que importa grafos por design):
```bash
grep -r "import com.ipb.castelobranco.features" app/src/main/java/com/ipb/castelobranco/core/
```

Verificar zero violações feature→feature:
```bash
for feat in auth profile schedule hymnal gallery worshiphub admin settings; do
  echo "=== $feat ==="
  grep -r "import com.ipb.castelobranco.features" app/src/main/java/com/ipb/castelobranco/features/$feat/ \
    | grep -v "features/$feat"
done
```
Resultado esperado após Etapa 4: zero linhas em todas as features.
