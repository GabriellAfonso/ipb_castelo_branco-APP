package com.gabrielafonso.ipb.castelobranco.features.admin.register.data.mapper

import com.gabrielafonso.ipb.castelobranco.features.admin.register.data.dto.RegisterSundayPlayItemDto
import com.gabrielafonso.ipb.castelobranco.features.admin.register.data.dto.RegisterSundayPlaysRequestDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SundayPlayPushItem
import javax.inject.Inject

class RegisterSundayPlaysRequestMapper @Inject constructor() {

    fun map(date: String, plays: List<SundayPlayPushItem>): RegisterSundayPlaysRequestDto =
        RegisterSundayPlaysRequestDto(
            date = date,
            plays = plays.map {
                RegisterSundayPlayItemDto(
                    songId = it.songId,
                    position = it.position,
                    tone = it.tone
                )
            }
        )
}