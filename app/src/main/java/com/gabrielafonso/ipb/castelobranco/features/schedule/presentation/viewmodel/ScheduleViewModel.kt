package com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.logTime
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.MonthSchedule
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.repository.ScheduleRepository
import com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.components.ScheduleSectionUi
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.mapper.toSectionsUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
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
    val nextSection: StateFlow<ScheduleSectionUi?> = uiState
        .map { state ->
            if (state is ScheduleUiState.Success) {
                findNextSection(state.sections)
            } else null
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private fun findNextSection(sections: List<ScheduleSectionUi>): ScheduleSectionUi? {
        // Dias que existem: TUESDAY=3, THURSDAY=5, SUNDAY=1
        // Calendar.DAY_OF_WEEK: Dom=1, Seg=2, Ter=3, Qua=4, Qui=5, Sex=6, Sab=7
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

        // Para cada dia da semana, qual é o próximo culto?
        // Ex: Seg(2)->Ter(3), Ter(3)->Qui(5), Qua(4)->Qui(5), Qui(5)->Dom(1), Sex(6)->Dom(1), Sab(7)->Dom(1), Dom(1)->Ter(3)
        val nextMeetingKeyword = when (today) {
            Calendar.MONDAY -> "Terça"
            Calendar.TUESDAY -> "Quinta"  // Após a terça, próximo é quinta
            Calendar.WEDNESDAY -> "Quinta"
            Calendar.THURSDAY -> "Domingo" // Após quinta, próximo é domingo
            Calendar.FRIDAY, Calendar.SATURDAY -> "Domingo"
            Calendar.SUNDAY -> "Terça"   // Após domingo, próximo é terça
            else -> "Terça"
        }

        return sections.firstOrNull { section ->
            section.title.contains(nextMeetingKeyword, ignoreCase = true)
        }
    }

    init {
        logTime("ScheduleViewModel", "ViewModel criada e conectada ao fluxo reativo")
        viewModelScope.launch { refreshMonthSchedule() }
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

            else -> ScheduleUiState.Empty
        }
    }
}