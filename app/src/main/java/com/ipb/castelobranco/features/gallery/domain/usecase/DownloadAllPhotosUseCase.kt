package com.ipb.castelobranco.features.gallery.domain.usecase

import com.ipb.castelobranco.features.gallery.domain.repository.DownloadProgress
import com.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DownloadAllPhotosUseCase @Inject constructor(
    private val repository: GalleryRepository,
) {
    fun downloadProgress(): Flow<DownloadProgress> = repository.downloadAllPhotos()

    suspend fun preload() = repository.preload()
}
