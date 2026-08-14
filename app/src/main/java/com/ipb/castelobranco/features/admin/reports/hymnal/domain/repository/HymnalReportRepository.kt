package com.ipb.castelobranco.features.admin.reports.hymnal.domain.repository

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BucketGranularity
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.DateRange
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.OccurrenceReport
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.TopHymn

/**
 * The two reading sources, kept apart on purpose.
 *
 * [getOccurrences] is bounded at [DateRange.MAX_DAYS] but knows about services and dates, so it
 * feeds every sliced and period-bounded reading. [getAllTimeTopHymns] has no date limit but
 * cannot slice and returns no dates, so it feeds only all-time statements. No reading mixes the
 * two into one number.
 */
interface HymnalReportRepository {

    suspend fun getOccurrences(
        range: DateRange,
        granularity: BucketGranularity,
    ): Result<OccurrenceReport>

    suspend fun getAllTimeTopHymns(): Result<List<TopHymn>>
}
