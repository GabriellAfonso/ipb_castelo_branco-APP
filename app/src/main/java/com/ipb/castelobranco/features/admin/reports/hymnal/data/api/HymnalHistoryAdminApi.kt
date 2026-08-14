package com.ipb.castelobranco.features.admin.reports.hymnal.data.api

import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.CollectionSettingsDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.CollectionSettingsPatchDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.ServiceWindowDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.ServiceWindowListDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.ServiceWindowWriteDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Settings and service windows, all as an administrator.
 *
 * The settings `GET` is public on the service, but this caller is administrative and uses the
 * authenticated client. The anonymous read the invisible collection performs lives in
 * `features/hymnal` and deliberately stays a separate interface.
 */
interface HymnalHistoryAdminApi {

    @GET(HymnalReportEndpoints.SETTINGS_PATH)
    suspend fun getSettings(): Response<CollectionSettingsDto>

    @PATCH(HymnalReportEndpoints.SETTINGS_PATH)
    suspend fun patchSettings(
        @Body body: CollectionSettingsPatchDto,
    ): Response<CollectionSettingsDto>

    @GET(HymnalReportEndpoints.SERVICE_WINDOWS_PATH)
    suspend fun getServiceWindows(): Response<ServiceWindowListDto>

    @POST(HymnalReportEndpoints.SERVICE_WINDOWS_PATH)
    suspend fun createServiceWindow(
        @Body body: ServiceWindowWriteDto,
    ): Response<ServiceWindowDto>

    @PATCH(HymnalReportEndpoints.SERVICE_WINDOW_DETAIL_PATH)
    suspend fun updateServiceWindow(
        @Path("id") id: Int,
        @Body body: ServiceWindowWriteDto,
    ): Response<ServiceWindowDto>

    @DELETE(HymnalReportEndpoints.SERVICE_WINDOW_DETAIL_PATH)
    suspend fun deleteServiceWindow(@Path("id") id: Int): Response<Unit>
}
