package com.gabrielafonso.ipb.castelobranco.features.gallery.data.local

import android.content.Context
import com.gabrielafonso.ipb.castelobranco.features.gallery.data.dto.GalleryPhotoDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

import android.util.Log

class GalleryPhotoStorage(
    @ApplicationContext private val context: Context
) {

    private fun albumDir(albumId: Long): File =
        File(context.filesDir, "gallery/$albumId")

    fun save(
        albumId: Long,
        photoId: Long,
        ext: String,
        input: InputStream
    ): File {
        val dir = albumDir(albumId).apply { mkdirs() }
        val file = File(dir, "$photoId.$ext")

        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
        return file
    }

    fun savePhotoMetadata(
        albumId: Long,
        photoId: Long,
        dto: GalleryPhotoDto
    ) {
        val dir = albumDir(albumId)
        val jsonFile = File(dir, "$photoId.json")
        val json = Json.encodeToString(dto)
        jsonFile.writeText(json)
    }

    fun getPhotoMetadata(
        albumId: Long,
        photoId: Long
    ): GalleryPhotoDto? {
        val jsonFile = File(albumDir(albumId), "$photoId.json")
        if (!jsonFile.exists()) return null
        return try {
            Json.decodeFromString(jsonFile.readText())
        } catch (e: Exception) {
            Log.w("GalleryPhotoStorage", "Failed to parse metadata for photo $photoId in album $albumId", e)
            null
        }
    }

    fun exists(albumId: Long, photoId: Long): Boolean =
        albumDir(albumId)
            .listFiles()
            ?.any { it.name.startsWith("$photoId.") && !it.name.endsWith(".json") } == true

    fun listPhotos(albumId: Long): List<File> =
        albumDir(albumId)
            .listFiles()
            ?.filter { it.isFile && !it.name.endsWith(".json") }
            ?.sortedBy { it.name }
            ?: emptyList()

    fun clearAlbum(albumId: Long) {
        albumDir(albumId)
            .listFiles()
            ?.forEach { it.delete() }
    }

    fun listAllPhotos(): List<File> {
        val root = File(context.filesDir, "gallery")
        if (!root.exists()) return emptyList()

        return root.walkTopDown()
            .filter { it.isFile && !it.name.endsWith(".json") }
            .sortedBy { it.name }
            .toList()
    }

    fun clearAll() {
        val root = File(context.filesDir, "gallery")
        if (root.exists()) {
            root.deleteRecursively()
        }
    }

    fun listAlbums(): List<Pair<Long, String>> {
        val root = File(context.filesDir, "gallery")
        if (!root.exists()) return emptyList()

        return root.listFiles()?.filter { it.isDirectory }?.mapNotNull { dir ->
            val id = dir.name.toLongOrNull() ?: return@mapNotNull null
            val name = getAlbumName(id) ?: "Álbum $id"
            id to name
        } ?: emptyList()
    }

    private fun getAlbumName(albumId: Long): String? {
        val photos = listPhotos(albumId)
        if (photos.isEmpty()) return null
        val firstPhotoId = photos.first().name.substringBefore(".").toLongOrNull() ?: return null
        return getPhotoMetadata(albumId, firstPhotoId)?.albumName
    }

    fun getPhotoName(albumId: Long, photoId: Long): String? {
        val name = getPhotoMetadata(albumId, photoId)?.name
        return name?.substringBeforeLast(".")
    }

    fun getThumbnailFile(albumId: Long): File? {
        val files = listPhotos(albumId)
        if (files.isEmpty()) return null

        // Tenta encontrar a img00 primeiro (Para no primeiro match)
        val img00 = files.firstOrNull { file ->
            val photoId = file.name.substringBefore(".").toLongOrNull()
            photoId != null && getPhotoMetadata(albumId, photoId)?.name?.lowercase() == "img00.jpg"
        }

        // Se achou a img00, retorna ela. Se não, retorna a primeira foto da lista.
        return img00 ?: files.firstOrNull()
    }
}