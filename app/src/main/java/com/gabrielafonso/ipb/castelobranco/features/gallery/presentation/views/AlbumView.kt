package com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.features.gallery.data.repository.Album
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.navigation.GalleryNav
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.viewmodel.GalleryViewModel
import java.io.File

@Composable
fun AlbumView(
    albumId: Long,
    viewModel: GalleryViewModel,
    nav: GalleryNav
) {
    val albums by viewModel.albums.collectAsState()
    val albumName = remember(albumId, albums) {
        albums.firstOrNull { it.id == albumId }?.name ?: "Álbum"
    }

    AlbumScreen(
        albumId = albumId,
        albumName = albumName,
        viewModel = viewModel,
        actions = nav
    )
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumScreen(
    albumId: Long,
    albumName: String,
    viewModel: GalleryViewModel,
    actions: GalleryNav
) {
    var photos by remember { mutableStateOf<List<File>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(albumId) {
        photos = viewModel.getLocalPhotos(albumId)
    }

    BaseScreen(
        tabName = albumName,
        logoRes = R.drawable.ic_galery,
        showBackArrow = true,
        onBackClick = actions.back
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(4.dp)
                .fillMaxSize()
        ) {
            if (photos.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(photos) { file ->
                        val index = photos.indexOf(file)
                        AsyncImage(
                            model = file,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,  // Crop para quadrado uniforme na grid
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth()
                                .clickable { actions.toPhoto(albumId, index) }
                        )
                    }
                }
            } else {
                Text("Nenhuma foto neste álbum.")
            }
        }
    }
}
