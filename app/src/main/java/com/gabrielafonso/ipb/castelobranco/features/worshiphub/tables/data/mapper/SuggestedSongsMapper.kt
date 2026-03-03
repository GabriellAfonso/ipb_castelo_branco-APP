package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.mapper

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.SuggestedSongDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SuggestedSong
import javax.inject.Inject

class SuggestedSongsMapper @Inject constructor() {

    fun map(dtos: List<SuggestedSongDto>): List<SuggestedSong> =
        dtos.map { s ->
            SuggestedSong(
                id = s.id,
                songId = s.song.id,
                title = s.song.title,
                artist = s.song.artist,
                date = s.date,
                tone = s.tone,
                position = s.position
            )
        }.sortedBy { it.position }
}