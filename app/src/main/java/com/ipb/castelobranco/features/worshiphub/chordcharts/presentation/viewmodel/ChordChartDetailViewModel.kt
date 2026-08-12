package com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipb.castelobranco.core.data.local.SongScrollMode
import com.ipb.castelobranco.core.data.local.ThemePreferences
import com.ipb.castelobranco.core.domain.error.toAppError
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.core.presentation.error.toUserMessage
import com.ipb.castelobranco.features.profile.data.snapshot.ProfileSnapshotRepository
import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.repository.ChordChartRepository
import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.usecase.GetChordChartsUseCase
import com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.parser.ChordProParser
import com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.state.ChordChartDetailUiState
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
class ChordChartDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChordChartsUseCase: GetChordChartsUseCase,
    private val chordChartRepository: ChordChartRepository,
    private val songsRepository: SongsRepository,
    private val themePreferences: ThemePreferences,
    private val profileSnapshot: ProfileSnapshotRepository,
) : ViewModel() {

    private val chordChartId: Int = checkNotNull(savedStateHandle["chordChartId"])

    private val _editState = MutableStateFlow(EditState())

    val uiState: StateFlow<ChordChartDetailUiState> = combine(
        getChordChartsUseCase.observe(),
        songsRepository.observeAllSongs(),
        profileSnapshot.observe(),
        _editState,
    ) { chartsState, songsState, profileState, editState ->
        val songMap = (songsState as? SnapshotState.Data)?.value
            .orEmpty()
            .associateBy { it.id }

        val isAdmin = (profileState as? SnapshotState.Data)?.value?.isAdmin == true

        when (chartsState) {
            is SnapshotState.Loading -> ChordChartDetailUiState(isLoading = true, isAdmin = isAdmin)
            is SnapshotState.Error   -> ChordChartDetailUiState(
                error = chartsState.error.toUserMessage(), isAdmin = isAdmin,
            )
            is SnapshotState.Data    -> {
                val chart = chartsState.value.find { it.id == chordChartId }
                    ?: return@combine ChordChartDetailUiState(error = "Chart not found")

                ChordChartDetailUiState(
                    songName    = songMap[chart.songId]?.title ?: "Song #${chart.songId}",
                    tone        = chart.tone,
                    blocks      = ChordProParser.parse(chart.content),
                    rawContent  = chart.content,
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
        initialValue = ChordChartDetailUiState(isLoading = true),
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
            chordChartRepository.updateContent(chordChartId, _editState.value.editContent)
                .onSuccess { _editState.update { EditState() } }
                .onFailure { e ->
                    _editState.update { it.copy(isSaving = false, saveError = e.toAppError().toUserMessage()) }
                }
        }
    }
}
