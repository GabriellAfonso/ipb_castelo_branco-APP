package com.ipb.castelobranco.features.admin.reports.hymnal.data.api

import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.OccurrencesResponseDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.TopHymnsResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * The two reading endpoints. Both are administrator-only and therefore built from
 * `@AuthedRetrofit`.
 */
interface HymnalReportApi {

    /**
     * `from` and `to` are always sent explicitly rather than relying on the service default, so
     * what is on screen and what was asked for cannot drift.
     */
    @GET(HymnalReportEndpoints.OCCURRENCES_PATH)
    suspend fun getOccurrences(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("group_by") groupBy: String,
    ): Response<OccurrencesResponseDto>

    /**
     * No date parameters: omitting both covers all recorded history, which is the only reason
     * this endpoint is used here. The 366-day cap does not apply to it.
     */
    @GET(HymnalReportEndpoints.TOP_HYMNS_PATH)
    suspend fun getAllTimeTopHymns(): Response<TopHymnsResponseDto>
}
