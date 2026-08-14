package com.ipb.castelobranco.features.admin.reports.hymnal.data.mapper

import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.OccurrenceDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.OccurrencesResponseDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.TopHymnDto
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BucketGranularity
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.DateRange
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnOccurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.OccurrenceReport
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.TopHymn
import java.time.LocalDate

/**
 * The response order is meaningful — `occurred_on` ascending, then service start time, then hymn
 * number — and the evolution chart renders in it, so nothing here re-sorts.
 */
fun OccurrencesResponseDto.toDomain(
    range: DateRange,
    granularity: BucketGranularity,
): OccurrenceReport = OccurrenceReport(
    range = range,
    granularity = granularity,
    occurrences = occurrences.map { it.toDomain() },
)

fun OccurrenceDto.toDomain(): HymnOccurrence = HymnOccurrence(
    hymnNumber = hymnNumber,
    hymnTitle = hymnTitle,
    occurredOn = LocalDate.parse(occurredOn),
    serviceWindowId = serviceWindowId,
    serviceWindowName = serviceWindowName,
    bucket = bucket,
    deviceCount = deviceCount,
)

fun TopHymnDto.toDomain(): TopHymn = TopHymn(
    number = hymnNumber,
    title = hymnTitle,
    occurrenceCount = occurrenceCount,
)
