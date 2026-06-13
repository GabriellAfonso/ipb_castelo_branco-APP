package com.ipb.castelobranco.features.worshiphub.lyrics.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.data.local.SongScrollMode
import com.ipb.castelobranco.core.data.local.ThemePreferences
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.profile.data.snapshot.ProfileSnapshotRepository
import com.ipb.castelobranco.features.worshiphub.lyrics.domain.repository.LyricsRepository
import com.ipb.castelobranco.features.worshiphub.lyrics.domain.usecase.GetLyricsUseCase
import com.ipb.castelobranco.features.worshiphub.lyrics.presentation.parser.LyricsParser
import com.ipb.castelobranco.features.worshiphub.lyrics.presentation.state.LyricsDetailUiState
import com.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class EditState(
    val isEditing: Boolean = false,
    val editContent: String = "",
    val isSaving: Boolean = false,
    val saveError: String? = null,
)

@HiltViewModel
class LyricsDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLyricsUseCase: GetLyricsUseCase,
    private val lyricsRepository: LyricsRepository,
    private val songsRepository: SongsRepository,
    private val themePreferences: ThemePreferences,
    private val profileSnapshot: ProfileSnapshotRepository,
) : ViewModel() {

    private val lyricsId: Int = checkNotNull(savedStateHandle["lyricsId"])

    private val _editState = MutableStateFlow(EditState())

    val uiState: StateFlow<LyricsDetailUiState> = combine(
        getLyricsUseCase.observe(),
        songsRepository.observeAllSongs(),
        profileSnapshot.observe(),
        _editState,
    ) { lyricsState, songsState, profileState, editState ->
        val songMap = (songsState as? SnapshotState.Data)?.value
            .orEmpty()
            .associateBy { it.id }

        val isAdmin = (profileState as? SnapshotState.Data)?.value?.isAdmin == true

        when (lyricsState) {
            is SnapshotState.Loading -> LyricsDetailUiState(isLoading = true, isAdmin = isAdmin)
            is SnapshotState.Error   -> LyricsDetailUiState(
                error = lyricsState.throwable.message, isAdmin = isAdmin,
            )
            is SnapshotState.Data    -> {
                val lyrics = lyricsState.value.find { it.id == lyricsId }
                    ?: return@combine LyricsDetailUiState(error = "Lyrics not found")

                LyricsDetailUiState(
                    songName    = songMap[lyrics.songId]?.title ?: "Song #${lyrics.songId}",
                    stanzas     = LyricsParser.parse(lyrics.content),
                    rawContent  = lyrics.content,
                    isAdmin     = isAdmin,
                    isEditing   = editState.isEditing,
                    editContent = editState.editContent,
                    isSaving    = editState.isSaving,
                    saveError   = editState.saveError,
                )
            }
        }
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.Eagerly,
        initialValue = LyricsDetailUiState(isLoading = true),
    )

    val scrollMode: StateFlow<SongScrollMode> = themePreferences.songScrollModeFlow.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.Eagerly,
        initialValue = SongScrollMode.HORIZONTAL,
    )

    fun toggleScrollMode() {
        viewModelScope.launch {
            val next = when (scrollMode.value) {
                SongScrollMode.HORIZONTAL -> SongScrollMode.VERTICAL
                SongScrollMode.VERTICAL   -> SongScrollMode.HORIZONTAL
            }
            themePreferences.setSongScrollMode(next)
        }
    }

    fun enterEditMode() {
        _editState.update { it.copy(isEditing = true, editContent = uiState.value.rawContent, saveError = null) }
    }

    fun onEditContentChange(text: String) {
        _editState.update { it.copy(editContent = text) }
    }

    fun cancelEdit() {
        _editState.update { EditState() }
    }

    fun saveEdit() {
        viewModelScope.launch {
            _editState.update { it.copy(isSaving = true, saveError = null) }
            lyricsRepository.updateContent(lyricsId, _editState.value.editContent)
                .onSuccess { _editState.update { EditState() } }
                .onFailure { e -> _editState.update { it.copy(isSaving = false, saveError = e.message) } }
        }
    }
}
