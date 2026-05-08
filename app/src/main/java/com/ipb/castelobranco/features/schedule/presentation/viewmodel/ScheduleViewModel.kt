package com.ipb.castelobranco.features.schedule.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.domain.snapshot.HttpPermissionException
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.core.domain.snapshot.logTime
import com.ipb.castelobranco.features.schedule.domain.model.MonthSchedule
import com.ipb.castelobranco.features.schedule.domain.repository.ScheduleRepository
import com.ipb.castelobranco.features.schedule.presentation.components.ScheduleSectionUi
import com.ipb.castelobranco.features.schedule.presentation.mapper.toSectionsUi
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
    data class Error(val message: String, val httpCode: Int? = null) : ScheduleUiState
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
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // If today is a meeting day and the meeting hasn't started yet, show today's section
        val todayKeyword = when (today) {
            Calendar.TUESDAY  -> "Terça"
            Calendar.THURSDAY -> "Quinta"
            Calendar.SUNDAY   -> "Domingo"
            else              -> null
        }
        if (todayKeyword != null && currentHour < 22) {
            val todaySection = sections.firstOrNull { it.title.contains(todayKeyword, ignoreCase = true) }
            if (todaySection != null) return todaySection
        }

        val nextKeyword = when (today) {
            Calendar.MONDAY              -> "Terça"
            Calendar.TUESDAY             -> "Quinta"
            Calendar.WEDNESDAY           -> "Quinta"
            Calendar.THURSDAY            -> "Domingo"
            Calendar.FRIDAY,
            Calendar.SATURDAY            -> "Domingo"
            Calendar.SUNDAY              -> "Terça"
            else                         -> "Terça"
        }
        return sections.firstOrNull { it.title.contains(nextKeyword, ignoreCase = true) }
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

            is SnapshotState.Error -> {
                val code = (snapshot.throwable as? HttpPermissionException)?.code
                ScheduleUiState.Error(
                    message = snapshot.throwable.message ?: "Erro desconhecido",
                    httpCode = code,
                )
            }
        }
    }
}