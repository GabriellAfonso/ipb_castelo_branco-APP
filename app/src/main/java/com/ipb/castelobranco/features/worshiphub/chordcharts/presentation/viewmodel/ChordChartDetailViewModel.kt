package com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.usecase.GetChordChartsUseCase
import com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.parser.ChordProParser
import com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.state.ChordChartDetailUiState
import com.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChordChartDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChordChartsUseCase: GetChordChartsUseCase,
    private val songsRepository: SongsRepository,
) : ViewModel() {

    private val chordChartId: Int = checkNotNull(savedStateHandle["chordChartId"])

    val uiState: StateFlow<ChordChartDetailUiState> = combine(
        getChordChartsUseCase.observe(),
        songsRepository.observeAllSongs(),
    ) { chartsState, songsState ->
        val songMap = (songsState as? SnapshotState.Data)?.value
            .orEmpty()
            .associateBy { it.id }

        when (chartsState) {
            is SnapshotState.Loading -> ChordChartDetailUiState(isLoading = true)
            is SnapshotState.Error   -> ChordChartDetailUiState(error = chartsState.throwable.message)
            is SnapshotState.Data    -> {
                val chart = chartsState.value.find { it.id == chordChartId }
                    ?: return@combine ChordChartDetailUiState(error = "Chart not found")

                ChordChartDetailUiState(
                    songName = songMap[chart.songId]?.title ?: "Song #${chart.songId}",
                    tone     = chart.tone,
                    blocks   = ChordProParser.parse(chart.content),
                )
            }
        }
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChordChartDetailUiState(isLoading = true),
    )
}
