package com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.data.local.SetlistPreferences
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.usecase.GetChordChartsUseCase
import com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.state.ChordChartListItem
import com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.state.ChordChartsUiState
import com.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChordChartsViewModel @Inject constructor(
    private val getChordChartsUseCase: GetChordChartsUseCase,
    private val songsRepository: SongsRepository,
    private val setlistPreferences: SetlistPreferences,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh(minDurationMs: Long = 600L) {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            val start = System.currentTimeMillis()
            runCatching { getChordChartsUseCase.refresh() }
            val elapsed = System.currentTimeMillis() - start
            if (elapsed < minDurationMs) delay(minDurationMs - elapsed)
            _isRefreshing.value = false
        }
    }

    val uiState: StateFlow<ChordChartsUiState> = combine(
        getChordChartsUseCase.observe(),
        songsRepository.observeAllSongs(),
        setlistPreferences.pinnedChordChartIds,
        _query,
    ) { chartsState, songsState, pinnedIds, query ->
        val songMap = (songsState as? SnapshotState.Data)?.value
            .orEmpty()
            .associateBy { it.id }

        when (chartsState) {
            is SnapshotState.Loading -> ChordChartsUiState(isLoading = true)
            is SnapshotState.Error   -> ChordChartsUiState(error = chartsState.throwable.message)
            is SnapshotState.Data    -> {
                val pinOrder = pinnedIds.withIndex().associate { (index, id) -> id to index }
                val sorted = chartsState.value.map { chart ->
                    ChordChartListItem(
                        id         = chart.id,
                        songName   = songMap[chart.songId]?.title ?: "Song #${chart.songId}",
                        tone       = chart.tone,
                        instrument = chart.instrument,
                        isPinned   = chart.id in pinOrder,
                    )
                }.sortedBy { pinOrder[it.id] ?: Int.MAX_VALUE }

                val filtered = if (query.isBlank()) sorted
                else sorted.filter { it.songName.contains(query, ignoreCase = true) }

                ChordChartsUiState(
                    charts         = sorted,
                    filteredCharts = filtered,
                    query          = query,
                )
            }
        }
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChordChartsUiState(isLoading = true),
    )

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onTogglePin(id: Int) {
        viewModelScope.launch { setlistPreferences.toggleChordChart(id) }
    }
}
