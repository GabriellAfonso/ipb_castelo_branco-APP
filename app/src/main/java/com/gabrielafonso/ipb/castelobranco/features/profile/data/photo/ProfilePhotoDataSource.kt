package com.gabrielafonso.ipb.castelobranco.features.profile.data.photo

import android.content.Context
import com.gabrielafonso.ipb.castelobranco.core.di.ApiBaseUrl
import com.gabrielafonso.ipb.castelobranco.features.profile.data.api.ProfileApi
import com.gabrielafonso.ipb.castelobranco.features.profile.data.local.ProfilePhotoBus
import com.gabrielafonso.ipb.castelobranco.features.profile.data.local.ProfilePhotoCacheStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ProfilePhotoDataSource @Inject constructor(
    private val api: ProfileApi,
    private val photoCache: ProfilePhotoCacheStorage,
    @ApplicationContext private val context: Context,
    @ApiBaseUrl private val baseUrl: String
) {

    suspend fun upload(bytes: ByteArray, fileName: String): String? {
        val body = bytes.toRequestBody("image/*".toMediaType())
        val part = MultipartBody.Part.createFormData("photo", fileName, body)

        val response = api.uploadProfilePhoto(part)
        if (!response.isSuccessful) error(response.errorBody()?.string() ?: "HTTP ${response.code()}")

        return response.body()?.photoUrl
    }

    suspend fun delete() {
        val response = api.deleteProfilePhoto()
        if (!response.isSuccessful) error(response.errorBody()?.string() ?: "HTTP ${response.code()}")

        clearLocal()
        ProfilePhotoBus.bump()
    }

    suspend fun download(photoUrl: String): File? {
        val absoluteUrl = toAbsoluteUrl(photoUrl)
        photoCache.saveLastUrl(absoluteUrl)

        val response = api.downloadFile(
            absoluteUrl = absoluteUrl,
            ifNoneMatch = photoCache.loadETagOrNull()
        )

        when (response.code()) {
            404 -> {
                clearLocal()
                ProfilePhotoBus.bump()
                return null
            }
            304 -> return latestLocalFile()
        }

        if (!response.isSuccessful) {
            error(response.errorBody()?.string() ?: "HTTP ${response.code()}")
        }

        val body = response.body() ?: error("Resposta vazia")
        val ext = body.contentType()?.subtype ?: "jpg"

        val dir = File(context.filesDir, "profile").apply { mkdirs() }
        val out = File(dir, "profile_photo.$ext")

        body.byteStream().use { input ->
            FileOutputStream(out).use { output -> input.copyTo(output) }
        }

        response.headers()["ETag"]?.let { photoCache.saveETag(it) }
        ProfilePhotoBus.bump()

        return out
    }

    suspend fun clearLocal() {
        File(context.filesDir, "profile")
            .listFiles()
            ?.filter { it.name.startsWith("profile_photo") }
            ?.forEach(File::delete)

        photoCache.clearAll()
    }

    private fun latestLocalFile(): File? =
        File(context.filesDir, "profile")
            .listFiles()
            ?.filter { it.isFile && it.length() > 0 }
            ?.maxByOrNull { it.lastModified() }

    private fun toAbsoluteUrl(url: String): String =
        if (url.startsWith("http")) url else "${baseUrl.trimEnd('/')}/${url.trimStart('/')}"
}
