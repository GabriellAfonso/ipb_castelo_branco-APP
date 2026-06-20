package com.ipb.castelobranco.core.network.error

import org.json.JSONObject
import timber.log.Timber

fun parseApiError(errorBody: String?): ApiErrorBody? {
    if (errorBody.isNullOrBlank()) return null
    return try {
        val json = JSONObject(errorBody)
        val errorCode = json.optString("error_code", "").ifBlank { return null }
        val detail = json.optString("detail", "")
        val fieldErrors = json.optJSONObject("field_errors")?.let { obj ->
            val map = mutableMapOf<String, List<String>>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val arr = obj.optJSONArray(key)
                if (arr != null) {
                    map[key] = (0 until arr.length()).map { arr.getString(it) }
                }
            }
            map.ifEmpty { null }
        }
        ApiErrorBody(errorCode = errorCode, detail = detail, fieldErrors = fieldErrors)
    } catch (e: Exception) {
        Timber.w(e, "Failed to parse API error body")
        null
    }
}
