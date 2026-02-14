package com.gabrielafonso.ipb.castelobranco.features.worshiphub.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SongsBySundayDto(
    val date: String, // "dd/MM/yyyy"
    val songs: List<SundaySongDto>
)

@Serializable
data class SundaySongDto(
    val position: Int,
    @SerialName("song") val title: String,
    val artist: String,
    val tone: String
)

@Serializable
data class TopSongDto(
    @SerialName("song__title") val title: String,
    @SerialName("play_count") val playCount: Int
)

@Serializable
data class TopToneDto(
    val tone: String,
    @SerialName("tone_count") val count: Int
)

@Serializable
data class SuggestedSongDto(
    val id: Int,
    val song: SuggestedSongInnerDto,
    val date: String, // "dd/MM/yyyy"
    val tone: String,
    val position: Int
)

@Serializable
data class SuggestedSongInnerDto(
    val id: Int,
    val title: String,
    val artist: String
)