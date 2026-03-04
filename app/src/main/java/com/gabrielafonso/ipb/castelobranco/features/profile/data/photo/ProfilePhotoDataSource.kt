package com.gabrielafonso.ipb.castelobranco.features.profile.data.photo

import android.content.Context
import com.gabrielafonso.ipb.castelobranco.core.data.local.StorageDirConstants
import com.gabrielafonso.ipb.castelobranco.core.di.ApiBaseUrl
import com.gabrielafonso.ipb.castelobranco.core.domain.error.AppError
import com.gabrielafonso.ipb.castelobranco.core.domain.error.mapError
import com.gabrielafonso.ipb.castelobranco.features.profile.data.api.ProfileApi
import com.gabrielafonso.ipb.castelobranco.features.profile.data.local.ProfilePhotoBus
import com.gabrielafonso.ipb.castelobranco.features.profile.data.local.ProfilePhotoCacheStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfilePhotoDataSource @Inject constructor(
    private val api: ProfileApi,
    @ApplicationContext private val context: Context,
    @ApiBaseUrl private val baseUrl: String,
    private val photoCache: ProfilePhotoCacheStorage,
) {

    suspend fun upload(bytes: ByteArray, fileName: String): Result<String?> =
        runCatching {
            val body = bytes.toRequestBody("image/*".toMediaType())
            val part = MultipartBody.Part.createFormData(
                name = "photo",
                filename = fileName,
                body = body
            )

            val response = api.uploadProfilePhoto(part)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                val code = response.code()
                if (code == 401 || code == 403) {
                    throw AppError.Auth(message = errorBody ?: "HTTP $code")
                } else {
                    throw AppError.Server(code = code, message = errorBody ?: "HTTP $code")
                }
            }

            response.body()?.photoUrl
        }.mapError()

    suspend fun delete(): Result<Unit> =
        runCatching {
            val response = api.deleteProfilePhoto()
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                val code = response.code()
                if (code == 401 || code == 403) {
                    throw AppError.Auth(message = errorBody ?: "HTTP $code")
                } else {
                    throw AppError.Server(code = code, message = errorBody ?: "HTTP $code")
                }
            }
            Unit
        }.mapError().also { result ->
            if (result.isSuccess) {
                clearLocal().getOrNull()
                ProfilePhotoBus.bump()
            }
        }

    suspend fun downloadAndPersist(photoUrl: String): Result<File?> =
        withContext(Dispatchers.IO) {
            runCatching {
                val absoluteUrl = toAbsoluteUrl(photoUrl)

                val lastUrl = photoCache.loadLastUrlOrNull()
                if (lastUrl != null && lastUrl != absoluteUrl) {
                    photoCache.clearETag()
                }
                photoCache.saveLastUrl(absoluteUrl)

                val lastETag = photoCache.loadETagOrNull()

                val response = api.downloadFile(
                    absoluteUrl = absoluteUrl,
                    ifNoneMatch = lastETag
                )

                when {
                    response.code() == 404 -> {
                        clearLocal().getOrNull()
                        photoCache.clearAll()
                        ProfilePhotoBus.bump()
                        null
                    }

                    response.code() == 304 -> findLastLocalPhotoOrNull()

                    !response.isSuccessful -> {
                        val err = response.errorBody()?.string()?.trim().orEmpty()
                        val code = response.code()
                        if (code == 401 || code == 403) {
                            throw AppError.Auth(message = err.ifBlank { "HTTP $code" })
                        } else {
                            throw AppError.Server(code = code, message = err.ifBlank { "HTTP $code" })
                        }
                    }

                    else -> {
                        val body = response.body()
                            ?: throw AppError.Server(code = response.code(), message = "Corpo de resposta vazio")

                        val contentType = body.contentType()?.toString().orEmpty()
                        val ext = when {
                            contentType.contains("png", ignoreCase = true) -> "png"
                            contentType.contains("webp", ignoreCase = true) -> "webp"
                            contentType.contains("jpeg", ignoreCase = true) ||
                                    contentType.contains("jpg", ignoreCase = true) -> "jpg"
                            else -> "jpg"
                        }

                        val dir = File(context.filesDir, StorageDirConstants.PROFILE).apply { mkdirs() }
                        val outFile = File(dir, "profile_photo.$ext")

                        body.byteStream().use { input ->
                            FileOutputStream(outFile).use { output ->
                                input.copyTo(output)
                            }
                        }

                        val newETag = response.headers()["ETag"]?.trim()
                        if (!newETag.isNullOrBlank()) photoCache.saveETag(newETag)

                        ProfilePhotoBus.bump()
                        outFile
                    }
                }
            }.mapError()
        }

    suspend fun clearLocal(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(context.filesDir, StorageDirConstants.PROFILE)
                if (dir.exists()) {
                    dir.listFiles()?.forEach { file ->
                        if (file.isFile && file.name.startsWith("profile_photo")) {
                            file.delete()
                        }
                    }
                }

                photoCache.clearAll()
                Unit
            }.mapError().also { result ->
                if (result.isSuccess) ProfilePhotoBus.bump()
            }
        }

    private fun findLastLocalPhotoOrNull(): File? {
        val dir = File(context.filesDir, StorageDirConstants.PROFILE)
        return dir.listFiles()
            ?.asSequence()
            ?.filter { it.isFile && it.name.startsWith("profile_photo.") && it.length() > 0L }
            ?.maxByOrNull { it.lastModified() }
    }

    private fun toAbsoluteUrl(url: String): String {
        val trimmed = url.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
        return "${baseUrl.trimEnd('/')}/${trimmed.trimStart('/')}"
    }
}