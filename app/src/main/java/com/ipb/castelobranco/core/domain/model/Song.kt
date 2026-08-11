package com.ipb.castelobranco.core.domain.model

data class Song(
    val id: Int,
    val title: String,
    val artist: String,
    val categoryName: String,
    val youtubeLink: String? = null,
)
