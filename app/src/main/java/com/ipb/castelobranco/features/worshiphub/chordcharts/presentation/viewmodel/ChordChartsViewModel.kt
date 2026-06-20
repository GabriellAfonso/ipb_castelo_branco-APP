package com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.data.local.SetlistPreferences
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.core.domain.util.normalize
import com.ipb.castelobranco.features.profile.data.snapshot.ProfileSnapshotRepository
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
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChordChartsViewModel @Inject constructor(
    private val getChordChartsUseCase: GetChordChartsUseCase,
    private val songsRepository: SongsRepository,
    private val setlistPreferences: SetlistPreferences,
    private val profileSnapshot: ProfileSnapshotRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val pinnedSongs = setlistPreferences.pinnedSongIds
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val profileState = profileSnapshot.observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, profileSnapshot.observe().value)

    private val queryAndAdmin = combine(_query, profileState) { q, p -> q to p }

    fun refresh(minDurationMs: Long = 600L) {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            val start = System.currentTimeMillis()
            runCatching { getChordChartsUseCase.refresh() }
                .onFailure { Timber.w(it, "Failed to refresh chord charts") }
            val elapsed = System.currentTimeMillis() - start
            if (elapsed < minDurationMs) delay(minDurationMs - elapsed)
            _isRefreshing.value = false
        }
    }

    val uiState: StateFlow<ChordChartsUiState> = combine(
        getChordChartsUseCase.observe(),
        songsRepository.observeAllSongs(),
        pinnedSongs,
        queryAndAdmin,
    ) { chartsState, songsState, pinnedSongIds, (query, profileState) ->
        val songMap = (songsState as? SnapshotState.Data)?.value
            .orEmpty()
            .associateBy { it.id }

        val isAdmin = (profileState as? SnapshotState.Data)?.value?.isAdmin == true

        when (chartsState) {
            is SnapshotState.Loading -> ChordChartsUiState(isLoading = true, isAdmin = isAdmin)
            is SnapshotState.Error   -> ChordChartsUiState(error = chartsState.throwable.message, isAdmin = isAdmin)
            is SnapshotState.Data    -> {
                val pinOrder = pinnedSongIds.withIndex().associate { (index, songId) -> songId to index }
                val sorted = chartsState.value.map { chart ->
                    ChordChartListItem(
                        id         = chart.id,
                        songId     = chart.songId,
                        songName   = songMap[chart.songId]?.title ?: "Song #${chart.songId}",
                        tone       = chart.tone,
                        instrument = chart.instrument,
                        isPinned   = chart.songId in pinOrder,
                    )
                }.sortedBy { pinOrder[it.songId] ?: Int.MAX_VALUE }

                val filtered = if (query.isBlank()) sorted
                else sorted.filter { it.songName.normalize().contains(query.normalize(), ignoreCase = true) }

                ChordChartsUiState(
                    charts         = sorted,
                    filteredCharts = filtered,
                    query          = query,
                    isAdmin        = isAdmin,
                )
            }
        }
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.Eagerly,
        initialValue = ChordChartsUiState(isLoading = true),
    )

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onTogglePin(songId: Int) {
        viewModelScope.launch { setlistPreferences.toggleSong(songId) }
    }
}
