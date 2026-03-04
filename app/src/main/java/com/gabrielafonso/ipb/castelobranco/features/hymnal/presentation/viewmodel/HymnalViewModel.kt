package com.gabrielafonso.ipb.castelobranco.features.hymnal.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.usecase.ObserveHymnsUseCase
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.usecase.SearchHymnsUseCase
import com.gabrielafonso.ipb.castelobranco.features.hymnal.presentation.screens.HymnalUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HymnalViewModel @Inject constructor(
    private val observeHymnsUseCase: ObserveHymnsUseCase,
    private val searchHymnsUseCase: SearchHymnsUseCase,
) : ViewModel() {

    companion object {
        private const val TAG = "HymnalViewModel"
    }

    private val _uiState = MutableStateFlow(HymnalUiState())
    val uiState: StateFlow<HymnalUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeHymnsUseCase().collect { state ->
                when (state) {
                    is SnapshotState.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                error = null
                            )
                        }
                    }

                    is SnapshotState.Data -> {
                        _uiState.update { current ->
                            current.copy(
                                hymns = state.value,
                                filteredHymns = searchHymnsUseCase(state.value, current.query),
                                isLoading = false,
                                error = null
                            )
                        }
                    }

                    is SnapshotState.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = state.throwable.message
                                    ?: "Erro ao carregar hinário"
                            )
                        }
                    }
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { current ->
            current.copy(
                query = query,
                filteredHymns = searchHymnsUseCase(current.hymns, query)
            )
        }
    }

    fun teste() {
        viewModelScope.launch {
            Log.d(TAG, "só pra testar action")
        }
    }
}
