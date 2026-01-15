package com.gabrielafonso.ipb.castelobranco.ui.components

import com.gabrielafonso.ipb.castelobranco.ui.tables.TableHeader
import com.gabrielafonso.ipb.castelobranco.ui.tables.TableRow
import com.gabrielafonso.ipb.castelobranco.ui.tables.TablesTabs

import androidx.compose.foundation.lazy.LazyListScope
import com.gabrielafonso.ipb.castelobranco.data.model.SongRow
import com.gabrielafonso.ipb.castelobranco.data.model.TableView
import com.gabrielafonso.ipb.castelobranco.ui.tables.DateHeader
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gabrielafonso.ipb.castelobranco.data.model.stableKey
fun LazyListScope.PraiseTables(
    currentView: TableView,
    onViewChange: (TableView) -> Unit,
    data: List<SongRow>,
    lastSongsItems: List<LastSongItem>
) {
    item {
        TablesTabs(
            selected = currentView,
            onSelect = onViewChange
        )
    }

    item {
        TableHeader(currentView)
    }

    if (currentView == TableView.LAST_SONGS) {

        itemsIndexed(
            items = lastSongsItems,
            key = { index, item ->
                when (item) {
                    is LastSongItem.DateHeader ->
                        "header_${item.date}"

                    is LastSongItem.Song ->
                        "song_${item.row.stableKey()}_$index"
                }
            },
            contentType = { _, item ->
                when (item) {
                    is LastSongItem.DateHeader -> "date_header"
                    is LastSongItem.Song -> "song_row"
                }
            }
        ) { index, item ->
            when (item) {
                is LastSongItem.DateHeader -> {
                    DateHeader(
                        date = item.date,
                        isFirst = index == 0
                    )
                }

                is LastSongItem.Song -> {
                    TableRow(
                        view = currentView,
                        row = item.row.copy(
                            index = item.dayIndex
                        )
                    )
                }
            }
        }

    } else {

        items(
            items = data,
            key = { row ->
                row.stableKey()
            },
            contentType = { "song_row" }
        ) { row ->
            TableRow(
                view = currentView,
                row = row
            )
        }
    }
}

sealed class LastSongItem {

    data class DateHeader(
        val date: String
    ) : LastSongItem()

    data class Song(
        val row: SongRow,
        val dayIndex: Int
    ) : LastSongItem()
}

