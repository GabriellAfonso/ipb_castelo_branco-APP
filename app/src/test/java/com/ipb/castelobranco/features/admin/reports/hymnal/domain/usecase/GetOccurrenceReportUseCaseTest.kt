package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.core.domain.error.AppError
import com.ipb.castelobranco.features.admin.reports.hymnal.FakeHymnalReportRepository
import com.ipb.castelobranco.features.admin.reports.hymnal.TODAY
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BucketGranularity
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.DateRange
import com.ipb.castelobranco.features.admin.reports.hymnal.occurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.report
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetOccurrenceReportUseCaseTest {

    private val range = DateRange(TODAY.minusDays(9), TODAY)
    private val period = ResolvedPeriod(
        range = range,
        granularity = BucketGranularity.DAY,
        precedingRange = range.preceding(),
    )

    @Test
    fun `fetches the chosen range and the preceding one`() = runTest {
        val repository = FakeHymnalReportRepository(
            occurrencesResult = { asked -> Result.success(report(listOf(occurrence()), asked)) }
        )

        val bundle = GetOccurrenceReportUseCase(repository)(period).getOrThrow()

        assertEquals(setOf(range, period.precedingRange), repository.requestedRanges.toSet())
        assertEquals(2, repository.requestedRanges.size)
        assertEquals(1, bundle.current.occurrences.size)
        assertEquals(1, bundle.preceding?.occurrences?.size)
    }

    @Test
    fun `a failing preceding range still yields the chosen one, without deltas`() = runTest {
        val repository = FakeHymnalReportRepository(
            occurrencesResult = { asked ->
                if (asked == range) Result.success(report(listOf(occurrence()), asked))
                else Result.failure(AppError.Network())
            }
        )

        val result = GetOccurrenceReportUseCase(repository)(period)

        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow().preceding)
    }

    @Test
    fun `an empty preceding range is treated as no comparison at all`() = runTest {
        val repository = FakeHymnalReportRepository(
            occurrencesResult = { asked ->
                if (asked == range) Result.success(report(listOf(occurrence()), asked))
                else Result.success(report(emptyList(), asked))
            }
        )

        val bundle = GetOccurrenceReportUseCase(repository)(period).getOrThrow()

        assertNull(bundle.preceding)
    }

    @Test
    fun `a failing chosen range fails the whole call`() = runTest {
        val repository = FakeHymnalReportRepository(
            occurrencesResult = { Result.failure(AppError.Server(code = 500)) }
        )

        val result = GetOccurrenceReportUseCase(repository)(period)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.Server)
    }
}
