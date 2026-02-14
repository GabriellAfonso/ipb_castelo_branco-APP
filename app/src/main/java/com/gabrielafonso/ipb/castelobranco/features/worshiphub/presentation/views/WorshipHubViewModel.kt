package com.gabrielafonso.ipb.castelobranco.features.worshiphub.presentation.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.Song
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.SuggestedSong
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.SundayPlayPushItem
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.TopSong
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.model.TopTone
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.domain.repository.SongsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorshipHubViewModel @Inject constructor(
    private val repository: SongsRepository
) : ViewModel() {

    val allSongs: StateFlow<List<Song>> =
        repository.observeAllSongs()
            .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000), emptyList())

    fun refreshAllSongs() {
        viewModelScope.launch { repository.refreshAllSongs() }
    }

    private val _lastSundays = MutableStateFlow<List<SundaySet>>(emptyList())
    val lastSundays: StateFlow<List<SundaySet>> = _lastSundays.asStateFlow()

    private val _topSongs = MutableStateFlow<List<TopSong>>(emptyList())
    val topSongs: StateFlow<List<TopSong>> = _topSongs.asStateFlow()

    private val _topTones = MutableStateFlow<List<TopTone>>(emptyList())
    val topTones: StateFlow<List<TopTone>> = _topTones.asStateFlow()

    private val _suggestedSongs = MutableStateFlow<List<SuggestedSong>>(emptyList())
    val suggestedSongs: StateFlow<List<SuggestedSong>> = _suggestedSongs.asStateFlow()

    private val _isRefreshingSuggestedSongs = MutableStateFlow(false)
    val isRefreshingSuggestedSongs: StateFlow<Boolean> = _isRefreshingSuggestedSongs.asStateFlow()

    private val _fixedByPosition = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val fixedByPosition: StateFlow<Map<Int, Int>> = _fixedByPosition.asStateFlow()

    init {
        viewModelScope.launch { repository.observeSongsBySunday().collect { _lastSundays.value = it } }
        viewModelScope.launch { repository.observeTopSongs().collect { _topSongs.value = it } }
        viewModelScope.launch { repository.observeTopTones().collect { _topTones.value = it } }
        viewModelScope.launch { repository.observeSuggestedSongs().collect { _suggestedSongs.value = it } }
    }

    fun toggleFixed(song: SuggestedSong) {
        val pos = song.position
        val playedId = song.id

        _fixedByPosition.value = _fixedByPosition.value.toMutableMap().apply {
            val current = this[pos]
            if (current == playedId) remove(pos) else put(pos, playedId)
        }.toMap()
    }

    fun refreshSuggestedSongs(minDurationMs: Long = 600L) {
        viewModelScope.launch {
            if (_isRefreshingSuggestedSongs.value) return@launch
            _isRefreshingSuggestedSongs.value = true

            try {
                val fixed = _fixedByPosition.value
                val refreshJob = async { repository.refreshSuggestedSongs(fixed) }
                val minTimeJob = async { delay(minDurationMs) }

                refreshJob.await()
                minTimeJob.await()
            } finally {
                _isRefreshingSuggestedSongs.value = false
            }
        }
    }

    sealed class SubmitResult {
        data object Success : SubmitResult()
        data class Error(val message: String) : SubmitResult()
    }

    fun submitSundayPlays(
        date: String,
        rows: List<SundayPlayPushItem>,
        onResult: (SubmitResult) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.pushSundayPlays(date = date, plays = rows)
                onResult(SubmitResult.Success)
            } catch (t: Throwable) {
                val msg =
                    t.message?.trim().takeIf { !it.isNullOrBlank() }
                        ?: "Erro inesperado ao enviar."
                onResult(SubmitResult.Error(msg))
            }
        }
    }
}