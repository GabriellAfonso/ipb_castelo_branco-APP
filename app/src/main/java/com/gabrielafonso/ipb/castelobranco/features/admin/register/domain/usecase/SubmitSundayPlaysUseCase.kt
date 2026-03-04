package com.gabrielafonso.ipb.castelobranco.features.admin.register.domain.usecase

import com.gabrielafonso.ipb.castelobranco.features.admin.register.domain.mapper.dateIso
import com.gabrielafonso.ipb.castelobranco.features.admin.register.domain.mapper.toSundayPlayItems
import com.gabrielafonso.ipb.castelobranco.features.admin.register.domain.repository.WorshipRegisterRepository
import com.gabrielafonso.ipb.castelobranco.features.admin.register.domain.validation.MusicRegistrationValidator
import com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.state.SundaySongRowState
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.Song
import java.time.LocalDate
import javax.inject.Inject

class SubmitSundayPlaysUseCase @Inject constructor(
    private val repository: WorshipRegisterRepository
) {
    sealed interface Result {
        data class ValidationError(val errorsByPosition: Map<Int, String>) : Result
        data object Success : Result
        data class Failure(val message: String) : Result
    }

    suspend operator fun invoke(
        rows: List<SundaySongRowState>,
        availableSongs: List<Song>,
        selectedDate: LocalDate?
    ): Result {
        val validation = MusicRegistrationValidator.validateSundayRows(rows, availableSongs)
        if (validation.errorsByPosition.isNotEmpty()) {
            return Result.ValidationError(validation.errorsByPosition)
        }

        val dateIso = dateIso(selectedDate)
        val plays = toSundayPlayItems(rows, availableSongs)

        return try {
            repository.pushSundayPlays(date = dateIso, plays = plays).getOrThrow()
            Result.Success
        } catch (t: Throwable) {
            val msg = t.message?.trim().takeIf { !it.isNullOrBlank() } ?: "Erro inesperado ao enviar."
            Result.Failure(msg)
        }
    }
}
