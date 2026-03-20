package com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.viewmodel
    
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
    import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.usecase.GetChordChartsUseCase
    import com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.state.ChordChartListItem
    import com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.state.ChordChartsUiState
    import com.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
    import dagger.hilt.android.lifecycle.HiltViewModel
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.SharingStarted
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.combine
    import kotlinx.coroutines.flow.stateIn
    import javax.inject.Inject
    
    @HiltViewModel
    class ChordChartsViewModel @Inject constructor(
        private val getChordChartsUseCase: GetChordChartsUseCase,
        private val songsRepository: SongsRepository,
    ) : ViewModel() {
    
        private val _query = MutableStateFlow("")
    
        val uiState: StateFlow<ChordChartsUiState> = combine(
            getChordChartsUseCase.observe(),
            songsRepository.observeAllSongs(),
            _query,
        ) { chartsState, songsState, query ->
            val songMap = (songsState as? SnapshotState.Data)?.value
                .orEmpty()
                .associateBy { it.id }
    
            when (chartsState) {
                is SnapshotState.Loading -> ChordChartsUiState(isLoading = true)
                is SnapshotState.Error   -> ChordChartsUiState(error = chartsState.throwable.message)
                is SnapshotState.Data    -> {
                    val items = chartsState.value.map { chart ->
                        ChordChartListItem(
                            id         = chart.id,
                            songName   = songMap[chart.songId]?.title ?: "Song #${chart.songId}",
                            tone       = chart.tone,
                            instrument = chart.instrument,
                        )
                    }
                    val filtered = if (query.isBlank()) items
                    else items.filter { it.songName.contains(query, ignoreCase = true) }
    
                    ChordChartsUiState(
                        charts         = items,
                        filteredCharts = filtered,
                        query          = query,
                    )
                }
            }
        }.stateIn(
            scope          = viewModelScope,
            started        = SharingStarted.WhileSubscribed(5_000),
            initialValue   = ChordChartsUiState(isLoading = true),
        )
    
        fun onQueryChange(query: String) {
            _query.value = query
        }
    }
