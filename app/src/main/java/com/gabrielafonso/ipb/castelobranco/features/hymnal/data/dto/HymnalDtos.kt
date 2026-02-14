package com.gabrielafonso.ipb.castelobranco.features.hymnal.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class HymnDto(
    val number: String,
    val title: String,
    val lyrics: List<HymnLyricDto>
)

@Serializable
data class HymnLyricDto(
    val type: String, // "verse" | "chorus" (importante p/ UI)
    val text: String
)
