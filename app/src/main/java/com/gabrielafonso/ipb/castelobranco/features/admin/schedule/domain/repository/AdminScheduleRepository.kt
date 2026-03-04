package com.gabrielafonso.ipb.castelobranco.features.admin.schedule.domain.repository

import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.domain.model.EditableScheduleItem
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.domain.model.Member

interface AdminScheduleRepository {
    suspend fun getMembers(): Result<List<Member>>
    suspend fun generateSchedule(year: Int, month: Int): Result<List<EditableScheduleItem>>
    suspend fun saveSchedule(year: Int, month: Int, items: List<EditableScheduleItem>): Result<Unit>
}