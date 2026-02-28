package com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabrielafonso.ipb.castelobranco.features.gallery.data.repository.Album
import com.gabrielafonso.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val repository: GalleryRepository
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
            repository.downloadAllPhotos().collect { progress ->
                _downloadState.update {
                    it.copy(
                        isDownloading = progress.downloaded < progress.total,
                        downloaded = progress.downloaded,
                        total = progress.total
                    )
                }
            }
        repository.preload()
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