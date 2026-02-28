package com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.repository

import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.api.AdminScheduleApi
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.dto.GenerateScheduleRequestDto
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.dto.SaveScheduleItemDto
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.dto.SaveScheduleRequestDto
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.mapper.toDomain
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.domain.model.Member
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.domain.repository.AdminScheduleRepository
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.dto.MonthScheduleDto
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.presentation.state.EditableScheduleItem
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminScheduleRepositoryImpl @Inject constructor(
    private val api: AdminScheduleApi
) : AdminScheduleRepository {
    override suspend fun getMembers(): Result<List<Member>> = runCatching {
        api.getMembers().members.map { it.toDomain() }
    }

    override suspend fun generateSchedule(year: Int, month: Int): Result<List<EditableScheduleItem>> = runCatching {
        val response = api.generateSchedule(
            GenerateScheduleRequestDto(year = year, month = month)
        )
        val scheduleTypeIdByName = mapOf(
            "Terça de Oração" to 1,
            "Quinta de Oração" to 2,
            "Domingo Liturgia de Adoração" to 3,
        )
        response.items.map { item ->
            EditableScheduleItem(
                date = item.date,
                day = item.day,
                scheduleTypeName = item.scheduleType.name,
                scheduleTypeId = scheduleTypeIdByName[item.scheduleType.name] ?: 0,
                selectedMember = Member(item.member.id, item.member.name),
                memberQuery = item.member.name
            )
        }
    }

    override suspend fun saveSchedule(year: Int, month: Int, items: List<EditableScheduleItem>): Result<Unit> {
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
        return try {
            val response = api.saveSchedule(body)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    JSONObject(errorBody).getString("error")
                } catch (e: Exception) {
                    "Erro desconhecido do servidor"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}