# Relatório de Não-Conformidades — IPB Castelo Branco

> Análise automática do codebase contra as regras definidas em `CLAUDE.md`.
> Gerado em: 2026-04-16

---

## Índice

1. [Imports Cruzados entre Features (CRÍTICO)](#1-imports-cruzados-entre-features)
2. [runBlocking em Produção (CRÍTICO)](#2-runblocking-em-produção)
3. [collectAsState ao invés de collectAsStateWithLifecycle (ALTO)](#3-collectasstate-ao-invés-de-collectasstatewithlifecycle)
4. [Android Imports na Camada Domain (ALTO)](#4-android-imports-na-camada-domain)
5. [Composables sem @Preview (MÉDIO)](#5-composables-sem-preview)
6. [Lógica Assíncrona dentro de Composable (MÉDIO)](#6-lógica-assíncrona-dentro-de-composable)
7. [Single Responsibility Violation (MÉDIO)](#7-single-responsibility-violation)
8. [Linhas acima de 120 caracteres (BAIXO)](#8-linhas-acima-de-120-caracteres)
9. [Pendências Documentadas (Informativo)](#9-pendências-documentadas)

---

## Resumo Executivo

| Categoria | Arquivos Afetados | Severidade |
|---|---|---|
| Imports cruzados entre features | 30+ | **CRÍTICO** |
| `runBlocking` em produção | 1 | **CRÍTICO** |
| `collectAsState()` sem lifecycle | 7 | **ALTO** |
| Android imports em `domain/` | 2 | **ALTO** |
| Composables sem `@Preview` | 19 | **MÉDIO** |
| Lógica assíncrona em Composable | 1 | **MÉDIO** |
| Single Responsibility violation | 1 | **MÉDIO** |
| Linhas > 120 chars | 1+ | **BAIXO** |

---

## 1. Imports Cruzados entre Features

> **Regra violada:** Seção 4.2 — *"Features não importam umas das outras. Compartilhe via `core/`."*

Esta é a violação mais sistêmica do projeto. Múltiplas features importam diretamente tipos e contratos de outras features, criando acoplamento forte que viola Clean Architecture.

### 1.1 CoreViewModel importa de features externas

**Arquivo:** `core/presentation/viewmodel/CoreViewModel.kt` — linhas 10–11

```kotlin
import com.ipb.castelobranco.features.auth.data.local.AuthSession
import com.ipb.castelobranco.features.gallery.domain.usecase.GalleryAutoDownloadUseCase
import com.ipb.castelobranco.features.profile.domain.usecase.FetchProfileUseCase
```

O `CoreViewModel` (núcleo do app) está acoplado às implementações internas de `auth`, `gallery` e `profile`. Qualquer mudança nessas features pode quebrar o core.

**Correção:** Mover `AuthSession` para `core/domain/`, criar interfaces genéricas para os use cases ou movê-los para `core/domain/usecase/`.

---

### 1.2 HymnalViewModel importa de Settings

**Arquivo:** `features/hymnal/presentation/viewmodel/HymnalViewModel.kt` — linha 10

```kotlin
import com.ipb.castelobranco.features.settings.domain.repository.SettingsRepository
```

A feature `hymnal` depende diretamente de `settings`. Mudanças no contrato de `SettingsRepository` podem quebrar o hinário.

**Correção:** Mover `SettingsRepository` para `core/domain/repository/`.

---

### 1.3 SettingsViewModel importa de Gallery

**Arquivo:** `features/settings/presentation/viewmodel/SettingsViewModel.kt` — linha 6

```kotlin
import com.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
```

Settings depende de gallery para expor a opção "limpar fotos". Viola isolamento de features.

**Correção:** Criar um serviço de limpeza de cache em `core/` ou expor a funcionalidade via interface em `core/domain/`.

---

### 1.4 Admin importa modelo `Song` de WorshipHub

**Arquivo:** `features/admin/register/presentation/viewmodel/MusicRegistrationViewModel.kt` — linha 14
**Arquivo:** `features/admin/register/presentation/screens/MusicRegistrationScreen.kt` — linha 45

```kotlin
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.Song
```

A feature `admin/register` depende do model `Song` definido em `worshiphub.tables`. Violação grave de separação de features.

**Correção:** Mover o model `Song` para `core/domain/model/` ou criar um model local `RegisteredSong` em `admin/register/domain/model/`.

---

### 1.5 Resumo de tipos compartilhados ilegalmente

Os types abaixo são importados por múltiplas features que não deveriam conhecê-los. Todos deveriam residir em `core/domain/model/` ou `core/domain/repository/`:

| Tipo | Origem | Nº de features importadoras |
|---|---|---|
| `Song` | `worshiphub/tables/domain/model` | 3+ features |
| `TopTone` | `worshiphub` | 2+ features |
| `TopSong` | `worshiphub` | 2+ features |
| `SundaySet` | `worshiphub` | 2+ features |
| `Hymn` | `hymnal/domain/model` | 2+ features |
| `SongsRepository` | `worshiphub/tables/domain` | 2+ features |
| `Member` | `admin/schedule/domain` | 2+ features |
| `SuggestedSong` | `worshiphub` | 2+ features |
| `MonthSchedule` | `schedule/domain` | 2+ features |
| `SettingsRepository` | `settings/domain` | 2+ features |

---

## 2. runBlocking em Produção

> **Regra violada:** Seção 8 — *"❌ Errado: `GlobalScope.launch`. Sempre use `viewModelScope` ou scope injetado."* + *"Prefira `flow {}` a callbacks aninhados."*

### 2.1 TokenAuthenticator bloqueia thread de rede

**Arquivo:** `core/network/TokenAuthenticator.kt` — linha 28

```kotlin
return runBlocking(Dispatchers.IO) {
    refreshTokenMutex.withLock {
        // lógica de refresh de token
    }
}
```

O `OkHttp Authenticator` é chamado em threads de rede gerenciadas pelo OkHttp. Usar `runBlocking` aqui bloqueia uma dessas threads indefinidamente durante o refresh, podendo causar:
- Deadlock quando todas as threads do pool estão esperando refresh
- Performance degradada
- ANR em casos extremos

**Correção:** Refatorar para um `Interceptor` que suspende de forma cooperativa, ou usar `suspend` function adapter pattern via coroutine bridge adequado.

---

## 3. collectAsState ao invés de collectAsStateWithLifecycle

> **Regra violada:** Seção 6.1 — *"A Screen coleta com `collectAsStateWithLifecycle()`, nunca `collectAsState()`."*

O uso de `collectAsState()` mantém o Flow ativo mesmo quando o app vai para background, desperdiçando recursos.

| Arquivo | Linhas | Flows afetados |
|---|---|---|
| `features/auth/presentation/screens/AuthScreen.kt` | 74–75 | `loginError`, `isGoogleLoading` |
| `features/auth/presentation/screens/RegisterScreen.kt` | 54 | `registerErrors` |
| `features/gallery/presentation/screens/GalleryScreen.kt` | 36–37 | `downloadState`, `isOnWifi` |
| `features/gallery/presentation/screens/AlbumScreen.kt` | 39 | `albums` |
| `features/settings/presentation/screens/SettingsScreen.kt` | 28 | `uiState` |

**Correção em cada arquivo:**
```kotlin
// Antes (errado)
val uiState by viewModel.uiState.collectAsState()

// Depois (correto)
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

Requer import: `androidx.lifecycle.compose.collectAsStateWithLifecycle`

---

## 4. Android Imports na Camada Domain

> **Regra violada:** Seção 4.2 — *"A camada `domain/` não conhece Android — sem `Context`, sem `ViewModel`, sem Compose."*

### 4.1 BaseSnapshotRepository usa `android.os.SystemClock` e `android.util.Log`

**Arquivo:** `core/domain/snapshot/BaseSnapshotRepository.kt` — linhas 3–4, 14

```kotlin
import android.os.SystemClock
import android.util.Log

inline fun logTime(tag: String, message: String) {
    Log.d(tag, "[${SystemClock.elapsedRealtime()} ms] $message")
}
```

A camada domain está acoplada ao framework Android. Isso impede testar as classes de domínio fora de um ambiente Android (e.g., testes unitários JVM puros).

**Correção:**
1. Criar interface em domain: `interface Logger { fun log(tag: String, message: String) }`
2. Implementar em `data/` ou `core/di/` com `android.util.Log`
3. Injetar via Hilt no construtor das classes que precisam

---

## 5. Composables sem @Preview

> **Regra violada:** Seção 6.3 — *"Todo Composable de conteúdo deve ter ao menos um `@Preview` com dados fake explícitos, não placeholders vazios."*

Nenhum dos arquivos de Screen abaixo possui `@Preview`:

| Arquivo |
|---|
| `features/admin/panel/presentation/screens/AdminScreen.kt` |
| `features/admin/register/presentation/screens/MusicRegistrationScreen.kt` |
| `features/admin/schedule/presentation/screens/AdminScheduleScreen.kt` |
| `features/auth/presentation/screens/AuthScreen.kt` |
| `features/auth/presentation/screens/RegisterScreen.kt` |
| `features/gallery/presentation/screens/AlbumScreen.kt` |
| `features/gallery/presentation/screens/GalleryScreen.kt` |
| `features/gallery/presentation/screens/PhotoScreen.kt` |
| `features/hymnal/presentation/screens/HymnalScreen.kt` |
| `features/hymnal/presentation/screens/HymnDetailScreen.kt` |
| `features/profile/presentation/screens/ProfileScreen.kt` |
| `features/schedule/presentation/screens/ScheduleScreen.kt` |
| `features/settings/presentation/screens/SettingsScreen.kt` |
| `features/worshiphub/chordcharts/presentation/screens/ChordChartDetailScreen.kt` |
| `features/worshiphub/chordcharts/presentation/screens/ChordChartsScreen.kt` |
| `features/worshiphub/hub/presentation/screens/WorshipHubScreen.kt` |
| `features/worshiphub/lyrics/presentation/screens/LyricsDetailScreen.kt` |
| `features/worshiphub/lyrics/presentation/screens/LyricsScreen.kt` |
| `features/worshiphub/tables/presentation/screens/SongsTableScreen.kt` |

**Padrão esperado:**
```kotlin
@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
private fun SettingsContentPreview() {
    IPBTheme {
        SettingsContent(
            darkMode = true,
            onToggleDark = {},
            onClearGallery = {}
        )
    }
}
```

---

## 6. Lógica Assíncrona dentro de Composable

> **Regra violada:** Seção 6.2 — *"❌ Errado: chamar use case ou fazer operação assíncrona dentro de um Composable."*

### 6.1 AuthScreen usa rememberCoroutineScope para Google Sign-In

**Arquivo:** `features/auth/presentation/screens/AuthScreen.kt` — linhas 41, 78, 98–138, 251

```kotlin
val coroutineScope = rememberCoroutineScope()

suspend fun launchGoogleSignIn() {
    val credentialRequest = GetCredentialRequest(...)
    val result = credentialManager.getCredential(context, credentialRequest) // I/O assíncrono
    // ...
}

Button(
    onClick = { coroutineScope.launch { launchGoogleSignIn() } }
)
```

A lógica de autenticação Google (chamada de rede/I/O) está dentro do Composable. Isso viola a regra de Composables burros e dificulta testes.

**Correção:** Mover `launchGoogleSignIn()` para `AuthViewModel` via `viewModelScope`. A Screen apenas chama `viewModel.onGoogleSignInClicked()`.

---

## 7. Single Responsibility Violation

> **Regra violada:** Seção 5.1 — *"Cada classe tem uma única razão para mudar."*

### 7.1 ProfilePhotoDataSource com múltiplas responsabilidades

**Arquivo:** `features/profile/data/photo/ProfilePhotoDataSource.kt` — linhas 3, 25, 124, 146, 163–168

Esta classe gerencia simultaneamente:
1. Chamadas HTTP via `ProfileApi`
2. I/O de arquivo com `FileOutputStream`
3. Cache com `ProfilePhotoCacheStorage`
4. Lógica de ETag HTTP

**Correção:** Separar em:
- `ProfilePhotoRemoteDataSource` — apenas chamadas HTTP
- `ProfilePhotoLocalDataSource` — apenas leitura/escrita de arquivo
- `ProfilePhotoCacheManager` — apenas gerenciamento de ETag/cache

---

## 8. Linhas acima de 120 caracteres

> **Regra violada:** Seção 14 — *"Limite de linha: 120 caracteres."*

### 8.1 AuthScreen

**Arquivo:** `features/auth/presentation/screens/AuthScreen.kt` — linha 198

```kotlin
visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
```

Linha possui ~129 caracteres (máximo: 120).

**Correção:**
```kotlin
visualTransformation = if (passwordVisible.value)
    VisualTransformation.None
else
    PasswordVisualTransformation(),
```

---

## 9. Pendências Documentadas

As itens abaixo são **migração pendente** já conhecida e documentada no próprio `CLAUDE.md`. Estão listados aqui apenas como rastreabilidade:

| Item | Status | Seção CLAUDE.md |
|---|---|---|
| Tokens em plain `DataStore` (deveria ser `EncryptedSharedPreferences`) | Pendente | Seção 11 |
| Rotas string em `AppRoutes` (deveria migrar para type-safe Navigation 2.8+) | Pendente | Seção 9 |

---

## Priorização Sugerida

### Fase 1 — Corrigir agora (impacto em estabilidade/segurança)
1. `TokenAuthenticator.runBlocking()` — risco de deadlock em produção
2. `collectAsState()` → `collectAsStateWithLifecycle()` em todas as 7 Screens

### Fase 2 — Correções arquiteturais (refactoring planejado)
3. Mover tipos compartilhados para `core/domain/model/` e `core/domain/repository/`
4. Remover imports cruzados entre features
5. Remover Android imports de `core/domain/snapshot/BaseSnapshotRepository.kt`

### Fase 3 — Qualidade e manutenibilidade
6. Mover lógica de Google Sign-In para `AuthViewModel`
7. Dividir `ProfilePhotoDataSource` por responsabilidade
8. Adicionar `@Preview` nos 19 Composables listados
9. Corrigir linha longa em `AuthScreen.kt`
