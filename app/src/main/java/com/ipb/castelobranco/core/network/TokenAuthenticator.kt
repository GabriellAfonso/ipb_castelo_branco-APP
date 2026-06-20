package com.ipb.castelobranco.core.network

import com.ipb.castelobranco.features.auth.data.api.AuthApi
import com.ipb.castelobranco.features.auth.data.dto.RefreshRequest
import com.ipb.castelobranco.features.auth.data.local.TokenStorage
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
import timber.log.Timber

@Singleton
class TokenAuthenticator @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
) : Authenticator {

    private val refreshTokenMutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            Timber.w("Token refresh aborted: too many retries")
            return null
        }

        return runBlocking(Dispatchers.IO) {
            refreshTokenMutex.withLock {
                val current = tokenStorage.peekOrNull()
                if (current == null) {
                    Timber.d("Token refresh skipped: no stored tokens")
                    return@runBlocking null
                }

                val failedAuthHeader = response.request.header("Authorization")
                val currentAccess = current.access
                if (!failedAuthHeader.isNullOrBlank() &&
                    !currentAccess.isNullOrBlank() &&
                    failedAuthHeader != "Bearer $currentAccess"
                ) {
                    Timber.d("Token already refreshed by another request, retrying")
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer $currentAccess")
                        .build()
                }

                val refresh = current.refresh
                if (refresh.isBlank()) return@runBlocking null

                Timber.d("Attempting token refresh")
                val refreshResponse = runCatching {
                    authApi.refresh(RefreshRequest(refresh = refresh))
                }.onFailure { Timber.e(it, "Token refresh request failed") }
                    .getOrNull() ?: return@runBlocking null

                if (!refreshResponse.isSuccessful) {
                    Timber.w("Token refresh failed: HTTP %d", refreshResponse.code())
                    if (refreshResponse.code() == 401 || refreshResponse.code() == 400) {
                        Timber.w("Clearing tokens due to %d on refresh", refreshResponse.code())
                        tokenStorage.clear()
                    }
                    return@runBlocking null
                }

                val newTokens = refreshResponse.body() ?: return@runBlocking null

                tokenStorage.save(newTokens)
                Timber.d("Token refresh successful")

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