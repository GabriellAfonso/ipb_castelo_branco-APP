package com.ipb.castelobranco.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateChordChartRequest(
    @SerialName("song_id") val songId: Int,
    val content: String,
    val tone: String,
    val instrument: String,
)
