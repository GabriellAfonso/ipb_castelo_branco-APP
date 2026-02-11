// app/src/main/java/com/gabrielafonso/ipb/castelobranco/core/network/TokenAuthenticator.kt
package com.gabrielafonso.ipb.castelobranco.core.network

import com.gabrielafonso.ipb.castelobranco.core.di.ApiBaseUrl
import com.gabrielafonso.ipb.castelobranco.core.di.AuthLessClient
import com.gabrielafonso.ipb.castelobranco.data.api.RefreshRequest
import com.gabrielafonso.ipb.castelobranco.data.local.TokenStorage
import com.gabrielafonso.ipb.castelobranco.domain.model.AuthTokens
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    @AuthLessClient private val authlessClient: OkHttpClient,
    @ApiBaseUrl private val baseUrl: String,
    private val tokenStorage: TokenStorage,
    private val json: Json,
) : Authenticator {

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    // O Mutex garante que apenas UMA requisição tente o refresh por vez
    private val refreshTokenMutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        return runBlocking {
            // Entra no lock. Se outra thread já estiver dando refresh, esta espera aqui.
            refreshTokenMutex.withLock {
                val current = tokenStorage.loadOrNull() ?: return@runBlocking null

                // --- VERIFICAÇÃO CRÍTICA PARA REFRESH ROTATION ---
                // Verifica se outra thread já atualizou o token enquanto esta esperava no lock.
                // Se o token atual no storage for diferente do token que falhou na requisição,
                // significa que o refresh já foi feito com sucesso.
                val authHeader = response.request.header("Authorization")
                if (authHeader != null && current.access.isNotBlank() && "Bearer ${current.access}" != authHeader) {
                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer ${current.access}")
                        .build()
                }

                val refresh = current.refresh
                if (refresh.isBlank()) return@runBlocking null

                val refreshUrl = baseUrl.trimEnd('/') + "/" + Endpoints.AUTH_REFRESH_PATH.trimStart('/')

                val bodyJson = json.encodeToString(
                    RefreshRequest.serializer(),
                    RefreshRequest(refresh = refresh),
                )

                val refreshRequest = Request.Builder()
                    .url(refreshUrl)
                    .post(bodyJson.toRequestBody(mediaType))
                    .build()

                val refreshResponse = runCatching {
                    authlessClient.newCall(refreshRequest).execute()
                }.getOrNull() ?: return@runBlocking null

                refreshResponse.use { rr ->
                    if (!rr.isSuccessful) {
                        // Se o refresh falhar (401 ou 400), limpa tudo e desloga
                        if (rr.code == 401 || rr.code == 400) {
                            tokenStorage.clear()
                        }
                        return@runBlocking null
                    }

                    val raw = rr.body?.string().orEmpty()
                    val decoded = runCatching {
                        json.decodeFromString(AuthTokens.serializer(), raw)
                    }.getOrNull() ?: return@runBlocking null

                    // Salva os novos tokens (mantém o refresh antigo se o novo vier vazio)
                    val newTokens = decoded.copy(
                        refresh = decoded.refresh.ifBlank { current.refresh }
                    )

                    tokenStorage.save(newTokens)

                    // Retorna a requisição original com o novo Token de Acesso
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.access}")
                        .build()
                }
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