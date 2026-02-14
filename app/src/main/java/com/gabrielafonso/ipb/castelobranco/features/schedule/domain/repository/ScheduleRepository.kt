package com.gabrielafonso.ipb.castelobranco.features.schedule.domain.repository

import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.MonthSchedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun observeMonthSchedule(): Flow<MonthSchedule?>
    suspend fun refreshMonthSchedule(): Boolean
}