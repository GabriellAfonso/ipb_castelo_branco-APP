package com.gabrielafonso.ipb.castelobranco.features.gallery.domain.repository

import com.gabrielafonso.ipb.castelobranco.features.gallery.data.repository.Album
import kotlinx.coroutines.flow.Flow
import java.io.File

interface GalleryRepository {

    /**
     * Baixa todas as fotos de um álbum.
     *
     * @param albumId id do álbum
     * @return Flow de progresso (0..100)
     */
    fun downloadAlbum(
        albumId: Long,
    ): Flow<DownloadProgress>

    /**
     * Retorna todas as fotos locais de um álbum (offline)
     */
    suspend fun getLocalPhotos(
        albumId: Long,
    ): List<File>

    /**
     * Remove todas as fotos locais de um álbum
     */
    suspend fun clearAlbum(
        albumId: Long,
    )

    fun downloadAllPhotos(): Flow<DownloadProgress>
    suspend fun getAllLocalPhotos(): List<File>
    suspend fun clearAllPhotos()
    suspend fun getLocalAlbums(): List<Album>
    suspend fun getThumbnailForAlbum(albumId: Long): File?
}

data class DownloadProgress(
    val downloaded: Int,
    val total: Int
) {
    val percentage: Int
        get() = if (total == 0) 0 else (downloaded * 100) / total
}