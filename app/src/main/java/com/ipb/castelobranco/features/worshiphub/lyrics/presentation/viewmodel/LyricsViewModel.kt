package com.ipb.castelobranco.features.worshiphub.lyrics.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.data.local.SetlistPreferences
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.lyrics.domain.usecase.GetLyricsUseCase
import com.ipb.castelobranco.features.worshiphub.lyrics.presentation.state.LyricsListItem
import com.ipb.castelobranco.features.worshiphub.lyrics.presentation.state.LyricsUiState
import com.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val getLyricsUseCase: GetLyricsUseCase,
    private val songsRepository: SongsRepository,
    private val setlistPreferences: SetlistPreferences,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh(minDurationMs: Long = 600L) {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            val start = System.currentTimeMillis()
            runCatching { getLyricsUseCase.refresh() }
            val elapsed = System.currentTimeMillis() - start
            if (elapsed < minDurationMs) delay(minDurationMs - elapsed)
            _isRefreshing.value = false
        }
    }

    val uiState: StateFlow<LyricsUiState> = combine(
        getLyricsUseCase.observe(),
        songsRepository.observeAllSongs(),
        setlistPreferences.pinnedSongIds,
        _query,
    ) { lyricsState, songsState, pinnedSongIds, query ->
        val songMap = (songsState as? SnapshotState.Data)?.value
            .orEmpty()
            .associateBy { it.id }

        when (lyricsState) {
            is SnapshotState.Loading -> LyricsUiState(isLoading = true)
            is SnapshotState.Error   -> LyricsUiState(error = lyricsState.throwable.message)
            is SnapshotState.Data    -> {
                val pinOrder = pinnedSongIds.withIndex().associate { (index, songId) -> songId to index }
                val sorted = lyricsState.value.map { lyrics ->
                    LyricsListItem(
                        id       = lyrics.id,
                        songId   = lyrics.songId,
                        songName = songMap[lyrics.songId]?.title ?: "Song #${lyrics.songId}",
                        isPinned = lyrics.songId in pinOrder,
                    )
                }.sortedBy { pinOrder[it.songId] ?: Int.MAX_VALUE }

                val filtered = if (query.isBlank()) sorted
                else sorted.filter { it.songName.contains(query, ignoreCase = true) }

                LyricsUiState(
                    lyrics         = sorted,
                    filteredLyrics = filtered,
                    query          = query,
                )
            }
        }
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = LyricsUiState(isLoading = true),
    )

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onTogglePin(songId: Int) {
        viewModelScope.launch { setlistPreferences.toggleSong(songId) }
    }
}
