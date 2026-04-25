package com.ipb.castelobranco.features.gallery.presentation.screens

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
import com.ipb.castelobranco.R
import com.ipb.castelobranco.core.presentation.base.BaseScreen
import com.ipb.castelobranco.core.presentation.components.PermissionErrorPlaceholder
import com.ipb.castelobranco.features.gallery.domain.model.Album
import com.ipb.castelobranco.features.gallery.presentation.components.AlbumItem
import com.ipb.castelobranco.features.gallery.presentation.navigation.GalleryNav
import com.ipb.castelobranco.features.gallery.presentation.viewmodel.GalleryDownloadState
import com.ipb.castelobranco.features.gallery.presentation.viewmodel.GalleryViewModel

@Composable
fun GalleryScreen(
    nav: GalleryNav,
    viewModel: GalleryViewModel,
    albums: List<Album>,
    onNavigateToAuth: () -> Unit,
) {
    GalleryContent(actions = nav, viewModel = viewModel, albums = albums, onNavigateToAuth = onNavigateToAuth)
}

@Composable
fun GalleryContent(
    viewModel: GalleryViewModel,
    actions: GalleryNav,
    albums: List<Album>,
    onNavigateToAuth: () -> Unit = {},
) {
    val downloadState by viewModel.downloadState.collectAsState()
    val isOnWifi by viewModel.isOnWifi.collectAsState()

    BaseScreen(
        tabName = "Galeria",
        logoRes = R.drawable.ic_galery,
        showBackArrow = true,
        onBackClick = actions.back,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Banner de progresso não-bloqueante (visível mesmo com álbuns na grid)
            when {
                downloadState.error != null -> { /* tratado no bloco abaixo */ }
                downloadState.isDownloading -> DownloadProgressBanner(downloadState)
                downloadState.isPending && !isOnWifi -> WaitingForWifiBanner(
                    onDownloadWithMobileData = { viewModel.downloadWithMobileData() },
                )
                downloadState.isPending -> PendingWifiBanner()
            }

            if (albums.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(albums, key = { it.id }) { album ->
                        AlbumItem(
                            album = album,
                            viewModel = viewModel,
                            onClick = { actions.toAlbum(album.id) },
                        )
                    }
                }
            } else if (!downloadState.isDownloading) {
                // Galeria vazia e nenhum download em andamento
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!downloadState.isResolved) {
                        CircularProgressIndicator()
                    } else {
                        val errorMsg = downloadState.error
                        if (errorMsg != null) {
                            PermissionErrorPlaceholder(
                                message = errorMsg,
                                onLoginClick = onNavigateToAuth,
                                showLoginButton = downloadState.errorCode != 403,
                            )
                        } else {
                            EmptyGalleryPlaceholder(
                                onDownloadClick = { viewModel.downloadAllPhotos() },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadProgressBanner(state: GalleryDownloadState) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Text(
                    text = if (state.total > 0)
                        "Baixando fotos: ${state.downloaded} / ${state.total}"
                    else
                        "Iniciando download…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            if (state.total > 0) {
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { state.downloaded.toFloat() / state.total.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PendingWifiBanner() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Aguardando WiFi para baixar a galeria…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun WaitingForWifiBanner(onDownloadWithMobileData: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Aguardando WiFi…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onDownloadWithMobileData) {
                Text("Usar dados móveis")
            }
        }
    }
}

@Composable
private fun EmptyGalleryPlaceholder(onDownloadClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Nenhum álbum disponível localmente.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onDownloadClick,
            modifier = Modifier.height(56.dp),
        ) {
            Text("Baixar Galeria Completa")
        }
    }
}
