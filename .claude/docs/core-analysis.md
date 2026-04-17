# Análise do Core — SOLID, Clean Code e Riscos Futuros

> Gerado em: 2026-03-14
> Escopo: `app/src/main/java/com/gabrielafonso/ipb/castelobranco/core/`

---

## Sumário Executivo

| Categoria | Severidade | Qtd |
|-----------|-----------|-----|
| Violações de SRP | CRÍTICA | 4 |
| Violações de DIP | CRÍTICA | 3 |
| Problemas de Concorrência | CRÍTICA | 3 |
| Tratamento de Erros | ALTA | 5 |
| Duplicação de Código | MÉDIA | 3 |
| Nomenclatura / Clareza | MÉDIA | 4 |
| Desalinhamento Arquitetural | ALTA | 3 |
| Código Morto | BAIXA | 2 |

---

## 1. Violações de SOLID

### 1.1 SRP — Single Responsibility Principle

#### `BaseScreen.kt` — CRÍTICO
- Gerencia ao mesmo tempo: carregamento de foto de perfil, estado de auth, navegação do TopBar, resolução de arquivo no sistema de arquivos e integração com Coil.
- `TopBarProfileViewModel` aninhado mistura estado do TopBar com lógica de sessão.
- **Regra quebrada:** um componente deve ter um único motivo para mudar.

#### `CoreViewModel.kt` — MODERADO
- Acumula: preload da app, estado de login, logout e refresh de perfil.
- Mistura "inicialização do app" com "ciclo de vida de autenticação".

#### `CoreActivity.kt` — MODERADO
- Mistura lógica de in-app update com lifecycle e inicialização de tema.
- A checagem de update está duplicada entre `onCreate()` e `onResume()`.

#### `PreloadDataUseCase.kt` — MODERADO
- Orquestra 6+ refreshes de repositórios diretamente no corpo do use case.
- Deve delegar para um `DataRefresher` abstrato injetável.

---

### 1.2 OCP — Open/Closed Principle

#### `BaseSnapshotRepository.kt`
- `Dispatchers.IO` hardcoded em 4 pontos (linhas 30, 42, 49, 55).
- Mudar a estratégia de dispatch exige modificar a classe base.
- **Correção:** injetar o dispatcher via construtor.

#### `TopBar.kt`
- Sound effect do botão Voltar está hardcoded.
- Qualquer customização exige modificação da fonte.

---

### 1.3 DIP — Dependency Inversion Principle

#### `BaseScreen.kt` — CRÍTICO
```kotlin
// Antipadrão: acessa Hilt manualmente no Composable
val entryPoint = EntryPointAccessors.fromActivity(
    context.findActivity() as ComponentActivity, ...
)
```
- Depende da infraestrutura concreta do Hilt em vez de receber dependências injetadas.
- Torna o componente praticamente não testável.

#### `PreloadDataUseCase.kt`
- Construtor lista 6 repositórios concretos como parâmetros.
- Adicionar uma nova feature exige modificar o construtor.
- **Correção:** `constructor(refreshers: List<DataRefresher>)` com binding Hilt via `@IntoSet`.

#### `CoreViewModel.kt`
- Importa diretamente `ProfileRepository` (implementação de feature) no core.
- Core não deveria conhecer detalhes de features.

---

### 1.4 ISP — Interface Segregation Principle

#### `SnapshotRepository.kt`
```kotlin
interface SnapshotRepository<T> {
    fun observe(): Flow<T?>  // null = loading? erro? sem dados?
}
```
- Retornar `T?` é ambíguo — o caller não sabe o que `null` significa.
- A implementação interna já usa `SnapshotState<T>`; a interface não está alinhada com ela.
- **Correção:** `fun observe(): Flow<SnapshotState<T>>`

---

## 2. Violações de Clean Code

### 2.1 Nomenclatura Confusa

- `CoreView.kt` expõe duas funções: `CoreView` (com side effects) e `CoreScreen` (pura) — nomes não comunicam a diferença.
- `DiskCache.kt` — objeto de legado sem uso aparente; nome não reflete que foi substituído por `JsonSnapshotStorage`.
- `TopBar.kt` parâmetro `accountImageModel: Any?` — extremamente permissivo; Coil aceita qualquer coisa.

### 2.2 Duplicação de Código

- **`CoreActivity.kt`:** `checkForImmediateUpdate()` e bloco em `onResume()` executam a mesma checagem de update.
- **`PreloadDataUseCase.kt`:** dois blocos `supervisorScope` consecutivos com estrutura idêntica.
- **`JsonSnapshotStorage.kt`:** múltiplos `withContext(Dispatchers.IO)` poderiam ser extraídos para `private suspend fun <T> io(block: () -> T)`.

### 2.3 Comentários / Documentação

- `BaseSnapshotRepository.kt` linha 13: `inline fun logTime()` definida mas nunca chamada — código de debug esquecido.
- `PreloadDataUseCase.kt` linhas 33–39: chamadas de preload comentadas sem explicação.
- Mistura de comentários em português e inglês no mesmo arquivo.

### 2.4 Magic Numbers / Valores Hardcoded

| Arquivo | Local | Valor | Problema |
|---------|-------|-------|---------|
| `CoreActivity.kt` | linha 26 | `UPDATE_REQUEST_CODE = 500` | sem justificativa |
| `TopBar.kt` | linhas 50, 92, 114 | `24.dp`, `20.dp`, `32.dp` | sem design tokens |
| `JsonSnapshotStorage.kt` | linha 23 | `"[^a-z0-9_-]+"` | regex sem constante nomeada |

### 2.5 Tratamento de Erros Insuficiente

#### `TokenAuthenticator.kt` — ALTA
```kotlin
runCatching { authApi.refresh(...) }  // exceção silenciada
```
- Se o refresh falhar, o token é limpo e o usuário fica em loop de 401 sem feedback.

#### `PreloadDataUseCase.kt` — ALTA
```kotlin
jobs.forEach { deferred ->
    runCatching { deferred.await() }  // erro descartado silenciosamente
}
```
- Se todos os refreshes falharem, a app mostra tela vazia sem indicar o motivo.
- Deveria ao menos logar qual repositório falhou.

#### `JsonSnapshotStorage.kt`
- `writeText()` e `deleteRecursively()` podem lançar `IOException` — não tratados.

---

## 3. Problemas de Concorrência — CRÍTICOS

### 3.1 `TokenAuthenticator.kt` — `runBlocking` no thread de rede
```kotlin
// PERIGO: bloqueia thread pool do OkHttp
val response = runBlocking(Dispatchers.IO) {
    authApi.refresh(...)
}
```
- O `Authenticator` do OkHttp já roda em background thread, mas `runBlocking` monopoliza o thread.
- Com requisições concorrentes que expiram simultaneamente → deadlock.
- **Correção:** usar um `Mutex` compartilhado + `suspendCoroutine` ou mover para um `CoroutineScope` dedicado.

### 3.2 `BaseScreen.kt` — Múltiplos coletores acumulados
```kotlin
// Novo collector a cada recomposição
viewModelScope.launch {
    authSession.isLoggedInFlow.collect { ... }
}
```
- Se `BaseScreen` for recomposto, um novo collector é criado sem cancelar o anterior.
- **Correção:** usar `collectAsState()` ou `LaunchedEffect(Unit)`.

### 3.3 `PreloadDataUseCase.kt` — `forEach` não aguarda conclusão
```kotlin
jobs.forEach { deferred -> runCatching { deferred.await() } }
// deveria ser: jobs.awaitAll()
```
- A função retorna antes de todos os jobs finalizarem.
- A UI pode mostrar estado vazio enquanto o preload ainda está rodando.

---

## 4. Problemas Arquiteturais

### 4.1 `BaseScreen.kt` — Acoplamento Excessivo
- Acopla diretamente: `ProfilePhotoBus`, `AuthSession`, `StorageDirConstants.PROFILE`, Coil, `EntryPointAccessors`.
- Na prática não é uma "BaseScreen" genérica — é o TopBar do app disfarçado de utilitário.
- **Risco:** qualquer nova feature no TopBar (notificações, busca) vai inflar ainda mais este arquivo.

### 4.2 `SnapshotRepository` — Interface Desalinhada com Implementação
- A interface expõe `Flow<T?>`, mas `BaseSnapshotRepository` trabalha com `SnapshotState<T>` internamente.
- Quem implementa a interface perde informação de estado (loading, error, data).

### 4.3 `CoreViewModel` — Importa Implementações de Features
- `CoreViewModel` depende de `ProfileRepository` (feature `profile`).
- Core importando feature = dependência em sentido errado.
- Features deveriam registrar comportamentos no core via interfaces / DI, não o oposto.

### 4.4 `PreloadDataUseCase` — Não Escala
- A cada nova feature com cache offline, deve-se adicionar um parâmetro no construtor e uma linha no body.
- Sem feedback de progresso para a UI.
- **Correção:** `@IntoSet` no Hilt + `Set<DataRefresher>` injetado.

### 4.5 `DiskCache.kt` — Legado Não Removido
- Aparentemente substituído por `JsonSnapshotStorage` e não usado em lugar algum.
- Mantê-lo gera confusão para futuros devs.

---

## 5. Riscos Futuros

### 5.1 Performance / ANR
- `TokenAuthenticator.runBlocking` pode causar ANR em dispositivos lentos ou com muitas requisições simultâneas.
- `BaseScreen.kt` faz `dir.listFiles()` a cada mudança de estado — lento em armazenamento cheio.
- `PreloadDataUseCase` dispara 9 requests simultâneos no início — saturação de rede em conexões lentas.

### 5.2 Confiabilidade
- Erros de preload silenciados: usuário vê tela vazia sem opção de retry.
- Loop de 401: se o refresh de token falhar, o usuário fica preso sem feedback.

### 5.3 Vazamento de Memória
- `TopBarProfileViewModel` criado com `ViewModelProvider.Factory` manual pode acumular instâncias se `BaseScreen` for usado em múltiplas abas/grafos.

### 5.4 Testabilidade
- `BaseScreen.kt` com 150+ linhas, 3 ViewModels e EntryPoints é praticamente não testável sem infraestrutura Hilt completa.
- `CoreViewModel.initialize()` tem side effects fora do construtor — pattern problemático em testes unitários.

### 5.5 Update Dialog Repetido
- `CoreActivity.onResume()` checa update a cada retorno ao app.
- Usuário que minimize/abra repetidamente pode ver múltiplos diálogos de update.
- **Correção:** flag booleana `updateCheckDone` por ciclo de vida.

---

## 6. Top 5 Correções Prioritárias

| # | Arquivo | Problema | Impacto se não corrigido |
|---|---------|---------|--------------------------|
| 1 | `TokenAuthenticator.kt:28` | `runBlocking` no thread OkHttp | ANR / deadlock em produção |
| 2 | `PreloadDataUseCase.kt:59` | `forEach` em vez de `awaitAll` + erros silenciados | Tela vazia sem motivo |
| 3 | `BaseScreen.kt:93` | `EntryPointAccessors` manual | Impossível testar; vai crescer sem controle |
| 4 | `SnapshotRepository.kt` | Interface retorna `Flow<T?>` desalinhada | Acumula inconsistências em cada nova feature |
| 5 | `PreloadDataUseCase.kt` | Construtor com 6 repositórios concretos | Cada feature nova exige modificar core |
