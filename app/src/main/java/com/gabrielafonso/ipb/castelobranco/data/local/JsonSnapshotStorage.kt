// app/src/main/java/com/gabrielafonso/ipb/castelobranco/data/local/JsonSnapshotStorage.kt
package com.gabrielafonso.ipb.castelobranco.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class JsonSnapshotStorage(
    private val appContext: Context
) {
    private val dirName = "snapshots"

    private fun safeKey(key: String): String {
        val safe = key
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9_-]+"), "_")
            .trim('_')

        require(safe.isNotBlank()) { "Snapshot key inválida" }
        return safe
    }

    private fun dir(): File = File(appContext.filesDir, dirName).apply { mkdirs() }

    private fun jsonFileForKey(key: String): File =
        File(dir(), "${safeKey(key)}.json")

    private fun etagFileForKey(key: String): File =
        File(dir(), "${safeKey(key)}_etag.txt")

    suspend fun save(key: String, json: String) = withContext(Dispatchers.IO) {
        jsonFileForKey(key).writeText(json)
    }

    suspend fun loadOrNull(key: String): String? = withContext(Dispatchers.IO) {
        val file = jsonFileForKey(key)
        if (!file.exists()) return@withContext null
        file.readText()
    }

    suspend fun clear(key: String) = withContext(Dispatchers.IO) {
        jsonFileForKey(key).takeIf { it.exists() }?.delete()
        etagFileForKey(key).takeIf { it.exists() }?.delete()
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        val d = File(appContext.filesDir, dirName)
        if (d.exists()) d.deleteRecursively()
    }

    suspend fun loadETagOrNull(key: String): String? = withContext(Dispatchers.IO) {
        val file = etagFileForKey(key)
        if (!file.exists()) return@withContext null
        file.readText().trim().takeIf { it.isNotBlank() }
    }

    suspend fun saveETag(key: String, etag: String) = withContext(Dispatchers.IO) {
        etagFileForKey(key).writeText(etag)
    }

    fun getAbsolutePathForDebug(key: String): String =
        jsonFileForKey(key).absolutePath
}
