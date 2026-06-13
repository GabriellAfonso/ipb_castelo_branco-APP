package com.ipb.castelobranco.features.worshiphub.lyrics.presentation.state

import com.ipb.castelobranco.features.worshiphub.tables.domain.model.Song

data class LyricsCreateUiState(
    val allSongs: List<Song> = emptyList(),
    val songQuery: String = "",
    val filteredSongs: List<Song> = emptyList(),
    val selectedSong: Song? = null,
    val content: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val savedSuccessfully: Boolean = false,
)
