package com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.state

import com.ipb.castelobranco.features.worshiphub.tables.domain.model.Song

data class ChordChartCreateUiState(
    val allSongs: List<Song> = emptyList(),
    val songQuery: String = "",
    val filteredSongs: List<Song> = emptyList(),
    val selectedSong: Song? = null,
    val content: String = "",
    val tone: String = "",
    val instrument: String = "",
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val savedSuccessfully: Boolean = false,
)
