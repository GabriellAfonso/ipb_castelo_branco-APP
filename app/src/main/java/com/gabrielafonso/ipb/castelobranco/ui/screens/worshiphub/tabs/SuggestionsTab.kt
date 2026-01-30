package com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gabrielafonso.ipb.castelobranco.domain.model.SuggestedSong
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.components.ColumnAlignment
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.components.Header
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.components.TableColumn

private val columns = listOf(
    TableColumn("#", 0.3f, ColumnAlignment.Center),
    TableColumn("Nome", 2.5f),
    TableColumn("Tom", 1f, ColumnAlignment.Center),
    TableColumn("Artista", 1f)
)
@Composable
fun SuggestionsTab(
    suggestedSongs: List<SuggestedSong>,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        Header(columns)

        Box(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    .height(150.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                itemsIndexed(suggestedSongs) { index, song ->
                    SuggestionsRow(
                        song = song,
                        isRefreshing = isRefreshing,
                    )
                }
            }

            if (isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .weight(1f)
        ) {
            Button(
                onClick = onRefreshClick,
                enabled = !isRefreshing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                if (!isRefreshing) {
                    Text(text = "Atualizar")
                } else {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}


@Composable
fun SuggestionsRow(
    song: SuggestedSong,
    isRefreshing: Boolean
) {
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    if (isRefreshing) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        )
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {

        Box(Modifier.weight(columns[0].weight), contentAlignment = Alignment.Center) {
            Text(song.position.toString(), color = textColor)
        }

        Box(Modifier.weight(columns[1].weight), contentAlignment = Alignment.CenterStart) {
            Text(
                song.title,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(Modifier.weight(columns[2].weight), contentAlignment = Alignment.Center) {
            Text(song.tone, color = textColor)
        }

        Box(Modifier.weight(columns[3].weight), contentAlignment = Alignment.CenterStart) {
            Text(
                song.artist,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
