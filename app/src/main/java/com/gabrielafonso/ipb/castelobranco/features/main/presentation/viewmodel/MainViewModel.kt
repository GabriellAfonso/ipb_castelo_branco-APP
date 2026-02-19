package com.gabrielafonso.ipb.castelobranco.features.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.auth.data.local.AuthSession
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.repository.HymnalRepository
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.repository.ProfileRepository
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.repository.ScheduleRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val songsRepository: SongsRepository,
    private val hymnalRepository: HymnalRepository,
    private val scheduleRepository: ScheduleRepository,
    private val authSession: AuthSession,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    sealed interface MainEvent {
        data object LogoutSuccess : MainEvent
    }

    private val _events = Channel<MainEvent>(capacity = Channel.Factory.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _isPreloading = MutableStateFlow(false)
    val isPreloading: StateFlow<Boolean> = _isPreloading.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        // Observa estado de login
        viewModelScope.launch {
            authSession.isLoggedInFlow.collect { logged ->
                _isLoggedIn.value = logged
            }
        }

        // Inicialização em cascata: Primeiro Cache (Rápido), depois Rede (Lento)
        startAppInitialization()
    }

    private fun startAppInitialization() {
        viewModelScope.launch {
            _isPreloading.value = true

            // 1. CARREGAMENTO EM MEMÓRIA (Preload)
            // Lê o disco e joga para o StateFlow dos Repositories
            // Isso mata o lag de abertura das views
            preloadCachesFromDisk()

            // 2. ATUALIZAÇÃO DE REDE (Refresh)
            // Agora que a UI já tem o que mostrar (cache), buscamos o novo em background
            refreshDataFromNetwork()

            // Perfil é um caso à parte pois depende de login
            refreshProfileOnAppOpen()

            _isPreloading.value = false
        }
    }

    private suspend fun preloadCachesFromDisk() = withContext(Dispatchers.IO) {
        supervisorScope {
            val preloads = listOf(
//                launch { songsRepository.preload() },
//                launch { hymnalRepository.preload() },
                launch { scheduleRepository.preload() },
//                launch { profileRepository.preload() }
            )
            preloads.joinAll()
        }
    }

    private suspend fun refreshDataFromNetwork() = withContext(Dispatchers.IO) {
        supervisorScope {
            // Usamos async para não travar um refresh se o outro falhar
            val jobs = listOf(
                async { songsRepository.refreshAllSongs() },
                async { songsRepository.refreshSongsBySunday() },
                async { songsRepository.refreshTopSongs() },
                async { songsRepository.refreshTopTones() },
                async { songsRepository.refreshSuggestedSongs() },
                async { hymnalRepository.refreshHymnal() },
                async { scheduleRepository.refreshMonthSchedule() }
            )

            jobs.forEach { deferred ->
                runCatching { deferred.await() }
            }
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
            authSession.logout()
            _events.trySend(MainEvent.LogoutSuccess)
        }
    }
}