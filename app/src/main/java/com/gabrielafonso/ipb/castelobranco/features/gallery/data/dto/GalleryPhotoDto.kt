package com.gabrielafonso.ipb.castelobranco.features.gallery.data.dto


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GalleryPhotoDto(
    val id: Long,
    val name: String,
    val description: String,
    @SerialName("album_id")
    val albumId: Long,
    @SerialName("album_name")
    val albumName: String,
    @SerialName("image_url")
    val imageUrl: String,
    @SerialName("date_taken")
    val dateTaken: String?, // pode ser null
    @SerialName("uploaded_at")
    val uploadedAt: String
) {
    fun fileExtension(): String =
        when {
            imageUrl.endsWith(".png", true) -> "png"
            imageUrl.endsWith(".webp", true) -> "webp"
            imageUrl.endsWith(".jpeg", true) ||
            imageUrl.endsWith(".jpg", true) -> "jpg"
            else -> "jpg"
        }
}