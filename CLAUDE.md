# CLAUDE.md — IPB Castelo Branco

> Este arquivo define como o Claude deve se comportar neste projeto.

---

## 1. Visão Geral do Projeto

- **Nome:** IPB Castelo Branco
- **Objetivo:** App da Igreja Presbiteriana de Castelo Branco — agenda, hinário, galeria, área administrativa
- **Público-alvo:** Membros e liderança da igreja
- **Package:** `com.ipb.castelobranco`
- **minSdk:** 24 | **targetSdk/compileSdk:** 36 | **versionName:** 0.7.2
- **API base:** `https://gabrielafonso.com.br/ipbcb/`
- **Gradle modules:** 1 apenas (`:app`) — todo código em `app/src/main/java/`

---

## 2. Stack Principal

```
Kotlin 2.3.10
Jetpack Compose BOM 2026.02.00 + Material3
Min SDK: 24 / Target SDK: 36
JVM target: 17
DI: Hilt 2.59.1 (KSP)
Rede: Retrofit 3.0.0 + OkHttp 5.3.2
Serialização: Kotlinx Serialization JSON 1.10.0
Persistência: DataStore Preferences 1.2.0 + local JSON snapshot
Imagens: Coil 2.6.0
Navegação: Navigation Compose 2.9.7 + Hilt Navigation Compose 1.3.0
Atualizações: Google Play In-App Updates (app-update-ktx 2.1.0)
AGP: 9.0.1
```

---

## 3. Fluxo de Trabalho — Plano Antes de Agir

Para qualquer tarefa que envolva criar ou modificar código, o Claude DEVE:

1. **Apresentar um plano** antes de escrever qualquer linha. O plano deve conter:
   - Quais arquivos serão criados ou modificados
   - O que cada arquivo fará (em uma frase)
   - Se alguma dependência nova será necessária
   - Riscos ou pontos de atenção

2. **Aguardar confirmação** do usuário antes de implementar.

3. **Só então implementar.**

Formato esperado:
```
Plano de implementação:

Criar:
  - features/orders/presentation/OrderListScreen.kt — tela de listagem
  - features/orders/presentation/OrderListViewModel.kt — gerencia estado da tela

Modificar:
  - core/presentation/navigation/AppNavHost.kt — adicionar rota de orders
  - core/presentation/navigation/AppRoutes.kt — registrar nova rota

Dependências novas: nenhuma
Riscos: nenhum identificado

Posso aplicar?
```

---

## 4. Arquitetura — Feature-Based + MVVM

**Padrão:** MVVM + Clean Architecture com camadas `data/`, `domain/`, `presentation/` por feature.

**Single Activity:** `CoreActivity` é a ÚNICA `@AndroidEntryPoint`. Todas as telas são Composables.

### 4.1 Estrutura de Pastas

```
app/src/main/java/com/ipb/castelobranco/
├── MyApp.kt                        — @HiltAndroidApp, inicializa tema
├── core/
│   ├── data/                       — DataStore, repositórios, snapshot cache
│   ├── di/                         — Módulos Hilt (ApiModule, HttpClientModule, RetrofitModule, ...)
│   ├── domain/                     — Interfaces de repositório, use cases, AuthEventBus
│   ├── network/                    — AuthInterceptor, TokenAuthenticator (auto refresh)
│   └── presentation/
│       ├── CoreActivity.kt         — única Activity; in-app updates aqui
│       ├── viewmodel/CoreViewModel.kt — estado global (auth, preload, logout)
│       ├── screens/CoreView.kt
│       ├── navigation/
│       │   ├── AppRoutes.kt        — constantes de rota (migração para type-safe pendente)
│       │   ├── AppNavHost.kt       — único NavHost
│       │   └── LocalAppNavigator.kt — CompositionLocal para navegação da TopBar
│       └── base/BaseScreen.kt      — Scaffold padrão com TopBar
└── features/
    ├── auth/         — login, registro, refresh de token
    ├── profile/      — perfil, foto (UCrop)
    ├── schedule/     — agenda mensal
    ├── settings/     — tema (dark/light/system)
    ├── gallery/      — galeria de fotos
    ├── hymnal/       — hinário
    ├── worshiphub/   — hub de louvor (letras, cifras, tabelas)
    └── admin/        — painel admin (agenda, registro de culto)
```

Cada feature segue internamente `data/` → `domain/` → `presentation/`.

### 4.2 Regras da Arquitetura

- Features não importam umas das outras. Compartilhe via `core/`.
- A camada `domain/` não conhece Android — sem `Context`, sem `ViewModel`, sem Compose.
- A camada `data/` implementa as interfaces definidas em `domain/`.
- A camada `presentation/` só conhece o ViewModel. Nunca acessa repositórios ou use cases diretamente.
- Erros de domínio são sealed classes ou `Result<T>` — exceções HTTP cruas nunca chegam ao ViewModel.

---

## 5. Princípios de Design

### 5.1 SOLID

**S — Single Responsibility**
Cada classe tem uma única razão para mudar.
- Screen: renderiza estado e emite eventos. Nada mais.
- ViewModel: transforma dados em estado de UI e lida com eventos. Nada mais.
- UseCase: executa uma única operação de negócio. Nada mais.
- ❌ Errado: ViewModel que faz chamada de rede, formata string e navega.

**O — Open/Closed**
Estenda comportamento sem modificar código existente.
- Prefira composição de use cases a modificar um use case existente.
- ❌ Errado: adicionar `if (tipo == "admin")` dentro de um use case existente. Certo: criar `GetAdminScheduleUseCase`.

**L — Liskov Substitution**
Implementações devem substituir suas interfaces sem quebrar o contrato.
- `FakeRepository` em testes deve se comportar exatamente como a interface espera.
- ❌ Errado: implementação que lança exceções não previstas na interface.

**I — Interface Segregation**
Interfaces pequenas e focadas.
- Um repositório somente-leitura não deve implementar métodos de escrita.
- ❌ Errado: `GalleryRepository` com 10 métodos quando a feature usa apenas 2.

**D — Dependency Inversion**
Dependa de abstrações, não de implementações.
- ViewModel depende da interface `ScheduleRepository`, não de `ScheduleRepositoryImpl`.
- Injete dependências via construtor (Hilt).
- ❌ Errado: instanciar `RepositoryImpl()` dentro do ViewModel.

### 5.2 Clean Architecture

Fluxo de dependências sempre aponta para dentro:

```
UI (Compose) → ViewModel → UseCase → Repository (interface) → Repository (impl) + DataSources
```

- `domain/` não depende de nada externo — é o núcleo.
- `data/` depende de `domain/` (implementa suas interfaces).
- `presentation/` depende de `domain/` (modelos) e observa o ViewModel.

---

## 6. UI — Jetpack Compose

### 6.1 Estado de UI

Toda tela tem um sealed class ou data class de estado. Sempre trate os três casos:

```kotlin
sealed class ScheduleUiState {
    object Loading : ScheduleUiState()
    data class Success(val months: List<Month>) : ScheduleUiState()
    data class Error(val message: String) : ScheduleUiState()
}
```

- O ViewModel expõe `StateFlow<UiState>`.
- A Screen coleta com `collectAsStateWithLifecycle()`, nunca `collectAsState()`.
- Nunca deixe loading, error ou empty sem tratamento visual — sempre os três.

### 6.2 Regras de Composables

- Composables são **burros**: recebem estado e emitem eventos via lambdas. Sem lógica.
- Separe a Screen (que coleta do ViewModel) do Composable de conteúdo (que recebe dados puros). Facilita preview e teste.
- Prefira `remember` com chave explícita. Evite `remember` sem dependências em dados mutáveis.
- Side effects (`LaunchedEffect`, `DisposableEffect`) apenas para operações que dependem do ciclo de vida.
- ❌ Errado: chamar use case ou fazer operação assíncrona dentro de um Composable.
- Nomenclatura: PascalCase, sem prefixo (`ScheduleScreen`, não `ScheduleScreenComposable`).

### 6.3 Previews

Todo Composable de conteúdo deve ter ao menos um `@Preview` com dados fake explícitos, não placeholders vazios.

---

## 7. ViewModel

- Expõe apenas `StateFlow` e `SharedFlow`. Nunca `LiveData`.
- Use `viewModelScope` para coroutines. Nunca crie `CoroutineScope` manualmente.
- Navegação e eventos únicos (snackbar, dialog) via `SharedFlow<UiEvent>`, não via estado.
- ViewModel não conhece `Context`. Se precisar de string de recurso, receba via parâmetro ou use `UiText`.
- **Shared ViewModels** em grafos multi-tela usam graph-scoped hiltViewModel:
  ```kotlin
  val graphEntry = remember(entry) { navController.getBackStackEntry(AppRoutes.GRAPH_ROUTE) }
  val vm: XViewModel = hiltViewModel(graphEntry)
  ```
  Aplicado em: `authGraph`, `adminGraph`, `worshipHubGraph`, `hymnalGraph`, `galleryGraph`.

---

## 8. Coroutines

- `Dispatchers.IO` para operações de I/O (rede, banco). Nunca no Main.
- `Dispatchers.Default` para CPU-intensive.
- Prefira `flow {}` a callbacks aninhados.
- Sempre trate cancelamento — não ignore `CancellationException`.
- Use `supervisorScope` quando quiser que falha de um filho não cancele os demais.
- ❌ Errado: `GlobalScope.launch`. Sempre use `viewModelScope` ou scope injetado.

---

## 9. Navegação

- **Features com múltiplas telas** usam `fun NavGraphBuilder.xGraph(navController)` em `XNavGraph.kt`.
- **Features de tela única** (schedule, settings, profile) são `composable {}` inline no `AppNavHost`.
- **Grafos existentes:** `authGraph`, `adminGraph`, `worshipHubGraph`, `hymnalGraph`, `galleryGraph`.
- **`AppRoutes`** define as constantes de rota — ao adicionar telas, registre a rota aqui primeiro.

**Migração pendente — Type-safe Navigation:**
O projeto deve migrar as string constants de `AppRoutes` para rotas tipadas (Compose Navigation 2.8+). Novas rotas criadas a partir de agora devem usar o padrão type-safe. Rotas existentes serão migradas gradualmente.

---

## 10. DI — Custom Qualifiers

```kotlin
@AuthedRetrofit    // Retrofit com auth token
@AuthLessRetrofit  // Retrofit sem token (login/registro)
@Client            // OkHttpClient autenticado
@AuthLessClient    // OkHttpClient sem auth
@ApiBaseUrl        // String da base URL
@AuthPrefs         // Auth DataStore
@SettingsPrefs     // Settings DataStore
```

Todos os módulos ficam em `SingletonComponent`.

APIs públicas (auth) usam `@AuthLessRetrofit`. APIs protegidas usam `@AuthedRetrofit`. Qualifier errado causa 401 silencioso.

---

## 11. Segurança

- **Nunca hardcode** API keys, secrets ou tokens no código ou em `strings.xml`. Use `local.properties` + `BuildConfig`.
- **Tokens de autenticação** devem ser armazenados em `EncryptedSharedPreferences` (Android Keystore / AES-256). O projeto atualmente usa plain `DataStore` — **migração pendente**.
- Não logue dados de usuário (`Log.d` com PII é proibido em produção).
- ProGuard/R8 habilitado em release. Não desabilite ofuscação sem justificativa.
- Valide inputs no lado do cliente antes de enviar — mas nunca confie apenas nisso.

---

## 12. Variáveis de Ambiente e Config

- `local.properties` contém `GOOGLE_CLIENT_ID` — **não commitar**.
- Acessado via `BuildConfig` no build script.

---

## 13. Testes

- Use cases e repositórios: testes unitários com `JUnit4` + `MockK` + `kotlinx-coroutines-test`.
- ViewModels: testes unitários com `turbine` para testar flows.
- UI: testes de Composable com `ComposeTestRule` para fluxos críticos.
- Mínimo: **caminho feliz + 1 caso de erro** por use case.
- Prefira fakes a mocks quando possível — são mais estáveis.

### Executando testes no container

```bash
# Todos os testes
./gradlew :app:testDebugUnitTest

# Pacote específico (ex: core)
./gradlew :app:testDebugUnitTest --tests "com.ipb.castelobranco.core.*"

# Classe específica
./gradlew :app:testDebugUnitTest --tests "com.ipb.castelobranco.core.network.AuthInterceptorTest"
```

**Regras críticas para não destruir o cache:**
- ❌ Nunca use `clean` antes de rodar testes — apaga todo o cache incremental do Kotlin/KSP e transforma uma execução de segundos em 15+ minutos.
- ❌ Nunca use `--rerun-tasks` — quebra o cache do KSP e causa falha de build.
- ❌ Nunca use `--no-daemon` — mata o warm-up da JVM entre execuções.
- ✅ Execute normalmente: o Gradle reutiliza o cache e é incremental.
- ✅ Use `clean` **somente** se houver erro de compilação inexplicável.

O `sdk.dir` em `local.properties` deve apontar para `/home/node/.local/android-sdk` no container (não para o path do Windows).

---

## 14. Regras de Código

- **Todo código em inglês.** Nomes de variáveis, funções, classes, arquivos, comentários e mensagens de commit. Exceção: strings visíveis ao usuário final (em português).
- **Strings** são hardcoded em português no código — sem uso consistente de `strings.xml`.
- **Seja conciso.** Sem abstrações desnecessárias. Se pode ser uma função, não precisa ser uma classe.
- Limite de linha: 120 caracteres. Kotlin code style: `official` (definido em `gradle.properties`).
- Organize imports: Android → Compose → Hilt → projeto.
- **Sem magic strings.** Constantes em `companion object` ou arquivo de constantes.
- **Kotlin 2.3.10 + AGP 9.0.1:** versões recentes — verifique compatibilidade antes de adicionar libs.

---

## 15. Git

- **Version bump:** sempre em commit separado, após commits de feature/refactor, usando `chore(release): bump version to X.Y.Z`.
- Mensagens de commit seguem Conventional Commits (`feat`, `fix`, `refactor`, `chore`, `test`, `docs`).

---

## 16. Como o Claude Deve se Comportar

### Faça:
- **Sempre apresente o plano e aguarde confirmação antes de implementar** (ver seção 3).
- Trate sempre os três estados de UI: loading, success e error.
- Mantenha Composables sem lógica — lógica fica no ViewModel ou UseCase.
- Pergunte antes de criar arquivos fora da estrutura definida.
- Aponte violações de arquitetura e riscos de segurança que encontrar.
- Ao adicionar tela a um grafo existente, use `hiltViewModel(graphEntry)`.

### Não faça:
- Não implemente sem o plano aprovado.
- Não misture lógica de negócio em Composables.
- Não use `GlobalScope`, `runBlocking` em produção ou `LiveData` em código novo.
- Não crie novas Activities — tudo via Compose/Navigation.
- Não adicione módulos Gradle por feature — projeto é intencionalmente monolítico.
- Não use `ViewBinding`/`DataBinding` — UI é 100% Compose.
- Não adicione dependências sem perguntar.
- Não omita tratamento de erro achando que "o happy path basta".
- Não mexa em `app/proguard-rules.pro` sem necessidade.

---

## 17. Pitfalls Comuns

1. **Graph-scoped ViewModel:** Ao adicionar tela dentro de um grafo existente (hymnal, gallery, worshiphub, admin), use `hiltViewModel(graphEntry)` — não `hiltViewModel()` direto.

2. **Dois clientes Retrofit:** APIs públicas (auth) usam `@AuthLessRetrofit`; APIs protegidas usam `@AuthedRetrofit`. Qualifier errado causa 401 silencioso.

3. **Tela única vs. grafo:** Adicione feature nova como `composable {}` inline no `AppNavHost` ou como grafo separado — depende de ter múltiplas telas. Consulte `AppNavHost.kt` primeiro.

4. **Snapshot cache:** Algumas features têm módulo de snapshot próprio (`HymnalSnapshotModule`, `ScheduleSnapshotModule`, etc.). Para persistência offline, siga o padrão `JsonSnapshotStorage` existente.

5. **UCropActivity** está no Manifest — não remover; usado para upload de foto de perfil.

6. **`AppRoutes`** define as constantes de rota — ao adicionar telas, registre aqui primeiro.

7. **Theme change:** use `context.findActivity()?.recreate()` para aplicar mudança de tema (chamado da Screen, nunca do ViewModel).

8. **Auth success / logout:**
   - Auth success: lambda `onAuthSuccess()` propagado de `AppNavHost` → `authGraph`.
   - Logout: `popUpTo(MAIN) { inclusive = true }` via lambda `onLogoutSuccess` no `AppNavHost`.

9. **Share intents:** `navController.context.startActivity(Intent.createChooser(...))`.
