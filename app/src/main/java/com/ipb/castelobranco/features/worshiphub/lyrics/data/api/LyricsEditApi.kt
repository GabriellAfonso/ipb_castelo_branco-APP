package com.ipb.castelobranco.features.worshiphub.lyrics.data.api

import com.ipb.castelobranco.core.network.CreateLyricsRequest
import com.ipb.castelobranco.core.network.UpdateContentRequest
import com.ipb.castelobranco.features.worshiphub.lyrics.data.dto.LyricsDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface LyricsEditApi {

    @POST(LyricsEndpoint.LYRICS_PATH)
    suspend fun createLyrics(@Body body: CreateLyricsRequest): Response<LyricsDto>

    @PATCH("${LyricsEndpoint.LYRICS_PATH}{id}/")
    suspend fun updateContent(
        @Path("id") id: Int,
        @Body body: UpdateContentRequest,
    ): Response<LyricsDto>
}
