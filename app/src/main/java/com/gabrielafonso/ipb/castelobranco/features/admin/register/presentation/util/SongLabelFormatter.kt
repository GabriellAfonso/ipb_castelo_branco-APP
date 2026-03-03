package com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.util

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.Song

object SongLabelFormatter {
    fun format(song: Song): String =
        if (song.artist.isBlank()) song.title else "${song.title} [${song.artist}]"
}