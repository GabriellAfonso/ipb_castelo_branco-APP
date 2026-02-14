package com.gabrielafonso.ipb.castelobranco.features.settings.presentation.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import com.gabrielafonso.ipb.castelobranco.features.settings.domain.repository.SettingsRepository

data class SettingsUiState(val darkMode: Boolean? = null)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    // evento para sinalizar conclusão da alteração
    private val _events = MutableSharedFlow<Unit>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.darkModeFlow.collect { dark ->
                _uiState.value = _uiState.value.copy(darkMode = dark)
            }
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val current = _uiState.value.darkMode ?: run {
                when (AppCompatDelegate.getDefaultNightMode()) {
                    AppCompatDelegate.MODE_NIGHT_YES -> true
                    AppCompatDelegate.MODE_NIGHT_NO -> false
                    else -> false
                }
            }
            val new = !current
            repository.setDarkMode(new)

            val newMode = if (new) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(newMode)

            // emitir evento após aplicar tudo
            _events.emit(Unit)
        }
    }
}
