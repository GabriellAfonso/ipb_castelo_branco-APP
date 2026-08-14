# Galeria — Spec

A Galeria é o acervo de fotos dos eventos da igreja. É **conteúdo restrito a membros**: o servidor
protege os dois endpoints com `IsMemberUser`. No app ela funciona **offline-first** — nada é
exibido a partir da rede, só a partir do disco. O que a tela mostra é sempre o resultado de um
download já concluído.

---

## 1. Telas

| Tela      | Rota                        | Origem                        |
|-----------|-----------------------------|-------------------------------|
| Galeria   | `GalleryMain`               | `CoreScreen → botão Galeria`  |
| Álbum     | `Album/{albumId}`           | Clique num álbum da grid      |
| Foto      | `Photo/{albumId}/{index}`   | Clique numa foto do álbum     |

As três vivem em `galleryGraph` (`GalleryNavGraph.kt`), com `GalleryViewModel` escopado ao grafo
via `hiltViewModel(graphEntry)`.

`isLoggedIn` é **parâmetro de `galleryGraph`** (`StateFlow<Boolean>`), vindo do `CoreViewModel` que
o `AppNavHost` resolve. Não pode ser buscado por `getBackStackEntry(AppRoutes.CORE)`: o
`CoreViewModel` é resolvido **fora** do `NavHost`, no escopo da Activity, então aquele lookup
devolve uma segunda instância — sem `initialize()`, com `_isLoggedIn` travado em `false`. Era o
que fazia a galeria pedir login a quem já estava logado.

### 1.1 Galeria

Grid de 2 colunas de álbuns (`AlbumItem`: thumbnail + nome). Acima da grid, um banner
não-bloqueante reflete o estado do download em andamento.

### 1.2 Álbum

Grid de 3 colunas com as fotos do álbum, lidas do disco (`getLocalPhotos`). Vazio exibe
"Nenhuma foto neste álbum."

### 1.3 Foto

Visualizador em pager horizontal, com o nome da foto na top bar.

---

## 2. Dados

### 2.1 `GET api/photos/` — `IsMemberUser`

```json
[{
  "id": 12,
  "name": "img00.jpg",
  "description": "",
  "album_id": 3,
  "album_name": "Acampamento 2025",
  "image_url": "https://.../media/gallery/img00.jpg",
  "date_taken": null,
  "uploaded_at": "2026-03-11T14:02:00Z"
}]
```

A extensão do arquivo salvo é derivada de `image_url` (`png` / `webp` / `jpg`, com `jpg` como
padrão).

Galeria vazia é `200` com `[]` — **não** é erro.

| Código | Quando                          | `detail`                                              |
|--------|---------------------------------|-------------------------------------------------------|
| `401`  | Sem token / token inválido      | "As credenciais de autenticação não foram fornecidas." |
| `403`  | Autenticado, mas não é membro   | "Disponível apenas para membros."                      |

### 2.2 `GET api/albums/{id}/photos/` — `IsMemberUser`

Mesmo payload, filtrado por álbum. Usado só pelo download por álbum.

### 2.3 Disco

`filesDir/gallery/{albumId}/{photoId}.{ext}` para a imagem e `{photoId}.json` para o
`GalleryPhotoDto` correspondente. Os álbuns são **derivados do disco**: cada subdiretório é um
álbum, e o nome vem do `album_name` da primeira foto. Thumbnail é a foto chamada `img00.jpg`, ou
a primeira do álbum.

`GalleryRepositoryImpl` é `@Singleton` e expõe `albumsFlow`, `thumbnailsFlow` e `photosFlow`,
repopulados por `preload()` (registrado como `Preloadable`).

---

## 3. Download

Roda em `GalleryDownloadWorker` (WorkManager, trabalho único `gallery_auto_download`), nunca no
`viewModelScope` — o download é longo e precisa sobreviver à saída da tela.

| Gatilho                          | Política   | Rede       |
|----------------------------------|------------|------------|
| Boot do app, galeria vazia       | `KEEP`     | `UNMETERED`|
| Login bem-sucedido               | `REPLACE`  | `UNMETERED`|
| Botão "Baixar Galeria Completa"  | `KEEP`     | `UNMETERED`|
| Botão "Tentar novamente"         | `REPLACE`  | `UNMETERED`|
| Botão "Usar dados móveis"        | `REPLACE`  | `CONNECTED`|
| Logout                           | cancela    | —          |

O worker chama `repository.preload()` a cada álbum concluído, então a grid ganha álbuns durante o
download. Erros de rede fazem até 3 tentativas; `401` e `403` falham na hora — repetir não muda o
resultado.

### 3.1 O download só é disparado com sessão ativa

`triggerIfNeeded()` no boot **exige login**. Sem essa guarda, um usuário deslogado enfileira um
trabalho que só pode terminar em `401`, e o `WorkInfo` `FAILED` fica retido no banco do
WorkManager por dias. Como `downloadState` é derivado de `getWorkInfosForUniqueWorkFlow`, esse
erro antigo continua sendo emitido **depois** de o usuário logar — a tela pedia login a quem já
estava logado. Por isso o login bem-sucedido também re-enfileira com `REPLACE`: substituir o
trabalho é o que apaga o `WorkInfo` falho.

Logout cancela o trabalho e apaga as fotos do disco — o acervo é restrito a membros e não deve
sobreviver ao fim da sessão.

---

## 4. Estados da tela

`GalleryDownloadState` (`isDownloading`, `isPending`, `downloaded`, `total`, `error`, `errorCode`,
`isResolved`) é mapeado do `WorkInfo`:

| `WorkInfo.State`     | Estado                                       |
|----------------------|----------------------------------------------|
| `RUNNING`            | `isDownloading` + progresso                  |
| `ENQUEUED`           | `isPending`                                  |
| `FAILED`             | `error` + `errorCode`                        |
| demais / ausente     | resolvido, sem erro                          |

`isResolved` distingue "ainda não sei" (valor inicial do `stateIn`) de "sei que não há nada" —
sem ele a tela piscaria o estado vazio antes do primeiro `WorkInfo` chegar.

Ordem de decisão da tela:

1. **Deslogado** → "Faça login para acessar a galeria." + botão de login.
2. **Banner** (independe da grid): baixando / aguardando WiFi (com atalho para dados móveis).
3. **Com álbuns** → grid.
4. **Sem álbuns e sem download**:
   - não resolvido → `CircularProgressIndicator`
   - `errorCode == 401` → mensagem + "Conectar à sua conta"
   - `errorCode == 403` → mensagem, **sem** botão de login (logar de novo não torna ninguém membro)
   - outro erro → mensagem + "Tentar novamente"
   - sem erro → "Nenhum álbum disponível localmente." + "Baixar Galeria Completa"

O botão de login aparece **somente** em `401`. Falha de rede não é problema de sessão, e oferecer
login ali manda o usuário a uma tela que não resolve nada.
