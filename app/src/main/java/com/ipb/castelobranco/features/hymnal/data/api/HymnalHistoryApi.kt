package com.ipb.castelobranco.features.hymnal.data.api

import com.ipb.castelobranco.features.hymnal.data.dto.HistorySettingsDto
import com.ipb.castelobranco.features.hymnal.data.dto.IngestRequestDto
import com.ipb.castelobranco.features.hymnal.data.dto.IngestResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface HymnalHistoryApi {

    @POST(HymnalHistoryEndpoints.EVENTS_PATH)
    suspend fun submitEvents(@Body body: IngestRequestDto): Response<IngestResponseDto>

    @GET(HymnalHistoryEndpoints.SETTINGS_PATH)
    suspend fun getSettings(): Response<HistorySettingsDto>
}
