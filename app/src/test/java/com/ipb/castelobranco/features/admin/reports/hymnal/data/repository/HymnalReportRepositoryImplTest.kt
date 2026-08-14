package com.ipb.castelobranco.features.admin.reports.hymnal.data.repository

import com.ipb.castelobranco.core.domain.error.AppError
import com.ipb.castelobranco.features.admin.reports.hymnal.TODAY
import com.ipb.castelobranco.features.admin.reports.hymnal.data.api.HymnalReportApi
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.OccurrenceDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.OccurrencesResponseDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.TopHymnDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.TopHymnsResponseDto
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BucketGranularity
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.DateRange
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class HymnalReportRepositoryImplTest {

    private lateinit var api: HymnalReportApi
    private lateinit var repository: HymnalReportRepositoryImpl

    private val range = DateRange(TODAY.minusDays(29), TODAY)

    @Before
    fun setup() {
        api = mockk()
        repository = HymnalReportRepositoryImpl(api)
    }

    // region occurrences

    @Test
    fun `getOccurrences maps the response and keeps the server order`() = runTest {
        coEvery { api.getOccurrences(any(), any(), any()) } returns Response.success(
            OccurrencesResponseDto(
                from = range.from.toString(),
                to = range.to.toString(),
                groupBy = "day",
                occurrences = listOf(
                    OccurrenceDto(
                        hymnNumber = "50",
                        hymnTitle = "Grandioso És Tu",
                        occurredOn = "2026-08-09",
                        serviceWindowId = 3,
                        serviceWindowName = "Culto de Domingo à Noite",
                        bucket = "2026-08-09:3",
                        deviceCount = 27,
                    ),
                    OccurrenceDto(
                        hymnNumber = "120",
                        hymnTitle = "Saudosa Lembrança",
                        occurredOn = "2026-08-12",
                        serviceWindowId = null,
                        serviceWindowName = null,
                        bucket = "2026-08-12:none",
                        deviceCount = 2,
                    ),
                ),
            )
        )

        val result = repository.getOccurrences(range, BucketGranularity.DAY)

        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        assertEquals(listOf("50", "120"), report.occurrences.map { it.hymnNumber })
        assertEquals(27, report.occurrences.first().deviceCount)
    }

    @Test
    fun `getOccurrences preserves a null service window as outside-service use`() = runTest {
        coEvery { api.getOccurrences(any(), any(), any()) } returns Response.success(
            OccurrencesResponseDto(
                from = range.from.toString(),
                to = range.to.toString(),
                groupBy = "day",
                occurrences = listOf(
                    OccurrenceDto(
                        hymnNumber = "120",
                        hymnTitle = "Saudosa Lembrança",
                        occurredOn = "2026-08-12",
                        bucket = "2026-08-12:none",
                        deviceCount = 2,
                    ),
                ),
            )
        )

        val occurrence = repository.getOccurrences(range, BucketGranularity.DAY)
            .getOrThrow().occurrences.single()

        assertNull(occurrence.serviceWindowId)
        assertNull(occurrence.serviceWindowName)
    }

    @Test
    fun `getOccurrences sends the range and the granularity the caller asked for`() = runTest {
        coEvery { api.getOccurrences(any(), any(), any()) } returns Response.success(
            OccurrencesResponseDto(range.from.toString(), range.to.toString(), "month")
        )

        repository.getOccurrences(range, BucketGranularity.MONTH)

        coVerify {
            api.getOccurrences(
                from = range.from.toString(),
                to = range.to.toString(),
                groupBy = "month",
            )
        }
    }

    @Test
    fun `getOccurrences maps an HTTP failure to AppError`() = runTest {
        coEvery { api.getOccurrences(any(), any(), any()) } returns
            Response.error(500, "boom".toResponseBody())

        val result = repository.getOccurrences(range, BucketGranularity.DAY)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is AppError.Server)
        assertEquals(500, (error as AppError.Server).code)
    }

    @Test
    fun `getOccurrences maps a 403 to Auth so the screen shows the authorisation text`() = runTest {
        coEvery { api.getOccurrences(any(), any(), any()) } returns
            Response.error(403, """{"error_code":"PERMISSION_DENIED","detail":"Sem permissão"}""".toResponseBody())

        val error = repository.getOccurrences(range, BucketGranularity.DAY).exceptionOrNull()

        assertTrue(error is AppError.Auth)
        assertEquals(403, (error as AppError.Auth).code)
    }

    @Test
    fun `getOccurrences maps a network failure to AppError Network`() = runTest {
        coEvery { api.getOccurrences(any(), any(), any()) } throws IOException("offline")

        val error = repository.getOccurrences(range, BucketGranularity.DAY).exceptionOrNull()

        assertTrue(error is AppError.Network)
    }

    // endregion

    // region top hymns

    @Test
    fun `getAllTimeTopHymns maps the response preserving the server ordering`() = runTest {
        coEvery { api.getAllTimeTopHymns() } returns Response.success(
            TopHymnsResponseDto(
                hymns = listOf(
                    TopHymnDto("50", "Grandioso És Tu", 42),
                    TopHymnDto("12", "Firme nas Promessas", 31),
                )
            )
        )

        val hymns = repository.getAllTimeTopHymns().getOrThrow()

        assertEquals(listOf("50", "12"), hymns.map { it.number })
        assertEquals(42, hymns.first().occurrenceCount)
    }

    @Test
    fun `getAllTimeTopHymns maps an HTTP failure to AppError`() = runTest {
        coEvery { api.getAllTimeTopHymns() } returns Response.error(500, "boom".toResponseBody())

        val result = repository.getAllTimeTopHymns()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.Server)
    }

    // endregion
}
