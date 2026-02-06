package com.gabrielafonso.ipb.castelobranco.core.network

import com.gabrielafonso.ipb.castelobranco.data.local.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val requiresAuth = original.header(HEADER_REQUIRES_AUTH)
            ?.equals("true", ignoreCase = true) == true

        // Remove o header interno antes de enviar para o backend
        val baseRequest = original.newBuilder()
            .removeHeader(HEADER_REQUIRES_AUTH)
            .build()

        if (!requiresAuth) {
            return chain.proceed(baseRequest)
        }

        val access = runBlocking { tokenStorage.loadOrNull()?.access }

        val authed = if (!access.isNullOrBlank()) {
            baseRequest.newBuilder()
                .header("Authorization", "Bearer $access")
                .build()
        } else {
            baseRequest
        }

        return chain.proceed(authed)
    }

    private companion object {
        private const val HEADER_REQUIRES_AUTH = "Requires-Auth"
    }
}
