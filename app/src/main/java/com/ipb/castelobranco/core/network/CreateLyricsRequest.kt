package com.ipb.castelobranco.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateLyricsRequest(
    @SerialName("song_id") val songId: Int,
    val content: String,
)
