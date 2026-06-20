package com.ipb.castelobranco.core.data.mapper

import com.ipb.castelobranco.core.data.dto.BirthdaysResponseDto
import com.ipb.castelobranco.core.domain.model.Birthday

fun BirthdaysResponseDto.toDomain(): List<Birthday> =
    birthdays
        .filter { it.name.isNotBlank() }
        .map { Birthday(name = it.name, day = it.birthDay) }
        .sortedBy { it.day }
