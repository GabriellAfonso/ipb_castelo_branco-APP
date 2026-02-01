package com.gabrielafonso.ipb.castelobranco.data.api

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    @SerializedName("password_confirm")
    val passwordConfirm: String
)
