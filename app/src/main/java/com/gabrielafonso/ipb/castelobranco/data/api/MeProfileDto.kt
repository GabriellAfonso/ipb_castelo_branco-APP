package com.gabrielafonso.ipb.castelobranco.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MeProfileDto(
    @SerialName("name") val name: String,
    @SerialName("active") val active: Boolean,
    @SerialName("is_member") val isMember: Boolean,
    @SerialName("photo_url") val photoUrl: String? = null
)
