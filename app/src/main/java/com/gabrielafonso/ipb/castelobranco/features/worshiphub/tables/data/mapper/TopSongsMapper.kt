package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.mapper

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.TopSongDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.TopSong
import javax.inject.Inject

class TopSongsMapper @Inject constructor() {

    fun map(dtos: List<TopSongDto>): List<TopSong> =
        dtos.map { TopSong(title = it.title, playCount = it.playCount) }
}