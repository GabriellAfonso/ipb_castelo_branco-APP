package com.gabrielafonso.ipb.castelobranco.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _photoUrl = MutableStateFlow<String?>(null)
    val photoUrl: StateFlow<String?> = _photoUrl.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    fun uploadProfilePhoto(bytes: ByteArray, fileName: String = "profile.jpg") {
        viewModelScope.launch {
            if (_isUploading.value) return@launch
            _isUploading.value = true
            _error.value = null

            try {
                val result = repository.uploadProfilePhoto(bytes, fileName)
                result.onSuccess { url -> _photoUrl.value = url }
                    .onFailure { e -> _error.value = e.message ?: "Falha ao enviar imagem" }
            } finally {
                _isUploading.value = false
            }
        }
    }
}
