package com.gabrielafonso.ipb.castelobranco.features.admin.schedule.domain.repository

import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.domain.model.Member
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.presentation.state.EditableScheduleUiState

interface AdminScheduleRepository {
    suspend fun getMembers(): Result<List<Member>>
    suspend fun generateSchedule(year: Int, month: Int): Result<List<EditableScheduleUiState>>
    suspend fun saveSchedule(year: Int, month: Int, items: List<EditableScheduleUiState>): Result<Unit>
}