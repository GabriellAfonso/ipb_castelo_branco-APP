package com.ipb.castelobranco.features.admin.schedule.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.features.admin.schedule.domain.model.Member
import com.ipb.castelobranco.features.admin.schedule.domain.model.ScheduleItem
import com.ipb.castelobranco.features.admin.schedule.domain.repository.AdminScheduleRepository
import com.ipb.castelobranco.features.admin.schedule.presentation.state.AdminScheduleEvent
import com.ipb.castelobranco.features.admin.schedule.presentation.state.AdminScheduleUiState
import com.ipb.castelobranco.features.admin.schedule.presentation.state.EditableScheduleUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminScheduleViewModel @Inject constructor(
    private val repository: AdminScheduleRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminScheduleUiState())
    val uiState: StateFlow<AdminScheduleUiState> = _uiState.asStateFlow()

    fun onEvent(event: AdminScheduleEvent) {
        when (event) {
            AdminScheduleEvent.LoadMembers -> loadMembers()
            is AdminScheduleEvent.MonthChanged -> changeMonth(event.year, event.month)
            is AdminScheduleEvent.MemberSelected -> selectMember(event.itemIndex, event.member)
            AdminScheduleEvent.GenerateSchedule -> generateSchedule()
            AdminScheduleEvent.SaveSchedule -> saveSchedule()
            AdminScheduleEvent.SnackbarShown -> _uiState.update { it.copy(snackbarMessage = null) }
        }
    }

    private fun loadMembers() {
        if (_uiState.value.members.isNotEmpty() || _uiState.value.isLoadingMembers) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMembers = true) }
            repository.getMembers()
                .onSuccess { members ->
                    _uiState.update { it.copy(members = members, isLoadingMembers = false) }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoadingMembers = false,
                            snackbarMessage = "Falha ao carregar membros."
                        )
                    }
                }
        }
    }

    private fun changeMonth(year: Int, month: Int) {
        _uiState.update { it.copy(year = year, month = month, items = emptyList()) }
    }

    private fun selectMember(index: Int, member: Member) {
        _uiState.update { state ->
            state.copy(items = state.items.mapIndexed { i, item ->
                if (i == index) item.copy(selectedMember = member) else item
            })
        }
    }

    private fun generateSchedule() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            repository.generateSchedule(year = state.year, month = state.month)
                .onSuccess { items ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            items = items.map { item ->
                                EditableScheduleUiState(
                                    date = item.date,
                                    day = item.day,
                                    scheduleTypeName = item.scheduleTypeName,
                                    scheduleTypeId = item.scheduleTypeId,
                                    selectedMember = item.selectedMember
                                )
                            }
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            snackbarMessage = "Falha ao gerar escala."
                        )
                    }
                }
        }
    }

    private fun saveSchedule() {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val domainItems = state.items.map { item ->
                ScheduleItem(
                    date = item.date,
                    day = item.day,
                    scheduleTypeName = item.scheduleTypeName,
                    scheduleTypeId = item.scheduleTypeId,
                    selectedMember = item.selectedMember
                )
            }
            repository.saveSchedule(year = state.year, month = state.month, items = domainItems)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            snackbarMessage = "Escala salva com sucesso."
                        )
                    }
                }
                .onFailure { error ->
                    val message = error.message ?: "Falha ao salvar escala."
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            snackbarMessage = message
                        )
                    }
                }
        }
    }
}