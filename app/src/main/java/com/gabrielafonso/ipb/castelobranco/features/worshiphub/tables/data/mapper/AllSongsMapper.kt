package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.mapper

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.AllSongDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.Song

fun AllSongDto.toDomain(): Song =
    Song(
        id = id,
        title = title,
        artist = artist,
        categoryName = categoryName
    )

fun List<AllSongDto>.toDomain(): List<Song> = map { it.toDomain() }
