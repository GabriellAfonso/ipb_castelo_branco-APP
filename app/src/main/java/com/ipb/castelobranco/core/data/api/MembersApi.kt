package com.ipb.castelobranco.core.data.api

import com.ipb.castelobranco.core.data.dto.BirthdaysResponseDto
import com.ipb.castelobranco.core.network.ApiConstants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MembersApi {

    @GET(MembersEndpoints.BIRTHDAYS)
    suspend fun getBirthdays(
        @Query("month") month: Int,
        @Header("If-None-Match") ifNoneMatch: String? = null,
    ): Response<BirthdaysResponseDto>
}

object MembersEndpoints {
    const val BIRTHDAYS = "${ApiConstants.BASE_PATH}members/birthdays/"
}
