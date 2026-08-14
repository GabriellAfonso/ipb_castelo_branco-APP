package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.domain.error.AppError
import com.ipb.castelobranco.core.domain.error.toAppError
import com.ipb.castelobranco.core.presentation.error.toUserMessage
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindow
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindowDraft
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase.DeleteServiceWindowUseCase
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase.GetServiceWindowsUseCase
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase.SaveServiceWindowUseCase
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase.ValidateServiceWindowUseCase
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.state.ServiceWindowsEvent
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.state.ServiceWindowsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The weekly services every service-based slice, bulletin and recurrence statement depends on.
 *
 * Validation runs locally first, so an impossible window never becomes a request; anything the
 * service still refuses comes back as `field_errors` and lands on the same field.
 */
@HiltViewModel
class ServiceWindowsViewModel @Inject constructor(
    private val getServiceWindows: GetServiceWindowsUseCase,
    private val saveServiceWindow: SaveServiceWindowUseCase,
    private val deleteServiceWindow: DeleteServiceWindowUseCase,
    private val validate: ValidateServiceWindowUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceWindowsUiState())
    val uiState: StateFlow<ServiceWindowsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ServiceWindowsEvent>()
    val events: SharedFlow<ServiceWindowsEvent> = _events.asSharedFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getServiceWindows()
                .onSuccess { windows ->
                    _uiState.update { it.copy(isLoading = false, windows = windows) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.toAppError().toUserMessage())
                    }
                }
        }
    }

    // region editing

    fun onCreateRequested() {
        _uiState.update { it.copy(editing = ServiceWindowDraft(), fieldErrors = emptyMap()) }
    }

    fun onEditRequested(window: ServiceWindow) {
        _uiState.update {
            it.copy(
                editing = ServiceWindowDraft(
                    id = window.id,
                    name = window.name,
                    weekday = window.weekday,
                    startTime = window.startTime,
                    endTime = window.endTime,
                    active = window.active,
                ),
                fieldErrors = emptyMap(),
            )
        }
    }

    fun onDraftChanged(draft: ServiceWindowDraft) {
        _uiState.update { it.copy(editing = draft) }
    }

    fun onEditDismissed() {
        _uiState.update { it.copy(editing = null, fieldErrors = emptyMap()) }
    }

    fun onSave() {
        val draft = _uiState.value.editing ?: return

        val localErrors = validate(draft)
        if (localErrors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = localErrors) }
            return
        }

        persist(draft) { _uiState.update { it.copy(editing = null, fieldErrors = emptyMap()) } }
    }

    /** Deactivating is a patch of `active` alone, and never touches history. */
    fun onActiveToggled(window: ServiceWindow) {
        persist(
            ServiceWindowDraft(
                id = window.id,
                name = window.name,
                weekday = window.weekday,
                startTime = window.startTime,
                endTime = window.endTime,
                active = !window.active,
            )
        )
    }

    private fun persist(draft: ServiceWindowDraft, onSaved: () -> Unit = {}) {
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            saveServiceWindow(draft)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    onSaved()
                    _events.emit(ServiceWindowsEvent.WindowsChanged)
                    load()
                }
                .onFailure { throwable -> handleFailure(throwable) }
        }
    }

    // endregion

    // region deletion

    fun onDeleteRequested(window: ServiceWindow) {
        _uiState.update { it.copy(pendingDelete = window) }
    }

    fun onDeleteDismissed() {
        _uiState.update { it.copy(pendingDelete = null) }
    }

    fun onDeleteConfirmed() {
        val window = _uiState.value.pendingDelete ?: return
        _uiState.update { it.copy(pendingDelete = null, isSaving = true) }

        viewModelScope.launch {
            deleteServiceWindow(window.id)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(
                        ServiceWindowsEvent.ShowMessage(
                            "Culto apagado. Nenhum registro do histórico foi removido."
                        )
                    )
                    _events.emit(ServiceWindowsEvent.WindowsChanged)
                    load()
                }
                .onFailure { throwable -> handleFailure(throwable) }
        }
    }

    // endregion

    /**
     * A field-level refusal belongs on its field. Only what has no field goes to the snackbar.
     */
    private suspend fun handleFailure(throwable: Throwable) {
        val error = throwable.toAppError()
        val serverFieldErrors = (error as? AppError.Server)
            ?.fieldErrors
            ?.mapNotNull { (field, messages) -> messages.firstOrNull()?.let { field to it } }
            ?.toMap()
            .orEmpty()

        _uiState.update { it.copy(isSaving = false, fieldErrors = serverFieldErrors) }

        if (serverFieldErrors.isEmpty()) {
            _events.emit(ServiceWindowsEvent.ShowMessage(error.toUserMessage()))
        }
    }
}
