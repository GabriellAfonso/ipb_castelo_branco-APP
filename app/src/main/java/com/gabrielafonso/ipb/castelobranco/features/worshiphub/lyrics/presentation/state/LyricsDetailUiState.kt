package com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.state

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.parser.LyricsPage

data class LyricsDetailUiState(
    val songName: String = "",
    val pages: List<LyricsPage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
