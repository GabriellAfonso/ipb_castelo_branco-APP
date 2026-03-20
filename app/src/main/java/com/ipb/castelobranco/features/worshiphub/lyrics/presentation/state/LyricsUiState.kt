package com.ipb.castelobranco.features.worshiphub.lyrics.presentation.state

data class LyricsUiState(
    val lyrics: List<LyricsListItem> = emptyList(),
    val filteredLyrics: List<LyricsListItem> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class LyricsListItem(
    val id: Int,
    val songName: String,
)
