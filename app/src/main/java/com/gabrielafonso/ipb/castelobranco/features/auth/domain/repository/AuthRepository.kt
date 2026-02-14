package com.gabrielafonso.ipb.castelobranco.features.auth.domain.repository

import com.gabrielafonso.ipb.castelobranco.features.auth.domain.model.AuthTokens

interface AuthRepository {
    suspend fun signIn(username: String, password: String): Result<AuthTokens>
    fun getAuthToken(): String?
    suspend fun signOut()
    suspend fun signUp(
        username: String,
        firstName: String,
        lastName: String,
        password: String,
        passwordConfirm: String
    ): Result<AuthTokens>
}