// app/src/main/java/com/gabrielafonso/ipb/castelobranco/core/network/TokenAuthenticator.kt
package com.gabrielafonso.ipb.castelobranco.core.network

import com.gabrielafonso.ipb.castelobranco.data.api.RefreshRequest
import com.gabrielafonso.ipb.castelobranco.data.local.TokenStorage
import com.gabrielafonso.ipb.castelobranco.domain.model.AuthTokens
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val authlessClient: OkHttpClient,
    private val baseUrl: String,
    private val tokenStorage: TokenStorage,
    private val json: Json, // \<= vindo do \`SerializationModule\`
) : Authenticator {

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        return runBlocking {
            val current = tokenStorage.loadOrNull() ?: return@runBlocking null
            val refresh = current.refresh
            if (refresh.isBlank()) return@runBlocking null

            val refreshUrl = baseUrl.trimEnd('/') + "/" + Endpoints.AUTH_REFRESH_PATH.trimStart('/')

            val bodyJson = json.encodeToString(
                RefreshRequest.serializer(),
                RefreshRequest(refresh = refresh),
            )

            val request = Request.Builder()
                .url(refreshUrl)
                .post(bodyJson.toRequestBody(mediaType))
                .build()

            val refreshResponse = runCatching { authlessClient.newCall(request).execute() }.getOrNull()
                ?: return@runBlocking null

            if (!refreshResponse.isSuccessful) return@runBlocking null

            val raw = refreshResponse.body?.string().orEmpty()
            val tokens = runCatching { json.decodeFromString(AuthTokens.serializer(), raw) }.getOrNull()
                ?: return@runBlocking null

            tokenStorage.save(tokens)

            response.request.newBuilder()
                .header("Authorization", "Bearer ${tokens.access}")
                .build()
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
