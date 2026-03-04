package com.gabrielafonso.ipb.castelobranco.features.admin.register.data.repository

import com.gabrielafonso.ipb.castelobranco.features.admin.register.data.api.WorshipRegisterApi
import com.gabrielafonso.ipb.castelobranco.features.admin.register.data.mapper.buildRegisterRequest
import com.gabrielafonso.ipb.castelobranco.features.admin.register.domain.repository.WorshipRegisterRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SundayPlayPushItem
import com.gabrielafonso.ipb.castelobranco.core.domain.error.AppError
import com.gabrielafonso.ipb.castelobranco.core.domain.error.mapError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorshipRegisterRepositoryImpl @Inject constructor(
    private val api: WorshipRegisterApi,
) : WorshipRegisterRepository {

    override suspend fun pushSundayPlays(
        date: String,
        plays: List<SundayPlayPushItem>
    ): Result<Unit> = runCatching {
        val body = buildRegisterRequest(date = date, plays = plays)
        val response = api.registerSundayPlays(body)
        if (!response.isSuccessful) throw AppError.Server(response.code())
        Unit
    }.mapError()
}
