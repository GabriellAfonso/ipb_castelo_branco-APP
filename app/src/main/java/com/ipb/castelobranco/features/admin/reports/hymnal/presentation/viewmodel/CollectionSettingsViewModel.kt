package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.domain.error.AppError
import com.ipb.castelobranco.core.domain.error.toAppError
import com.ipb.castelobranco.core.presentation.error.toUserMessage
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.CollectionSettings
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.SettingField
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase.GetCollectionSettingsUseCase
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase.UpdateCollectionSettingsUseCase
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase.ValidateCollectionSettingsUseCase
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.state.CollectionSettingsEvent
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.state.CollectionSettingsUiState
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
 * The six collection parameters.
 *
 * The ranges are checked locally before anything is sent; whatever the service still refuses
 * arrives as `field_errors` and is routed back by [SettingField.wireName], so an out-of-range
 * value is reported on its own field either way — never as a generic alert.
 */
@HiltViewModel
class CollectionSettingsViewModel @Inject constructor(
    private val getSettings: GetCollectionSettingsUseCase,
    private val updateSettings: UpdateCollectionSettingsUseCase,
    private val validate: ValidateCollectionSettingsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionSettingsUiState())
    val uiState: StateFlow<CollectionSettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CollectionSettingsEvent>()
    val events: SharedFlow<CollectionSettingsEvent> = _events.asSharedFlow()

    /** What the server currently holds — the baseline the partial patch is computed against. */
    private var loaded: CollectionSettings? = null

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getSettings()
                .onSuccess { settings ->
                    loaded = settings
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            values = settings.asFormValues(),
                            fieldErrors = emptyMap(),
                            isDirty = false,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.toAppError().toUserMessage())
                    }
                }
        }
    }

    fun onValueChanged(field: SettingField, value: String) {
        _uiState.update { state ->
            state.copy(
                values = state.values + (field to value),
                fieldErrors = state.fieldErrors - field,
                isDirty = true,
            )
        }
    }

    fun onSave() {
        val current = loaded ?: return
        val validation = validate(_uiState.value.values)

        val values = validation.values
        if (values == null) {
            _uiState.update { it.copy(fieldErrors = validation.errors) }
            return
        }

        _uiState.update { it.copy(isSaving = true, fieldErrors = emptyMap()) }

        viewModelScope.launch {
            updateSettings(current, values)
                .onSuccess { saved ->
                    loaded = saved
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            values = saved.asFormValues(),
                            isDirty = false,
                        )
                    }
                    _events.emit(CollectionSettingsEvent.ShowMessage(SAVED))
                }
                .onFailure { throwable -> handleFailure(throwable) }
        }
    }

    /**
     * A key the app does not know cannot be shown on a field, so it falls back to the generic
     * message rather than vanishing.
     */
    private suspend fun handleFailure(throwable: Throwable) {
        val error = throwable.toAppError()
        val wireErrors = (error as? AppError.Server)?.fieldErrors.orEmpty()

        val mapped = wireErrors.mapNotNull { (wireName, messages) ->
            val field = SettingField.byWireName(wireName) ?: return@mapNotNull null
            val message = messages.firstOrNull() ?: return@mapNotNull null
            field to message
        }.toMap()

        _uiState.update { it.copy(isSaving = false, fieldErrors = mapped) }

        if (mapped.isEmpty()) {
            _events.emit(CollectionSettingsEvent.ShowMessage(error.toUserMessage()))
        }
    }

    private fun CollectionSettings.asFormValues(): Map<SettingField, String> =
        SettingField.entries.associateWith { valueOf(it).toString() }

    private companion object {
        const val SAVED = "Parâmetros salvos."
    }
}
