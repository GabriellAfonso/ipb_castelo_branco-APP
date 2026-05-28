# Worship Hub — Spec

O Worship Hub (Min. Louvor) e o hub central do ministerio de louvor da igreja. Concentra todas as ferramentas relacionadas a musicas: tabelas de historico, cifras, letras, e catalogo de musicas.

---

## 1. Estrutura do Hub

Tela principal com grid de botoes 2x2 (expansivel):

| Posicao | Botao     | Icone           | Destino                |
|---------|-----------|-----------------|------------------------|
| 1       | Tabelas   | `ic_table`      | Tela de tabelas (tabs) |
| 2       | Musicas   | `ic_songs`      | Lista de musicas       |
| 3       | Cifras    | `ic_chord_chart` | Lista de cifras       |
| 4       | Letras    | `ic_lyrics`     | Lista de letras        |

Navegacao: `CoreScreen → WorshipHubScreen → [Tabelas | Musicas | Cifras | Letras]`

---

## 2. Tabelas

Tela com 4 tabs horizontais e busca global via FAB.

### 2.1 Ultimos Domingos

Lista agrupada por data (`dd/MM/yyyy`). Cada domingo mostra as musicas tocadas com posicao, titulo, artista e tom.

**Click no titulo:** titulo da musica e clicavel (ripple padrao Material). Navega para `SongDetailScreen` usando `songId`. Apenas o texto do titulo e clicavel, nao a row inteira.

**Busca:** filtra por data, titulo, artista ou tom (accent-insensitive via `normalize()`).

**Dados:** `GET songs-by-sunday/`
```
[{
  "date": "dd/MM/yyyy",
  "songs": [{ "position": int, "song": string, "song_id": int, "artist": string, "tone": string }]
}]
```

### 2.2 Mais Tocadas

Ranking de musicas por numero de vezes tocadas aos domingos. Lista ordenada por `play_count` decrescente.

**Click no titulo:** mesmo comportamento de Ultimos Domingos — titulo clicavel com ripple, navega para `SongDetailScreen` usando `songId`.

**Dados:** `GET top-songs/`
```
[{ "song__title": string, "song_id": int, "play_count": int }]
```

### 2.3 Top Tons

Ranking global de tons mais utilizados. Lista ordenada por `tone_count` decrescente.

**Dados:** `GET top-tones/`
```
[{ "tone": string, "tone_count": int }]
```

### 2.4 Repertorio (Sugestoes)

Gera sugestao de 4 musicas para o proximo domingo, priorizando musicas nao tocadas nos ultimos 90 dias. Permite fixar musicas em posicoes especificas e re-gerar as demais.

**Tom automatico:** ao selecionar uma musica, o tom mais usado historicamente para aquela musica e preenchido automaticamente (calculado client-side a partir de `songsBySunday`).

**Icone de detalhe:** quando uma musica esta selecionada no select, aparece icone `(i)` flutuante sobrepondo o canto direito do select (overlay). Tap no icone navega para `SongDetailScreen` usando `songId`. Icone aparece com `AnimatedVisibility` (fade+scale) e some quando select esta vazio. Nao conflita com tap (selecionar) nem long press (fixar).

**Dados:** `GET suggested-songs/?fixed=1:12,3:45`
```
[{
  "id": int,
  "song": { "id": int, "title": string, "artist": string },
  "date": "dd/MM/yyyy",
  "tone": string,
  "position": int
}]
```

### 2.5 Busca Global (Tabelas)

- FAB circular no canto inferior direito
- Abre barra de busca animada acima das tabs
- Busca accent-insensitive usando `normalize()`
- Ativa apenas na tab "Ultimos Domingos" (demais tabs nao filtram)

---

## 3. Musicas

### 3.1 Lista de Musicas

Lista de todas as musicas cadastradas no sistema. Cada item mostra titulo e artista.

**Busca:** campo de busca no topo, filtra por titulo e artista (accent-insensitive via `normalize()`). Mesmo padrao visual de Cifras e Letras.

**Dados:** reutiliza `GET songs/` (snapshot ja existente em `AllSongsSnapshotRepository`).

```
[{
  "id": int,
  "title": string,
  "artist": string,
  "category": string,
  "youtube_link": string | null
}]
```

> `youtube_link` e campo novo adicionado ao endpoint `songs/`. Pode ser `null` ou string vazia quando a musica nao tem link.

**Sem snapshot proprio** — consome dados dos snapshots ja existentes (AllSongs, SongsBySunday, ChordCharts, Lyrics).

### 3.2 Tela de Detalhe da Musica

Tela com todas as informacoes consolidadas de uma musica. Dados vem de multiplas fontes cruzadas por `songId`.

#### Layout

**Header:**
- Nome da musica (titulo principal)
- Artista (subtitulo)

**Estatisticas:**
- Vezes tocada aos domingos (contagem calculada client-side a partir de `songsBySunday`)
- Tons utilizados (lista de tons distintos extraida client-side de `songsBySunday`, filtrada por `songId`)
- Ultimo(s) domingo(s) em que foi tocada (ate 3, depende do espaco na tela)

**Acoes (botoes):**
- **Cifra** — navega para tela de cifra existente:
  - Se 1 cifra registrada: navega direto para `ChordChartDetailScreen`
  - Se multiplas cifras: abre dialog/bottom sheet para escolher (mostra tom + instrumento de cada)
  - Se nenhuma cifra: botao desabilitado (visualmente claro que esta indisponivel)
- **Letra** — navega para `LyricsDetailScreen`:
  - Se tem letra: navega direto
  - Se nao tem: botao desabilitado
- **YouTube** — abre link externo no navegador/app do YouTube:
  - Se `youtube_link` presente: abre via `Intent(ACTION_VIEW, uri)`
  - Se `youtube_link` null/vazio: botao desabilitado

#### Fontes de dados (todas client-side)

| Dado | Fonte | Relacao |
|------|-------|---------|
| nome, artista, categoria, youtube_link | `AllSongs` snapshot | direto por `songId` |
| vezes tocada | `SongsBySunday` snapshot | filtrar itens por `songId`, contar |
| tons utilizados | `SongsBySunday` snapshot | filtrar por `songId`, coletar tons distintos |
| ultimos domingos | `SongsBySunday` snapshot | filtrar por `songId`, pegar ultimas datas |
| tem cifra(s)? | `ChordCharts` snapshot | filtrar por `songId` |
| tem letra? | `Lyrics` snapshot | filtrar por `songId` |

---

## 4. Cifras

### 4.1 Lista de Cifras

Lista de todas as cifras cadastradas. Cada item mostra nome da musica, tom e instrumento.

**Busca:** campo no topo, filtra por nome da musica (accent-insensitive).

**Pinned:** musicas podem ser fixadas no topo da lista via `SetlistPreferences`. Ordem de exibicao: pinned primeiro (na ordem de pin), depois o resto.

**Dados:** `GET chord-charts/`
```
[{
  "id": int,
  "song_id": int,
  "content": string,
  "tone": string,
  "instrument": string,
  "updated_at": string
}]
```

Nome da musica vem do cruzamento com `AllSongs` por `song_id`.

### 4.2 Detalhe da Cifra

Exibe cifra em formato ChordPro parseado. Conteudo dividido em blocos (Intro, Verso, Coro, etc.) com acordes posicionados acima das letras correspondentes.

**Parser:** `ChordProParser` converte string ChordPro em `List<ChordBlock>`, cada bloco com titulo e linhas de `ChordLine` contendo `LineToken.Chord` e `LineToken.Lyrics`.

**Paginacao:** `BlockPaginator` divide blocos em paginas que cabem na tela, com navegacao por swipe/botoes.

---

## 5. Letras

### 5.1 Lista de Letras

Lista de todas as letras cadastradas. Cada item mostra nome da musica.

**Busca:** campo no topo, filtra por nome da musica (accent-insensitive).

**Pinned:** mesmo mecanismo de `SetlistPreferences` das cifras.

**Dados:** `GET lyrics/`
```
[{
  "id": int,
  "song_id": int,
  "content": string,
  "updated_at": string
}]
```

Nome da musica vem do cruzamento com `AllSongs` por `song_id`.

### 5.2 Detalhe da Letra

Exibe letra dividida em estrofes. `LyricsParser` separa o texto em `List<LyricsStanza>`, cada estrofe com suas linhas.

---

## 6. Modelos de Dominio

### Song
```
id: Int
title: String
artist: String
categoryName: String
youtubeLink: String?        ← novo
```

### SundaySet
```
date: String                // "dd/MM/yyyy"
songs: List<SundaySetItem>
```

### SundaySetItem
```
position: Int
title: String
artist: String
tone: String
songId: Int                 ← novo (antes relacionava por title)
```

### TopSong
```
title: String
playCount: Int
songId: Int                 ← novo
```

### TopTone
```
tone: String
count: Int
```

### SuggestedSong
```
id: Int
songId: Int
title: String
artist: String
date: String
tone: String
position: Int
```

### ChordChart
```
id: Int
songId: Int
content: String             // formato ChordPro
tone: String
instrument: String
```

### Lyrics
```
id: Int
songId: Int
content: String             // texto puro
```

---

## 7. Navegacao

```
worshipHubGraph (AppRoutes.WORSHIP_HUB_GRAPH)
├── WorshipHubScreen (hub com botoes)
├── Tables (tela unica, inline)
├── songsGraph (sub-graph novo)
│   ├── SongsListScreen (lista com busca)
│   └── SongDetailScreen (detalhe da musica)
├── chordChartsGraph (sub-graph existente)
│   ├── ChordChartsScreen (lista com busca)
│   └── ChordChartDetailScreen (detalhe da cifra)
└── lyricsGraph (sub-graph existente)
    ├── LyricsScreen (lista com busca)
    └── LyricsDetailScreen (detalhe da letra)
```

Navegacao entre features a partir de SongDetailScreen:
- Botao Cifra → navega para `ChordChartDetailScreen` (rota existente dentro de `chordChartsGraph`)
- Botao Letra → navega para `LyricsDetailScreen` (rota existente dentro de `lyricsGraph`)
- Botao YouTube → `Intent(ACTION_VIEW)` para URL externa

---

## 8. Endpoints (API)

Todos publicos (`AllowAny`), sem autenticacao.

| Metodo | Path | Descricao |
|--------|------|-----------|
| GET | `songs/` | Todas as musicas cadastradas |
| GET | `songs-by-sunday/` | Historico de domingos com musicas tocadas |
| GET | `top-songs/` | Ranking de musicas mais tocadas |
| GET | `top-tones/` | Ranking de tons mais usados |
| GET | `suggested-songs/` | Sugestao de repertorio |
| GET | `chord-charts/` | Todas as cifras |
| GET | `lyrics/` | Todas as letras |

Todos suportam `If-None-Match` / ETag para cache (exceto `suggested-songs`).

---

## 9. Cache / Offline

Cada fonte de dados usa `JsonSnapshotStorage` com snapshot proprio:

| Snapshot | Repositorio |
|----------|-------------|
| AllSongs | `AllSongsSnapshotRepository` |
| SongsBySunday | `SongsBySundaySnapshotRepository` |
| TopSongs | `TopSongsSnapshotRepository` |
| TopTones | `TopTonesSnapshotRepository` |
| ChordCharts | `ChordChartsSnapshotModule` |
| Lyrics | `LyricsSnapshotModule` |

Feature "Musicas" **nao cria snapshot proprio** — consome dados dos snapshots existentes acima.

---

## 10. Busca

Todas as listas usam busca accent-insensitive via `String.normalize()` (em `core/domain/util/`).

| Tela | Campos buscaveis |
|------|-----------------|
| Ultimos Domingos | data, titulo, artista, tom |
| Musicas (lista) | titulo, artista |
| Cifras | nome da musica |
| Letras | nome da musica |

---

## 11. Itens Futuros

- **Categorias:** campo `categoryName` existe no modelo mas nunca foi implementado como filtro na UI. Potencial filtro por categoria na lista de musicas.
- **Pinned em Musicas:** avaliar se faz sentido ter o mesmo mecanismo de pin de Cifras/Letras na lista de Musicas.
