package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.CalendarMonth
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.EvolutionSeries
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.Highlight
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnRanking
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.InVsOutside
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.OccurrenceReport
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ReportEmptyReason
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ReportSlice
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceBulletin
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindow
import javax.inject.Inject

/** Every period-bounded reading of one slice, computed together over data already in memory. */
data class ReportReadings(
    val highlights: List<Highlight>,
    val ranking: HymnRanking,
    val evolution: EvolutionSeries,
    val bulletins: List<ServiceBulletin>,
    val calendar: List<CalendarMonth>,
    val inVsOutside: InVsOutside,
    val emptyReason: ReportEmptyReason?,
)

/**
 * Composes the individual readings so the ViewModel depends on one entry point instead of seven,
 * while each reading stays its own testable use case.
 *
 * Everything here is pure and runs over occurrences already loaded: switching the slice or the
 * reading must never reach the network.
 */
class BuildReportReadingsUseCase @Inject constructor(
    private val filterOccurrences: FilterOccurrencesUseCase,
    private val buildHighlights: BuildHighlightsUseCase,
    private val buildRanking: BuildHymnRankingUseCase,
    private val buildEvolution: BuildEvolutionSeriesUseCase,
    private val buildBulletins: BuildServiceBulletinsUseCase,
    private val buildCalendar: BuildCalendarUseCase,
    private val buildInVsOutside: BuildInVsOutsideUseCase,
    private val resolveEmptyReason: ResolveEmptyReasonUseCase,
) {

    operator fun invoke(
        bundle: OccurrenceReportBundle,
        slice: ReportSlice,
        serviceWindows: List<ServiceWindow>,
        rankingLimit: Int = HymnRanking.DEFAULT_LIMIT,
        collectionIsEmpty: Boolean? = null,
    ): ReportReadings {
        val report: OccurrenceReport = bundle.current
        val sliced = filterOccurrences(report.occurrences, slice)
        val slicedPreceding = bundle.preceding?.let { filterOccurrences(it.occurrences, slice) }

        return ReportReadings(
            highlights = buildHighlights(sliced, slicedPreceding),
            ranking = buildRanking(sliced, rankingLimit),
            evolution = buildEvolution(sliced, report.granularity),
            bulletins = buildBulletins(sliced),
            calendar = buildCalendar(sliced, report.range),
            inVsOutside = buildInVsOutside(report.occurrences),
            emptyReason = resolveEmptyReason(
                periodOccurrences = report.occurrences,
                slicedOccurrences = sliced,
                slice = slice,
                serviceWindows = serviceWindows,
                collectionIsEmpty = collectionIsEmpty,
            ),
        )
    }
}
