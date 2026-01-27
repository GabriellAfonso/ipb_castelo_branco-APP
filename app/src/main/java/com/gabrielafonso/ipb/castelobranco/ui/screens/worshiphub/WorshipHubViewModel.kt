// app/src/main/java/com/gabrielafonso/ipb/castelobranco/ui/screens/worshiphub/WorshipHubViewModel.kt
package com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.gabrielafonso.ipb.castelobranco.domain.repository.SongsRepository
import com.gabrielafonso.ipb.castelobranco.domain.model.SuggestedSong
import com.gabrielafonso.ipb.castelobranco.domain.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.domain.model.TopSong
import com.gabrielafonso.ipb.castelobranco.domain.model.TopTone
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class WorshipHubViewModel(
    private val repository: SongsRepository
) : ViewModel() {

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

    init {
        viewModelScope.launch { repository.observeSongsBySunday().collect { _lastSundays.value = it } }
        viewModelScope.launch { repository.observeTopSongs().collect { _topSongs.value = it } }
        viewModelScope.launch { repository.observeTopTones().collect { _topTones.value = it } }
        viewModelScope.launch { repository.observeSuggestedSongs().collect { _suggestedSongs.value = it } }
    }

    fun refreshSuggestedSongs(minDurationMs: Long = 1_000L) {
        viewModelScope.launch {
            if (_isRefreshingSuggestedSongs.value) return@launch
            _isRefreshingSuggestedSongs.value = true

            try {
                val refreshJob = async { repository.refreshSuggestedSongs() }
                val minTimeJob = async { delay(minDurationMs) }

                refreshJob.await()
                minTimeJob.await()
            } finally {
                _isRefreshingSuggestedSongs.value = false
            }
        }
    }
}
