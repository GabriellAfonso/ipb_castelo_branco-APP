package com.gabrielafonso.ipb.castelobranco.features.auth.data.repository

import com.gabrielafonso.ipb.castelobranco.features.auth.data.api.AuthApi
import com.gabrielafonso.ipb.castelobranco.features.auth.domain.model.AuthTokens
import com.gabrielafonso.ipb.castelobranco.features.auth.data.dto.LoginRequest
import com.gabrielafonso.ipb.castelobranco.features.auth.data.dto.RegisterRequest
import com.gabrielafonso.ipb.castelobranco.features.auth.data.local.TokenStorage
import com.gabrielafonso.ipb.castelobranco.features.auth.domain.repository.AuthRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun signIn(
        username: String,
        password: String
    ): Result<AuthTokens> =
        authenticate {
            api.login(LoginRequest(username, password))
        }

    override suspend fun signUp(
        username: String,
        firstName: String,
        lastName: String,
        password: String,
        passwordConfirm: String
    ): Result<AuthTokens> =
        authenticate {
            api.register(
                RegisterRequest(
                    username = username,
                    firstName = firstName,
                    lastName = lastName,
                    password = password,
                    passwordConfirm = passwordConfirm
                )
            )
        }

    override suspend fun getAuthToken(): String? =
        tokenStorage.loadOrNull()?.access

    override suspend fun signOut() {
        tokenStorage.clear()
    }

    private suspend fun authenticate(
        call: suspend () -> Response<AuthTokens>
    ): Result<AuthTokens> =
        runCatching {
            val response = call()

           if (!response.isSuccessful) {
            // Pega o JSON de erro do servidor (ex: {"detail": "..."})
            val errorBody = response.errorBody()?.string()

            // Se houver um corpo de erro, lança ele, senão lança o código HTTP
            throw Exception(errorBody ?: "HTTP ${response.code()}")
        }

            val tokens = response.body()
                ?: throw Exception("Resposta vazia")

            require(tokens.access.isNotBlank()) { "Access token ausente" }
            require(tokens.refresh.isNotBlank()) { "Refresh token ausente" }

            tokenStorage.save(tokens)
            tokens
        }
}
