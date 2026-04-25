package com.ipb.castelobranco.features.worshiphub.tables.presentation.tabs

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.Song
import com.ipb.castelobranco.features.worshiphub.tables.presentation.viewmodel.RepertoireRowState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Green = Color(0xFF0F6B5C)

@Composable
fun RepertoireTab(
    rows: List<RepertoireRowState>,
    availableSongs: List<Song>,
    isRefreshing: Boolean,
    onSongSelect: (position: Int, song: Song?) -> Unit,
    onGenerateClick: () -> Unit
) {
    val context = LocalContext.current

    fun buildShareText(): String {
        val date = SimpleDateFormat("dd/MM/yy", Locale.forLanguageTag("pt-BR")).format(Date())
        val header = "*Louvor — $date*"

        val lines = rows
            .filter { it.selectedSong != null }
            .joinToString(separator = "\n\n") { row ->
                val song = row.selectedSong!!
                val titlePart = if (row.tone.isBlank()) {
                    "*${song.title}*"
                } else {
                    "*${song.title} (${row.tone})*"
                }
                "$titlePart\n_- ${song.artist}_"
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            RepertoireRow(
                row = row,
                availableSongs = availableSongs,
                enabled = !isRefreshing,
                onSongSelect = { song -> onSongSelect(row.position, song) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onGenerateClick,
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
                    Text(text = "Gerar")
                } else {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            val hasAnySelection = rows.any { it.selectedSong != null }
            Button(
                onClick = { share() },
                enabled = !isRefreshing && hasAnySelection,
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
}

@Composable
private fun RepertoireRow(
    row: RepertoireRowState,
    availableSongs: List<Song>,
    enabled: Boolean,
    onSongSelect: (Song?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(searchQuery, availableSongs) {
        val q = searchQuery.trim()
        if (q.isBlank()) availableSongs
        else availableSongs.filter {
            it.title.contains(q, ignoreCase = true) || it.artist.contains(q, ignoreCase = true)
        }
    }

    LaunchedEffect(row.selectedSong) {
        expanded = false
        searchQuery = ""
    }

    val triggerShape = if (expanded)
        RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
    else
        RoundedCornerShape(8.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "${row.position}.",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .width(18.dp)
                .padding(top = 14.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(triggerShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .clickable(enabled = enabled) { expanded = !expanded }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val label = row.selectedSong?.let { formatSongLabel(it) }
                Text(
                    text = label ?: "Música",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (label != null)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (expanded) Green else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(Green),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Buscar música...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                        )
                                    }
                                    inner()
                                }
                            }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    if (filtered.isEmpty()) {
                        Text(
                            text = "Nenhuma música encontrada",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        filtered.forEach { song ->
                            val isSelected = song.id == row.selectedSong?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelected) Green.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable { onSongSelect(song) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatSongLabel(song),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Green else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

private fun formatSongLabel(song: Song): String =
    if (song.artist.isBlank()) song.title else "${song.title} [${song.artist}]"
