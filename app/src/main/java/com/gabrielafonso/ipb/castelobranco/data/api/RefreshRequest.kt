package com.gabrielafonso.ipb.castelobranco.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(
    @SerialName("refresh") val refresh: String
)
