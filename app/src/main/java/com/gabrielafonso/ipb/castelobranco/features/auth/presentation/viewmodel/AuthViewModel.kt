package com.gabrielafonso.ipb.castelobranco.features.auth.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.features.auth.data.mapper.AuthErrorMapper
import com.gabrielafonso.ipb.castelobranco.features.auth.domain.model.RegisterErrors
import com.gabrielafonso.ipb.castelobranco.features.auth.domain.usecase.LoginUseCase
import com.gabrielafonso.ipb.castelobranco.features.auth.domain.usecase.RegisterUseCase
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
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val authErrorMapper: AuthErrorMapper,
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
            when (val result = loginUseCase.withCredentials(username, password)) {
                LoginUseCase.Result.Success -> {
                    Log.d(TAG, "Login sucesso")
                    _events.tryEmit(AuthEvent.LoginSuccess)
                }
                is LoginUseCase.Result.Failure -> {
                    _loginError.value = authErrorMapper.parseLoginError(result.rawMessage)
                    Log.e(TAG, "Falha no login")
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        Log.d("GoogleSignIn", "signInWithGoogle chamado")
        viewModelScope.launch {
            _loginError.value = null
            when (val result = loginUseCase.withGoogle(idToken)) {
                LoginUseCase.Result.Success -> _events.tryEmit(AuthEvent.LoginSuccess)
                is LoginUseCase.Result.Failure -> {
                    _loginError.value = result.rawMessage
                    Log.e(TAG, "Falha no login com Google")
                }
            }
        }
    }

    fun singUp(
        username: String,
        firstName: String,
        lastName: String,
        password: String,
        passwordConfirm: String,
    ) {
        viewModelScope.launch {
            _registerErrors.value = RegisterErrors()
            when (val result = registerUseCase(username, firstName, lastName, password, passwordConfirm)) {
                RegisterUseCase.Result.Success -> {
                    Log.d(TAG, "Registro sucesso")
                    _events.tryEmit(AuthEvent.RegisterSuccess)
                }
                is RegisterUseCase.Result.Failure -> {
                    _registerErrors.value = authErrorMapper.parseRegisterError(result.rawMessage)
                    Log.e(TAG, "Falha no registro")
                }
            }
        }
    }

    fun signInWithGoogle() {
        // iniciar fluxo de login com Google
    }
}
