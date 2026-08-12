package com.ipb.castelobranco.core.presentation.error

import com.ipb.castelobranco.core.domain.error.AppError

/**
 * Displayable text for an [AppError]. Uses the app-authored text when there is one, otherwise falls
 * back to a generic message per category. Never exposes [AppError.message], which may carry the
 * server's response body.
 */
fun AppError.toUserMessage(): String = userMessage ?: when (this) {
    is AppError.Network -> "Sem conexão com a internet. Verifique sua rede e tente novamente."
    is AppError.Auth    -> "Faça login para continuar."
    is AppError.Server  -> "Não foi possível completar a operação. Tente novamente mais tarde."
    is AppError.Unknown -> "Algo deu errado. Tente novamente."
}
