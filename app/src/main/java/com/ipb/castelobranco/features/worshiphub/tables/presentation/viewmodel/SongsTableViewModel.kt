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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RepertoireRowState(
    val position: Int,
    val selectedSong: Song? = null,
    val tone: String = ""
)

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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refreshCurrentTab(tabIndex: Int, minDurationMs: Long = 600L) {
        if (tabIndex == 3) return // Repertório has its own generate button
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            val start = System.currentTimeMillis()
            runCatching {
                when (tabIndex) {
                    0 -> repository.refreshSongsBySunday()
                    1 -> repository.refreshTopSongs()
                    2 -> repository.refreshTopTones()
                }
            }
            val elapsed = System.currentTimeMillis() - start
            if (elapsed < minDurationMs) delay(minDurationMs - elapsed)
            _isRefreshing.value = false
        }
    }

    private val _repertoireRows = MutableStateFlow(
        (1..4).map { RepertoireRowState(position = it) }
    )
    val repertoireRows: StateFlow<List<RepertoireRowState>> = _repertoireRows.asStateFlow()

    fun initialize() {
        viewModelScope.launch {
            runCatching { repository.refreshSongsBySunday() }
        }
        refreshAllSongs()
    }

    fun selectSong(position: Int, song: Song?) {
        _repertoireRows.update { rows ->
            rows.map { row ->
                if (row.position == position) row.copy(selectedSong = song, tone = "") else row
            }
        }
    }

    fun refreshSuggestedSongs(minDurationMs: Long = 600L) {
        viewModelScope.launch {
            if (_isRefreshingSuggestedSongs.value) return@launch
            _isRefreshingSuggestedSongs.value = true

            try {
                val fixed = _repertoireRows.value
                    .mapNotNull { row -> row.selectedSong?.let { row.position to it.id } }
                    .toMap()

                val refreshJob = async { repository.refreshSuggestedSongs(fixed) }
                val minTimeJob = async { delay(minDurationMs) }

                refreshJob.await()
                minTimeJob.await()

                syncRepertoireFromSuggestions()
            } catch (_: Exception) {
                // network errors are non-fatal; the observer will surface cached data
            } finally {
                _isRefreshingSuggestedSongs.value = false
            }
        }
    }

    private suspend fun syncRepertoireFromSuggestions() {
        val suggestionsState = repository.observeSuggestedSongs().first()
        val suggestions = (suggestionsState as? SnapshotState.Data)?.value ?: return
        val songs = (repository.observeAllSongs().first() as? SnapshotState.Data)?.value ?: emptyList()

        _repertoireRows.update { rows ->
            rows.map { row ->
                val suggestion = suggestions.find { it.position == row.position }
                if (suggestion != null) {
                    val song = songs.find { it.id == suggestion.songId }
                        ?: row.selectedSong
                    row.copy(selectedSong = song, tone = suggestion.tone)
                } else {
                    row
                }
            }
        }
    }

    sealed class SubmitResult {
        data object Success : SubmitResult()
        data class Error(val message: String) : SubmitResult()
    }


}
