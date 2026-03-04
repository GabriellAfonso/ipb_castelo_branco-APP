package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.mapper

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.SongsBySundayDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SundaySetItem

fun SongsBySundayDto.toDomain(): SundaySet =
    SundaySet(
        date = date,
        songs = songs.map { s ->
            SundaySetItem(
                position = s.position,
                title = s.title,
                artist = s.artist,
                tone = s.tone
            )
        }
    )

fun List<SongsBySundayDto>.toDomain(): List<SundaySet> = map { it.toDomain() }
