package com.gabrielafonso.ipb.castelobranco.features.gallery.domain.repository

import com.gabrielafonso.ipb.castelobranco.features.gallery.data.repository.Album
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface GalleryRepository {
    val albumsFlow: StateFlow<List<Album>>
    val thumbnailsFlow: StateFlow<Map<Long, File?>>
    val photosFlow: StateFlow<Map<Long, List<File>>> // O novo cache de fotos

    suspend fun preload()
    fun downloadAlbum(albumId: Long): Flow<DownloadProgress>
    suspend fun getLocalPhotos(albumId: Long): List<File>
    suspend fun clearAlbum(albumId: Long)
    fun downloadAllPhotos(): Flow<DownloadProgress>
    suspend fun getAllLocalPhotos(): List<File>
    suspend fun clearAllPhotos()
    suspend fun getLocalAlbums(): List<Album>
    suspend fun getThumbnailForAlbum(albumId: Long): File?
    suspend fun getPhotoName(albumId: Long, photoId: Long): String?
}
data class DownloadProgress(
    val downloaded: Int,
    val total: Int
) {
    val percentage: Int
        get() = if (total == 0) 0 else (downloaded * 100) / total
}