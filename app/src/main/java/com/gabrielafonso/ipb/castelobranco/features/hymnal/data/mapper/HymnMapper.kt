package com.gabrielafonso.ipb.castelobranco.features.hymnal.data.mapper

import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.dto.HymnDto
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.Hymn
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.HymnLyric
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.HymnLyricType
import javax.inject.Inject

class HymnMapper @Inject constructor() {

    fun map(dtos: List<HymnDto>): List<Hymn> =
        dtos.map { h ->
            Hymn(
                number = h.number,
                title = h.title,
                lyrics = h.lyrics.map { l ->
                    HymnLyric(
                        type = lyricTypeOf(l.type),
                        text = l.text
                    )
                }
            )
        }.sortedWith(
            compareBy(
                { it.number.toIntOrNull() ?: Int.MAX_VALUE },
                { it.number }
            )
        )

    private fun lyricTypeOf(raw: String): HymnLyricType =
        when (raw.trim().lowercase()) {
            "verse" -> HymnLyricType.VERSE
            "chorus" -> HymnLyricType.CHORUS
            else -> HymnLyricType.OTHER
        }
}
