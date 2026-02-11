package com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.register

import com.gabrielafonso.ipb.castelobranco.domain.model.Song
import com.gabrielafonso.ipb.castelobranco.domain.model.SundayPlayPushItem

object SundayPlaysMapper {

    fun dateIso(selectedDate: java.time.LocalDate?): String =
        selectedDate?.format(MusicRegistrationUiState.ISO_DATE)?.trim().orEmpty()

    /**
     * Pré-condição: o caller (ViewModel) já validou que as linhas estão completas.
     * Aqui a gente só converte linhas completas em payload.
     */
    fun toSundayPlayItems(rows: List<SundaySongRowState>, availableSongs: List<Song>): List<SundayPlayPushItem> {
        return rows.mapNotNull { row ->
            val hasAnySongInput = row.songQuery.isNotBlank() || row.selectedSongId != null
            val hasTone = row.tone.isNotBlank()
            val bothEmpty = !hasAnySongInput && !hasTone
            if (bothEmpty) return@mapNotNull null

            val sid = row.selectedSongId ?: return@mapNotNull null
            val songExists = availableSongs.any { it.id == sid }
            if (!songExists) return@mapNotNull null

            SundayPlayPushItem(
                songId = sid,
                position = row.position,
                tone = row.tone.trim()
            )
        }
    }
}