package com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.register

import com.gabrielafonso.ipb.castelobranco.domain.model.Song

object SongLabelFormatter {
    fun format(song: Song): String =
        if (song.artist.isBlank()) song.title else "${song.title} [${song.artist}]"
}