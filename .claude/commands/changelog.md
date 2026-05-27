---
description: Gera um changelog em pt-BR agrupado por área de funcionalidade, a partir dos commits entre duas versões. Use: /changelog v0.9.0 v0.9.1
allowed-tools: Bash(git log:*), Bash(git tag:*)
---

## Contexto

- Tags disponíveis: !`git tag --sort=-version:refname | head -20`
- Commits entre as versões informadas (ou últimos 30 se nenhuma faixa passada): !`git log --oneline --no-merges $ARGUMENTS 2>/dev/null || git log --oneline --no-merges -30`
- Log completo com corpo (para detalhes): !`git log --pretty=format:"%s|%b" --no-merges $ARGUMENTS 2>/dev/null || git log --pretty=format:"%s|%b" --no-merges -30`

## Sua tarefa

Gere um changelog em **pt-BR** no formato Play Store/App Store a partir dos commits listados acima.

### Mapeamento de escopos → seções

Use o escopo do commit convencional (ex: `feat(hymnal): ...`) para agrupar:

| Escopos dos commits          | Seção no changelog          |
|------------------------------|-----------------------------|
| `hymnal`, `chord`, `cifra`   | **Cifras** ou **Cifras e Letras** (use "Cifras e Letras" se a mudança afeta letra também) |
| `worshiphub`, `repertorio`, `lyrics` | **Repertório**       |
| `gallery`, `galeria`         | **Galeria**                 |
| `bible`, `biblia`            | **Bíblia**                  |
| `schedule`, `agenda`         | **Agenda**                  |
| `auth`, `profile`, `settings`, `core`, `admin` | **Geral** |
| Sem escopo reconhecível      | **Geral**                   |

### Formato de saída

```
<versão alvo>

  <Seção 1>
  - <Descrição humana, imperativo, em pt-BR>
  - <Descrição humana, imperativo, em pt-BR>

  <Seção 2>
  - <Descrição humana, imperativo, em pt-BR>

  Geral
  - <Descrição humana, imperativo, em pt-BR>
```

### Regras

1. **Traduza e humanize** — não copie mensagens de commit cruas. Transforme `feat(hymnal): add vertical scroll mode` em `Novo modo de rolagem vertical`.
2. **Agrupe semanticamente** — múltiplos commits na mesma funcionalidade viram uma só linha.
3. **Omita** commits de chore, test, docs, bump de versão e refactor internos — só inclua o que o usuário final percebe.
4. **Ordem das seções:** coloque as seções com mais mudanças primeiro; "Geral" sempre por último.
5. **Sem seções vazias** — omita seções sem nenhuma mudança visível ao usuário.
6. Se não houver argumento (`$ARGUMENTS` vazio), use os últimos 30 commits e indique no topo que a faixa não foi especificada.
7. Exiba o changelog final em um bloco de código para fácil cópia.
