package com.gabrielafonso.ipb.castelobranco.features.hymnal.data.mapper

import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.dto.HymnDto
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.Hymn
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.HymnLyric
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.HymnLyricType

fun HymnDto.toDomain(): Hymn =
    Hymn(
        number = number,
        title = title,
        lyrics = lyrics.map { l ->
            HymnLyric(
                type = lyricTypeOf(l.type),
                text = l.text
            )
        }
    )

fun List<HymnDto>.toDomain(): List<Hymn> =
    map { it.toDomain() }.sortedWith(
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
