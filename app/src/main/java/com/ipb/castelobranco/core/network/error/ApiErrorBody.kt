package com.ipb.castelobranco.core.network.error

data class ApiErrorBody(
    val errorCode: String,
    val detail: String,
    val fieldErrors: Map<String, List<String>>? = null,
)
