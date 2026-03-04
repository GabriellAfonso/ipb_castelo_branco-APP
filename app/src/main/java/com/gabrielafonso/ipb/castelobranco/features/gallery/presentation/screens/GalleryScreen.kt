package com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.presentation.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.features.gallery.domain.model.Album
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.components.AlbumItem
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.navigation.GalleryNav
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.viewmodel.GalleryViewModel

import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.viewmodel.GalleryDownloadState

@Composable
fun GalleryScreen(
    nav: GalleryNav,
    viewModel: GalleryViewModel,
    // Agora recebemos a lista já coletada do NavGraph para garantir zero delay
    albums: List<Album>
) {
    GalleryContent(
        actions = nav,
        viewModel = viewModel,
        albums = albums
    )
}

@Composable
fun GalleryContent(
    viewModel: GalleryViewModel,
    actions: GalleryNav,
    albums: List<Album> // Recebe a lista aqui
) {
    val downloadState by viewModel.downloadState.collectAsState()

    BaseScreen(
        tabName = "Galeria",
        logoRes = R.drawable.ic_galery,
        showBackArrow = true,
        onBackClick = actions.back
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                // 1. Prioridade para o progresso de download
                downloadState.isDownloading -> {
                    DownloadProgressIndicator(downloadState)
                }

                // 2. Se a lista vinda do preload/repository tiver dados, mostra a Grid
                // Como isso foi carregado no MainViewModel, o 'isNotEmpty' será true no primeiro frame
                albums.isNotEmpty() -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(albums, key = { it.id }) { album ->
                            AlbumItem(
                                album = album,
                                viewModel = viewModel,
                                onClick = { actions.toAlbum(album.id) }
                            )
                        }
                    }
                }

                // 3. Só cai aqui se o preload realmente não achou pastas
                else -> {
                    EmptyGalleryPlaceholder(onDownloadClick = { viewModel.downloadAllPhotos() })
                }
            }
        }
    }
}

// Componentes auxiliares para limpar o código principal
@Composable
private fun DownloadProgressIndicator(state: GalleryDownloadState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        if (state.total > 0) {
            Text(
                text = "Baixando: ${state.downloaded} / ${state.total}",
                style = MaterialTheme.typography.bodyMedium
            )
            LinearProgressIndicator(
                progress = { state.downloaded.toFloat() / state.total.toFloat() },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun EmptyGalleryPlaceholder(onDownloadClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Nenhum álbum disponível localmente.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onDownloadClick,
            modifier = Modifier.height(56.dp)
        ) {
            Text("Baixar Galeria Completa")
        }
    }
}




