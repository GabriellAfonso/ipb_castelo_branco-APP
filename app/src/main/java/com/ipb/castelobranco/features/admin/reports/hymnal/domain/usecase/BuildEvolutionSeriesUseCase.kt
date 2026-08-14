package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BarPoint
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BucketGranularity
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.EvolutionSeries
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnOccurrence
import javax.inject.Inject

/**
 * Volume over the period, one bar per bucket.
 *
 * The service already returns occurrences in chronological order, so grouping preserves that
 * order and nothing here re-sorts: a chart that re-sorts its own X axis stops being a timeline.
 */
class BuildEvolutionSeriesUseCase @Inject constructor() {

    operator fun invoke(
        occurrences: List<HymnOccurrence>,
        granularity: BucketGranularity,
    ): EvolutionSeries {
        if (occurrences.isEmpty()) {
            return EvolutionSeries(bars = emptyList(), granularity = granularity)
        }

        val buckets = occurrences.groupBy { it.bucket }
        val max = buckets.values.maxOf { it.size }

        val bars = buckets.map { (bucket, its) ->
            val reach = its.sumOf { it.deviceCount }
            BarPoint(
                label = labelFor(bucket, granularity),
                value = its.size,
                secondaryLabel = if (reach == 1) "1 aparelho" else "$reach aparelhos",
                fraction = (its.size.toFloat() / max).coerceIn(BarPoint.MIN_FRACTION, 1f),
            )
        }

        return EvolutionSeries(bars = bars, granularity = granularity)
    }

    /**
     * Bucket labels come off the wire as `2026-08-09`, `2026-W32` or `2026-08`. Anything the
     * service starts emitting that does not match is shown as-is rather than mangled.
     */
    private fun labelFor(bucket: String, granularity: BucketGranularity): String = when (granularity) {
        BucketGranularity.DAY -> bucket.split(DATE_SEPARATOR)
            .takeIf { it.size == DATE_PARTS }
            ?.let { "${it[2]}/${it[1]}" }
            ?: bucket

        BucketGranularity.WEEK -> bucket.substringAfter(WEEK_MARKER, missingDelimiterValue = "")
            .takeIf { it.isNotBlank() }
            ?.let { "Sem $it" }
            ?: bucket

        BucketGranularity.MONTH -> bucket.split(DATE_SEPARATOR)
            .takeIf { it.size == MONTH_PARTS }
            ?.let { parts -> parts[1].toIntOrNull()?.let { MONTH_LABELS.getOrNull(it - 1) } }
            ?: bucket
    }

    private companion object {
        const val DATE_SEPARATOR = "-"
        const val WEEK_MARKER = "-W"
        const val DATE_PARTS = 3
        const val MONTH_PARTS = 2

        val MONTH_LABELS = listOf(
            "jan", "fev", "mar", "abr", "mai", "jun",
            "jul", "ago", "set", "out", "nov", "dez",
        )
    }
}
