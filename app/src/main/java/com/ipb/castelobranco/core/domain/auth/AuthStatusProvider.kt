package com.ipb.castelobranco.core.domain.auth

/**
 * Read-only view of the session, exposed from `core/` so features can ask whether a request may
 * carry credentials without importing `features/auth`.
 */
fun interface AuthStatusProvider {

    /**
     * True when a stored access token exists **and** is not yet expired.
     *
     * Deliberately stricter than "is logged in". Sending an expired token makes the server reply
     * 401, which drives `TokenAuthenticator` into a refresh attempt that clears the token store
     * when it fails — signing the member out. Background callers must gate on this.
     */
    suspend fun hasValidAccessToken(): Boolean
}
