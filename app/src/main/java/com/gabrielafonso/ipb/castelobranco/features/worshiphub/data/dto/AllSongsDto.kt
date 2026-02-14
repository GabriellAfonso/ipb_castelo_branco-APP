// app/src/main/java/com/gabrielafonso/ipb/castelobranco/data/api/AllSongsDto.kt
package com.gabrielafonso.ipb.castelobranco.features.worshiphub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllSongDto(
    val id: Int,
    val title: String,
    val artist: String,
    @SerialName("category") val categoryName: String = ""
)
