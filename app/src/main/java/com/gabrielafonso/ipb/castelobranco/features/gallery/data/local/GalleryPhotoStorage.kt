package com.gabrielafonso.ipb.castelobranco.features.gallery.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class GalleryPhotoStorage(
    @param:ApplicationContext private val context: Context
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

    fun exists(albumId: Long, photoId: Long): Boolean =
        albumDir(albumId)
            .listFiles()
            ?.any { it.name.startsWith("$photoId.") } == true

    fun listPhotos(albumId: Long): List<File> =
        albumDir(albumId)
            .listFiles()
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
            .filter { it.isFile }
            .sortedBy { it.name }
            .toList()
    }

    fun clearAll() {
        val root = File(context.filesDir, "gallery")
        if (root.exists()) {
            root.deleteRecursively()
        }
    }
}