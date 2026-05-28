package com.ipb.castelobranco.features.worshiphub.songs.presentation.state

data class SongDetailUiState(
    val songName: String = "",
    val artist: String = "",
    val playCount: Int = 0,
    val tones: List<String> = emptyList(),
    val lastSundays: List<String> = emptyList(),
    val chordCharts: List<ChordChartOption> = emptyList(),
    val hasLyrics: Boolean = false,
    val lyricsId: Int? = null,
    val youtubeLink: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class ChordChartOption(
    val id: Int,
    val tone: String,
    val instrument: String,
)
