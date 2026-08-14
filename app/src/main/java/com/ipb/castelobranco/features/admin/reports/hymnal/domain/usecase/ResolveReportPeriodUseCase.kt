package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.core.domain.util.DateProvider
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BucketGranularity
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.DateRange
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ReportPeriod
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

/** A period turned into everything a request and a comparison need. */
data class ResolvedPeriod(
    val range: DateRange,
    val granularity: BucketGranularity,
    /** The range of the same length immediately before, for the highlight deltas. */
    val precedingRange: DateRange,
)

sealed interface PeriodResolution {
    data class Resolved(val period: ResolvedPeriod) : PeriodResolution

    /** Refused locally, before any request, with the rule that was broken. */
    data class Invalid(val message: String) : PeriodResolution
}

/**
 * Turns the administrator's choice into an inclusive range, a bucket granularity and the
 * preceding range — against a day that is a parameter, never `LocalDate.now()`.
 *
 * The granularity is decided here rather than offered as a fourth selector: `group_by` changes
 * only the bucket label, never how many occurrences there are, so exposing it would give the
 * administrator a knob whose only effect is the width of a bar and invite the reading that
 * different groupings mean different totals.
 */
class ResolveReportPeriodUseCase @Inject constructor(
    private val dateProvider: DateProvider,
) {

    operator fun invoke(period: ReportPeriod): PeriodResolution {
        val today = dateProvider.today()
        val range = when (period) {
            ReportPeriod.ThisWeek -> DateRange(startOfWeek(today), today)
            ReportPeriod.ThisMonth -> DateRange(today.withDayOfMonth(1), today)
            ReportPeriod.ThisYear -> DateRange(today.withDayOfYear(1), today)
            is ReportPeriod.Custom -> DateRange(period.from, period.to)
        }

        if (range.from.isAfter(range.to)) return PeriodResolution.Invalid(REVERSED)
        if (range.days > DateRange.MAX_DAYS) return PeriodResolution.Invalid(TOO_LONG)

        return PeriodResolution.Resolved(
            ResolvedPeriod(
                range = range,
                granularity = granularityFor(period, range),
                precedingRange = range.preceding(),
            )
        )
    }

    /** Brazilian weeks start on Sunday, and the report never reaches into the future. */
    private fun startOfWeek(today: LocalDate): LocalDate =
        today.minusDays((today.dayOfWeek.value % DayOfWeek.entries.size).toLong())

    private fun granularityFor(period: ReportPeriod, range: DateRange): BucketGranularity =
        when (period) {
            ReportPeriod.ThisWeek, ReportPeriod.ThisMonth -> BucketGranularity.DAY
            ReportPeriod.ThisYear -> BucketGranularity.MONTH
            is ReportPeriod.Custom -> when {
                range.days <= DAILY_MAX_DAYS -> BucketGranularity.DAY
                range.days <= WEEKLY_MAX_DAYS -> BucketGranularity.WEEK
                else -> BucketGranularity.MONTH
            }
        }

    private companion object {
        const val DAILY_MAX_DAYS = 31
        const val WEEKLY_MAX_DAYS = 120

        const val REVERSED = "A data inicial não pode ser depois da final."
        const val TOO_LONG = "O período não pode passar de ${DateRange.MAX_DAYS} dias."
    }
}
