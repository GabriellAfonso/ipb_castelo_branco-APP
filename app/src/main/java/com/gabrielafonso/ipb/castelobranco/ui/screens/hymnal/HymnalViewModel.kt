package com.gabrielafonso.ipb.castelobranco.ui.screens.hymnal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.domain.repository.HymnalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HymnalViewModel @Inject constructor(
    private val repository: HymnalRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HymnalViewModel"
    }

    private val _uiState = MutableStateFlow(HymnalUiState())
    val uiState: StateFlow<HymnalUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeHymnal().collect { hymns ->
                _uiState.update { it.copy(hymns = hymns) }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun teste() {
        viewModelScope.launch {
            Log.d(TAG, "só pra testar action")
        }
    }
}
