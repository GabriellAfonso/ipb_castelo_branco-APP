package com.gabrielafonso.ipb.castelobranco.features.profile.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfilePhotoCacheStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dirName = "profile"

    private fun dir(): File = File(context.filesDir, dirName).apply { mkdirs() }

    private fun etagFile(): File = File(dir(), "profile_photo_etag.txt")
    private fun urlFile(): File = File(dir(), "profile_photo_url.txt")

    suspend fun loadETagOrNull(): String? = withContext(Dispatchers.IO) {
        val f = etagFile()
        if (!f.exists()) return@withContext null
        f.readText().trim().takeIf { it.isNotBlank() }
    }

    suspend fun saveETag(etag: String) = withContext(Dispatchers.IO) {
        etagFile().writeText(etag.trim())
    }

    suspend fun clearETag() = withContext(Dispatchers.IO) {
        etagFile().takeIf { it.exists() }?.delete()
    }

    suspend fun loadLastUrlOrNull(): String? = withContext(Dispatchers.IO) {
        val f = urlFile()
        if (!f.exists()) return@withContext null
        f.readText().trim().takeIf { it.isNotBlank() }
    }

    suspend fun saveLastUrl(url: String) = withContext(Dispatchers.IO) {
        urlFile().writeText(url.trim())
    }

    suspend fun clearLastUrl() = withContext(Dispatchers.IO) {
        urlFile().takeIf { it.exists() }?.delete()
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        clearETag()
        clearLastUrl()
    }
}