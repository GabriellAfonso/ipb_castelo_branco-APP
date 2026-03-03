package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.Song
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SuggestedSong
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.TopSong
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.TopTone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongsTableViewModel @Inject constructor(
    private val repository: SongsRepository
) : ViewModel() {

    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs: StateFlow<List<Song>> = _allSongs.asStateFlow()

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
        viewModelScope.launch {
            repository.refreshSongsBySunday()
        }
        viewModelScope.launch {
            repository.observeSongsBySunday().collect { state ->
                if (state is SnapshotState.Data) _lastSundays.value = state.value
            }
        }
        viewModelScope.launch {
            repository.observeTopSongs().collect { state ->
                if (state is SnapshotState.Data) _topSongs.value = state.value
            }
        }
        viewModelScope.launch {
            repository.observeTopTones().collect { state ->
                if (state is SnapshotState.Data) _topTones.value = state.value
            }
        }
        viewModelScope.launch {
            repository.observeSuggestedSongs().collect { state ->
                if (state is SnapshotState.Data) _suggestedSongs.value = state.value
            }
        }
        viewModelScope.launch {
            repository.observeAllSongs().collect { state ->
                if (state is SnapshotState.Data) _allSongs.value = state.value
            }
        }
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


}