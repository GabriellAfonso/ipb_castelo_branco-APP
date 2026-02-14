package com.gabrielafonso.ipb.castelobranco.features.profile.data.api

import com.gabrielafonso.ipb.castelobranco.features.profile.data.dto.MeProfileDto
import com.gabrielafonso.ipb.castelobranco.features.profile.data.dto.ProfilePhotoResponseDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ProfileApi {

    @Multipart
    @POST(ProfileEndpoints.ME_PROFILE_PHOTO_PATH)
    suspend fun uploadProfilePhoto(
        @Part photo: MultipartBody.Part
    ): Response<ProfilePhotoResponseDto>

    @DELETE(ProfileEndpoints.ME_PROFILE_PHOTO_PATH)
    suspend fun deleteProfilePhoto(): Response<Unit>

    @GET(ProfileEndpoints.ME_PROFILE_PATH)
    suspend fun getMeProfile(
        @Header("If-None-Match") ifNoneMatch: String? = null
    ): Response<MeProfileDto>

    @Streaming
    @GET
    suspend fun downloadFile(
        @Url absoluteUrl: String,
        @Header("If-None-Match") ifNoneMatch: String? = null
    ): Response<ResponseBody>
}