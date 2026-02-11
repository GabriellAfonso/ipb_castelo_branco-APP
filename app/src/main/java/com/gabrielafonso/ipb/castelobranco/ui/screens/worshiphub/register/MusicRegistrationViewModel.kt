package com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.domain.repository.SongsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MusicRegistrationViewModel @Inject constructor(
    private val repository: SongsRepository
) : ViewModel() {

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
        }
    }

    private fun init() {
        val alreadyObserving = _uiState.value.isLoadingSongs || _uiState.value.availableSongs.isNotEmpty()
        if (alreadyObserving) return

        viewModelScope.launch {
            repository.observeAllSongs().collect { songs ->
                _uiState.update { state ->
                    val updated = state.copy(availableSongs = songs)
                    updated.recomputeSundayErrors()
                }
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSongs = true) }
            try {
                repository.refreshAllSongs()
            } catch (_: Throwable) {
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

    private fun confirmDate(date: java.time.LocalDate) {
        _uiState.update { it.copy(selectedDate = date, showDatePicker = false) }
    }

    private fun updateSongQuery(position: Int, query: String) {
        _uiState.update { state ->
            state.copy(
                sundayRows = state.sundayRows.map { row ->
                    if (row.position == position) row.copy(songQuery = query, selectedSongId = null) else row
                }
            ).recomputeSundayErrors()
        }
    }

    private fun selectSong(position: Int, song: com.gabrielafonso.ipb.castelobranco.domain.model.Song) {
        val label = SongLabelFormatter.format(song)
        _uiState.update { state ->
            state.copy(
                sundayRows = state.sundayRows.map { row ->
                    if (row.position == position) row.copy(songQuery = label, selectedSongId = song.id) else row
                }
            ).recomputeSundayErrors()
        }
    }

    private fun updateTone(position: Int, tone: String) {
        _uiState.update { state ->
            state.copy(
                sundayRows = state.sundayRows.map { row ->
                    if (row.position == position) row.copy(tone = tone) else row
                }
            ).recomputeSundayErrors()
        }
    }

    private fun addRow() {
        _uiState.update { state ->
            val nextPos = (state.sundayRows.maxOfOrNull { it.position } ?: 0) + 1
            state.copy(sundayRows = state.sundayRows + SundaySongRowState(position = nextPos))
                .recomputeSundayErrors()
        }
    }

    private fun removeRow(position: Int) {
        _uiState.update { state ->
            if (position <= 4) state
            else state.copy(sundayRows = state.sundayRows.filterNot { it.position == position }).recomputeSundayErrors()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        if (state.registrationType != RegistrationType.SUNDAY) {
            _uiState.update { it.copy(snackbarMessage = "Registro de música ainda não implementado.") }
            return
        }

        // Revalida antes de enviar
        val validation = MusicRegistrationValidator.validateSundayRows(state.sundayRows, state.availableSongs)
        if (validation.errorsByPosition.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    sundayRowErrors = validation.errorsByPosition,
                    snackbarMessage = "Existem posições incompletas. Corrija antes de enviar."
                )
            }
            return
        }

        if (!state.canSubmit) return

        val dateIso = SundayPlaysMapper.dateIso(state.selectedDate)
        val plays = SundayPlaysMapper.toSundayPlayItems(state.sundayRows, state.availableSongs)

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            try {
                repository.pushSundayPlays(date = dateIso, plays = plays)
                _uiState.update { it.copy(snackbarMessage = "Enviado com sucesso.") }
            } catch (t: Throwable) {
                val msg = t.message?.trim().takeIf { !it.isNullOrBlank() } ?: "Erro inesperado ao enviar."
                _uiState.update { it.copy(snackbarMessage = msg) }
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
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