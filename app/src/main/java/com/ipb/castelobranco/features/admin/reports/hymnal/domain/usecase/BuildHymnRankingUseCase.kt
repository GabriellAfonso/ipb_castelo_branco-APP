package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BarPoint
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnOccurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnRanking
import javax.inject.Inject

/**
 * How many times each hymn was sung, ordered by that count.
 *
 * The occurrence count is the bar; device reach is a label beside it and never a length or an
 * ordering — twenty phones on one hymn is one singing with a reach of twenty, not twenty
 * singings.
 */
class BuildHymnRankingUseCase @Inject constructor() {

    operator fun invoke(
        occurrences: List<HymnOccurrence>,
        limit: Int = HymnRanking.DEFAULT_LIMIT,
    ): HymnRanking {
        if (occurrences.isEmpty()) return HymnRanking(bars = emptyList(), totalHymns = 0)

        val byHymn = occurrences.groupBy { it.hymnNumber }
        val ranked = byHymn.entries
            .map { (number, its) ->
                RankedHymn(
                    number = number,
                    title = its.first().hymnTitle,
                    occurrenceCount = its.size,
                    reach = its.sumOf { it.deviceCount },
                )
            }
            .sortedWith(RANKING_ORDER)

        val top = ranked.take(limit)
        val max = top.first().occurrenceCount

        return HymnRanking(
            bars = top.map { it.toBarPoint(max) },
            totalHymns = ranked.size,
        )
    }

    private data class RankedHymn(
        val number: String,
        val title: String,
        val occurrenceCount: Int,
        val reach: Int,
    ) {
        fun toBarPoint(max: Int): BarPoint = BarPoint(
            label = "$number · $title",
            value = occurrenceCount,
            secondaryLabel = reachLabel(reach),
            fraction = fractionOf(occurrenceCount, max),
            hymnNumber = number,
        )
    }

    private companion object {

        /**
         * Count descending, then hymn number ascending — numerically when the number is one, so
         * hymn 50 does not sort after hymn 120 the way a plain string comparison would.
         */
        val RANKING_ORDER: Comparator<RankedHymn> =
            compareByDescending<RankedHymn> { it.occurrenceCount }
                .thenBy { it.number.toIntOrNull() ?: Int.MAX_VALUE }
                .thenBy { it.number }

        fun fractionOf(value: Int, max: Int): Float =
            if (max <= 0) BarPoint.MIN_FRACTION
            else (value.toFloat() / max).coerceIn(BarPoint.MIN_FRACTION, 1f)

        fun reachLabel(reach: Int): String =
            if (reach == 1) "1 aparelho" else "$reach aparelhos"
    }
}
