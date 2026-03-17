package com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.state

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.parser.LyricsStanza

data class LyricsDetailUiState(
    val songName: String = "",
    val stanzas: List<LyricsStanza> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
