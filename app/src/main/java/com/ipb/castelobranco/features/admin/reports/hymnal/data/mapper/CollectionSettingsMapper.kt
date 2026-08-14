package com.ipb.castelobranco.features.admin.reports.hymnal.data.mapper

import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.CollectionSettingsDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.CollectionSettingsPatchDto
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.CollectionSettings

fun CollectionSettingsDto.toDomain(): CollectionSettings = CollectionSettings(
    minSecondsToCount = minSecondsToCount,
    collapseWindowMinutes = collapseWindowMinutes,
    maxBatchSize = maxBatchSize,
    maxPastDays = maxPastDays,
    futureToleranceMinutes = futureToleranceMinutes,
    windowGraceMinutes = windowGraceMinutes,
)

/**
 * The partial patch: a field that did not change stays `null` and, because the shared `Json` is
 * built with `explicitNulls = false`, never reaches the wire at all.
 *
 * @receiver the settings currently on the server
 * @param updated what the administrator wants
 */
fun CollectionSettings.diff(updated: CollectionSettings): CollectionSettingsPatchDto =
    CollectionSettingsPatchDto(
        minSecondsToCount = updated.minSecondsToCount.takeIf { it != minSecondsToCount },
        collapseWindowMinutes = updated.collapseWindowMinutes.takeIf { it != collapseWindowMinutes },
        maxBatchSize = updated.maxBatchSize.takeIf { it != maxBatchSize },
        maxPastDays = updated.maxPastDays.takeIf { it != maxPastDays },
        futureToleranceMinutes =
            updated.futureToleranceMinutes.takeIf { it != futureToleranceMinutes },
        windowGraceMinutes = updated.windowGraceMinutes.takeIf { it != windowGraceMinutes },
    )
