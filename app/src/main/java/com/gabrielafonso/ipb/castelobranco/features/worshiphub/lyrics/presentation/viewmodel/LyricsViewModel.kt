package com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.domain.usecase.GetLyricsUseCase
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.state.LyricsListItem
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.state.LyricsUiState
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val getLyricsUseCase: GetLyricsUseCase,
    private val songsRepository: SongsRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val uiState: StateFlow<LyricsUiState> = combine(
        getLyricsUseCase.observe(),
        songsRepository.observeAllSongs(),
        _query,
    ) { lyricsState, songsState, query ->
        val songMap = (songsState as? SnapshotState.Data)?.value
            .orEmpty()
            .associateBy { it.id }

        when (lyricsState) {
            is SnapshotState.Loading -> LyricsUiState(isLoading = true)
            is SnapshotState.Error   -> LyricsUiState(error = lyricsState.throwable.message)
            is SnapshotState.Data    -> {
                val items = lyricsState.value.map { lyrics ->
                    LyricsListItem(
                        id       = lyrics.id,
                        songName = songMap[lyrics.songId]?.title ?: "Song #${lyrics.songId}",
                    )
                }
                val filtered = if (query.isBlank()) items
                else items.filter { it.songName.contains(query, ignoreCase = true) }

                LyricsUiState(
                    lyrics         = items,
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
}
