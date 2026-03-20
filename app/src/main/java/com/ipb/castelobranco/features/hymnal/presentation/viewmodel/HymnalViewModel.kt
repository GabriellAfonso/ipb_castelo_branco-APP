package com.ipb.castelobranco.features.hymnal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.data.local.ThemePreferences
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.hymnal.domain.usecase.ObserveHymnsUseCase
import com.ipb.castelobranco.features.hymnal.domain.usecase.SearchHymnsUseCase
import com.ipb.castelobranco.features.hymnal.presentation.screens.HymnalUiState
import com.ipb.castelobranco.features.settings.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HymnalViewModel @Inject constructor(
    private val observeHymnsUseCase: ObserveHymnsUseCase,
    private val searchHymnsUseCase: SearchHymnsUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val hymnalFontSize: StateFlow<Float> = settingsRepository.hymnalFontSizeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemePreferences.DEFAULT_HYMNAL_FONT_SIZE
        )

    fun setHymnalFontSize(size: Float) {
        viewModelScope.launch { settingsRepository.setHymnalFontSize(size) }
    }

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
