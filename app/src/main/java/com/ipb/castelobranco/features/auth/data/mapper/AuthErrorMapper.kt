package com.ipb.castelobranco.features.auth.data.mapper

import com.ipb.castelobranco.features.auth.domain.model.RegisterErrors
import org.json.JSONArray
import org.json.JSONObject

private val LOGIN_ERROR_KEY_PRIORITY = listOf("detail", "message", "error", "non_field_errors")

fun parseLoginError(message: String): String {
    return try {
        val trimmed = message.trim()
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return message

        val json = JSONObject(trimmed)

        for (k in LOGIN_ERROR_KEY_PRIORITY) {
            if (json.has(k)) return jsonValueToMessage(json.get(k))
        }

        val keys = json.keys()
        if (keys.hasNext()) {
            val k = keys.next()
            return jsonValueToMessage(json.get(k))
        }

        message
    } catch (_: Exception) {
        message
    }
}

fun parseRegisterError(message: String): RegisterErrors {
    return try {
        val trimmed = message.trim()
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return RegisterErrors(general = message)
        }

        val json = JSONObject(trimmed)
        var errors = RegisterErrors()

        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.get(key)
            val msg = when (value) {
                is JSONArray -> if (value.length() > 0) value.getString(0) else value.toString()
                else -> value.toString()
            }

            errors = when (key) {
                "username" -> errors.copy(username = msg)
                "first_name", "firstName" -> errors.copy(firstName = msg)
                "last_name", "lastName" -> errors.copy(lastName = msg)
                "password" -> errors.copy(password = msg)
                "password_confirm", "passwordConfirm" -> errors.copy(passwordConfirm = msg)
                "detail" -> errors.copy(general = msg)
                else -> errors.copy(general = (errors.general?.let { "$it\n$msg" } ?: msg))
            }
        }

        if (errors == RegisterErrors()) RegisterErrors(general = message) else errors
    } catch (_: Exception) {
        RegisterErrors(general = message)
    }
}

private fun jsonValueToMessage(value: Any?): String {
    return when (value) {
        is JSONArray -> when {
            value.length() > 0 -> value.optString(0, value.toString())
            else -> value.toString()
        }
        is JSONObject -> value.optString("detail", value.toString())
        else -> value?.toString().orEmpty().ifBlank { "Erro ao fazer login" }
    }
}
