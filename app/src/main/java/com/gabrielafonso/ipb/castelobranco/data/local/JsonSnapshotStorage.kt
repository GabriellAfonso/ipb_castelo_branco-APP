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

    private fun fileForKey(key: String): File {
        val safe = key
            .trim()
            .lowercase()
            // mantém só [a-z0-9_-] para virar nome de ficheiro
            .replace(Regex("[^a-z0-9_-]+"), "_")
            .trim('_')

        require(safe.isNotBlank()) { "Snapshot key inválida" }

        val dir = File(appContext.filesDir, dirName).apply { mkdirs() }
        return File(dir, "$safe.json")
    }

    suspend fun save(key: String, json: String) = withContext(Dispatchers.IO) {
        fileForKey(key).writeText(json)
    }

    suspend fun loadOrNull(key: String): String? = withContext(Dispatchers.IO) {
        val file = fileForKey(key)
        if (!file.exists()) return@withContext null
        file.readText()
    }

    suspend fun clear(key: String) = withContext(Dispatchers.IO) {
        val file = fileForKey(key)
        if (file.exists()) file.delete()
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        val dir = File(appContext.filesDir, dirName)
        if (dir.exists()) dir.deleteRecursively()
    }

    fun getAbsolutePathForDebug(key: String): String =
        fileForKey(key).absolutePath
}
