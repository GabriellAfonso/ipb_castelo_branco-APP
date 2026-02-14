package com.gabrielafonso.ipb.castelobranco.features.worshiphub.presentation.register

import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.Song

object SongLabelFormatter {
    fun format(song: Song): String =
        if (song.artist.isBlank()) song.title else "${song.title} [${song.artist}]"
}