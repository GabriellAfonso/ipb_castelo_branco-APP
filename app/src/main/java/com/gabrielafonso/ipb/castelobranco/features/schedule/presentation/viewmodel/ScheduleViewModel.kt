package com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.logTime
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.MonthSchedule
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.repository.ScheduleRepository
import com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.components.ScheduleSectionUi
import com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.views.toSectionsUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ScheduleUiState {
    object Loading : ScheduleUiState
    object Empty : ScheduleUiState
    data class Success(
        val sections: List<ScheduleSectionUi>,
        val data: MonthSchedule
    ) : ScheduleUiState
    // Dica: Adicione um 'data class Error(val message: String) : ScheduleUiState' aqui se quiser mostrar na tela
}

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val uiState: StateFlow<ScheduleUiState> = repository.observeMonthSchedule()
        .map { snapshot ->
            mapSnapshotToUiState(snapshot)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = mapSnapshotToUiState(repository.getCurrentSnapshot())
        )

    init {
        logTime("ScheduleViewModel", "ViewModel criada e conectada ao fluxo reativo")
    }

    fun refreshMonthSchedule(minDurationMs: Long = 600L) {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            val startTime = System.currentTimeMillis()

            try {
                repository.refreshMonthSchedule()
            } catch (e: Exception) {
                // Erro de rede tratado silenciosamente pois o Snapshot mantém o cache
            } finally {
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed < minDurationMs) {
                    delay(minDurationMs - elapsed)
                }
                _isRefreshing.value = false
            }
        }
    }

    private fun mapSnapshotToUiState(snapshot: SnapshotState<MonthSchedule>): ScheduleUiState {
        return when (snapshot) {
            is SnapshotState.Data -> {
                val sections = snapshot.value.toSectionsUi()
                if (sections.isNotEmpty()) {
                    ScheduleUiState.Success(
                        sections = sections,
                        data = snapshot.value
                    )
                } else {
                    ScheduleUiState.Empty
                }
            }
            is SnapshotState.Loading -> ScheduleUiState.Loading

            // AQUI ESTAVA O ERRO: O Kotlin exige que você trate o caso de erro ou qualquer outro
            else -> ScheduleUiState.Empty
        }
    }
}