package com.ipb.castelobranco.features.worshiphub.songs.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.songs.domain.usecase.GetSongDetailUseCase
import com.ipb.castelobranco.features.worshiphub.songs.presentation.state.ChordChartOption
import com.ipb.castelobranco.features.worshiphub.songs.presentation.state.SongDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSongDetailUseCase: GetSongDetailUseCase,
) : ViewModel() {

    private val songId: Int = savedStateHandle["songId"]!!

    val uiState: StateFlow<SongDetailUiState> = getSongDetailUseCase.observe(songId)
        .map { state ->
            when (state) {
                is SnapshotState.Loading -> SongDetailUiState(isLoading = true)
                is SnapshotState.Error   -> SongDetailUiState(error = state.throwable.message)
                is SnapshotState.Data    -> {
                    val detail = state.value
                    SongDetailUiState(
                        songName    = detail.song.title,
                        artist      = detail.song.artist,
                        playCount   = detail.playCount,
                        tones       = detail.tones,
                        lastSundays = detail.lastSundays,
                        chordCharts = detail.chordCharts.map { chart ->
                            ChordChartOption(
                                id         = chart.id,
                                tone       = chart.tone,
                                instrument = chart.instrument,
                            )
                        },
                        hasLyrics   = detail.lyrics != null,
                        lyricsId    = detail.lyrics?.id,
                        youtubeLink = detail.song.youtubeLink,
                    )
                }
            }
        }.stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.Eagerly,
            initialValue = SongDetailUiState(isLoading = true),
        )
}
