package com.gabrielafonso.ipb.castelobranco.features.auth.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.features.auth.domain.repository.AuthRepository
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class RegisterErrors(
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val password: String? = null,
    val passwordConfirm: String? = null,
    val general: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    sealed class AuthEvent {
        data object RegisterSuccess : AuthEvent()
        data object LoginSuccess : AuthEvent()
    }

    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registerErrors = MutableStateFlow(RegisterErrors())
    val registerErrors: StateFlow<RegisterErrors> = _registerErrors.asStateFlow()

    fun clearLoginError() {
        _loginError.value = null
    }

    fun clearRegisterErrors() {
        _registerErrors.value = RegisterErrors()
    }

    fun singIn(username: String, password: String) {
        viewModelScope.launch {
            _loginError.value = null
            try {
                val result = repository.signIn(username, password)
                result.onSuccess { authResponse ->
                    Log.d(TAG, "Login sucesso: $authResponse")

                    // Busca perfil \+ baixa/persiste foto antes de navegar
                    val ok = fetchUserData()
                    if (!ok) {
                        _loginError.value = "Falha ao carregar dados do usuário"
                        return@onSuccess
                    }

                    _events.tryEmit(AuthEvent.LoginSuccess)
                }.onFailure { throwable ->
                    val raw = throwable.message ?: "Erro ao fazer login"
                    _loginError.value = parseLoginError(raw)
                    Log.e(TAG, "Falha no login", throwable)
                }
            } catch (e: Exception) {
                val raw = e.message ?: "Erro inesperado"
                _loginError.value = parseLoginError(raw)
                Log.e(TAG, "Erro inesperado no login", e)
            }
        }
    }

    fun singUp(
        username: String,
        firstName: String,
        lastName: String,
        password: String,
        passwordConfirm: String
    ) {
        viewModelScope.launch {
            _registerErrors.value = RegisterErrors()
            try {
                val result = repository.signUp(username, firstName, lastName, password, passwordConfirm)
                result.onSuccess { authResponse ->
                    Log.d(TAG, "Registro sucesso: $authResponse")

                    val ok = fetchUserData()
                    if (!ok) {
                        _registerErrors.value = RegisterErrors(general = "Falha ao carregar dados do usuário")
                        return@onSuccess
                    }

                    _events.tryEmit(AuthEvent.RegisterSuccess)
                }.onFailure { throwable ->
                    val message = throwable.message ?: "Erro ao registrar"
                    _registerErrors.value = parseRegisterError(message)
                    Log.e(TAG, "Falha no registro", throwable)
                }
            } catch (e: Exception) {
                _registerErrors.value = RegisterErrors(general = e.message ?: "Erro inesperado")
                Log.e(TAG, "Erro inesperado no registro", e)
            }
        }
    }

    private suspend fun fetchUserData(): Boolean {
        return try {
            val profile = profileRepository.getMeProfile().getOrThrow()

            val photoUrl = profile.photoUrl
            if (!photoUrl.isNullOrBlank()) {
                profileRepository.downloadAndPersistProfilePhoto(photoUrl).getOrThrow()
            }

            true
        } catch (t: Throwable) {
            Log.e(TAG, "Falha ao buscar dados do usuário", t)
            false
        }
    }

    private fun parseLoginError(message: String): String {
        return try {
            val trimmed = message.trim()
            if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return message

            val json = JSONObject(trimmed)

            val keysPriority = listOf("detail", "message", "error", "non_field_errors")
            for (k in keysPriority) {
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

    private fun parseRegisterError(message: String): RegisterErrors {
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

    fun signInWithGoogle() {
        // iniciar fluxo de login com Google
    }
}