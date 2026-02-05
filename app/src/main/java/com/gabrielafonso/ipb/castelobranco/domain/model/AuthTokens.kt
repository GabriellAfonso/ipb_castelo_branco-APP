package com.gabrielafonso.ipb.castelobranco.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthTokens(
    val access: String,
    val refresh: String
)
