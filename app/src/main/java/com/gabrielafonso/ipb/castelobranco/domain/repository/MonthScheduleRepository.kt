package com.gabrielafonso.ipb.castelobranco.domain.repository

import com.gabrielafonso.ipb.castelobranco.domain.model.MonthSchedule
import kotlinx.coroutines.flow.Flow

interface MonthScheduleRepository {
    fun observeMonthSchedule(): Flow<MonthSchedule?>
    suspend fun refreshMonthSchedule(): Boolean
}
