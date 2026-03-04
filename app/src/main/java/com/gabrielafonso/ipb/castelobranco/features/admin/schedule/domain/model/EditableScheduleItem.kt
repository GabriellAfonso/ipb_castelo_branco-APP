package com.gabrielafonso.ipb.castelobranco.features.admin.schedule.domain.model

data class EditableScheduleItem(
    val date: String,
    val day: Int,
    val scheduleTypeName: String,
    val scheduleTypeId: Int = 0,
    val selectedMember: Member? = null,
    val memberQuery: String = ""
)
