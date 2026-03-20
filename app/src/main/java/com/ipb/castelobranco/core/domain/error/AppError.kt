package com.ipb.castelobranco.core.domain.error

import java.io.IOException

/**
 * Sealed hierarchy of domain-level errors. Repositories must map all raw [Throwable]s
 * to one of these subtypes so callers can react to specific failure categories without
 * depending on HTTP or I/O implementation details.
 */
sealed class AppError(message: String?, cause: Throwable?) : Exception(message, cause) {

    /** Connectivity failure: no network, DNS error, socket timeout, etc. */
    class Network(
        message: String? = "Erro de rede",
        cause: Throwable? = null,
    ) : AppError(message, cause)

    /** Authentication / authorisation failure (HTTP 401 or 403). */
    class Auth(
        message: String? = "Falha de autenticação",
        cause: Throwable? = null,
    ) : AppError(message, cause)

    /** The server responded with an error status code (4xx / 5xx, except 401/403). */
    class Server(
        val code: Int,
        message: String? = "Erro no servidor ($code)",
        cause: Throwable? = null,
    ) : AppError(message, cause)

    /** An unexpected error that does not fit the categories above. */
    class Unknown(
        message: String? = "Erro desconhecido",
        cause: Throwable? = null,
    ) : AppError(message, cause)
}

/**
 * Maps any raw [Throwable] to an [AppError], preserving the original as [cause].
 * Already-mapped [AppError]s are returned unchanged.
 */
fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this
    is IOException -> AppError.Network(message = message, cause = this)
    else -> AppError.Unknown(message = message, cause = this)
}

/**
 * Maps the failure inside this [Result] to an [AppError] via [toAppError].
 * Successful results are returned unchanged.
 */
fun <T> Result<T>.mapError(): Result<T> =
    recoverCatching { throw it.toAppError() }
