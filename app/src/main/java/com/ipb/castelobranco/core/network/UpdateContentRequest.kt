package com.ipb.castelobranco.core.network

import kotlinx.serialization.Serializable

@Serializable
data class UpdateContentRequest(val content: String)
