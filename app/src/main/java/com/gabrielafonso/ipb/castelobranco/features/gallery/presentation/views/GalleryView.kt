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
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.navigation.GalleryNav
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.viewmodel.GalleryViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import java.io.File

@Composable
fun GalleryView(
    nav: GalleryNav,
    viewModel: GalleryViewModel,
    onBackClick: () -> Unit
) {
    GalleryScreen(
        actions = nav,
        viewModel = viewModel
    )
}

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    actions: GalleryNav
) {
    val downloadState by viewModel.downloadState.collectAsState()
    val albums by viewModel.albums.collectAsState()

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

            if (albums.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(albums) { album ->
                        AlbumItem(album, viewModel, onClick = { actions.toAlbum(album.id) })
                    }
                }
            } else {
                Text(
                    text = "Nenhum álbum disponível. Baixe a galeria.",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AlbumItem(
    album: Album,
    viewModel: GalleryViewModel,
    onClick: () -> Unit
) {
    val thumbnail by viewModel.getAlbumThumbnail(album.id).collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)  // Mantém o card quadrado para consistência
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()  // Preenche todo o card
        ) {
            if (thumbnail != null) {
                AsyncImage(
                    model = thumbnail,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,  // Crop para preencher sem distorção
                    modifier = Modifier.fillMaxSize()  // Imagem preenche todo o Box/Card
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sem imagem")
                }
            }

            // Overlay semi-transparente no fundo para o nome
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)  // Alinha no fundo
                    .background(Color.Black.copy(alpha = 0.5f))  // Cinza escuro semi-transparente (ajuste alpha ou cor se quiser mais claro)
                    .padding(8.dp)
            ) {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,  // Texto branco para contraste
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumScreen(
    albumId: Long,
    viewModel: GalleryViewModel,
    actions: GalleryNav
) {
    var photos by remember { mutableStateOf<List<File>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(albumId) {
        photos = viewModel.getLocalPhotos(albumId)
    }

    BaseScreen(
        tabName = "Álbum",
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
            if (photos.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoScreen(
    albumId: Long,
    initialIndex: Int,
    viewModel: GalleryViewModel,
    actions: GalleryNav
) {
    var photos by remember { mutableStateOf<List<File>>(emptyList()) }
    val pagerState = rememberPagerState(initialPage = initialIndex) { photos.size }
    val context = LocalContext.current

    var isZoomed by remember { mutableStateOf(false) }

    LaunchedEffect(albumId) {
        photos = viewModel.getLocalPhotos(albumId)
    }

    BaseScreen(
        tabName = "Foto",
        logoRes = R.drawable.ic_galery,
        showBackArrow = true,
        onBackClick = actions.back
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (photos.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    userScrollEnabled = !isZoomed
                ) { index ->
                    var scale by remember { mutableFloatStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }

                    LaunchedEffect(pagerState.currentPage) {
                        scale = 1f
                        offset = Offset.Zero
                        isZoomed = false
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                // Primeiro detector: Apenas para o Double Tap
                                detectTapGestures(
                                    onDoubleTap = {
                                        scale = 1f
                                        offset = Offset.Zero
                                        isZoomed = false
                                    }
                                )
                            }
                            .pointerInput(Unit) {
                                // Segundo detector: Zoom e Pan
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    do {
                                        val event = awaitPointerEvent()
                                        val zoom = event.calculateZoom()
                                        val pan = event.calculatePan()

                                        // Só atualizamos e consumimos se houver mudança real (zoom ou arrasto com zoom)
                                        if (zoom != 1f || (scale > 1f && pan != Offset.Zero)) {
                                            scale = (scale * zoom).coerceIn(1f, 5f)
                                            isZoomed = scale > 1f

                                            if (isZoomed) {
                                                offset += pan
                                                // Consome os eventos para o Pager não rodar
                                                event.changes.forEach { it.consume() }
                                            }
                                        }

                                        // Se resetarmos pro 1.0f manualmente ou na pinça
                                        if (scale <= 1f) {
                                            isZoomed = false
                                            offset = Offset.Zero
                                        }

                                    } while (event.changes.any { it.pressed })
                                }
                            }
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                    ) {
                        AsyncImage(
                            model = photos[index],
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { sharePhoto(context, photos[pagerState.currentPage]) }) {
                        Text("Compartilhar")
                    }
                    Button(onClick = { copyToDownloads(context, photos[pagerState.currentPage]) }) {
                        Text("Baixar")
                    }
                }
            }
        }
    }
}
private fun sharePhoto(context: Context, file: File) {
    val uri = Uri.fromFile(file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar imagem"))
}

private fun copyToDownloads(context: Context, file: File) {
    val downloadsDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val destFile = File(downloadsDir, file.name)
    file.copyTo(destFile, overwrite = true)
    // Note: In real app, handle permissions and MediaScanner if needed
}