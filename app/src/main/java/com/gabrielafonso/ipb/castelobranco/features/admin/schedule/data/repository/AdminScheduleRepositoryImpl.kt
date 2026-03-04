package com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.repository

import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.api.AdminScheduleApi
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.dto.GenerateScheduleRequestDto
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.dto.SaveScheduleItemDto
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.dto.SaveScheduleRequestDto
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.mapper.toDomain
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.domain.model.Member
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.domain.model.ScheduleType
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.presentation.state.EditableScheduleUiState
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.domain.repository.AdminScheduleRepository
import com.gabrielafonso.ipb.castelobranco.core.domain.error.AppError
import com.gabrielafonso.ipb.castelobranco.core.domain.error.mapError
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminScheduleRepositoryImpl @Inject constructor(
    private val api: AdminScheduleApi
) : AdminScheduleRepository {
    override suspend fun getMembers(): Result<List<Member>> = runCatching {
        api.getMembers().members.map { it.toDomain() }
    }.mapError()

    override suspend fun generateSchedule(year: Int, month: Int): Result<List<EditableScheduleUiState>> = runCatching {
        val response = api.generateSchedule(
            GenerateScheduleRequestDto(year = year, month = month)
        )
        response.items.map { item ->
            EditableScheduleUiState(
                date = item.date,
                day = item.day,
                scheduleTypeName = item.scheduleType.name,
                scheduleTypeId = ScheduleType.idByName(item.scheduleType.name),
                selectedMember = Member(item.member.id, item.member.name),
                memberQuery = item.member.name
            )
        }
    }.mapError()

    override suspend fun saveSchedule(year: Int, month: Int, items: List<EditableScheduleUiState>): Result<Unit> = runCatching {
        val body = SaveScheduleRequestDto(
            year = year,
            month = month,
            items = items.map { item ->
                SaveScheduleItemDto(
                    date = item.date,
                    scheduleTypeId = item.scheduleTypeId,
                    memberId = item.selectedMember!!.id
                )
            }
        )
        val response = api.saveSchedule(body)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            val errorMessage = try {
                JSONObject(errorBody).getString("error")
            } catch (e: Exception) {
                errorBody ?: "Erro desconhecido do servidor"
            }
            throw AppError.Server(code = response.code(), message = errorMessage)
        }
    }.mapError()
}