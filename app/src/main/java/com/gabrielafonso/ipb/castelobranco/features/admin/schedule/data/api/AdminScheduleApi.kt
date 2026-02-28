package com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.api

import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.dto.GenerateScheduleRequestDto
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.dto.GenerateScheduleResponseDto
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.dto.MemberListDto
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.dto.SaveScheduleRequestDto
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.dto.MonthScheduleDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AdminScheduleApi {

    @GET(AdminScheduleEndpoints.MEMBERS)
    suspend fun getMembers(): MemberListDto

    @POST(AdminScheduleEndpoints.GENERATE)
    suspend fun generateSchedule(
        @Body body: GenerateScheduleRequestDto
    ): GenerateScheduleResponseDto

   @POST(AdminScheduleEndpoints.SAVE)
    suspend fun saveSchedule(@Body body: SaveScheduleRequestDto): Response<Unit>
}