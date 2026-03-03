package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.mapper

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.AllSongDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.model.Song
import javax.inject.Inject

class AllSongsMapper @Inject constructor() {

    fun map(dtos: List<AllSongDto>): List<Song> =
        dtos.map {
            Song(
                id = it.id,
                title = it.title,
                artist = it.artist,
                categoryName = it.categoryName
            )
        }
}