package com.gabrielafonso.ipb.castelobranco.data.repository

import android.content.Context
import com.gabrielafonso.ipb.castelobranco.core.di.ApiBaseUrl
import com.gabrielafonso.ipb.castelobranco.data.api.BackendApi
import com.gabrielafonso.ipb.castelobranco.data.api.MeProfileDto
import com.gabrielafonso.ipb.castelobranco.data.local.ProfilePhotoBus
import com.gabrielafonso.ipb.castelobranco.data.local.ProfilePhotoCacheStorage
import com.gabrielafonso.ipb.castelobranco.data.local.JsonSnapshotStorage
import com.gabrielafonso.ipb.castelobranco.domain.repository.ProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
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
    @ApiBaseUrl private val baseUrl: String,
    private val photoCache: ProfilePhotoCacheStorage,
    // ---- ADICIONAR ----
    private val jsonStorage: JsonSnapshotStorage,
    private val json: Json,
) : ProfileRepository {

    private companion object {
        private const val KEY_ME_PROFILE = "me_profile"
    }

    override suspend fun getMeProfile(): Result<MeProfileDto> =
        withContext(Dispatchers.IO) {
            runCatching<MeProfileDto> {
                val lastETag = runCatching { jsonStorage.loadETagOrNull(KEY_ME_PROFILE) }.getOrNull()
                val response = api.getMeProfile(ifNoneMatch = lastETag)

                when {
                    response.code() == 304 -> {
                        val cachedJson = jsonStorage.loadOrNull(KEY_ME_PROFILE)
                            ?: throw Exception("Perfil não modificado (304), mas não existe cache local.")

                        json.decodeFromString(MeProfileDto.serializer(), cachedJson)
                    }

                    response.isSuccessful -> {
                        val body = response.body() ?: throw Exception("Resposta vazia")

                        val raw = json.encodeToString(MeProfileDto.serializer(), body)
                        jsonStorage.save(KEY_ME_PROFILE, raw)

                        val newETag = response.headers()["ETag"]?.trim()
                        if (!newETag.isNullOrBlank()) {
                            jsonStorage.saveETag(KEY_ME_PROFILE, newETag)
                        }

                        body
                    }

                    else -> {
                        val errorBody = response.errorBody()?.string()
                        throw Exception(errorBody ?: "HTTP ${response.code()}")
                    }
                }
            }
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
                // servidor apagou: garante que a foto local antiga suma e notifica a UI
                clearLocalProfilePhoto().getOrNull()
                ProfilePhotoBus.bump()
            }
        }

    override suspend fun downloadAndPersistProfilePhoto(photoUrl: String): Result<File?> =
        withContext(Dispatchers.IO) {
            runCatching {
                val absoluteUrl = toAbsoluteUrl(photoUrl)

                // 1) Se a URL mudou, invalida o ETag (senão você pode receber 304 pra outra imagem)
                val lastUrl = photoCache.loadLastUrlOrNull()
                if (lastUrl != null && lastUrl != absoluteUrl) {
                    photoCache.clearETag()
                }
                photoCache.saveLastUrl(absoluteUrl)

                // 2) Carrega ETag anterior (se tiver) e monta request condicional
                val lastETag = photoCache.loadETagOrNull()

                val requestBuilder = Request.Builder()
                    .url(absoluteUrl)
                    .get()

                if (!lastETag.isNullOrBlank()) {
                    requestBuilder.header("If-None-Match", lastETag)
                }

                val request = requestBuilder.build()

                client.newCall(request).execute().use { response ->
                    // Caso servidor diga que não existe mais
                    if (response.code == 404) {
                        clearLocalProfilePhoto().getOrNull()
                        photoCache.clearAll()
                        ProfilePhotoBus.bump()
                        return@runCatching null
                    }

                    // Se não mudou, não baixa nada: usa o arquivo local atual
                    if (response.code == 304) {
                        val dir = File(context.filesDir, "profile")
                        val localFile = dir.listFiles()
                            ?.asSequence()
                            ?.filter { it.isFile && it.name.startsWith("profile_photo.") && it.length() > 0L }
                            ?.maxByOrNull { it.lastModified() }

                        return@runCatching localFile
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

                    // 3) Salva ETag novo se vier no header
                    val newETag = response.header("ETag")?.trim()
                    if (!newETag.isNullOrBlank()) {
                        photoCache.saveETag(newETag)
                    }

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

                // limpa metadados do cache também
                photoCache.clearAll()

                Unit
            }.also { result ->
                if (result.isSuccess) {
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