package com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import com.gabrielafonso.ipb.castelobranco.features.gallery.domain.usecase.DownloadAllPhotosUseCase
import com.gabrielafonso.ipb.castelobranco.core.domain.error.toAppError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class GalleryDownloadState(
    val isDownloading: Boolean = false,
    val downloaded: Int = 0,
    val total: Int = 0,
    val error: String? = null
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: GalleryRepository,
    private val downloadAllPhotosUseCase: DownloadAllPhotosUseCase,
) : ViewModel() {

    private val _downloadState =
        MutableStateFlow(GalleryDownloadState())
    val downloadState: StateFlow<GalleryDownloadState> = _downloadState

    val albums = repository.albumsFlow

    val thumbnails = repository.thumbnailsFlow
    val cachedPhotos = repository.photosFlow

    suspend fun getLocalPhotos(albumId: Long): List<File> {
        return repository.getLocalPhotos(albumId)
    }

    fun getThumbnailForAlbum(albumId: Long): File? {
        return thumbnails.value[albumId]
    }

    fun downloadAllPhotos() {
        if (_downloadState.value.isDownloading) return

        viewModelScope.launch {
            try {
                downloadAllPhotosUseCase.downloadProgress().collect { progress ->
                    _downloadState.update {
                        it.copy(
                            isDownloading = progress.downloaded < progress.total,
                            downloaded = progress.downloaded,
                            total = progress.total
                        )
                    }
                }
                downloadAllPhotosUseCase.preload()
            } catch (e: Exception) {
                val appError = e.toAppError()
                _downloadState.update {
                    it.copy(isDownloading = false, error = appError.message ?: "Erro ao baixar fotos")
                }
            }
        }
    }

    fun clearGallery() {
        viewModelScope.launch {
            repository.clearAllPhotos()
            _downloadState.value = GalleryDownloadState()
        }
    }

    suspend fun getPhotoName(albumId: Long, photoId: Long): String {
        Log.d("TAG", "getPhotoName: $albumId - $photoId")
        return repository.getPhotoName(albumId, photoId) ?: "Foto"
    }
}
