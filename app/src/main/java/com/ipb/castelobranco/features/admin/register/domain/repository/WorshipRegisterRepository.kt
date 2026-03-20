package com.ipb.castelobranco.features.admin.register.domain.repository

import com.ipb.castelobranco.features.worshiphub.tables.domain.model.SundayPlayPushItem

interface WorshipRegisterRepository {
    suspend fun pushSundayPlays(
        date: String,
        plays: List<SundayPlayPushItem>
    ): Result<Unit>
}