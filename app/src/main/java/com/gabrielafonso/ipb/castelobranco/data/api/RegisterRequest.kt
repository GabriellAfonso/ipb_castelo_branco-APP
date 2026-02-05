package com.gabrielafonso.ipb.castelobranco.data.api

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val password: String,
    @SerialName("password_confirm")
    val passwordConfirm: String
)
