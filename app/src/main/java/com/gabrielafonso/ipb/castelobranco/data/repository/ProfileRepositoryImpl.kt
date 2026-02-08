// app/src/main/java/com/gabrielafonso/ipb/castelobranco/data/repository/ProfileRepositoryImpl.kt
package com.gabrielafonso.ipb.castelobranco.data.repository

import android.content.Context
import com.gabrielafonso.ipb.castelobranco.core.di.ApiBaseUrl
import com.gabrielafonso.ipb.castelobranco.data.api.BackendApi
import com.gabrielafonso.ipb.castelobranco.data.api.MeProfileDto
import com.gabrielafonso.ipb.castelobranco.data.local.ProfilePhotoBus
import com.gabrielafonso.ipb.castelobranco.domain.repository.ProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: BackendApi,
    private val client: OkHttpClient,
    @ApplicationContext private val context: Context,
    @ApiBaseUrl private val baseUrl: String
) : ProfileRepository {

    override suspend fun getMeProfile(): Result<MeProfileDto> =
        runCatching {
            val response = api.getMeProfile()
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                throw Exception(errorBody ?: "HTTP ${response.code()}")
            }
            response.body() ?: throw Exception("Resposta vazia")
        }

    override suspend fun uploadProfilePhoto(bytes: ByteArray, fileName: String): Result<String?> =
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
                throw Exception(errorBody ?: "HTTP ${response.code()}")
            }

            response.body()?.photoUrl
        }

    override suspend fun deleteProfilePhoto(): Result<Unit> =
        runCatching {
            val response = api.deleteProfilePhoto()
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                throw Exception(errorBody ?: "HTTP ${response.code()}")
            }
            Unit
        }.also { result ->
            if (result.isSuccess) {
                // \- servidor apagou: garante que a foto local antiga suma e notifica a UI
                clearLocalProfilePhoto().getOrNull()
                ProfilePhotoBus.bump()
            }
        }

    override suspend fun downloadAndPersistProfilePhoto(photoUrl: String): Result<File?> =
        withContext(Dispatchers.IO) {
            runCatching {
                val absoluteUrl = toAbsoluteUrl(photoUrl)

                val request = Request.Builder()
                    .url(absoluteUrl)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.code == 404) {
                        // \- servidor diz que não existe: limpa local e notifica
                        clearLocalProfilePhoto().getOrNull()
                        ProfilePhotoBus.bump()
                        return@runCatching null
                    }
                    if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

                    val body = response.body ?: throw Exception("Corpo de resposta vazio")

                    val contentType = body.contentType()?.toString().orEmpty()
                    val ext = when {
                        contentType.contains("png", ignoreCase = true) -> "png"
                        contentType.contains("webp", ignoreCase = true) -> "webp"
                        contentType.contains("jpeg", ignoreCase = true) ||
                                contentType.contains("jpg", ignoreCase = true) -> "jpg"
                        else -> "jpg"
                    }

                    val dir = File(context.filesDir, "profile").apply { mkdirs() }
                    val outFile = File(dir, "profile_photo.$ext")

                    FileOutputStream(outFile).use { output ->
                        body.byteStream().use { input -> input.copyTo(output) }
                    }

                    // \- foto local mudou: notifica a UI
                    ProfilePhotoBus.bump()
                    outFile
                }
            }
        }

    override suspend fun clearLocalProfilePhoto(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(context.filesDir, "profile")
                if (dir.exists()) {
                    dir.listFiles()?.forEach { file ->
                        if (file.isFile && file.name.startsWith("profile_photo")) {
                            file.delete()
                        }
                    }
                }
                Unit
            }.also { result ->
                if (result.isSuccess) {
                    // \- foto local mudou: notifica a UI
                    ProfilePhotoBus.bump()
                }
            }
        }

    private fun toAbsoluteUrl(url: String): String {
        val trimmed = url.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed

        val base = baseUrl.trimEnd('/')
        val path = trimmed.trimStart('/')
        return "$base/$path"
    }
}
