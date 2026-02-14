package com.gabrielafonso.ipb.castelobranco.features.auth.data.repository

import com.gabrielafonso.ipb.castelobranco.features.auth.data.api.AuthApi
import com.gabrielafonso.ipb.castelobranco.features.auth.domain.model.AuthTokens
import com.gabrielafonso.ipb.castelobranco.features.auth.data.dto.LoginRequest
import com.gabrielafonso.ipb.castelobranco.features.auth.data.dto.RegisterRequest
import com.gabrielafonso.ipb.castelobranco.features.auth.data.local.TokenStorage
import com.gabrielafonso.ipb.castelobranco.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun signIn(username: String, password: String): Result<AuthTokens> =
        runCatching {
            val response = api.login(LoginRequest(username, password))
            handleAuthResponse(response).getOrThrow()
        }

    override suspend fun signUp(
        username: String,
        firstName: String,
        lastName: String,
        password: String,
        passwordConfirm: String
    ): Result<AuthTokens> =
        runCatching {
            val response = api.register(
                RegisterRequest(
                    username = username,
                    firstName = firstName,
                    lastName = lastName,
                    password = password,
                    passwordConfirm = passwordConfirm
                )
            )
            handleAuthResponse(response).getOrThrow()
        }

    private suspend fun handleAuthResponse(response: Response<AuthTokens>): Result<AuthTokens> {
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            return Result.failure(Exception(errorBody ?: "HTTP ${response.code()}"))
        }

        val body = response.body() ?: return Result.failure(Exception("Resposta vazia"))
        val access = body.access
        val refresh = body.refresh

        if (access.isBlank() || refresh.isBlank()) {
            return Result.failure(Exception("Tokens ausentes"))
        }

        tokenStorage.save(AuthTokens(access = access, refresh = refresh))
        return Result.success(body)
    }

    override fun getAuthToken(): String? =
        runCatching { runBlocking { tokenStorage.loadOrNull()?.access } }.getOrNull()

    override suspend fun signOut() {
        tokenStorage.clear()
    }
}