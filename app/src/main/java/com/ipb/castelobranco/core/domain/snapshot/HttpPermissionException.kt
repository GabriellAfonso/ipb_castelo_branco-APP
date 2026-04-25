package com.ipb.castelobranco.core.domain.snapshot

class HttpPermissionException(
    val code: Int,
    override val message: String,
) : Exception(message)
