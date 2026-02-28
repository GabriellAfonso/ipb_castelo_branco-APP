package com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.features.gallery.data.repository.Album
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.components.AlbumItem
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.navigation.GalleryNav
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.viewmodel.GalleryViewModel

import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.viewmodel.GalleryDownloadState
import java.io.File
@Composable
fun GalleryView(
    nav: GalleryNav,
    viewModel: GalleryViewModel,
    // Agora recebemos a lista já coletada do NavGraph para garantir zero delay
    albums: List<Album>
) {
    GalleryScreen(
        actions = nav,
        viewModel = viewModel,
        albums = albums
    )
}

@Composable
fun GalleryScreen(
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




