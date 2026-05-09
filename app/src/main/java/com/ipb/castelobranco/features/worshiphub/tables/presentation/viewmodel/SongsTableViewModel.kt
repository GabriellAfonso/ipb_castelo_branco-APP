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
    val tone: String = "",
    val isFixed: Boolean = false
)

@HiltViewModel
class SongsTableViewModel @Inject constructor(
    private val repository: SongsRepository
) : ViewModel() {

    val allSongs: StateFlow<List<Song>> = repository.observeAllSongs()
        .map { state -> if (state is SnapshotState.Data) state.value else emptyList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun refreshAllSongs() {
        viewModelScope.launch { runCatching { repository.refreshAllSongs() } }
    }

    val lastSundays: StateFlow<SnapshotState<List<SundaySet>>> = repository.observeSongsBySunday()
        .stateIn(viewModelScope, SharingStarted.Eagerly, SnapshotState.Loading)

    val topSongs: StateFlow<SnapshotState<List<TopSong>>> = repository.observeTopSongs()
        .stateIn(viewModelScope, SharingStarted.Eagerly, SnapshotState.Loading)

    val topTones: StateFlow<SnapshotState<List<TopTone>>> = repository.observeTopTones()
        .stateIn(viewModelScope, SharingStarted.Eagerly, SnapshotState.Loading)

    val suggestedSongs: StateFlow<SnapshotState<List<SuggestedSong>>> = repository.observeSuggestedSongs()
        .stateIn(viewModelScope, SharingStarted.Eagerly, SnapshotState.Loading)

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
        val autoTone = song?.let { mostUsedToneFor(it) } ?: ""
        _repertoireRows.update { rows ->
            rows.map { row ->
                if (row.position == position) {
                    row.copy(
                        selectedSong = song,
                        tone = autoTone,
                        isFixed = if (song == null) false else row.isFixed
                    )
                } else row
            }
        }
    }

    fun onToneChange(position: Int, tone: String) {
        _repertoireRows.update { rows ->
            rows.map { row ->
                if (row.position == position) row.copy(tone = tone) else row
            }
        }
    }

    fun toggleFixed(position: Int) {
        _repertoireRows.update { rows ->
            rows.map { row ->
                if (row.position == position && row.selectedSong != null) {
                    row.copy(isFixed = !row.isFixed)
                } else row
            }
        }
    }

    private fun mostUsedToneFor(song: Song): String {
        val sundays = (lastSundays.value as? SnapshotState.Data)?.value ?: return ""
        return sundays
            .flatMap { it.songs }
            .filter { it.title == song.title && it.artist == song.artist }
            .takeIf { it.isNotEmpty() }
            ?.groupingBy { it.tone }
            ?.eachCount()
            ?.maxByOrNull { it.value }
            ?.key
            .orEmpty()
    }

    fun refreshSuggestedSongs(minDurationMs: Long = 600L) {
        viewModelScope.launch {
            if (_isRefreshingSuggestedSongs.value) return@launch
            _isRefreshingSuggestedSongs.value = true

            try {
                val fixed = _repertoireRows.value
                    .filter { it.isFixed && it.selectedSong != null }
                    .associate { it.position to it.selectedSong!!.id }

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
                if (row.isFixed) return@map row
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
