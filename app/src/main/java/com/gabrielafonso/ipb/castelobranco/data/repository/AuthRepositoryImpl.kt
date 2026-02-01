package com.gabrielafonso.ipb.castelobranco.data.repository

import com.gabrielafonso.ipb.castelobranco.data.api.BackendApi
import com.gabrielafonso.ipb.castelobranco.data.api.LoginRequest
import com.gabrielafonso.ipb.castelobranco.data.api.RegisterRequest
import com.gabrielafonso.ipb.castelobranco.data.local.JsonSnapshotStorage
import com.gabrielafonso.ipb.castelobranco.domain.model.AuthResponse
import com.gabrielafonso.ipb.castelobranco.domain.repository.AuthRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: BackendApi,
    private val jsonStorage: JsonSnapshotStorage
) : AuthRepository {

    companion object {
        private const val KEY_TOKEN = "auth_token"
    }

    private fun <T> Response<T>.safeBody(): T? = if (isSuccessful) body() else null

    override suspend fun signIn(username: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            handleAuthResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(username: String, password: String, passwordConfirm: String): Result<AuthResponse> {
        return try {
            val response = api.register(RegisterRequest(username, password, passwordConfirm))
            handleAuthResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun handleAuthResponse(response: Response<AuthResponse>): Result<AuthResponse> {
        return if (response.isSuccessful) {
            val body = response.body()
            val token = body?.token
            if (!token.isNullOrBlank()) {
                jsonStorage.save(KEY_TOKEN, token)
                Result.success(body)
            } else {
                Result.failure(Exception("Token ausente"))
            }
        } else {
            Result.failure(Exception("HTTP ${response.code()}"))
        }
    }

    override fun getAuthToken(): String? =
        kotlin.runCatching {
            kotlinx.coroutines.runBlocking { jsonStorage.loadOrNull(KEY_TOKEN) }
        }.getOrNull()

    override suspend fun signOut() {
        jsonStorage.save(KEY_TOKEN, "")
    }
}
