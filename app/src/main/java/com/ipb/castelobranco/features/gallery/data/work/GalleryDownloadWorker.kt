package com.ipb.castelobranco.features.gallery.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ipb.castelobranco.features.gallery.data.api.GalleryApi
import com.ipb.castelobranco.features.gallery.data.local.GalleryPhotoStorage
import com.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.json.JSONObject

@HiltWorker
class GalleryDownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val api: GalleryApi,
    private val storage: GalleryPhotoStorage,
    private val repository: GalleryRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val response = api.getAllPhotos()
            if (!response.isSuccessful) {
                val errorMessage = try {
                    val raw = response.errorBody()?.string()
                    raw?.let { JSONObject(it).optString("detail", it) } ?: "HTTP ${response.code()}"
                } catch (_: Exception) {
                    "HTTP ${response.code()}"
                }
                val code = response.code()
                if (code == 401 || code == 403) {
                    return Result.failure(workDataOf(KEY_ERROR to errorMessage, KEY_ERROR_CODE to code))
                }
                return if (runAttemptCount < MAX_RETRIES) Result.retry()
                else Result.failure(workDataOf(KEY_ERROR to errorMessage, KEY_ERROR_CODE to code))
            }

            val photos = response.body() ?: return Result.success()
            val total = photos.size
            var downloaded = 0

            setProgressAsync(workDataOf(KEY_DOWNLOADED to 0, KEY_TOTAL to total))

            // Agrupa por álbum para fazer preload incremental após cada álbum completo
            val photosByAlbum = photos.groupBy { it.albumId }

            for ((_, albumPhotos) in photosByAlbum) {
                for (photo in albumPhotos) {
                    if (!storage.exists(photo.albumId, photo.id)) {
                        val fileResponse = api.downloadFile(photo.imageUrl)
                        if (fileResponse.isSuccessful) {
                            val body = fileResponse.body() ?: continue
                            storage.save(
                                albumId = photo.albumId,
                                photoId = photo.id,
                                ext = photo.fileExtension(),
                                input = body.byteStream(),
                            )
                            storage.savePhotoMetadata(photo.albumId, photo.id, photo)
                        }
                    }
                    downloaded++
                    setProgressAsync(workDataOf(KEY_DOWNLOADED to downloaded, KEY_TOTAL to total))
                }
                // Atualiza os flows do repositório após cada álbum — a UI mostra novos álbuns em tempo real
                repository.preload()
            }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "gallery_auto_download"
        const val KEY_DOWNLOADED = "downloaded"
        const val KEY_TOTAL = "total"
        const val KEY_ERROR = "error"
        const val KEY_ERROR_CODE = "error_code"
        private const val MAX_RETRIES = 3
    }
}
