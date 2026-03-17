package com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.state

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.parser.ChordBlock

data class ChordChartDetailUiState(
    val songName: String = "",
    val tone: String = "",
    val blocks: List<ChordBlock> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
