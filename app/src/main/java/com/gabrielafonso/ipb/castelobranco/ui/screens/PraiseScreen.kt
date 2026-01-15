package com.gabrielafonso.ipb.castelobranco.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator // Importe isso para o loading
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.data.model.SongRow
import com.gabrielafonso.ipb.castelobranco.data.model.TableView
import com.gabrielafonso.ipb.castelobranco.ui.components.LastSongItem
import com.gabrielafonso.ipb.castelobranco.ui.components.PraiseTables
import com.gabrielafonso.ipb.castelobranco.viewmodel.PraiseViewModel
import kotlin.collections.component1
import kotlin.collections.component2


@Composable
fun PraiseScreen(viewModel: PraiseViewModel, onBack: () -> Unit) {
    var currentView by remember { mutableStateOf(TableView.LAST_SONGS) }

    // Observa a lista mapeada vinda do ViewModel
    val data by viewModel.rows.observeAsState(emptyList())

    // Dispara a busca sempre que a aba mudar
    LaunchedEffect(currentView) {
        viewModel.fetchData(currentView)
    }

    BaseScreen(
        tabName = "Louvores",
        logo = painterResource(id = R.drawable.louvor_icon),
        accountImage = painterResource(id = R.drawable.ic_account),
        showBackArrow = true,
        onBackClick = onBack,
        backgroundColor = Color(0xFFc7dbd2)
    ) { innerPadding ->

        if (data.isEmpty()) {
            // Centraliza o loading enquanto os dados não chegam
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF1C1C1B))
            }
        } else {
            val lastSongsItems = remember(data) {
                buildLastSongsItems(data)
            }
            // ATENÇÃO: Se o PraiseTables já usa "item { ... }",
            // você NÃO deve envolver ele em outra Column ou Row dentro da LazyColumn.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Aqui chamamos o componente que estende a LazyListScope
                PraiseTables(
                    currentView = currentView,
                    onViewChange = { currentView = it },
                    data = data,
                    lastSongsItems = lastSongsItems
                )
            }
        }
    }
}
fun buildLastSongsItems(data: List<SongRow>): List<LastSongItem> =
    data
        .groupBy { it.date ?: "Sem data" }
        .flatMap { (date, songs) ->
            listOf(LastSongItem.DateHeader(date)) +
                    songs.mapIndexed { index, song ->
                        LastSongItem.Song(
                            row = song,
                            dayIndex = index + 1
                        )
                    }
        }