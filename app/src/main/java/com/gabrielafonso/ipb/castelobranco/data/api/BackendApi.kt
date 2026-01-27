// app/src/main/java/com/gabrielafonso/ipb/castelobranco/data/api/SongsApi.kt
package com.gabrielafonso.ipb.castelobranco.data.api

import com.gabrielafonso.ipb.castelobranco.core.network.Endpoints
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

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
}
