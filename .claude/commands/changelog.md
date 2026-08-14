---
description: Gera um changelog em pt-BR agrupado por área, salva em changelogs/vX.Y.Z.md e exibe versão resumida para Play Store (≤500 chars). Use: /changelog 0.9.6 0.9.7
allowed-tools: Bash(git log:*), Bash(git tag:*), Bash(git rev-parse:*), Bash(mkdir:*), Bash(tee:*), Bash(echo:*), Bash(grep:*), Bash(sed:*)
---

## Contexto

- Versão atual do app: !`grep 'versionName' app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/'`
- Tags de versão: !`git tag --sort=-version:refname | head -10 || echo "(nenhuma tag)"`
- Range resolvido: !`A="$ARGUMENTS"; r(){ git rev-parse -q --verify "v$1^{commit}" || git rev-parse -q --verify "$1^{commit}" || git log --format=%H --grep="bump version to $1" -1; }; if [ -z "$A" ]; then L=$(git tag --sort=-version:refname | head -1); echo "${L:-(sem tag)}..HEAD"; elif [ "${A#*..}" != "$A" ]; then echo "$A"; else set -- $A; F=$(r "$1"); T=$([ -n "$2" ] && r "$2"); echo "$1..${2:-HEAD}  =  $(git rev-parse --short ${F:-HEAD})..$(git rev-parse --short ${T:-HEAD})"; fi`
- Commits do range: !`A="$ARGUMENTS"; r(){ git rev-parse -q --verify "v$1^{commit}" || git rev-parse -q --verify "$1^{commit}" || git log --format=%H --grep="bump version to $1" -1; }; if [ -z "$A" ]; then L=$(git tag --sort=-version:refname | head -1); git log --oneline --no-merges ${L:+$L..}HEAD; elif [ "${A#*..}" != "$A" ]; then git log --oneline --no-merges $A; else set -- $A; F=$(r "$1"); T=$([ -n "$2" ] && r "$2"); git log --oneline --no-merges ${F:+$F..}${T:-HEAD}; fi`
- Log completo com corpo: !`A="$ARGUMENTS"; r(){ git rev-parse -q --verify "v$1^{commit}" || git rev-parse -q --verify "$1^{commit}" || git log --format=%H --grep="bump version to $1" -1; }; if [ -z "$A" ]; then L=$(git tag --sort=-version:refname | head -1); git log --pretty=format:"%s|%b" --no-merges ${L:+$L..}HEAD; elif [ "${A#*..}" != "$A" ]; then git log --pretty=format:"%s|%b" --no-merges $A; else set -- $A; F=$(r "$1"); T=$([ -n "$2" ] && r "$2"); git log --pretty=format:"%s|%b" --no-merges ${F:+$F..}${T:-HEAD}; fi`

## Sua tarefa

Gere um changelog completo e salve-o em arquivo. Siga os passos abaixo na ordem.

---

### Passo 1 — Identificar a versão alvo

O range é resolvido por **tag de versão**. Cada release lançada tem uma tag `vX.Y.Z` apontando para
o commit que virou APK. Se a tag não existir, o resolvedor cai no commit
`chore(release): bump version to X.Y.Z` — que é aproximado, porque o bump nem sempre é o último
commit da versão.

Formas aceitas em `$ARGUMENTS`:

- `0.9.6 0.9.7` — da tag `v0.9.6` (exclusiva) até a `v0.9.7` (inclusiva). **Versão alvo: `0.9.7`**
- `0.9.6` — da tag `v0.9.6` até `HEAD`. Versão alvo: o `versionName` atual
- `<sha>..HEAD` ou `<ref>..<ref>` — range literal do git, usado como veio
- sem argumento — da tag mais recente até `HEAD`. Versão alvo: o `versionName` atual

O bloco **Range resolvido** acima mostra o intervalo que realmente foi usado, com os SHAs. Confira
antes de gerar: se a versão informada não tiver tag nem commit de bump, o lado do range vem vazio e
o `git log` devolve o histórico inteiro — nesse caso avise em vez de gerar.

### Ao lançar uma versão nova

Depois de publicar, marque o commit lançado para que o próximo changelog tenha o range exato:

```bash
git tag vX.Y.Z <sha-do-commit-lançado>
git push origin vX.Y.Z
```

---

### Passo 2 — Mapeamento de escopos → seções

| Escopos dos commits                            | Seção no changelog                                                              |
|------------------------------------------------|---------------------------------------------------------------------------------|
| `hymnal`, `hinario`                            | **Hinário**                                                                     |
| `chord`, `cifra`, `lyrics`, `letra`            | **Cifras e Letras**                                                             |
| `worshiphub`, `repertorio`                     | **Repertório**                                                                  |
| `gallery`, `galeria`                           | **Galeria**                                                                     |
| `bible`, `biblia`                              | **Bíblia**                                                                      |
| `schedule`, `agenda`                           | **Agenda**                                                                      |
| `studies`, `estudos`                           | **Estudos**                                                                     |
| `auth`, `profile`, `settings`, `core`          | **Geral**                                                                       |
| Sem escopo reconhecível                        | **Geral**                                                                       |

> **Atenção — escopo `admin`:** commits com escopo `admin` devem ser classificados pela feature que afetam (leia o body do commit). Ex: `feat(admin): add chord chart creation` → **Cifras e Letras**, não Geral. Só use Geral se o commit for sobre o painel admin em si.

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

1. **Leia o body dos commits** — o título sozinho não basta. Use o body (disponível no log completo acima) para entender *o que* a mudança faz e *onde* ela se aplica. Não assuma a seção só pelo escopo do título.
2. **Traduza e humanize** — não copie mensagens de commit cruas. `feat(hymnal): add vertical scroll mode` → `Novo modo de rolagem vertical`.
3. **Agrupe semanticamente** — múltiplos commits da mesma funcionalidade viram uma linha.
4. **Omita** chore, test, docs, bump de versão e refactors internos — só o que o usuário final percebe.
5. **Ordem das seções:** mais mudanças primeiro; "Geral" sempre por último.
6. **Sem seções vazias** — omita seções sem mudanças visíveis ao usuário.
7. **Versão Play Store:** máximo **500 caracteres** (incluindo espaços). Se não couber tudo, priorize as mudanças mais impactantes. Informe a contagem ao final: `(XXX/500 caracteres)`.
8. O bloco Play Store deve ser a **última seção do arquivo**, separada por `---`, para fácil localização e cópia.