package com.gabrielafonso.ipb.castelobranco.features.gallery.data.repository

import com.gabrielafonso.ipb.castelobranco.features.gallery.data.api.GalleryApi
import com.gabrielafonso.ipb.castelobranco.features.gallery.data.dto.GalleryPhotoDto
import com.gabrielafonso.ipb.castelobranco.features.gallery.data.local.GalleryPhotoStorage
import com.gabrielafonso.ipb.castelobranco.features.gallery.domain.repository.DownloadProgress
import com.gabrielafonso.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.update

data class Album(val id: Long, val name: String)

@Singleton
class GalleryRepositoryImpl @Inject constructor(
    private val api: GalleryApi,
    private val storage: GalleryPhotoStorage,
) : GalleryRepository {

    private val _albumsFlow = MutableStateFlow<List<Album>>(emptyList())
    override val albumsFlow: StateFlow<List<Album>> = _albumsFlow.asStateFlow()

    private val _thumbnailsFlow = MutableStateFlow<Map<Long, File?>>(emptyMap())
    override val thumbnailsFlow: StateFlow<Map<Long, File?>> = _thumbnailsFlow.asStateFlow()

    // Cache de fotos: Map de AlbumId para Lista de Arquivos
    private val _photosFlow = MutableStateFlow<Map<Long, List<File>>>(emptyMap())
    override val photosFlow: StateFlow<Map<Long, List<File>>> = _photosFlow.asStateFlow()

    override suspend fun preload() = withContext(Dispatchers.IO) {
        val rawAlbums = storage.listAlbums()
        _albumsFlow.value = rawAlbums.map { Album(it.first, it.second) }
        _thumbnailsFlow.value = rawAlbums.associate { (id, _) ->
            id to storage.getThumbnailFile(id)
        }
        // Não carregamos as fotos aqui para manter o preload leve
    }

    override suspend fun getLocalPhotos(albumId: Long): List<File> = withContext(Dispatchers.IO) {
        // Se já estiver no cache, retorna direto
        _photosFlow.value[albumId]?.let { return@withContext it }

        // Se não, busca no storage e salva no cache
        val photos = storage.listPhotos(albumId)
        _photosFlow.update { it + (albumId to photos) }
        photos
    }

    override fun downloadAlbum(albumId: Long): Flow<DownloadProgress> = flow {
        val photos = api.getAlbumPhotos(albumId)
        emitAll(processDownload(photos))
        // Limpa o cache desse álbum para forçar recarga após download
        _photosFlow.update { it - albumId }
    }

    override fun downloadAllPhotos(): Flow<DownloadProgress> = flow {
        val photos = api.getAllPhotos().body() ?: throw Exception("Lista de fotos vazia")
        emitAll(processDownload(photos))
        _photosFlow.value = emptyMap() // Reseta cache de fotos
        preload()
    }

    private fun processDownload(photos: List<GalleryPhotoDto>): Flow<DownloadProgress> = flow {
        if (photos.isEmpty()) { emit(DownloadProgress(0, 0)); return@flow }
        val total = photos.size
        var downloaded = 0
        emit(DownloadProgress(downloaded, total))

        for (photo in photos) {
            if (!storage.exists(photo.albumId, photo.id)) {
                val response = api.downloadFile(photo.imageUrl)
                if (response.isSuccessful) {
                    val body = response.body() ?: throw Exception("Body nulo")
                    storage.save(photo.albumId, photo.id, photo.fileExtension(), body.byteStream())
                    storage.savePhotoMetadata(photo.albumId, photo.id, photo)
                }
            }
            downloaded++
            emit(DownloadProgress(downloaded, total))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun clearAlbum(albumId: Long) = withContext(Dispatchers.IO) {
        storage.clearAlbum(albumId)
        _photosFlow.update { it - albumId } // Remove do cache
    }

    override suspend fun clearAllPhotos() = withContext(Dispatchers.IO) {
        storage.clearAll()
        _photosFlow.value = emptyMap() // Limpa todo o cache
        preload()
    }

    // Métodos delegados (mantidos)
    override suspend fun getAllLocalPhotos(): List<File> = withContext(Dispatchers.IO) { storage.listAllPhotos() }
    override suspend fun getLocalAlbums(): List<Album> = withContext(Dispatchers.IO) { storage.listAlbums().map { Album(it.first, it.second) } }
    override suspend fun getThumbnailForAlbum(albumId: Long): File? = withContext(Dispatchers.IO) { storage.getThumbnailFile(albumId) }
    override suspend fun getPhotoName(albumId: Long, photoId: Long): String? = withContext(Dispatchers.IO) { storage.getPhotoName(albumId, photoId) }
}