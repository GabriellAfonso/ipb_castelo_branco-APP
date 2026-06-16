package com.ipb.castelobranco.core.network.error

import com.ipb.castelobranco.core.domain.error.AppError
import retrofit2.Response

fun Response<*>.toAppError(): AppError {
    val code = code()
    val raw = errorBody()?.string()
    val parsed = parseApiError(raw)
    val message = parsed?.detail?.ifBlank { null }
        ?: raw?.ifBlank { null }
        ?: "HTTP $code"

    return if (code == 401 || code == 403) {
        AppError.Auth(message = message)
    } else {
        AppError.Server(code = code, message = message, errorCode = parsed?.errorCode)
    }
}
