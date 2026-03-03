package com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.navigation.GalleryNav
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.viewmodel.GalleryViewModel
import java.io.File
import android.content.ContentValues
import android.provider.MediaStore
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import java.io.OutputStream
import kotlinx.coroutines.launch

@Composable
fun PhotoScreen(
    albumId: Long,
    photoIndex: Int,
    viewModel: GalleryViewModel,
    nav: GalleryNav
) {
    PhotoContent(
        albumId = albumId,
        initialIndex = photoIndex,
        viewModel = viewModel,
        actions = nav
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoContent(
    albumId: Long,
    initialIndex: Int,
    viewModel: GalleryViewModel,
    actions: GalleryNav
) {
    val view = androidx.compose.ui.platform.LocalView.current
    var photos by remember { mutableStateOf<List<File>>(emptyList()) }
    val pagerState = rememberPagerState(initialPage = initialIndex) { photos.size }
    val context = LocalContext.current
    var isZoomed by remember { mutableStateOf(false) }
    var photoName by remember { mutableStateOf("Carregando...") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(albumId) {
        photos = viewModel.getLocalPhotos(albumId)
    }

    // 2. Observa a mudança de página para atualizar o nome na TopBar
    LaunchedEffect(pagerState.currentPage, photos) {
        if (photos.isNotEmpty()) {
            val currentFile = photos[pagerState.currentPage]
            val currentPhotoId = currentFile.nameWithoutExtension.toLongOrNull()
            if (currentPhotoId != null) {
                photoName = viewModel.getPhotoName(albumId, currentPhotoId)
            }
        }
    }

    BaseScreen(
        tabName = photoName,
        logoRes = R.drawable.ic_galery,
        showBackArrow = true,
        onBackClick = actions.back
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(8.dp))
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
                        var isDownloadEnabled by remember { mutableStateOf(true) }
                        Button(
                            enabled = isDownloadEnabled,
                            onClick = {
                                isDownloadEnabled = false

                                view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                                saveImageToGallery(context, photos[pagerState.currentPage])

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Baixado com sucesso",
                                        duration = SnackbarDuration.Indefinite
                                    )
                                }

                                scope.launch {
                                    delay(1_200)
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                }

                                scope.launch {
                                    delay(1_200)
                                    isDownloadEnabled = true
                                }
                            }
                        ) {
                            Text("Baixar")
                        }
                        Button(onClick = {
                            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                            sharePhoto(context, photos[pagerState.currentPage])
                        }) {
                            Text("Compartilhar")
                        }
                    }
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.Center),
                snackbar = { data: SnackbarData ->
                    Snackbar(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = data.visuals.message,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            )
        }
    }
}

private fun sharePhoto(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar imagem"))
}

fun saveImageToGallery(
    context: Context,
    sourceFile: File
) {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, sourceFile.name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(
            MediaStore.Images.Media.RELATIVE_PATH,
            "${android.os.Environment.DIRECTORY_PICTURES}/ipb_castelobranco" // ou: "${Environment.DIRECTORY_DCIM}/ipb_castelobranco"
        )
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ) ?: return

    resolver.openOutputStream(uri)?.use { output: OutputStream ->
        sourceFile.inputStream().use { input ->
            input.copyTo(output)
        }
    }

    contentValues.clear()
    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
    resolver.update(uri, contentValues, null, null)
}