package com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.gabrielafonso.ipb.castelobranco.features.gallery.data.repository.Album
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.viewmodel.GalleryViewModel

@Composable
fun AlbumItem(
    album: Album,
    viewModel: GalleryViewModel,
    onClick: () -> Unit
) {

    val thumbnails by viewModel.thumbnails.collectAsState()
    val thumbFile = thumbnails[album.id]

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
            if (thumbFile != null) {
                AsyncImage(
                    model = thumbFile,
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