package com.gabrielafonso.ipb.castelobranco.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfilePhotoResponseDto(
    val detail: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null
)