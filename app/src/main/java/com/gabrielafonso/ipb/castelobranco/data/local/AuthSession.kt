package com.gabrielafonso.ipb.castelobranco.data.local

import android.util.Base64
import com.gabrielafonso.ipb.castelobranco.data.local.TokenStorage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSession @Inject constructor(
    private val tokenStorage: TokenStorage
) {
    suspend fun isLoggedIn(): Boolean {
        val tokens = tokenStorage.loadOrNull() ?: return false
        return isAccessTokenLocallyValid(tokens.access)
    }

    private fun isAccessTokenLocallyValid(accessToken: String): Boolean {
        if (accessToken.isBlank()) return false

        val expSeconds = jwtExpSecondsOrNull(accessToken)
            ?: return true // fallback: existe token, deixa o servidor validar quando precisar

        val nowSeconds = System.currentTimeMillis() / 1000
        return expSeconds > nowSeconds
    }

    private fun jwtExpSecondsOrNull(token: String): Long? {
        return runCatching {
            val parts = token.split('.')
            if (parts.size < 2) return null

            val payloadBytes = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )

            val payload = payloadBytes.decodeToString()
            val element = Json.parseToJsonElement(payload).jsonObject
            element["exp"]?.jsonPrimitive?.longOrNull
        }.getOrNull()
    }

    suspend fun logout() {
        tokenStorage.clear()
    }
}
