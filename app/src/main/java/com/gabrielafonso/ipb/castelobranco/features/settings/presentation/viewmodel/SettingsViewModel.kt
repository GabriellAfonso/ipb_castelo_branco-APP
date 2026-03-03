package com.gabrielafonso.ipb.castelobranco.features.settings.presentation.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import com.gabrielafonso.ipb.castelobranco.features.settings.domain.model.ThemeMode
import com.gabrielafonso.ipb.castelobranco.features.settings.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val darkMode: Boolean? = null,
    val themeMode: ThemeMode = ThemeMode.FOLLOW_SYSTEM
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val galleryRepository: GalleryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    private val _events = MutableSharedFlow<Unit>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.themeModeFlow.collect { mode ->
                val dark: Boolean? = when (mode) {
                    ThemeMode.FOLLOW_SYSTEM -> null
                    ThemeMode.DARK -> true
                    ThemeMode.LIGHT -> false
                }
                _uiState.value = _uiState.value.copy(
                    themeMode = mode,
                    darkMode = dark
                )
            }
        }
    }

    fun clearGallery() {
        viewModelScope.launch {
            galleryRepository.clearAllPhotos()
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val currentMode = _uiState.value.themeMode

            val isCurrentlyDark: Boolean = when (currentMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.FOLLOW_SYSTEM -> {
                    // FOLLOW_SYSTEM: inferir do modo atual aplicado no app
                    when (AppCompatDelegate.getDefaultNightMode()) {
                        AppCompatDelegate.MODE_NIGHT_YES -> true
                        AppCompatDelegate.MODE_NIGHT_NO -> false
                        else -> false
                    }
                }
            }

            val newMode: ThemeMode = if (isCurrentlyDark) {
                ThemeMode.LIGHT
            } else {
                ThemeMode.DARK
            }

            repository.setThemeMode(newMode)

            val newNightMode = when (newMode) {
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(newNightMode)

            _events.emit(Unit)
        }
    }
}