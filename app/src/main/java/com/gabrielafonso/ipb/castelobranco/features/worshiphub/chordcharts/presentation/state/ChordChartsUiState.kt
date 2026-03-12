package com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.state

data class ChordChartsUiState(
    val charts: List<ChordChartListItem> = emptyList(),
    val filteredCharts: List<ChordChartListItem> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class ChordChartListItem(
    val id: Int,
    val songName: String,
    val tone: String,
    val instrument: String,
)
