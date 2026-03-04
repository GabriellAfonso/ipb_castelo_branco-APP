package com.gabrielafonso.ipb.castelobranco.features.hymnal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.usecase.ObserveHymnsUseCase
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.usecase.SearchHymnsUseCase
import com.gabrielafonso.ipb.castelobranco.features.hymnal.presentation.screens.HymnalUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HymnalViewModel @Inject constructor(
    private val observeHymnsUseCase: ObserveHymnsUseCase,
    private val searchHymnsUseCase: SearchHymnsUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val uiState: StateFlow<HymnalUiState> = combine(
        observeHymnsUseCase(),
        _query
    ) { state, query ->
        when (state) {
            is SnapshotState.Loading -> HymnalUiState(isLoading = true, error = null)
            is SnapshotState.Data -> HymnalUiState(
                hymns = state.value,
                filteredHymns = searchHymnsUseCase(state.value, query),
                query = query,
                isLoading = false,
                error = null
            )
            is SnapshotState.Error -> HymnalUiState(
                isLoading = false,
                error = state.throwable.message ?: "Erro ao carregar hinário"
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HymnalUiState()
    )

    fun onQueryChange(query: String) {
        _query.value = query
    }

}
