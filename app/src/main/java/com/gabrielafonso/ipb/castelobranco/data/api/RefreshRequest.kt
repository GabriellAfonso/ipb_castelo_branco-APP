package com.gabrielafonso.ipb.castelobranco.data.api

import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequest(
    val refresh: String
)
