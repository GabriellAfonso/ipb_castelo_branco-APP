package com.gabrielafonso.ipb.castelobranco.features.schedule.data.repository

import com.gabrielafonso.ipb.castelobranco.core.data.repository.base.BaseSingleSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.data.api.BackendApi
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.dto.MonthScheduleDto
import com.gabrielafonso.ipb.castelobranco.data.local.JsonSnapshotStorage
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.MonthSchedule
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.ScheduleEntry
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.ScheduleItem
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val api: BackendApi,
    private val jsonStorage: JsonSnapshotStorage
) : ScheduleRepository {

    companion object {
        private const val KEY_MONTH_SCHEDULE = "month_schedule_current"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        encodeDefaults = true
    }

    private val base = object : BaseSingleSnapshotRepository<MonthScheduleDto, MonthSchedule>(
        json = json,
        jsonStorage = jsonStorage,
        dtoSerializer = MonthScheduleDto.serializer(),
        key = KEY_MONTH_SCHEDULE,
        tag = "observeMonthSchedule",
        fetchNetwork = { ifNoneMatch -> api.getMonthSchedule(ifNoneMatch) }
    ) {
        override fun mapToDomain(dto: MonthScheduleDto): MonthSchedule =
            MonthSchedule(
                year = dto.year,
                month = dto.month,
                schedule = dto.schedule.mapValues { (_, entryDto) ->
                    ScheduleEntry(
                        time = entryDto.time,
                        items = entryDto.items.map { i ->
                            ScheduleItem(
                                day = i.day,
                                member = i.member.name
                            )
                        }
                    )
                }
            )
    }

    override fun observeMonthSchedule(): Flow<MonthSchedule?> = base.observeSnapshot()

    override suspend fun refreshMonthSchedule(): Boolean = base.refreshSnapshot()
}