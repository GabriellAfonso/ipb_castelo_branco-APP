package com.gabrielafonso.ipb.castelobranco.data.api

import com.gabrielafonso.ipb.castelobranco.core.network.Endpoints
import com.gabrielafonso.ipb.castelobranco.domain.model.AuthTokens
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface BackendApi {

    @GET(Endpoints.SONGS_BY_SUNDAY_PATH)
    suspend fun getSongsBySunday(
        @Header("If-None-Match") ifNoneMatch: String? = null
    ): Response<List<SongsBySundayDto>>

    @GET(Endpoints.TOP_SONGS_PATH)
    suspend fun getTopSongs(
        @Header("If-None-Match") ifNoneMatch: String? = null
    ): Response<List<TopSongDto>>

    @GET(Endpoints.TOP_TONES_PATH)
    suspend fun getTopTones(
        @Header("If-None-Match") ifNoneMatch: String? = null
    ): Response<List<TopToneDto>>

    @GET(Endpoints.SUGGESTED_SONGS_PATH)
    suspend fun getSuggestedSongs(
        @Header("If-None-Match") ifNoneMatch: String? = null
    ): Response<List<SuggestedSongDto>>

    @GET(Endpoints.HYMNAL_PATH)
    suspend fun getHymnal(
        @Header("If-None-Match") ifNoneMatch: String?
    ): Response<List<HymnDto>>

    @POST(Endpoints.GENERATE_SCHEDULE_PATH)
    suspend fun getMonthSchedule(
        @Header("If-None-Match") ifNoneMatch: String? = null
    ): Response<MonthScheduleDto>

    // Públicas: sem marcação
    @POST(Endpoints.AUTH_LOGIN_PATH)
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthTokens>

    @POST(Endpoints.AUTH_REGISTER_PATH)
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthTokens>

    @POST(Endpoints.AUTH_REFRESH_PATH)
    suspend fun refresh(
        @Body request: RefreshRequest
    ): Response<AuthTokens>

    // Protegidas: marcar explicitamente
    @Headers("Requires-Auth: true")
    @Multipart
    @POST(Endpoints.ME_PROFILE_PHOTO_PATH)
    suspend fun uploadProfilePhoto(
        @Part photo: MultipartBody.Part
    ): Response<ProfilePhotoResponseDto>

    @Headers("Requires-Auth: true")
    @DELETE(Endpoints.ME_PROFILE_PHOTO_PATH)
    suspend fun deleteProfilePhoto(): Response<Unit>


    @Headers("Requires-Auth: true")
    @GET(Endpoints.ME_PROFILE_PATH)
    suspend fun getMeProfile(
        @Header("If-None-Match") ifNoneMatch: String? = null
    ): Response<MeProfileDto>
}
