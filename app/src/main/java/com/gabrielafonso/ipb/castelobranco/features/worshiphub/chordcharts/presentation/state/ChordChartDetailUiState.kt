package com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.state

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.parser.ChordPage

data class ChordChartDetailUiState(
    val songName: String = "",
    val tone: String = "",
    val pages: List<ChordPage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
