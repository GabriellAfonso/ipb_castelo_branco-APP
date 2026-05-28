package com.ipb.castelobranco.features.worshiphub.songs.presentation.state

data class SongsListUiState(
    val songs: List<SongListItem> = emptyList(),
    val filteredSongs: List<SongListItem> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class SongListItem(
    val id: Int,
    val title: String,
    val artist: String,
)
