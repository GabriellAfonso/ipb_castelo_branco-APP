package com.gabrielafonso.ipb.castelobranco.data.api

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)
