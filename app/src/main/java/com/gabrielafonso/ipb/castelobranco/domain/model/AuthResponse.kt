package com.gabrielafonso.ipb.castelobranco.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val refresh: String
)
