package com.ipb.castelobranco.features.worshiphub.tables.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.Song
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.SuggestedSong
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.SundaySet
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.TopSong
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.TopTone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongsTableViewModel @Inject constructor(
    private val repository: SongsRepository
) : ViewModel() {

    val allSongs: StateFlow<List<Song>> = repository.observeAllSongs()
        .map { state -> if (state is SnapshotState.Data) state.value else emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun refreshAllSongs() {
        viewModelScope.launch { runCatching { repository.refreshAllSongs() } }
    }

    val lastSundays: StateFlow<List<SundaySet>> = repository.observeSongsBySunday()
        .map { state -> if (state is SnapshotState.Data) state.value else emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val topSongs: StateFlow<List<TopSong>> = repository.observeTopSongs()
        .map { state -> if (state is SnapshotState.Data) state.value else emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val topTones: StateFlow<List<TopTone>> = repository.observeTopTones()
        .map { state -> if (state is SnapshotState.Data) state.value else emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val suggestedSongs: StateFlow<List<SuggestedSong>> = repository.observeSuggestedSongs()
        .map { state -> if (state is SnapshotState.Data) state.value else emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRefreshingSuggestedSongs = MutableStateFlow(false)
    val isRefreshingSuggestedSongs: StateFlow<Boolean> = _isRefreshingSuggestedSongs.asStateFlow()

    private val _fixedByPosition = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val fixedByPosition: StateFlow<Map<Int, Int>> = _fixedByPosition.asStateFlow()

    fun initialize() {
        viewModelScope.launch {
            runCatching { repository.refreshSongsBySunday() }
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
            } catch (_: Exception) {
                // network errors are non-fatal; the observer will surface cached data
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
