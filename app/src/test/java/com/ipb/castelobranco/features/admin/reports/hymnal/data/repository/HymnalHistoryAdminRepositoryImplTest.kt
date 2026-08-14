package com.ipb.castelobranco.features.admin.reports.hymnal.data.repository

import com.ipb.castelobranco.core.domain.error.AppError
import com.ipb.castelobranco.features.admin.reports.hymnal.data.api.HymnalHistoryAdminApi
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.CollectionSettingsDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.CollectionSettingsPatchDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.ServiceWindowDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.ServiceWindowListDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.ServiceWindowWriteDto
import com.ipb.castelobranco.features.admin.reports.hymnal.defaultSettings
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindowDraft
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.time.DayOfWeek
import java.time.LocalTime

class HymnalHistoryAdminRepositoryImplTest {

    private lateinit var api: HymnalHistoryAdminApi
    private lateinit var repository: HymnalHistoryAdminRepositoryImpl

    private val settingsDto = CollectionSettingsDto(
        minSecondsToCount = 30,
        collapseWindowMinutes = 10,
        maxBatchSize = 200,
        maxPastDays = 90,
        futureToleranceMinutes = 5,
        windowGraceMinutes = 30,
    )

    @Before
    fun setup() {
        api = mockk()
        repository = HymnalHistoryAdminRepositoryImpl(api)
    }

    // region settings

    @Test
    fun `getSettings maps the six values`() = runTest {
        coEvery { api.getSettings() } returns Response.success(settingsDto)

        val settings = repository.getSettings().getOrThrow()

        assertEquals(defaultSettings(), settings)
    }

    @Test
    fun `updateSettings sends only the changed field`() = runTest {
        val body = slot<CollectionSettingsPatchDto>()
        coEvery { api.patchSettings(capture(body)) } returns
            Response.success(settingsDto.copy(minSecondsToCount = 45))

        val current = defaultSettings()
        repository.updateSettings(current, current.copy(minSecondsToCount = 45))

        assertEquals(45, body.captured.minSecondsToCount)
        assertNull(body.captured.windowGraceMinutes)
    }

    @Test
    fun `updateSettings issues no request when nothing changed`() = runTest {
        val current = defaultSettings()

        val result = repository.updateSettings(current, current)

        assertEquals(current, result.getOrThrow())
        coVerify(exactly = 0) { api.patchSettings(any()) }
    }

    @Test
    fun `updateSettings surfaces field_errors on the error`() = runTest {
        val errorBody = """
            {"error_code":"VALIDATION_ERROR","detail":"Validation failed.",
             "field_errors":{"min_seconds_to_count":["Value 0 is out of range."]}}
        """.trimIndent()
        coEvery { api.patchSettings(any()) } returns Response.error(400, errorBody.toResponseBody())

        val current = defaultSettings()
        val error = repository.updateSettings(current, current.copy(minSecondsToCount = 0))
            .exceptionOrNull()

        assertTrue(error is AppError.Server)
        assertEquals(
            listOf("Value 0 is out of range."),
            (error as AppError.Server).fieldErrors?.get("min_seconds_to_count"),
        )
    }

    // endregion

    // region service windows

    @Test
    fun `getServiceWindows maps the list with translated weekdays`() = runTest {
        coEvery { api.getServiceWindows() } returns Response.success(
            ServiceWindowListDto(
                listOf(
                    ServiceWindowDto(3, "Culto de Domingo à Noite", 6, "19:00:00", "21:00:00", true)
                )
            )
        )

        val window = repository.getServiceWindows().getOrThrow().single()

        assertEquals(DayOfWeek.SUNDAY, window.weekday)
        assertEquals(LocalTime.of(19, 0), window.startTime)
    }

    @Test
    fun `saveServiceWindow creates when the draft has no id`() = runTest {
        val body = slot<ServiceWindowWriteDto>()
        coEvery { api.createServiceWindow(capture(body)) } returns Response.success(
            ServiceWindowDto(9, "Culto de Oração", 2, "19:30:00", "21:00:00", true)
        )

        val draft = ServiceWindowDraft(
            name = "Culto de Oração",
            weekday = DayOfWeek.WEDNESDAY,
            startTime = LocalTime.of(19, 30),
            endTime = LocalTime.of(21, 0),
        )
        val saved = repository.saveServiceWindow(draft).getOrThrow()

        assertEquals(2, body.captured.weekday)
        assertEquals(9, saved.id)
        coVerify(exactly = 0) { api.updateServiceWindow(any(), any()) }
    }

    @Test
    fun `saveServiceWindow updates when the draft has an id`() = runTest {
        coEvery { api.updateServiceWindow(eq(3), any()) } returns Response.success(
            ServiceWindowDto(3, "Culto de Domingo à Noite", 6, "19:00:00", "21:00:00", false)
        )

        val draft = ServiceWindowDraft(
            id = 3,
            name = "Culto de Domingo à Noite",
            weekday = DayOfWeek.SUNDAY,
            active = false,
        )
        val saved = repository.saveServiceWindow(draft).getOrThrow()

        assertEquals(false, saved.active)
        coVerify(exactly = 0) { api.createServiceWindow(any()) }
    }

    @Test
    fun `deleteServiceWindow maps a 404 to AppError Server`() = runTest {
        coEvery { api.deleteServiceWindow(3) } returns Response.error(
            404,
            """{"error_code":"NOT_FOUND","detail":"Não encontrado"}""".toResponseBody(),
        )

        val error = repository.deleteServiceWindow(3).exceptionOrNull()

        assertTrue(error is AppError.Server)
        assertEquals("NOT_FOUND", (error as AppError.Server).errorCode)
    }

    @Test
    fun `deleteServiceWindow succeeds on an empty 204`() = runTest {
        coEvery { api.deleteServiceWindow(3) } returns Response.success(204, Unit)

        assertTrue(repository.deleteServiceWindow(3).isSuccess)
    }

    // endregion
}
