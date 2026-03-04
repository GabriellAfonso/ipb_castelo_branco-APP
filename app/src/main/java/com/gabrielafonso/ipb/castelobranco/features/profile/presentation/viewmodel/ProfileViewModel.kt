package com.gabrielafonso.ipb.castelobranco.features.profile.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.core.data.local.StorageDirConstants
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.usecase.FetchProfileUseCase
import com.gabrielafonso.ipb.castelobranco.features.profile.domain.usecase.UploadProfilePhotoUseCase
import com.gabrielafonso.ipb.castelobranco.features.profile.presentation.state.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val fetchProfileUseCase: FetchProfileUseCase,
    private val uploadProfilePhotoUseCase: UploadProfilePhotoUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun initialize() {
        observeProfile()
        refreshLocalPhotoPathAndBump()
        refreshFromServer()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            fetchProfileUseCase.observe().collect { state ->
                when (state) {
                    is SnapshotState.Data -> {
                        val profile = state.value
                        _uiState.update {
                            it.copy(
                                userName = profile.name.trim().ifBlank { "Usuário" },
                                profileActive = profile.active,
                                isMember = profile.isMember,
                                isAdmin = profile.isAdmin,
                                photoUrl = profile.photoUrl
                            )
                        }
                        if (!profile.photoUrl.isNullOrBlank()) {
                            fetchProfileUseCase.downloadAndPersistPhoto(profile.photoUrl)
                        }
                        refreshLocalPhotoPathAndBump()
                    }

                    SnapshotState.Loading -> {
                        // opcional: nada ou loading
                    }

                    is SnapshotState.Error -> {
                        _uiState.update {
                            it.copy(error = state.throwable.message ?: "Erro ao carregar perfil")
                        }
                    }
                }
            }
        }
    }

    private fun refreshLocalPhotoPathAndBump() {
        val dir = File(context.filesDir, StorageDirConstants.PROFILE)
        val file = dir.listFiles()
            ?.firstOrNull { it.isFile && it.name.startsWith("profile_photo.") && it.length() > 0L }

        _uiState.update {
            it.copy(
                localPhotoPath = file?.absolutePath,
                localPhotoVersion = it.localPhotoVersion + 1
            )
        }
    }

    fun refreshFromServer() {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            try {
                when (fetchProfileUseCase.refresh()) {
                    is RefreshResult.Error -> {
                        _uiState.update { it.copy(error = "Falha ao atualizar perfil") }
                        refreshLocalPhotoPathAndBump()
                    }
                    else -> Unit
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(error = t.message ?: "Falha ao atualizar perfil") }
                refreshLocalPhotoPathAndBump()
            }
        }
    }

    fun uploadProfilePhoto(bytes: ByteArray, fileName: String = "profile.jpg") {
        viewModelScope.launch {
            if (_uiState.value.isUploading) return@launch
            _uiState.update { it.copy(isUploading = true, error = null) }

            when (val result = uploadProfilePhotoUseCase(bytes, fileName)) {
                UploadProfilePhotoUseCase.Result.Success -> refreshLocalPhotoPathAndBump()
                is UploadProfilePhotoUseCase.Result.Failure ->
                    _uiState.update { it.copy(error = result.message) }
            }

            _uiState.update { it.copy(isUploading = false) }
        }
    }
}
