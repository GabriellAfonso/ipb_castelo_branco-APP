// app/src/main/java/com/gabrielafonso/ipb/castelobranco/ui/screens/main/MainViewModel.kt
package com.gabrielafonso.ipb.castelobranco.ui.screens.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.data.local.AuthSession
import com.gabrielafonso.ipb.castelobranco.domain.repository.HymnalRepository
import com.gabrielafonso.ipb.castelobranco.domain.repository.MonthScheduleRepository
import com.gabrielafonso.ipb.castelobranco.domain.repository.SongsRepository
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
    private val monthScheduleRepository: MonthScheduleRepository,
    private val authSession: AuthSession
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }
    sealed interface MainEvent {
        data object LogoutSuccess : MainEvent
    }
    private val _events = Channel<MainEvent>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private val _isPreloading = MutableStateFlow(false)
    val isPreloading: StateFlow<Boolean> = _isPreloading.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        refreshLoginState()
        preload()
    }

    fun refreshLoginState() {
        viewModelScope.launch {
            _isLoggedIn.value = authSession.isLoggedIn()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authSession.logout()
            _isLoggedIn.value = false
            _events.trySend(MainEvent.LogoutSuccess)
        }
    }

    private fun preload() {
        viewModelScope.launch {
            _isPreloading.value = true
            try {
                withContext(Dispatchers.IO) {
                    supervisorScope {
                        val jobs = listOf(
                            async { repository.refreshSongsBySunday() to "refreshSongsBySunday" },
                            async { repository.refreshTopSongs() to "refreshTopSongs" },
                            async { repository.refreshTopTones() to "refreshTopTones" },
                            async { repository.refreshSuggestedSongs() to "refreshSuggestedSongs" },
                            async { hymnalRepository.refreshHymnal() to "refreshHymnal" },
                            async { monthScheduleRepository.refreshMonthSchedule() to "refreshMonthSchedule" }
                        )

                        jobs.forEach { deferred ->
                            try {
                                val (ok, name) = deferred.await()
                                if (!ok) Log.w(TAG, "$name falhou") else Log.d(TAG, "$name sucesso")
                            } catch (e: Exception) {
                                Log.e(TAG, "Erro ao executar refresh paralelo", e)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "preload geral falhou", e)
            } finally {
                _isPreloading.value = false
            }
        }
    }
}
