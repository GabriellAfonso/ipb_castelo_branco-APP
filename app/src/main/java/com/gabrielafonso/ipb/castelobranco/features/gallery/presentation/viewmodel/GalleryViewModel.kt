package com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.viewmodel

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

    private val _albums =
        MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums

    private val thumbnails = mutableMapOf<Long, MutableStateFlow<File?>>()

    init {
        loadLocalAlbums()
    }

    fun getAlbumThumbnail(albumId: Long): StateFlow<File?> {
        return thumbnails.getOrPut(albumId) {
            val flow = MutableStateFlow<File?>(null)
            viewModelScope.launch {
                flow.value = repository.getThumbnailForAlbum(albumId)
            }
            flow
        }.asStateFlow()
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

                if (progress.downloaded == progress.total) {
                    loadLocalAlbums()
                }
            }
        }
    }

    fun loadLocalAlbums() {
        viewModelScope.launch {
            _albums.value = repository.getLocalAlbums()
        }
    }

    fun clearGallery() {
        viewModelScope.launch {
            repository.clearAllPhotos()
            _albums.value = emptyList()
            _downloadState.value = GalleryDownloadState()
        }
    }

    suspend fun getLocalPhotos(albumId: Long): List<File> {
        return repository.getLocalPhotos(albumId)
    }
}