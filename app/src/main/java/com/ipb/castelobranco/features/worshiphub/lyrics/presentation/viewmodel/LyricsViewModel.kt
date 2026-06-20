package com.ipb.castelobranco.features.worshiphub.lyrics.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.data.local.SetlistPreferences
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.core.domain.util.normalize
import com.ipb.castelobranco.features.profile.data.snapshot.ProfileSnapshotRepository
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
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val getLyricsUseCase: GetLyricsUseCase,
    private val songsRepository: SongsRepository,
    private val setlistPreferences: SetlistPreferences,
    private val profileSnapshot: ProfileSnapshotRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val pinnedSongs = setlistPreferences.pinnedSongIds
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val profileState = profileSnapshot.observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, profileSnapshot.observe().value)

    private val queryAndAdmin = combine(_query, profileState) { q, p -> q to p }

    fun refresh(minDurationMs: Long = 600L) {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            val start = System.currentTimeMillis()
            runCatching { getLyricsUseCase.refresh() }
                .onFailure { Timber.w(it, "Failed to refresh lyrics") }
            val elapsed = System.currentTimeMillis() - start
            if (elapsed < minDurationMs) delay(minDurationMs - elapsed)
            _isRefreshing.value = false
        }
    }

    val uiState: StateFlow<LyricsUiState> = combine(
        getLyricsUseCase.observe(),
        songsRepository.observeAllSongs(),
        pinnedSongs,
        queryAndAdmin,
    ) { lyricsState, songsState, pinnedSongIds, (query, profileState) ->
        val songMap = (songsState as? SnapshotState.Data)?.value
            .orEmpty()
            .associateBy { it.id }

        val isAdmin = (profileState as? SnapshotState.Data)?.value?.isAdmin == true

        when (lyricsState) {
            is SnapshotState.Loading -> LyricsUiState(isLoading = true, isAdmin = isAdmin)
            is SnapshotState.Error   -> LyricsUiState(error = lyricsState.throwable.message, isAdmin = isAdmin)
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
                else sorted.filter { it.songName.normalize().contains(query.normalize(), ignoreCase = true) }

                LyricsUiState(
                    lyrics         = sorted,
                    filteredLyrics = filtered,
                    query          = query,
                    isAdmin        = isAdmin,
                )
            }
        }
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.Eagerly,
        initialValue = LyricsUiState(isLoading = true),
    )

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onTogglePin(songId: Int) {
        viewModelScope.launch { setlistPreferences.toggleSong(songId) }
    }
}
