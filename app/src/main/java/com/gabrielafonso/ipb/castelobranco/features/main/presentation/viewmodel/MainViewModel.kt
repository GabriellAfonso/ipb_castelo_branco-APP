package com.gabrielafonso.ipb.castelobranco.features.main.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.repository.ScheduleRepository
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.repository.ProfileRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.domain.repository.SongsRepository
import com.gabrielafonso.ipb.castelobranco.features.auth.data.local.AuthSession
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.repository.HymnalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: SongsRepository,
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
        viewModelScope.launch {
            authSession.isLoggedInFlow.collect { logged ->
                _isLoggedIn.value = logged
            }
        }

        preload()
        refreshProfileOnAppOpen()
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

    private fun refreshProfileOnAppOpen() {
        viewModelScope.launch {
            if (!authSession.isLoggedIn()) return@launch

            runCatching {
                val profile = profileRepository.getMeProfile().getOrThrow()
                val url = profile.photoUrl
                if (!url.isNullOrBlank()) {
                    profileRepository.downloadAndPersistProfilePhoto(url).getOrThrow()
                }
            }
        }
    }

    private fun preload() {
        viewModelScope.launch {
            _isPreloading.value = true
            try {
                withContext(Dispatchers.IO) {
                    supervisorScope {
                        val jobs = listOf(
                            async { repository.refreshAllSongs() to "refreshAllSongs" },
                            async { repository.refreshSongsBySunday() to "refreshSongsBySunday" },
                            async { repository.refreshTopSongs() to "refreshTopSongs" },
                            async { repository.refreshTopTones() to "refreshTopTones" },
                            async { repository.refreshSuggestedSongs() to "refreshSuggestedSongs" },
                            async { hymnalRepository.refreshHymnal() to "refreshHymnal" },
                            async { scheduleRepository.refreshMonthSchedule() to "refreshMonthSchedule" }
                        )

                        jobs.forEach { deferred ->
                            runCatching { deferred.await() }
                        }
                    }
                }
            } finally {
                _isPreloading.value = false
            }
        }
    }
}