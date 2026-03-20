package com.ipb.castelobranco.features.admin.register.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.admin.register.domain.usecase.ObserveSongsUseCase
import com.ipb.castelobranco.features.admin.register.domain.usecase.SubmitSundayPlaysUseCase
import com.ipb.castelobranco.features.admin.register.domain.validation.MusicRegistrationValidator
import com.ipb.castelobranco.features.admin.register.presentation.state.MusicRegistrationEvent
import com.ipb.castelobranco.features.admin.register.presentation.state.MusicRegistrationUiState
import com.ipb.castelobranco.features.admin.register.presentation.state.RegistrationType
import com.ipb.castelobranco.features.admin.register.presentation.util.SundayRowManager
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MusicRegistrationViewModel @Inject constructor(
    private val observeSongsUseCase: ObserveSongsUseCase,
    private val submitSundayPlaysUseCase: SubmitSundayPlaysUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "MusicRegistrationViewModel"
    }

    private val _uiState = MutableStateFlow(MusicRegistrationUiState())
    val uiState: StateFlow<MusicRegistrationUiState> = _uiState.asStateFlow()

    fun onEvent(event: MusicRegistrationEvent) {
        when (event) {
            MusicRegistrationEvent.Init -> init()
            is MusicRegistrationEvent.RegistrationTypeChanged -> changeType(event.type)

            MusicRegistrationEvent.OpenDatePicker -> openDatePicker()
            MusicRegistrationEvent.DismissDatePicker -> dismissDatePicker()
            is MusicRegistrationEvent.DatePicked -> confirmDate(event.date)

            is MusicRegistrationEvent.SundaySongQueryChanged -> updateSongQuery(event.position, event.query)
            is MusicRegistrationEvent.SundaySongSelected -> selectSong(event.position, event.song)
            is MusicRegistrationEvent.SundayToneChanged -> updateTone(event.position, event.tone)

            MusicRegistrationEvent.AddSundayRow -> addRow()
            is MusicRegistrationEvent.RemoveSundayRow -> removeRow(event.position)

            MusicRegistrationEvent.Submit -> submit()
            MusicRegistrationEvent.SnackbarShown -> consumeSnackbar()
            is MusicRegistrationEvent.MusicTitleChanged -> _uiState.update {
                it.copy(musicForm = it.musicForm.copy(title = event.title))
            }
            is MusicRegistrationEvent.MusicArtistChanged -> _uiState.update {
                it.copy(musicForm = it.musicForm.copy(artist = event.artist))
            }
        }
    }

    private fun init() {
        val alreadyObserving =
            _uiState.value.isLoadingSongs || _uiState.value.availableSongs.isNotEmpty()
        if (alreadyObserving) return

        viewModelScope.launch {
            observeSongsUseCase.observe().collect { snapshot ->
                when (snapshot) {
                    is SnapshotState.Loading -> {
                        _uiState.update { it.copy(isLoadingSongs = true) }
                    }
                    is SnapshotState.Data -> {
                        _uiState.update { state ->
                            state.copy(
                                availableSongs = snapshot.value,
                                isLoadingSongs = false
                            ).recomputeSundayErrors()
                        }
                    }
                    is SnapshotState.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingSongs = false,
                                snackbarMessage = "Falha ao carregar músicas."
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSongs = true) }
            try {
                observeSongsUseCase.refresh()
            } catch (e: Throwable) {
                Log.w(TAG, "Failed to refresh songs", e)
                _uiState.update { it.copy(snackbarMessage = "Falha ao atualizar músicas.") }
            } finally {
                _uiState.update { it.copy(isLoadingSongs = false) }
            }
        }
    }

    private fun changeType(type: RegistrationType) {
        _uiState.update { state ->
            val updated = if (type == RegistrationType.MUSIC) {
                state.copy(
                    registrationType = type,
                    selectedDate = null,
                    showDatePicker = false,
                    snackbarMessage = null,
                    sundayRowErrors = emptyMap()
                )
            } else {
                state.copy(registrationType = type, snackbarMessage = null)
            }
            updated.recomputeSundayErrors()
        }
    }

    private fun openDatePicker() {
        _uiState.update { state ->
            if (state.registrationType == RegistrationType.SUNDAY) state.copy(showDatePicker = true) else state
        }
    }

    private fun dismissDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    private fun confirmDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, showDatePicker = false) }
    }

    private fun updateSongQuery(position: Int, query: String) {
        _uiState.update { state ->
            state.copy(sundayRows = SundayRowManager.updateQuery(state.sundayRows, position, query))
                .recomputeSundayErrors()
        }
    }

    private fun selectSong(position: Int, song: Song) {
        _uiState.update { state ->
            state.copy(sundayRows = SundayRowManager.selectSong(state.sundayRows, position, song))
                .recomputeSundayErrors()
        }
    }

    private fun updateTone(position: Int, tone: String) {
        _uiState.update { state ->
            state.copy(sundayRows = SundayRowManager.updateTone(state.sundayRows, position, tone))
                .recomputeSundayErrors()
        }
    }

    private fun addRow() {
        _uiState.update { state ->
            state.copy(sundayRows = SundayRowManager.addRow(state.sundayRows))
                .recomputeSundayErrors()
        }
    }

    private fun removeRow(position: Int) {
        _uiState.update { state ->
            state.copy(sundayRows = SundayRowManager.removeRow(state.sundayRows, position))
                .recomputeSundayErrors()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        if (state.registrationType != RegistrationType.SUNDAY) {
            _uiState.update { it.copy(snackbarMessage = "Registro de música ainda não implementado.") }
            return
        }

        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = submitSundayPlaysUseCase(
                rows = state.sundayRows,
                availableSongs = state.availableSongs,
                selectedDate = state.selectedDate
            )
            _uiState.update { current ->
                when (result) {
                    is SubmitSundayPlaysUseCase.Result.ValidationError -> current.copy(
                        sundayRowErrors = result.errorsByPosition,
                        snackbarMessage = "Existem posições incompletas. Corrija antes de enviar.",
                        isSubmitting = false
                    )
                    SubmitSundayPlaysUseCase.Result.Success -> current.copy(
                        snackbarMessage = "Enviado com sucesso.",
                        isSubmitting = false
                    )
                    is SubmitSundayPlaysUseCase.Result.Failure -> current.copy(
                        snackbarMessage = result.message,
                        isSubmitting = false
                    )
                }
            }
        }
    }

    private fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun MusicRegistrationUiState.recomputeSundayErrors(): MusicRegistrationUiState {
        if (registrationType != RegistrationType.SUNDAY) return copy(sundayRowErrors = emptyMap())
        val validation = MusicRegistrationValidator.validateSundayRows(sundayRows, availableSongs)
        return copy(sundayRowErrors = validation.errorsByPosition)
    }
}
