# Analise de Arquitetura — IPB Castelo Branco (Android)

> Gerado em: 2026-03-07
> Escopo: `app/src/main/java/` — Clean Architecture, Clean Code, SOLID

---

## Sumario Executivo

O projeto tem uma base arquitetural solida e bem pensada. A separacao em camadas (domain / data / presentation) e respeitada na grande maioria dos casos, o sistema de snapshot com cache-first e elegante, e o uso de interfaces no dominio permite boa testabilidade. No entanto, ha violacoes pontuais que enfraquecem as garantias da Clean Architecture e do SOLID, alem de codigo morto e problemas de Clean Code que precisam ser corrigidos.

---

## 1. O que esta bem

### 1.1 Separacao de camadas por feature

Cada feature segue o padrao:
```
features/<name>/
  data/    — api, dto, mapper, repository impl, snapshot
  domain/  — model, repository interface, usecase
  presentation/ — screen, viewmodel, state, components
```
A direcao de dependencia esta correta: `presentation` -> `domain` <- `data`.

### 1.2 Sistema de Snapshot (cache-first)

`BaseSnapshotRepository` implementa um padrao cache-first robusto:
- Carrega cache do disco (preload) antes da rede
- Usa ETag para evitar downloads desnecessarios (304 Not Modified)
- Propaga `SnapshotState<T>` (Loading / Data / Error) via `StateFlow`
- Generics permitem reuso sem duplicacao

As interfaces `SnapshotCache<T>` e `SnapshotFetcher<T>` sao coesas e bem definidas.

### 1.3 Hierarquia de erros no dominio

`AppError` e uma sealed class no dominio que abstrai erros de rede, auth, servidor e desconhecidos. A extensao `Throwable.toAppError()` garante que o dominio nao vaze detalhes de IO/HTTP para cima.

### 1.4 AuthEventBus

`AuthEventBus` em `core/domain` permite comunicacao desacoplada entre features (auth -> core -> profile) sem criar dependencias diretas entre features. Correto uso do Shared Event Bus pattern.

### 1.5 MVVM + MVI hibrido

`MusicRegistrationViewModel` usa o padrao de eventos selados (`MusicRegistrationEvent`) como entrada unica, o que facilita rastreamento de estado e testes. O contrato `MusicRegistrationContract.kt` centraliza estado, eventos e acoes.

### 1.6 TokenAuthenticator com Mutex

A renovacao de token usa `Mutex` para evitar race conditions em requests paralelas — um ponto critico geralmente implementado de forma errada.

### 1.7 Injecao de dependencia via Hilt

A separacao dos modulos Hilt por feature (`WorshipRegisterModule`, `HymnalSnapshotModule`, etc.) e boa pratica. O uso de `SnapshotCacheFactory` centraliza a criacao de caches sem duplicar logica.

---

## 2. Violacoes de Clean Architecture

### [CA-01] CRITICO — Dominio com dependencias do Android Framework

**Arquivo:** `core/domain/snapshot/BaseSnapshotRepository.kt`

```kotlin
import android.os.SystemClock  // VIOLACAO
import android.util.Log         // VIOLACAO
```

A camada de dominio deve ser **puro Kotlin**, sem dependencias de `android.*`. Isso impede testes unitarios sem Android (precisaria de Robolectric ou mock).

**Impacto:** A camada de dominio passa a depender do Android SDK.
**Correcao:** Usar a abstracao `Logger` que ja existe no proprio projeto (`core/domain/snapshot/Logger.kt`). Mover a funcao `logTime` para a camada de dados ou remover.

---

### [CA-02] CRITICO — Core depende de features

**Arquivo:** `core/domain/usecase/PreloadDataUseCase.kt`

```kotlin
import com.gabrielafonso.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.repository.HymnalRepository
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.repository.ScheduleRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
```

**Arquivo:** `core/presentation/viewmodel/CoreViewModel.kt`

```kotlin
import com.gabrielafonso.ipb.castelobranco.features.auth.data.local.AuthSession
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.repository.ProfileRepository
import com.gabrielafonso.ipb.castelobranco.features.auth.domain.usecase.LogoutUseCase
```

O modulo `core` esta importando diretamente de `features/*`. Na Clean Architecture com modulos por feature, o `core` nao deve conhecer os features — e o sentido contrario (features dependem de core).

**Correcao:** Definir uma interface `Preloadable` no core e fazer os repositorios de cada feature implementa-la. O `PreloadDataUseCase` receberia `List<Preloadable>` via Hilt multibindings (`@IntoSet`).

---

### [CA-03] ALTO — ViewModel com dependencias da camada de dados

**Arquivo:** `features/admin/schedule/presentation/viewmodel/AdminScheduleViewModel.kt`

```kotlin
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.dto.MonthScheduleDto
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.dto.ScheduleEntryDto
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.dto.ScheduleItemDto
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.dto.MemberDto
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.dto.ScheduleTypeDto
```

O ViewModel constroi DTOs diretamente no metodo `toMonthScheduleDto()`. Presentation nao deve conhecer a camada de dados.

**Correcao:** A logica de montagem do DTO deve ir para o repositorio ou para um mapper dedicado na camada de dados. O ViewModel deve passar um modelo de dominio para o repositorio.

---

### [CA-04] ALTO — Domain depende de Presentation

**Arquivo:** `features/admin/register/domain/usecase/SubmitSundayPlaysUseCase.kt`

```kotlin
import com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.state.SundaySongRowState
```

**Arquivo:** `features/admin/register/domain/validation/MusicRegistrationValidator.kt`

```kotlin
import com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.state.SundaySongRowState
```

O domain validator e o use case dependem de um estado de apresentacao (`SundaySongRowState`). Esta e uma inversao grave da regra de dependencia: o dominio esta acoplado a camada externa.

**Correcao:** Criar um modelo de dominio intermediario (ex: `SundaySongEntry`) que o ViewModel mapeia antes de chamar o use case.

---

### [CA-05] MEDIO — ViewModel acessando camada de dados diretamente

**Arquivo:** `features/auth/presentation/viewmodel/AuthViewModel.kt`

```kotlin
import com.gabrielafonso.ipb.castelobranco.features.auth.data.mapper.parseLoginError
import com.gabrielafonso.ipb.castelobranco.features.auth.data.mapper.parseRegisterError
```

O ViewModel importa mappers da camada `data`. A transformacao de mensagens de erro deve ser responsabilidade do use case ou de um mapper de apresentacao.

---

### [CA-06] MEDIO — Interface `SnapshotRepository<T>` orfao

**Arquivo:** `core/domain/repository/SnapshotRepository.kt`

```kotlin
interface SnapshotRepository<T> {
    fun observe(): Flow<T?>
    suspend fun refresh(): Boolean
}
```

Esta interface nao e implementada por nenhuma classe e nao e usada em nenhum lugar do projeto. E uma abstracao morta que polui o dominio. Assinatura tambem e incompativel com o que o projeto usa (`Flow<SnapshotState<T>>` e `RefreshResult`).

**Correcao:** Remover ou unificar com a interface real usada pelo projeto.

---

### [CA-07] MEDIO — ProfileRepository expoe `java.io.File` no contrato de dominio

**Arquivo:** `features/profile/domain/repository/ProfileRepository.kt`

```kotlin
suspend fun downloadAndPersistProfilePhoto(photoUrl: String): Result<File?>
```

`java.io.File` e um detalhe de implementacao de infraestrutura. O contrato de dominio nao deve se comprometer com o tipo de retorno de IO. O dominio deveria retornar um `String` (caminho) ou um tipo proprio.

---

## 3. Violacoes de SOLID

### [S-01] SRP — ViewModel fazendo acesso direto ao sistema de arquivos

**Arquivo:** `features/profile/presentation/viewmodel/ProfileViewModel.kt`

```kotlin
private fun refreshLocalPhotoPathAndBump() {
    val dir = File(context.filesDir, StorageDirConstants.PROFILE)
    val file = dir.listFiles()
        ?.firstOrNull { it.isFile && it.name.startsWith("profile_photo.") && it.length() > 0L }
    // ...
}
```

O ViewModel esta com tres responsabilidades: gerenciar estado da UI, coordenar use cases, e acessar o sistema de arquivos. O acesso ao disco deveria ser delegado a um use case ou ao repositorio.

---

### [S-02] SRP — Estado de UI com logica de dominio embutida

**Arquivo:** `features/admin/register/presentation/state/MusicRegistrationContract.kt`

```kotlin
val canSubmit: Boolean
    get() = when (registrationType) {
        RegistrationType.SUNDAY -> {
            val validation = MusicRegistrationValidator.validateSundayRows(sundayRows, availableSongs)
            dateOk && validation.incompletePositions.isEmpty() && ...
        }
        // ...
    }
```

O data class `MusicRegistrationUiState` chama diretamente o `MusicRegistrationValidator` (dominio) no getter de uma propriedade computada. Um data class de UI nao deve conter logica de negocio. O `canSubmit` deve ser computado no ViewModel e armazenado como campo do estado.

---

### [S-03] ISP — `SongsRepository` interface muito ampla

**Arquivo:** `features/worshiphub/tables/domain/repository/SongsRepository.kt`

A interface tem 10 metodos cobrindo 5 tipos de dados diferentes (AllSongs, BySunday, TopSongs, TopTones, SuggestedSongs). Clientes que precisam apenas de `observeAllSongs` sao forcados a depender de toda a interface.

**Correcao:** Segregar em interfaces menores: `AllSongsRepository`, `TopSongsRepository`, `SuggestedSongsRepository`, etc.

---

### [S-04] DIP — `CoreViewModel` depende de `AuthSession` concreto

**Arquivo:** `core/presentation/viewmodel/CoreViewModel.kt`

```kotlin
private val authSession: AuthSession,
```

`AuthSession` e uma classe concreta da camada de dados (`features/auth/data/local`). O ViewModel deveria depender de uma interface (ex: `AuthSessionProvider`) definida no dominio de auth.

---

### [S-05] OCP — `SongsRepositoryImpl` duplica a logica de snapshot para `SuggestedSongs`

**Arquivo:** `features/worshiphub/tables/data/repository/SongsRepositoryImpl.kt`

O repositorio reimplementa manualmente toda a logica de cache-first para `SuggestedSongs` (gerencia `MutableStateFlow`, `AtomicBoolean`, carrega cache, etc.) que ja existe encapsulada em `BaseSnapshotRepository`. Isso viola OCP pois a extensao foi feita por copia ao inves de extensao.

**Correcao:** Criar um `SuggestedSongsSnapshotRepository` estendendo `BaseSnapshotRepository` (ajustando para aceitar o parametro `fixedByPosition`).

---

## 4. Problemas de Clean Code

### [CC-01] Typos em nomes

| Arquivo | Problema | Correto |
|---|---|---|
| `AuthEndpoins.kt` | `AuthEndpoins` | `AuthEndpoints` |
| `AuthViewModel.kt:53` | `fun singIn(...)` | `fun signIn(...)` |
| `AuthViewModel.kt:83` | `fun singUp(...)` | `fun signUp(...)` |

---

### [CC-02] Codigo morto

**`AuthViewModel.kt:105`** — Funcao vazia sem implementacao:
```kotlin
fun signInWithGoogle() {
    // iniciar fluxo de login com Google
}
```
Existe junto com `fun signInWithGoogle(idToken: String)`. A versao sem parametro nao faz nada.

**`SongsTableViewModel.kt:95`** — Sealed class definida mas nunca usada:
```kotlin
sealed class SubmitResult {
    data object Success : SubmitResult()
    data class Error(val message: String) : SubmitResult()
}
```

**`PreloadDataUseCase.kt`** — Linhas comentadas indicando implementacao incompleta:
```kotlin
// launch { songsRepository.preload() },
// launch { hymnalRepository.preload() },
// launch { profileRepository.preload() }
```

---

### [CC-03] Erro silenciado — estado perdido

**Arquivo:** `features/schedule/presentation/viewmodel/ScheduleViewModel.kt`

```kotlin
else -> ScheduleUiState.Empty  // Cobre SnapshotState.Error silenciosamente
```

Quando a rede falha e nao ha cache, o erro e mapeado para `Empty`. O usuario ve tela vazia sem feedback. O proprio codigo tem um comentario reconhecendo isso:
```
// Dica: Adicione um 'data class Error(val message: String) : ScheduleUiState'
```
O `ScheduleUiState.Error` deve ser implementado.

---

### [CC-04] Magic strings e numeros magicos

**`ScheduleViewModel.kt`** — Strings hardcoded de dias da semana em portugues:
```kotlin
val nextMeetingKeyword = when (today) {
    Calendar.MONDAY -> "Terca"
    Calendar.TUESDAY -> "Quinta"
    // ...
}
```
Esses valores dependem da forma como a API retorna os nomes das secoes. Qualquer mudanca no backend quebra silenciosamente. Extrair para constantes ou enum.

**`AdminScheduleViewModel.kt:139`** — Horario hardcoded:
```kotlin
time = "19:30"
```
Valor que deveria vir do modelo de dominio ou configuracao, nao ser fixo no ViewModel.

---

### [CC-05] `ScheduleUiState` definida dentro do arquivo do ViewModel

**Arquivo:** `features/schedule/presentation/viewmodel/ScheduleViewModel.kt:18`

Todos os outros features usam a pasta `state/` para contratos de UI (ex: `MusicRegistrationContract.kt`, `AdminScheduleContract.kt`). O `ScheduleUiState` esta no mesmo arquivo do ViewModel, quebrando a convencao do proprio projeto.

**Correcao:** Mover para `features/schedule/presentation/state/ScheduleUiState.kt`.

---

### [CC-06] `GalleryViewModel` com `suspend fun` publicos

**Arquivo:** `features/gallery/presentation/viewmodel/GalleryViewModel.kt`

```kotlin
suspend fun getLocalPhotos(albumId: Long): List<File>
suspend fun getPhotoName(albumId: Long, photoId: Long): String
```

ViewModels nao devem expor funcoes `suspend`. Isso forca as Screens a gerenciar coroutine scopes (`LaunchedEffect`, `rememberCoroutineScope`), acoplando a UI ao ciclo de vida da coroutine. Devem ser expostos como `StateFlow` ou acionados via eventos.

---

### [CC-07] `runBlocking` em `TokenAuthenticator`

**Arquivo:** `core/network/TokenAuthenticator.kt:28`

```kotlin
return runBlocking(Dispatchers.IO) { ... }
```

`runBlocking` dentro de um `Authenticator` do OkHttp pode causar deadlock se o dispatcher de IO estiver saturado. O OkHttp ja executa o `Authenticator` em uma thread separada, mas `runBlocking` bloqueia a thread completamente. Alternativa: usar `OkHttp`s propria API bloqueante diretamente, sem `runBlocking`.

---

### [CC-08] Log de debug em producao

**`AuthRepositoryImpl.kt`** e **`AuthViewModel.kt`** contem multiplos `Log.d("GoogleSignIn", ...)` com dados sensiveis (prefixo do idToken):

```kotlin
Log.d("GoogleSignIn", "Enviando idToken para o backend: ${idToken.take(20)}...")
```

Logs de tokens — mesmo parciais — nao devem ir para producao. Usar `BuildConfig.DEBUG` como guard ou remover.

---

### [CC-09] `logTime` no dominio com dependencia do Android

**Arquivo:** `core/domain/snapshot/BaseSnapshotRepository.kt:13`

```kotlin
inline fun logTime(tag: String, message: String) {
    Log.d(tag, "[${SystemClock.elapsedRealtime()} ms] $message")
}
```

Alem de violar a regra do dominio (CA-01), essa funcao esta definida no nivel de pacote dentro de um arquivo de classe de dominio, o que viola a coesao. Ela tambem e usada em `ScheduleViewModel` (`import logTime`), criando um acoplamento indireto entre o ViewModel e o arquivo de base do dominio.

---

## 5. Matriz de Prioridades

| ID | Descricao | Severidade | Esforco |
|---|---|---|---|
| CA-04 | Domain depende de Presentation (SubmitSundayPlaysUseCase / Validator) | CRITICO | Medio |
| CA-01 | Dominio com android.util.Log / SystemClock | CRITICO | Baixo |
| CA-03 | ViewModel constroe DTOs da camada de dados | ALTO | Medio |
| CA-02 | Core depende de features (PreloadDataUseCase / CoreViewModel) | ALTO | Alto |
| S-01 | ViewModel acessa sistema de arquivos diretamente (ProfileViewModel) | ALTO | Medio |
| S-02 | Estado de UI chama validator de dominio (canSubmit) | ALTO | Baixo |
| CC-03 | Erro de rede silenciado no ScheduleViewModel | ALTO | Baixo |
| CA-05 | AuthViewModel importa mapper da camada data | MEDIO | Baixo |
| S-03 | SongsRepository interface monolitica (ISP) | MEDIO | Alto |
| S-05 | SongsRepositoryImpl duplica logica de BaseSnapshotRepository | MEDIO | Medio |
| CA-06 | Interface SnapshotRepository<T> orfao e inconsistente | MEDIO | Baixo |
| CA-07 | ProfileRepository expoe File no contrato de dominio | MEDIO | Baixo |
| S-04 | CoreViewModel depende de AuthSession concreto (DIP) | MEDIO | Baixo |
| CC-07 | runBlocking em TokenAuthenticator | MEDIO | Baixo |
| CC-06 | GalleryViewModel com suspend fun publicos | MEDIO | Baixo |
| CC-01 | Typos: AuthEndpoins, singIn, singUp | BAIXO | Baixo |
| CC-02 | Codigo morto (signInWithGoogle vazio, SubmitResult, preloads comentados) | BAIXO | Baixo |
| CC-04 | Magic strings de dias da semana e horario hardcoded | BAIXO | Baixo |
| CC-05 | ScheduleUiState fora da pasta state/ | BAIXO | Baixo |
| CC-08 | Log de token parcial em producao | BAIXO | Baixo |
| CC-09 | logTime acoplado ao dominio e reusado no ViewModel | BAIXO | Baixo |

---

## 6. Plano de Correcao Recomendado

### Fase 1 — Rapido (baixo esforco, alto impacto)

1. **[CC-01]** Renomear `AuthEndpoins` -> `AuthEndpoints`, `singIn` -> `signIn`, `singUp` -> `signUp`
2. **[CA-01]** Remover `android.os.SystemClock` e `android.util.Log` de `BaseSnapshotRepository`. Usar `Logger` injetado.
3. **[CA-06]** Remover `core/domain/repository/SnapshotRepository.kt` (interface orfao)
4. **[CC-02]** Remover funcao `signInWithGoogle()` vazia, `SubmitResult` nao usada, e linhas comentadas do `PreloadDataUseCase`
5. **[CC-03]** Implementar `ScheduleUiState.Error` no `ScheduleViewModel`
6. **[S-02]** Mover calculo de `canSubmit` do `MusicRegistrationUiState` para o `MusicRegistrationViewModel`
7. **[CC-05]** Mover `ScheduleUiState` para `features/schedule/presentation/state/ScheduleUiState.kt`

### Fase 2 — Medio prazo (medio esforco)

8. **[CA-04]** Criar modelo de dominio `SundaySongEntry` para substituir `SundaySongRowState` no use case e validator
9. **[CA-03]** Mover `toMonthScheduleDto()` do `AdminScheduleViewModel` para um mapper na camada de dados
10. **[CA-05]** Mover `parseLoginError`/`parseRegisterError` para presentation mapper (fora da camada data)
11. **[S-01]** Mover logica de arquivo do `ProfileViewModel.refreshLocalPhotoPathAndBump()` para um use case
12. **[CC-06]** Converter `getLocalPhotos` e `getPhotoName` em `GalleryViewModel` para StateFlow
13. **[CC-08]** Adicionar guard `if (BuildConfig.DEBUG)` nos logs de token

### Fase 3 — Longo prazo (maior refatoracao)

14. **[CA-02]** Introduzir interface `Preloadable` no core e usar Hilt `@IntoSet` para desacoplar `PreloadDataUseCase` dos features
15. **[S-03]** Segregar `SongsRepository` em interfaces menores por tipo de dado
16. **[S-05]** Extrair `SuggestedSongsSnapshotRepository` estendendo `BaseSnapshotRepository`
17. **[S-04]** Criar interface `AuthStateProvider` para `CoreViewModel` nao depender de `AuthSession` concreto

---

## 7. Pontos de Excelencia a Preservar

- O padrao `BaseSnapshotRepository` e a `SnapshotCacheFactory` — nao alterar a estrutura, apenas corrigir as violacoes pontuais
- `AuthEventBus` como mecanismo de comunicacao desacoplada — bom padrao
- O uso de `MusicRegistrationEvent` como entrada unica no ViewModel (MVI) — expandir para outros ViewModels
- `AppError` com `toAppError()` — manter e garantir que todos os repositorios o usem consistentemente
- `TokenAuthenticator` com `Mutex` para renovacao atomica de token — manter a logica, apenas refatorar o `runBlocking`
- Separacao de modulos Hilt por feature — manter a granularidade atual
