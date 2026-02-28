// app/src/main/java/com/gabrielafonso/ipb/castelobranco/ui/screens/worshiphub/tabs/SuggestionsTab.kt
package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.presentation.tabs

import android.content.Intent
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.model.SuggestedSong
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.presentation.components.ColumnAlignment
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.presentation.components.Header
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.presentation.components.TableColumn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val columns = listOf(
    TableColumn("#", 0.3f, ColumnAlignment.Center),
    TableColumn("*", 0.6f, ColumnAlignment.Center),
    TableColumn("Nome", 2.2f),
    TableColumn("Tom", 1f, ColumnAlignment.Center),
    TableColumn("Artista", 1f)
)

@Composable
fun SuggestionsTab(
    suggestedSongs: List<SuggestedSong>,
    isRefreshing: Boolean,
    fixedByPosition: Map<Int, Int>,
    onToggleFixed: (SuggestedSong) -> Unit,
    onRefreshClick: () -> Unit
) {
    val context = LocalContext.current

    fun buildShareText(): String {
        val date = SimpleDateFormat("dd/MM/yy", Locale.forLanguageTag("pt-BR")).format(Date())
        val header = "Louvor $date"

        val lines = suggestedSongs
            .sortedBy { it.position }
            .joinToString(separator = "\n") { song ->
                val formattedArtist = if (song.artist.length > 8) {
                    "${song.artist.take(8)}."
                } else {
                    song.artist
                }

                "${song.title}(${song.tone}) ($formattedArtist)"
            }

        return if (lines.isBlank()) header else "$header\n\n$lines"
    }

    fun share() {
        val text = buildShareText()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(
            Intent.createChooser(intent, "Compartilhar").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(columns)

        Box(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    .height(250.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                itemsIndexed(suggestedSongs) { _, song ->
                    val isChecked = fixedByPosition[song.position] == song.id
                    SuggestionsRow(
                        song = song,
                        isRefreshing = isRefreshing,
                        isChecked = isChecked,
                        onCheckedChange = { onToggleFixed(song) }
                    )
                }
            }

            if (isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp),
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                    modifier = Modifier.weight(1f)
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

                Button(
                    onClick = { share() },
                    enabled = !isRefreshing && suggestedSongs.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Compartilhar")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun SuggestionsRow(
    song: SuggestedSong,
    isRefreshing: Boolean,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(columns[0].weight), contentAlignment = Alignment.Center) {
            Text(song.position.toString(), color = textColor)
        }

        Box(Modifier.weight(columns[1].weight), contentAlignment = Alignment.Center) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                enabled = true,
                modifier = Modifier.scale(0.70f)
            )
        }

        Box(Modifier.weight(columns[2].weight), contentAlignment = Alignment.CenterStart) {
            Text(
                song.title,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(Modifier.weight(columns[3].weight), contentAlignment = Alignment.Center) {
            Text(song.tone, color = textColor)
        }

        Box(Modifier.weight(columns[4].weight), contentAlignment = Alignment.CenterStart) {
            Text(
                song.artist,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
