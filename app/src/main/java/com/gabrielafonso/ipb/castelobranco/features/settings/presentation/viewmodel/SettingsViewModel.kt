package com.gabrielafonso.ipb.castelobranco.features.settings.presentation.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.core.data.local.ThemePreferences
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
    val themeMode: Int = ThemePreferences.MODE_FOLLOW_SYSTEM
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    private val _events = MutableSharedFlow<Unit>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.themeModeFlow.collect { mode ->
                val dark: Boolean? = when (mode) {
                    ThemePreferences.MODE_FOLLOW_SYSTEM -> null
                    ThemePreferences.MODE_DARK -> true
                    else -> false
                }
                _uiState.value = _uiState.value.copy(
                    themeMode = mode,
                    darkMode = dark
                )
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val currentMode = _uiState.value.themeMode

            val isCurrentlyDark: Boolean = when (currentMode) {
                ThemePreferences.MODE_DARK -> true
                ThemePreferences.MODE_LIGHT -> false
                else -> {
                    // FOLLOW_SYSTEM: inferir do modo atual aplicado no app
                    when (AppCompatDelegate.getDefaultNightMode()) {
                        AppCompatDelegate.MODE_NIGHT_YES -> true
                        AppCompatDelegate.MODE_NIGHT_NO -> false
                        else -> false
                    }
                }
            }

            val newMode = if (isCurrentlyDark) {
                ThemePreferences.MODE_LIGHT
            } else {
                ThemePreferences.MODE_DARK
            }

            repository.setThemeMode(newMode)

            val newNightMode = when (newMode) {
                ThemePreferences.MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                ThemePreferences.MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(newNightMode)

            _events.emit(Unit)
        }
    }
}