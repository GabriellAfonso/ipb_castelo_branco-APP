package com.gabrielafonso.ipb.castelobranco.features.worshiphub.presentation.register

import androidx.compose.runtime.Immutable
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.Song
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class RegistrationType(val label: String) {
    MUSIC("Registrar música"),
    SUNDAY("Registrar domingo")
}

@Immutable
data class SundaySongRowState(
    val position: Int,
    val songQuery: String = "",
    val selectedSongId: Int? = null,
    val tone: String = ""
)

@Immutable
data class MusicRegistrationUiState(
    val registrationType: RegistrationType = RegistrationType.SUNDAY,

    val availableSongs: List<Song> = emptyList(),
    val isLoadingSongs: Boolean = false,

    val selectedDate: LocalDate? = null,
    val showDatePicker: Boolean = false,
    val sundayRows: List<SundaySongRowState> = defaultSundayRows(),

    // NOVO: erros por posição (ex.: 2 -> "Posição 2 incompleta...")
    val sundayRowErrors: Map<Int, String> = emptyMap(),

    val isSubmitting: Boolean = false,
    val snackbarMessage: String? = null
) {
    val dateBr: String
        get() = if (registrationType == RegistrationType.SUNDAY) selectedDate?.format(BR_DATE).orEmpty() else ""

    val canSubmit: Boolean
        get() = when (registrationType) {
            RegistrationType.SUNDAY -> {
                val dateOk = selectedDate != null
                val validation = MusicRegistrationValidator.validateSundayRows(sundayRows, availableSongs)

                // precisa ter data, não pode ter linhas incompletas e precisa ter pelo menos 1 linha completa e válida
                dateOk && validation.incompletePositions.isEmpty() && validation.hasAtLeastOneCompleteValidRow
            }
            RegistrationType.MUSIC -> false
        }

    companion object {
        val BR_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        fun defaultSundayRows(): List<SundaySongRowState> = listOf(
            SundaySongRowState(position = 1),
            SundaySongRowState(position = 2),
            SundaySongRowState(position = 3),
            SundaySongRowState(position = 4),
        )
    }
}

sealed interface MusicRegistrationEvent {
    data object Init : MusicRegistrationEvent
    data class RegistrationTypeChanged(val type: RegistrationType) : MusicRegistrationEvent

    data object OpenDatePicker : MusicRegistrationEvent
    data object DismissDatePicker : MusicRegistrationEvent
    data class DatePicked(val date: LocalDate) : MusicRegistrationEvent

    data class SundaySongQueryChanged(val position: Int, val query: String) : MusicRegistrationEvent
    data class SundaySongSelected(val position: Int, val song: Song) : MusicRegistrationEvent
    data class SundayToneChanged(val position: Int, val tone: String) : MusicRegistrationEvent

    data object AddSundayRow : MusicRegistrationEvent
    data class RemoveSundayRow(val position: Int) : MusicRegistrationEvent

    data object Submit : MusicRegistrationEvent
    data object SnackbarShown : MusicRegistrationEvent
}