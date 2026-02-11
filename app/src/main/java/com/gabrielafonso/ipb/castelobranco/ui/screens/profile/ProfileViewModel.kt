// app/src/main/java/com/gabrielafonso/ipb/castelobranco/ui/screens/profile/ProfileViewModel.kt
package com.gabrielafonso.ipb.castelobranco.ui.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _photoUrl = MutableStateFlow<String?>(null)
    val photoUrl: StateFlow<String?> = _photoUrl.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _localPhotoPath = MutableStateFlow<String?>(null)
    val localPhotoPath: StateFlow<String?> = _localPhotoPath.asStateFlow()

    private val _localPhotoVersion = MutableStateFlow(0)
    val localPhotoVersion: StateFlow<Int> = _localPhotoVersion.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    // “ativo/desativo” vem de MeProfileDto.active
    private val _profileActive = MutableStateFlow<Boolean?>(null)
    val memberActive: StateFlow<Boolean?> = _profileActive.asStateFlow()

    // Se quiser usar na UI (ex.: “Membro: Sim/Não”), vem de MeProfileDto.isMember
    private val _isMember = MutableStateFlow<Boolean?>(null)
    val isMember: StateFlow<Boolean?> = _isMember.asStateFlow()

    init {
        refreshLocalPhotoPathAndBump()

        refreshFromServer()
    }

    fun clearError() {
        _error.value = null
    }

    private fun refreshLocalPhotoPathAndBump() {
        val dir = File(context.filesDir, "profile")
        val file = dir.listFiles()
            ?.firstOrNull { it.isFile && it.name.startsWith("profile_photo.") && it.length() > 0L }

        _localPhotoPath.value = file?.absolutePath
        _localPhotoVersion.value = _localPhotoVersion.value + 1
    }

    fun refreshFromServer() {
        viewModelScope.launch {
            _error.value = null
            try {
                val profile = repository.getMeProfile().getOrThrow()

                _userName.value = profile.name.trim().ifBlank { "Usuário" }
                _profileActive.value = profile.active
                _isMember.value = profile.isMember

                _photoUrl.value = profile.photoUrl
                val url = profile.photoUrl

                if (!url.isNullOrBlank()) {
                    repository.downloadAndPersistProfilePhoto(url).getOrThrow()
                }

                refreshLocalPhotoPathAndBump()
            } catch (t: Throwable) {
                _error.value = t.message ?: "Falha ao atualizar perfil"
                refreshLocalPhotoPathAndBump()
            }
        }
    }

    fun uploadProfilePhoto(bytes: ByteArray, fileName: String = "profile.jpg") {
        viewModelScope.launch {
            if (_isUploading.value) return@launch
            _isUploading.value = true
            _error.value = null

            try {
                repository.uploadProfilePhoto(bytes, fileName).getOrThrow()

                val profile = repository.getMeProfile().getOrThrow()

                _userName.value = profile.name.trim().ifBlank { "Usuário" }
                _profileActive.value = profile.active
                _isMember.value = profile.isMember

                _photoUrl.value = profile.photoUrl
                val url = profile.photoUrl

                if (!url.isNullOrBlank()) {
                    repository.downloadAndPersistProfilePhoto(url).getOrThrow()
                }

                refreshLocalPhotoPathAndBump()
            } catch (t: Throwable) {
                _error.value = t.message ?: "Falha ao enviar imagem"
            } finally {
                _isUploading.value = false
            }
        }
    }
}
