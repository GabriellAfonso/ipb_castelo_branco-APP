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

   fun getThumbnailFile(albumId: Long): File? {
    val TAG = "GalleryStorage"

    val files = listPhotos(albumId)
    Log.d(TAG, "getThumbnailFile: Album $albumId - Total files found: ${files.size}")

    if (files.isEmpty()) {
        Log.w(TAG, "getThumbnailFile: No files in album $albumId, returning null")
        return null
    }

    val photosWithName = files.mapNotNull { file ->
        val photoIdStr = file.name.substringBefore(".")
        Log.d(TAG, "getThumbnailFile: Processing file ${file.name} - Extracted photoIdStr: $photoIdStr")

        val photoId = photoIdStr.toLongOrNull()
        if (photoId == null) {
            Log.w(TAG, "getThumbnailFile: Invalid photoId for file ${file.name}, skipping")
            return@mapNotNull null
        }

        val dto = getPhotoMetadata(albumId, photoId)
        if (dto == null) {
            Log.w(TAG, "getThumbnailFile: No metadata for photoId $photoId in album $albumId, skipping")
            return@mapNotNull null
        }

        Log.d(TAG, "getThumbnailFile: Found valid DTO for ${file.name} - Name: ${dto.name}")
        dto.name to file
    }

    Log.d(TAG, "getThumbnailFile: Valid photos with names: ${photosWithName.size}")

    val sorted = photosWithName.sortedBy { it.first }
    Log.d(TAG, "getThumbnailFile: Sorted photos: ${sorted.map { it.first }}")

    return if (sorted.isNotEmpty()) {
        val thumbFile = sorted.first().second
        Log.i(TAG, "getThumbnailFile: Selected thumbnail: ${thumbFile.name} for album $albumId")
        thumbFile
    } else {
        Log.w(TAG, "getThumbnailFile: No valid thumbnails after processing, returning null")
        null
    }
}
}