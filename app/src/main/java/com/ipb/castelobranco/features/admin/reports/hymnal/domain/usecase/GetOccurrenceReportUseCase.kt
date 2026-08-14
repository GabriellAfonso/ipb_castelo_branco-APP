package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.OccurrenceReport
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.repository.HymnalReportRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * The chosen period plus the one before it, so the highlights can state a change.
 *
 * @param preceding `null` when the preceding range has no data or could not be fetched — the
 *   highlights then render without deltas rather than inventing a comparison.
 */
data class OccurrenceReportBundle(
    val current: OccurrenceReport,
    val preceding: OccurrenceReport?,
)

/**
 * The only call in the whole report that reaches the network, and it happens on a period change
 * and nowhere else. Both ranges are fetched concurrently; the preceding one failing never fails
 * the report.
 */
class GetOccurrenceReportUseCase @Inject constructor(
    private val repository: HymnalReportRepository,
) {

    suspend operator fun invoke(period: ResolvedPeriod): Result<OccurrenceReportBundle> =
        coroutineScope {
            val current = async {
                repository.getOccurrences(period.range, period.granularity)
            }
            val preceding = async {
                repository.getOccurrences(period.precedingRange, period.granularity)
            }

            val currentResult = current.await()
            val precedingResult = preceding.await()

            currentResult.map { report ->
                OccurrenceReportBundle(
                    current = report,
                    preceding = precedingResult.getOrNull()?.takeIf { it.occurrences.isNotEmpty() },
                )
            }
        }
}
