# Play Store — Checklist de Correções

Análise do estado atual do app antes de publicar na Play Store.

---

## CRITICO — Bloqueia publicação

### 1. Release assinado com keystore de debug
**Arquivo:** `app/build.gradle.kts:53`

```kotlin
signingConfig = signingConfigs.getByName("debug")  // ERRADO
```

A Play Store rejeita APKs assinados com a keystore de debug.
Criar uma keystore de release e configurar o `signingConfigs` corretamente no `build.gradle.kts`.

---

### 2. `android:usesCleartextTraffic="true"` global no Manifest
**Arquivo:** `app/src/main/AndroidManifest.xml:19`

O próprio código tem um comentário dizendo para remover:
```xml
<!-- tirar android:usesCleartextTraffic="true" -->
```

A flag está ativa globalmente, permitindo HTTP não criptografado em qualquer domínio.
O `network_security_config.xml` já libera cleartext apenas para os IPs de dev (`10.0.2.2`, `192.168.1.100`) — basta remover a flag global do `<application>`.

---

### 3. `HttpLoggingInterceptor` ativo em release sem guarda de BuildConfig
**Arquivo:** `app/src/main/java/.../core/di/HttpClientModule.kt:21-23`

```kotlin
fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
    HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC  // ativo em release
    }
```

Em release, loga headers HTTP (incluindo tokens JWT de autenticação) no Logcat.
Condicionar o nível pelo `BuildConfig.DEBUG`:

```kotlin
level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
        else HttpLoggingInterceptor.Level.NONE
```

---

## ALTO — Impacta a experiência do usuário

### 4. Botões "In Dev" visíveis no WorshipHub
**Arquivo:** `app/src/main/java/.../features/worshiphub/hub/presentation/screens/WorshipHubScreen.kt:71-72`

```kotlin
WorshipHubButtonInfo(R.drawable.ic_in_development, "In Dev", actions.button5),
WorshipHubButtonInfo(R.drawable.ic_in_development, "In Dev", actions.button6),
```

Dois botões com label `"In Dev"` expostos ao usuário final. Remover ou esconder antes de publicar.

---

### 5. Botão "Marcar Presença" no Admin sem ação
**Arquivo:** `app/src/main/java/.../features/admin/panel/presentation/screens/AdminScreen.kt:64`

```kotlin
onClick = { /* TODO */ }
```

O card aparece na tela, o usuário clica e nada acontece. Esconder o card ou desabilitar visualmente enquanto não estiver implementado.

---

### 6. `GOOGLE_CLIENT_ID` pode ser uma string vazia em produção
**Arquivo:** `app/build.gradle.kts:34-37`

```kotlin
buildConfigField(
    "String", "GOOGLE_CLIENT_ID",
    "\"${localProps.getProperty("GOOGLE_CLIENT_ID", "")}\""
)
```

Se `GOOGLE_CLIENT_ID` não estiver no `local.properties` do ambiente de build, o valor será `""` silenciosamente, quebrando o login com Google sem nenhum erro claro. Garantir que a chave esteja disponível no ambiente de build de release (ex: via CI secret ou arquivo local protegido).

---

## MEDIO — Boas práticas / Política da Play Store

### 7. `Log.d/e` com dados sensíveis sem guarda de BuildConfig
**Arquivos:**
- `features/auth/presentation/viewmodel/AuthViewModel.kt:58-99`
- `features/auth/presentation/screens/AuthScreen.kt:82-114`
- `features/auth/data/repository/AuthRepositoryImpl.kt:32-73`

Vários logs ativos em release que imprimem partes de tokens JWT e status de autenticação. Além de má prática de segurança, pode ser problema de conformidade com a política de privacidade da Play Store.
Envolver todos os `Log.*` de debug com `if (BuildConfig.DEBUG)` ou remover.

---

### 8. Regras Proguard para Retrofit comentadas
**Arquivo:** `app/proguard-rules.pro:28`

```
## 2. Manter a interface da API (para o Retrofit ler os métodos corretamente)
#-keep interface com.gabrielafonso.ipb.castelobranco.data.api.BackendApi { *; }
```

A regra está comentada. Com R8/minify ativo no release, obfuscação pode renomear as interfaces Retrofit e causar crashes em produção.
Descomentar e ajustar o padrão para cobrir todas as interfaces de API do projeto:

```
-keep interface com.gabrielafonso.ipb.castelobranco.features.**.data.api.** { *; }
```

---

### 9. Telas "Em construção" acessíveis pelo menu principal
**Arquivo:** `app/src/main/java/.../features/worshiphub/hub/presentation/navigation/WorshipHubNavGraph.kt:72-77`

Os botões "Cifras", "Letras" e outros no WorshipHub navegam para `InDevelopmentScreen`. Não é bloqueio técnico, mas pode ser motivo de rejeição no review da Play Store por conteúdo incompleto. Avaliar se vale esconder esses botões até as telas estarem prontas.

---

## BAIXO — Melhorias pos-lancamento

### 10. Sem testes automatizados
O projeto ainda não tem testes. Não é bloqueio para publicação, mas é risco de regressão a cada atualização.
Priorizar testes de ViewModels e Use Cases conforme documentado no `CLAUDE.md`.

### 11. Verificar `versionCode`
**Arquivo:** `app/build.gradle.kts:29-31`

```kotlin
versionCode = 1
versionName = "0.6.2"
```

Confirmar que `versionCode = 1` é o esperado para a primeira publicação. Se o app já foi distribuído por outro canal (APK direto, Firebase App Distribution, etc.), o `versionCode` precisa ser maior que qualquer versão já instalada.

---

## Resumo

| # | Problema | Arquivo | Prioridade |
|---|----------|---------|------------|
| 1 | Signing com keystore de debug | `build.gradle.kts:53` | CRITICO |
| 2 | `usesCleartextTraffic="true"` global | `AndroidManifest.xml:19` | CRITICO |
| 3 | Logging HTTP ativo em release | `HttpClientModule.kt:21` | CRITICO |
| 4 | Botoes "In Dev" visiveis no WorshipHub | `WorshipHubScreen.kt:71` | ALTO |
| 5 | Botao sem acao no Admin Panel | `AdminScreen.kt:64` | ALTO |
| 6 | GOOGLE_CLIENT_ID pode ser vazio | `build.gradle.kts:34` | ALTO |
| 7 | Logs de token em producao | `AuthViewModel.kt`, etc | MEDIO |
| 8 | Proguard Retrofit comentado | `proguard-rules.pro:28` | MEDIO |
| 9 | Telas "Em construcao" acessiveis | `WorshipHubNavGraph.kt:72` | MEDIO |
| 10 | Sem testes automatizados | — | BAIXO |
| 11 | Verificar versionCode | `build.gradle.kts:29` | BAIXO |
