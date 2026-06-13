---
description: Gera um changelog em pt-BR agrupado por área, salva em changelogs/vX.Y.Z.md e exibe versão resumida para Play Store (≤500 chars). Use: /changelog v0.9.0 v0.9.1
allowed-tools: Bash(git log:*), Bash(git tag:*), Bash(mkdir:*), Bash(tee:*), Bash(echo:*), Bash(grep:*)
---

## Contexto

- Tags disponíveis: !`git tag --sort=-version:refname | head -20`
- Versão atual do app: !`grep 'versionName' app/build.gradle.kts | grep -oP '"\K[^"]+'`
- Commits entre as versões informadas: !`git log --oneline --no-merges $ARGUMENTS 2>/dev/null || git log --oneline --no-merges -30`
- Log completo com corpo: !`git log --pretty=format:"%s|%b" --no-merges $ARGUMENTS 2>/dev/null || git log --pretty=format:"%s|%b" --no-merges -30`

## Sua tarefa

Gere um changelog completo e salve-o em arquivo. Siga os passos abaixo na ordem.

---

### Passo 1 — Identificar a versão alvo

Extraia a versão alvo do `$ARGUMENTS` (ex: de `v0.9.0 v0.9.1`, a versão alvo é `v0.9.1`).
Se não houver argumento, use a **versão atual do app** lida acima de `app/build.gradle.kts` (campo `versionName`) como versão alvo e indique no topo que a faixa não foi especificada.

---

### Passo 2 — Mapeamento de escopos → seções

| Escopos dos commits                            | Seção no changelog                                                              |
|------------------------------------------------|---------------------------------------------------------------------------------|
| `hymnal`, `chord`, `cifra`                     | **Cifras** ou **Cifras e Letras** (use "Cifras e Letras" se afeta letra também) |
| `worshiphub`, `repertorio`, `lyrics`           | **Repertório**                                                                  |
| `gallery`, `galeria`                           | **Galeria**                                                                     |
| `bible`, `biblia`                              | **Bíblia**                                                                      |
| `schedule`, `agenda`                           | **Agenda**                                                                      |
| `auth`, `profile`, `settings`, `core`, `admin` | **Geral**                                                                       |
| Sem escopo reconhecível                        | **Geral**                                                                       |

---

### Passo 3 — Gerar o changelog completo (sem limite de caracteres)

Formato do arquivo `.md`:

~~~
# Changelog — <versão alvo>

> Gerado em: <data atual>
> Commits: <range ou "últimos 30">

## <Seção com mais mudanças>
- <Descrição humana, imperativo, pt-BR — pode ser detalhada>
- ...

## <Próxima seção>
- ...

## Geral
- ...

---

## 🏪 Play Store (≤ 500 caracteres)

<versão alvo>

  <Seção 1>
  - <item resumido>

  <Seção 2>
  - <item resumido>

  Geral
  - <item resumido>

`(XXX/500 caracteres)`
~~~

---

### Passo 4 — Salvar o arquivo

Execute o seguinte comando para criar a pasta e salvar:

```bash
mkdir -p changelogs && tee changelogs/<versão-alvo>.md << 'EOF'
<conteúdo gerado>
EOF
```

Confirme a criação com: `echo "✅ Salvo em changelogs/<versão-alvo>.md"`

---

### Regras gerais

1. **Traduza e humanize** — não copie mensagens de commit cruas. `feat(hymnal): add vertical scroll mode` → `Novo modo de rolagem vertical`.
2. **Agrupe semanticamente** — múltiplos commits da mesma funcionalidade viram uma linha.
3. **Omita** chore, test, docs, bump de versão e refactors internos — só o que o usuário final percebe.
4. **Ordem das seções:** mais mudanças primeiro; "Geral" sempre por último.
5. **Sem seções vazias** — omita seções sem mudanças visíveis ao usuário.
6. **Versão Play Store:** máximo **500 caracteres** (incluindo espaços). Se não couber tudo, priorize as mudanças mais impactantes. Informe a contagem ao final: `(XXX/500 caracteres)`.
7. O bloco Play Store deve ser a **última seção do arquivo**, separada por `---`, para fácil localização e cópia.