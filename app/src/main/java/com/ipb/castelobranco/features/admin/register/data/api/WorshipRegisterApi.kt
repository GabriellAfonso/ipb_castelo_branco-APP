package com.ipb.castelobranco.features.admin.register.data.api

import com.ipb.castelobranco.core.network.ApiConstants
import com.ipb.castelobranco.features.admin.register.data.dto.RegisterSundayPlaysRequestDto
import com.ipb.castelobranco.features.admin.register.data.dto.RegisterSundayPlaysResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

private object WorshipRegisterEndpoints {
    const val REGISTER_SUNDAY_PLAYS_PATH = "${ApiConstants.BASE_PATH}played/register/"
}

interface WorshipRegisterApi {

    @POST(WorshipRegisterEndpoints.REGISTER_SUNDAY_PLAYS_PATH)
    suspend fun registerSundayPlays(
        @Body body: RegisterSundayPlaysRequestDto
    ): Response<RegisterSundayPlaysResponseDto>
}