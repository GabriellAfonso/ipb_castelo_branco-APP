package com.ipb.castelobranco.features.worshiphub.songs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.core.domain.util.normalize
import com.ipb.castelobranco.features.worshiphub.songs.presentation.state.SongListItem
import com.ipb.castelobranco.features.worshiphub.songs.presentation.state.SongsListUiState
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
class SongsListViewModel @Inject constructor(
    private val songsRepository: SongsRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh(minDurationMs: Long = 600L) {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            val start = System.currentTimeMillis()
            runCatching { songsRepository.refreshAllSongs() }
            val elapsed = System.currentTimeMillis() - start
            if (elapsed < minDurationMs) delay(minDurationMs - elapsed)
            _isRefreshing.value = false
        }
    }

    val uiState: StateFlow<SongsListUiState> = combine(
        songsRepository.observeAllSongs(),
        _query,
    ) { songsState, query ->
        when (songsState) {
            is SnapshotState.Loading -> SongsListUiState(isLoading = true)
            is SnapshotState.Error   -> SongsListUiState(error = songsState.throwable.message)
            is SnapshotState.Data    -> {
                val all = songsState.value.map { song ->
                    SongListItem(
                        id     = song.id,
                        title  = song.title,
                        artist = song.artist,
                    )
                }

                val filtered = if (query.isBlank()) all
                else {
                    val normalizedQuery = query.normalize()
                    all.filter {
                        it.title.normalize().contains(normalizedQuery, ignoreCase = true) ||
                            it.artist.normalize().contains(normalizedQuery, ignoreCase = true)
                    }
                }

                SongsListUiState(
                    songs         = all,
                    filteredSongs = filtered,
                    query         = query,
                )
            }
        }
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.Eagerly,
        initialValue = SongsListUiState(isLoading = true),
    )

    fun onQueryChange(query: String) {
        _query.value = query
    }
}
