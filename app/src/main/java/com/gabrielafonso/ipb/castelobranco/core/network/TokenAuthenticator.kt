package com.gabrielafonso.ipb.castelobranco.core.network

import com.gabrielafonso.ipb.castelobranco.features.auth.data.api.AuthApi
import com.gabrielafonso.ipb.castelobranco.features.auth.data.dto.RefreshRequest
import com.gabrielafonso.ipb.castelobranco.features.auth.data.local.TokenStorage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

@Singleton
class TokenAuthenticator @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
) : Authenticator {

    private val refreshTokenMutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        return runBlocking(Dispatchers.IO) {
            refreshTokenMutex.withLock {
                val current = tokenStorage.peekOrNull() ?: return@runBlocking null

                val failedAuthHeader = response.request.header("Authorization")
                val currentAccess = current.access
                if (!failedAuthHeader.isNullOrBlank() &&
                    !currentAccess.isNullOrBlank() &&
                    failedAuthHeader != "Bearer $currentAccess"
                ) {
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer $currentAccess")
                        .build()
                }

                val refresh = current.refresh
                if (refresh.isBlank()) return@runBlocking null

                val refreshResponse = runCatching {
                    authApi.refresh(RefreshRequest(refresh = refresh))
                }.getOrNull() ?: return@runBlocking null

                if (!refreshResponse.isSuccessful) {

                    if (refreshResponse.code() == 401 || refreshResponse.code() == 400) {
                        tokenStorage.clear()
                    }
                    return@runBlocking null
                }

                val newTokens = refreshResponse.body() ?: return@runBlocking null

                tokenStorage.save(newTokens)

                return@runBlocking response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.access}")
                    .build()
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var r: Response? = response
        var count = 1
        while (r?.priorResponse != null) {
            count++
            r = r.priorResponse
        }
        return count
    }
}