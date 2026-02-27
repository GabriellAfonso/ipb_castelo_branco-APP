package com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.navigation.GalleryNav
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.viewmodel.GalleryViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun GalleryView(
    nav: GalleryNav,
    viewModel: GalleryViewModel,
    onBackClick: () -> Unit
) {

    val remembered = remember(nav) { nav }
    GalleryScreen(
        actions = remembered,
        viewModel = viewModel
    )
    //TODO se for colocar mais actions cria um objeto pra armazenar os tipos de action
}

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    actions: GalleryNav
) {
    val downloadState by viewModel.downloadState.collectAsState()
    val photos by viewModel.photos.collectAsState()

    BaseScreen(
        tabName = "Galeria",
        logoRes = R.drawable.ic_galery,
        showBackArrow = true,
        onBackClick = actions.back
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = viewModel::downloadAllPhotos,
                    enabled = !downloadState.isDownloading
                ) {
                    Text("Baixar galeria")
                }

                OutlinedButton(
                    onClick = viewModel::clearGallery
                ) {
                    Text("Apagar galeria")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (downloadState.total > 0) {
                LinearProgressIndicator(
                    progress = {
                        downloadState.downloaded.toFloat() /
                                downloadState.total.toFloat()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "${downloadState.downloaded} / ${downloadState.total}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (photos.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(photos) { file ->
                        GalleryPhotoItem(file)
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryPhotoItem(file: File) {
    AsyncImage(
        model = file,
        contentDescription = null,
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
    )
}