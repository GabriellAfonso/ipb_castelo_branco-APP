package com.ipb.castelobranco.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.domain.auth.AuthEventBus
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.core.domain.usecase.PreloadDataUseCase
import com.ipb.castelobranco.features.auth.data.local.AuthSession
import com.ipb.castelobranco.features.auth.domain.usecase.LogoutUseCase
import com.ipb.castelobranco.features.profile.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoreViewModel @Inject constructor(
    private val preloadDataUseCase: PreloadDataUseCase,
    private val authSession: AuthSession,
    private val profileRepository: ProfileRepository,
    private val authEventBus: AuthEventBus,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    sealed interface CoreEvent {
        data object LogoutSuccess : CoreEvent
    }

    private val _events = Channel<CoreEvent>(capacity = Channel.Factory.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _isPreloading = MutableStateFlow(false)
    val isPreloading: StateFlow<Boolean> = _isPreloading.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun initialize() {
        // Observa estado de login
        viewModelScope.launch {
            authSession.isLoggedInFlow.collect { logged ->
                _isLoggedIn.value = logged
            }
        }

        // Reage ao login bem-sucedido para sincronizar dados de perfil
        viewModelScope.launch {
            authEventBus.events.collect { event ->
                if (event is AuthEventBus.Event.LoginSuccess) {
                    refreshProfileOnAppOpen()
                }
            }
        }

        // Inicialização em cascata: Primeiro Cache (Rápido), depois Rede (Lento)
        startAppInitialization()
    }

    private fun startAppInitialization() {
        viewModelScope.launch {
            _isPreloading.value = true

            // 1 & 2. PRELOAD (disk) + REFRESH (network) delegated to use case
            preloadDataUseCase()

            // Perfil é um caso à parte pois depende de login
            refreshProfileOnAppOpen()

            _isPreloading.value = false
        }
    }

    private fun refreshProfileOnAppOpen() {
        viewModelScope.launch {
            if (!authSession.isLoggedIn()) return@launch

            runCatching {
                // Atualiza dados do perfil
                profileRepository.refreshMeProfile()

                // Tenta pegar a foto se o perfil estiver em estado Data
                val profileState = profileRepository.observeMeProfile().first()
                if (profileState is SnapshotState.Data) {
                    val url = profileState.value.photoUrl
                    if (!url.isNullOrBlank()) {
                        profileRepository.downloadAndPersistProfilePhoto(url)
                    }
                }
            }
        }
    }

    fun refreshLoginState() {
        viewModelScope.launch {
            _isLoggedIn.value = authSession.isLoggedIn()
        }
    }

    fun logout() {
        viewModelScope.launch {
            profileRepository.clearLocalProfilePhoto()
            logoutUseCase()
            _events.trySend(CoreEvent.LogoutSuccess)
        }
    }
}
