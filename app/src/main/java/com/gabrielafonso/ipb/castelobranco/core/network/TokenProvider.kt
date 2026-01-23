package com.gabrielafonso.ipb.castelobranco.core.network



/**
 * Abstração para obter tokens do EncryptedDataStore (implementação será em data layer).
 * Se for sessão, implemente stub que retorna null e desabilite o interceptor.
 */
interface TokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
}