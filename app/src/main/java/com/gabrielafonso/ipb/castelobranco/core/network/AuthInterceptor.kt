package com.gabrielafonso.ipb.castelobranco.core.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: TokenProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        // tokenProvider é suspenso; usar runBlocking dentro do interceptor (ok para simples leitura)
        val token = runBlocking { tokenProvider.getAccessToken() }
        return if (!token.isNullOrBlank()) {
            val req = original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(req)
        } else {
            chain.proceed(original)
        }
    }
}