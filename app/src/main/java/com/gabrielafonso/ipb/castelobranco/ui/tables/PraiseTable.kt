package com.gabrielafonso.ipb.castelobranco.ui.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.gabrielafonso.ipb.castelobranco.data.model.SongRow
import com.gabrielafonso.ipb.castelobranco.data.lastSongs
import com.gabrielafonso.ipb.castelobranco.data.topSongs
import com.gabrielafonso.ipb.castelobranco.data.topTones
import com.gabrielafonso.ipb.castelobranco.data.suggestedSongs
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.unit.dp



import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

import com.gabrielafonso.ipb.castelobranco.data.model.TableView


@Composable
fun TablesTabs(
    selected: TableView,
    onSelect: (TableView) -> Unit
) {
    val tabs = listOf(
        "Últimas Tocadas" to TableView.LAST_SONGS,
        "Mais Tocadas" to TableView.TOP_SONGS,
        "Tons Frequentes" to TableView.TOP_TONES,
        "Sugestões" to TableView.SUGGESTED_SONGS
    )

    TabRow(
        selectedTabIndex = tabs.indexOfFirst { it.second == selected },
        divider = {},
        containerColor = Color(0xFFc7dbd2),
        contentColor = Color.Black,
        modifier = Modifier
            .padding(top = 10.dp)
//            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) // apenas topo arredondado

            .fillMaxWidth(),


        indicator = { tabPositions ->
            val currentTabPosition = tabPositions[tabs.indexOfFirst { it.second == selected }]
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(currentTabPosition)
                    .height(4.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) // indicador também arredondado só no topo
                    .background(Color(0xFF1C1C1B))
            )
        }
    ) {
        tabs.forEach { (title, view) ->
            Tab(
                selected = selected == view,
                onClick = { onSelect(view) },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.Black,
                text = {
                    Text(
                        text = title,
                        color = if (selected == view) Color.Black else Color.Black,
                        fontWeight = if (selected == view) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}
@Composable
fun TableHeader(view: TableView) {
    when (view) {
        TableView.LAST_SONGS -> Header(
            listOf(
                "#" to 0.9f,
                "Nome" to 4f,
                "Tom" to 1f,
                "Artista" to 2f
            )
        )
        TableView.TOP_SONGS -> Header(
            listOf(
                "#" to 0.8f,
                "Nome" to 3f,
                "Vezes" to 1f
            )
        )
        TableView.TOP_TONES -> Header(
            listOf(
                "#" to 0.8f,
                "Tom" to 2f,
                "Vezes" to 1f
            )
        )
        TableView.SUGGESTED_SONGS -> Header(
            listOf(
                "#" to 0.8f,
                "Nome" to 3f,
                "Tom" to 1f,
                "Data" to 1f,
                "Artista" to 2f
            )
        )
    }
}
@Composable
private fun Header(titles: List<Pair<String, Float>>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFc7dbd2))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        titles.forEach { (title, weight) ->
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(weight),

            )
        }
    }
}

@Composable
fun DateHeader(date: String, isFirst: Boolean = false) {
    Text(
        text = date,
        modifier = Modifier
            .fillMaxWidth()
            // Se for o primeiro, padding 0. Caso contrário, 10.dp
            .padding(top = if (isFirst) 0.dp else 5.dp)
            .background(Color(0xFFd1e7dd))
            .padding(vertical = 10.dp),

        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
}
@Composable
fun TableRow(view: TableView, row: SongRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFd1e7dd))
            .padding(horizontal = 8.dp)
            .padding(top = 4.dp, bottom = 8.dp)

    ) {
        when (view) {
            TableView.LAST_SONGS -> {
                Text("${row.index}", Modifier.weight(0.9f))
                Text(row.name, Modifier
                    .weight(4.5f)
                    .padding(end = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(row.tone ?: "", Modifier.weight(1f))
                Text(row.artist ?: "", Modifier
                    .weight(2f)
                    .padding(end = 3.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            TableView.TOP_SONGS -> {
                Text("${row.index}", Modifier.weight(0.8f))
                Text(row.name, Modifier.weight(3f))
                Text("${row.count}", Modifier.weight(1f))
            }

            TableView.TOP_TONES -> {
                Text("${row.index}", Modifier.weight(0.8f))
                Text(row.tone ?: "", Modifier.weight(2f))
                Text("${row.count}", Modifier.weight(1f))
            }

            TableView.SUGGESTED_SONGS -> {
                Text("${row.index}", Modifier.weight(0.8f))
                Text(row.name, Modifier.weight(3f))
                Text(row.tone ?: "", Modifier.weight(1f))
                Text(row.artist ?: "", Modifier.weight(2f))
            }
        }
    }
}
