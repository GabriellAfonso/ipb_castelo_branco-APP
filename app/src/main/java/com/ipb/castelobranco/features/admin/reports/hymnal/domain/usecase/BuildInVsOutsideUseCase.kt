package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnOccurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.InVsOutside
import javax.inject.Inject

/**
 * What the congregation sings together, beside what it opens alone during the week.
 *
 * These are pastorally different signals, and the second is the novelty the collection brought —
 * which is why it is a reading of its own rather than a filter someone has to know to apply.
 */
class BuildInVsOutsideUseCase @Inject constructor(
    private val buildRanking: BuildHymnRankingUseCase,
) {

    operator fun invoke(occurrences: List<HymnOccurrence>): InVsOutside {
        val (inService, outside) = occurrences.partition { it.serviceWindowId != null }

        val inNumbers = inService.mapTo(mutableSetOf()) { it.hymnNumber }
        val outsideNumbers = outside.mapTo(mutableSetOf()) { it.hymnNumber }

        return InVsOutside(
            inService = buildRanking(inService),
            outsideService = buildRanking(outside),
            onlyInService = (inNumbers - outsideNumbers).toList(),
            onlyOutsideService = (outsideNumbers - inNumbers).toList(),
        )
    }
}
