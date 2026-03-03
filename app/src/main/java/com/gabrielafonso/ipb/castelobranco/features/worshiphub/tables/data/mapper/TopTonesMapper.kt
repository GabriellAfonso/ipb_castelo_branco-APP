package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.mapper

import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.TopToneDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.model.TopTone
import javax.inject.Inject

class TopTonesMapper @Inject constructor() {

    fun map(dtos: List<TopToneDto>): List<TopTone> =
        dtos.map { TopTone(tone = it.tone, count = it.count) }
}