package com.gabrielafonso.ipb.castelobranco.features.auth.data.api

import com.gabrielafonso.ipb.castelobranco.features.auth.data.dto.LoginRequest
import com.gabrielafonso.ipb.castelobranco.features.auth.data.dto.RefreshRequest
import com.gabrielafonso.ipb.castelobranco.features.auth.data.dto.RegisterRequest
import com.gabrielafonso.ipb.castelobranco.features.auth.domain.model.AuthTokens
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST(AuthEndpoins.AUTH_LOGIN_PATH)
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthTokens>

    @POST(AuthEndpoins.AUTH_REGISTER_PATH)
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthTokens>

    @POST(AuthEndpoins.AUTH_REFRESH_PATH)
    suspend fun refresh(
        @Body request: RefreshRequest
    ): Response<AuthTokens>

}