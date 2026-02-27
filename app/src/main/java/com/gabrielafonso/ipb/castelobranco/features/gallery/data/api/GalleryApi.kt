package com.gabrielafonso.ipb.castelobranco.features.gallery.data.api


import com.gabrielafonso.ipb.castelobranco.features.gallery.data.dto.GalleryPhotoDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming
import retrofit2.http.Url
import retrofit2.http.Path

interface GalleryApi {

    @GET("api/photos/")
    suspend fun getAllPhotos(): Response<List<GalleryPhotoDto>>

    @GET("api/albums/{id}/photos/")
    suspend fun getAlbumPhotos(
        @Path("id") albumId: Long
    ): List<GalleryPhotoDto>

    @Streaming
    @GET
    suspend fun downloadFile(
        @Url absoluteUrl: String
    ): Response<ResponseBody>
}