package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.fixedDateProvider
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BucketGranularity
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.DateRange
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ReportPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ResolveReportPeriodUseCaseTest {

    /** A Wednesday, so "esta semana" has a Sunday behind it and days ahead of it. */
    private val wednesday = LocalDate.of(2026, 8, 12)

    private fun useCase(today: LocalDate = wednesday) =
        ResolveReportPeriodUseCase(fixedDateProvider(today))

    private fun resolved(period: ReportPeriod, today: LocalDate = wednesday): ResolvedPeriod {
        val resolution = useCase(today)(period)
        assertTrue(resolution is PeriodResolution.Resolved)
        return (resolution as PeriodResolution.Resolved).period
    }

    // region presets

    @Test
    fun `this week runs from Sunday to today`() {
        val period = resolved(ReportPeriod.ThisWeek)

        assertEquals(LocalDate.of(2026, 8, 9), period.range.from)
        assertEquals(wednesday, period.range.to)
        assertEquals(BucketGranularity.DAY, period.granularity)
    }

    @Test
    fun `this week on a Sunday is a single day`() {
        val sunday = LocalDate.of(2026, 8, 16)

        val period = resolved(ReportPeriod.ThisWeek, today = sunday)

        assertEquals(sunday, period.range.from)
        assertEquals(1, period.range.days)
    }

    @Test
    fun `this month runs from the first of the month to today, by day`() {
        val period = resolved(ReportPeriod.ThisMonth)

        assertEquals(LocalDate.of(2026, 8, 1), period.range.from)
        assertEquals(wednesday, period.range.to)
        assertEquals(BucketGranularity.DAY, period.granularity)
    }

    @Test
    fun `this year runs from January first to today, by month`() {
        val period = resolved(ReportPeriod.ThisYear)

        assertEquals(LocalDate.of(2026, 1, 1), period.range.from)
        assertEquals(wednesday, period.range.to)
        assertEquals(BucketGranularity.MONTH, period.granularity)
    }

    @Test
    fun `no preset ever reaches into the future`() {
        listOf(ReportPeriod.ThisWeek, ReportPeriod.ThisMonth, ReportPeriod.ThisYear)
            .forEach { assertEquals(wednesday, resolved(it).range.to) }
    }

    // endregion

    // region custom granularity

    @Test
    fun `a custom range of a month or less is grouped by day`() {
        val period = resolved(
            ReportPeriod.Custom(LocalDate.of(2026, 7, 15), LocalDate.of(2026, 8, 12))
        )

        assertEquals(BucketGranularity.DAY, period.granularity)
    }

    @Test
    fun `a custom range of a few months is grouped by week`() {
        val period = resolved(
            ReportPeriod.Custom(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 8, 12))
        )

        assertEquals(BucketGranularity.WEEK, period.granularity)
    }

    @Test
    fun `a long custom range is grouped by month`() {
        val period = resolved(
            ReportPeriod.Custom(LocalDate.of(2025, 9, 1), LocalDate.of(2026, 8, 12))
        )

        assertEquals(BucketGranularity.MONTH, period.granularity)
    }

    // endregion

    // region preceding range

    @Test
    fun `the preceding range has the same length and ends the day before`() {
        val period = resolved(
            ReportPeriod.Custom(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 10))
        )

        assertEquals(10, period.range.days)
        assertEquals(LocalDate.of(2026, 7, 22), period.precedingRange.from)
        assertEquals(LocalDate.of(2026, 7, 31), period.precedingRange.to)
        assertEquals(10, period.precedingRange.days)
    }

    // endregion

    // region local validation

    @Test
    fun `a reversed custom range is refused before any request`() {
        val resolution = useCase()(
            ReportPeriod.Custom(LocalDate.of(2026, 8, 12), LocalDate.of(2026, 8, 1))
        )

        assertTrue(resolution is PeriodResolution.Invalid)
        assertEquals(
            "A data inicial não pode ser depois da final.",
            (resolution as PeriodResolution.Invalid).message,
        )
    }

    @Test
    fun `a custom range longer than the maximum is refused and names the limit`() {
        val from = LocalDate.of(2025, 1, 1)
        val to = from.plusDays(DateRange.MAX_DAYS.toLong())

        val resolution = useCase()(ReportPeriod.Custom(from, to))

        assertTrue(resolution is PeriodResolution.Invalid)
        assertTrue((resolution as PeriodResolution.Invalid).message.contains("366"))
    }

    @Test
    fun `a custom range of exactly the maximum is accepted`() {
        val from = LocalDate.of(2025, 1, 1)
        val to = from.plusDays((DateRange.MAX_DAYS - 1).toLong())

        val resolution = useCase()(ReportPeriod.Custom(from, to))

        assertTrue(resolution is PeriodResolution.Resolved)
        assertEquals(DateRange.MAX_DAYS, (resolution as PeriodResolution.Resolved).period.range.days)
    }

    @Test
    fun `a single-day custom range is valid`() {
        val day = LocalDate.of(2026, 8, 12)

        val resolution = useCase()(ReportPeriod.Custom(day, day))

        assertTrue(resolution is PeriodResolution.Resolved)
    }

    // endregion
}
