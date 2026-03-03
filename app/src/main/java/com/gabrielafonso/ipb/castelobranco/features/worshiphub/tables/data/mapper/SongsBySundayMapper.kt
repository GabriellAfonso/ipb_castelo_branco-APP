package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.mapper

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.SongsBySundayDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SundaySetItem
import javax.inject.Inject

class SongsBySundayMapper @Inject constructor() {

    fun map(dtos: List<SongsBySundayDto>): List<SundaySet> =
        dtos.map { day ->
            SundaySet(
                date = day.date,
                songs = day.songs.map { s ->
                    SundaySetItem(
                        position = s.position,
                        title = s.title,
                        artist = s.artist,
                        tone = s.tone
                    )
                }
            )
        }
}