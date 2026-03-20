package com.ipb.castelobranco.features.worshiphub.lyrics.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.lyrics.domain.usecase.GetLyricsUseCase
import com.ipb.castelobranco.features.worshiphub.lyrics.presentation.parser.LyricsParser
import com.ipb.castelobranco.features.worshiphub.lyrics.presentation.state.LyricsDetailUiState
import com.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LyricsDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLyricsUseCase: GetLyricsUseCase,
    private val songsRepository: SongsRepository,
) : ViewModel() {

    private val lyricsId: Int = checkNotNull(savedStateHandle["lyricsId"])

    val uiState: StateFlow<LyricsDetailUiState> = combine(
        getLyricsUseCase.observe(),
        songsRepository.observeAllSongs(),
    ) { lyricsState, songsState ->
        val songMap = (songsState as? SnapshotState.Data)?.value
            .orEmpty()
            .associateBy { it.id }

        when (lyricsState) {
            is SnapshotState.Loading -> LyricsDetailUiState(isLoading = true)
            is SnapshotState.Error   -> LyricsDetailUiState(error = lyricsState.throwable.message)
            is SnapshotState.Data    -> {
                val lyrics = lyricsState.value.find { it.id == lyricsId }
                    ?: return@combine LyricsDetailUiState(error = "Lyrics not found")

                LyricsDetailUiState(
                    songName = songMap[lyrics.songId]?.title ?: "Song #${lyrics.songId}",
                    stanzas  = LyricsParser.parse(lyrics.content),
                )
            }
        }
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = LyricsDetailUiState(isLoading = true),
    )
}
