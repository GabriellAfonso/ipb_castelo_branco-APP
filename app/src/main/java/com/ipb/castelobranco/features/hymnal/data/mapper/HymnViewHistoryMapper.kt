package com.ipb.castelobranco.features.hymnal.data.mapper

import com.ipb.castelobranco.features.hymnal.data.local.QueuedHymnViewEvent
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewEvent
import java.time.OffsetDateTime

fun HymnViewEvent.toQueued(): QueuedHymnViewEvent =
    QueuedHymnViewEvent(
        clientEventId = clientEventId,
        hymnId = hymnId,
        deviceId = deviceId,
        viewedAt = viewedAt.toString(),
        durationSeconds = durationSeconds,
        appVersion = appVersion,
        platform = platform,
    )

fun QueuedHymnViewEvent.toDomain(): HymnViewEvent =
    HymnViewEvent(
        clientEventId = clientEventId,
        hymnId = hymnId,
        deviceId = deviceId,
        viewedAt = OffsetDateTime.parse(viewedAt),
        durationSeconds = durationSeconds,
        appVersion = appVersion,
        platform = platform,
    )
