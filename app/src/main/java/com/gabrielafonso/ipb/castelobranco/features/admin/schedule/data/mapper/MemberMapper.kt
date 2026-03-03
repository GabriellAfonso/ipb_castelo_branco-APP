package com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.mapper

import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.data.dto.MemberItemDto
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.domain.model.Member

fun MemberItemDto.toDomain() = Member(id = id, name = name)
