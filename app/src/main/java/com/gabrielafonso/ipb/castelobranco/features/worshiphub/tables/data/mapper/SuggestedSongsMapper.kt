package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.mapper

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.SuggestedSongDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SuggestedSong

fun SuggestedSongDto.toDomain(): SuggestedSong =
    SuggestedSong(
        id = id,
        songId = song.id,
        title = song.title,
        artist = song.artist,
        date = date,
        tone = tone,
        position = position
    )

fun List<SuggestedSongDto>.toDomain(): List<SuggestedSong> =
    map { it.toDomain() }.sortedBy { it.position }
