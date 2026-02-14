package com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model

data class Hymn(
    val number: String,
    val title: String,
    val lyrics: List<HymnLyric>
)

data class HymnLyric(
    val type: HymnLyricType,
    val text: String
)

enum class HymnLyricType {
    VERSE,
    CHORUS,
    OTHER
}
