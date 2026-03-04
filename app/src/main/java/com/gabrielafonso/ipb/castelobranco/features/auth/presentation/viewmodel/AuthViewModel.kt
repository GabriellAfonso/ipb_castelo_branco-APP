package com.gabrielafonso.ipb.castelobranco.features.auth.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.core.domain.auth.AuthEventBus
import com.gabrielafonso.ipb.castelobranco.features.auth.data.mapper.AuthErrorMapper
import com.gabrielafonso.ipb.castelobranco.features.auth.domain.model.RegisterErrors
import com.gabrielafonso.ipb.castelobranco.features.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val authEventBus: AuthEventBus,
    private val authErrorMapper: AuthErrorMapper
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
                    authEventBus.emit(AuthEventBus.Event.LoginSuccess)
                    _events.tryEmit(AuthEvent.LoginSuccess)
                }.onFailure { throwable ->
                    val raw = throwable.message ?: "Erro ao fazer login"
                    _loginError.value = authErrorMapper.parseLoginError(raw)
                    Log.e(TAG, "Falha no login", throwable)
                }
            } catch (e: Exception) {
                val raw = e.message ?: "Erro inesperado"
                _loginError.value = authErrorMapper.parseLoginError(raw)
                Log.e(TAG, "Erro inesperado no login", e)
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        Log.d("GoogleSignIn", "signInWithGoogle chamado")
        viewModelScope.launch {
            _loginError.value = null
            try {
                val result = repository.signInWithGoogle(idToken)
                result.onSuccess {
                    authEventBus.emit(AuthEventBus.Event.LoginSuccess)
                    _events.tryEmit(AuthEvent.LoginSuccess)
                }.onFailure { throwable ->
                    _loginError.value = throwable.message ?: "Erro ao entrar com Google"
                }
            } catch (e: Exception) {
                _loginError.value = e.message ?: "Erro inesperado"
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
                val result =
                    repository.signUp(username, firstName, lastName, password, passwordConfirm)
                result.onSuccess { authResponse ->
                    Log.d(TAG, "Registro sucesso: $authResponse")
                    authEventBus.emit(AuthEventBus.Event.LoginSuccess)
                    _events.tryEmit(AuthEvent.RegisterSuccess)
                }.onFailure { throwable ->
                    val message = throwable.message ?: "Erro ao registrar"
                    _registerErrors.value = authErrorMapper.parseRegisterError(message)
                    Log.e(TAG, "Falha no registro", throwable)
                }
            } catch (e: Exception) {
                _registerErrors.value = RegisterErrors(general = e.message ?: "Erro inesperado")
                Log.e(TAG, "Erro inesperado no registro", e)
            }
        }
    }

    fun signInWithGoogle() {
        // iniciar fluxo de login com Google
    }
}
