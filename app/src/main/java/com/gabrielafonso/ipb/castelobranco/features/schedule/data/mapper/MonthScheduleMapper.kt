package com.gabrielafonso.ipb.castelobranco.features.schedule.data.mapper

import com.gabrielafonso.ipb.castelobranco.features.schedule.data.dto.MonthScheduleDto
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.MonthSchedule
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.ScheduleEntry
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.ScheduleItem
import javax.inject.Inject

class MonthScheduleMapper @Inject constructor() {

    fun map(dto: MonthScheduleDto): MonthSchedule =
        MonthSchedule(
            year = dto.year,
            month = dto.month,
            schedule = dto.schedule.mapValues { (_, entryDto) ->
                ScheduleEntry(
                    time = entryDto.time,
                    items = entryDto.items.map { itemDto ->
                        ScheduleItem(
                            day = itemDto.day,
                            member = itemDto.member.name
                        )
                    }
                )
            }
        )
}
