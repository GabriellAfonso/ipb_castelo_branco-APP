package com.gabrielafonso.ipb.castelobranco.domain.model

data class MonthSchedule(
    val year: Int,
    val month: Int,
    val schedule: Map<String, ScheduleEntry>
)

data class ScheduleEntry(
    val time: String,
    val items: List<ScheduleItem>
)

data class ScheduleItem(
    val day: Int,
    val member: String
)
