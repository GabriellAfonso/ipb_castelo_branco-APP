package com.ipb.castelobranco.features.hymnal.data.mapper

import com.ipb.castelobranco.features.hymnal.data.dto.HistorySettingsDto
import com.ipb.castelobranco.features.hymnal.data.dto.HymnViewEventDto
import com.ipb.castelobranco.features.hymnal.data.local.QueuedHymnViewEvent
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewCollectionSettings
import com.ipb.castelobranco.features.hymnal.domain.model.RejectionReason

fun QueuedHymnViewEvent.toWireDto(): HymnViewEventDto =
    HymnViewEventDto(
        clientEventId = clientEventId,
        hymnId = hymnId,
        deviceId = deviceId,
        viewedAt = viewedAt,
        durationSeconds = durationSeconds,
        appVersion = appVersion,
        platform = platform,
    )

fun HistorySettingsDto.toDomain(): HymnViewCollectionSettings =
    HymnViewCollectionSettings.sanitized(
        minSecondsToCount = minSecondsToCount,
        maxBatchSize = maxBatchSize,
    )

fun String.toRejectionReason(): RejectionReason = RejectionReason.fromCode(this)
