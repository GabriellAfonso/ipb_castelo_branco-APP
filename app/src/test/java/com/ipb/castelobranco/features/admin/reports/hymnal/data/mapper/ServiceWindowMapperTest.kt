package com.ipb.castelobranco.features.admin.reports.hymnal.data.mapper

import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.ServiceWindowDto
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindowDraft
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * The service uses `0 = Monday … 6 = Sunday`. Reading `0` as Sunday rotates the whole week and
 * mislabels every service, plausibly enough that nobody notices until a bulletin is wrong.
 */
class ServiceWindowMapperTest {

    @Test
    fun `wire 6 is Sunday and wire 0 is Monday`() {
        assertEquals(DayOfWeek.SUNDAY, 6.toDayOfWeek())
        assertEquals(DayOfWeek.MONDAY, 0.toDayOfWeek())
    }

    @Test
    fun `every wire weekday maps to the expected day`() {
        val expected = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
        )

        assertEquals(expected, (0..6).map { it.toDayOfWeek() })
    }

    @Test
    fun `weekday round-trips through both directions for all seven days`() {
        (0..6).forEach { wire ->
            assertEquals(wire, wire.toDayOfWeek().toWireWeekday())
        }
        DayOfWeek.entries.forEach { day ->
            assertEquals(day, day.toWireWeekday().toDayOfWeek())
        }
    }

    @Test
    fun `dto maps to domain with the translated weekday and parsed times`() {
        val dto = ServiceWindowDto(
            id = 3,
            name = "Culto de Domingo à Noite",
            weekday = 6,
            startTime = "19:00:00",
            endTime = "21:00:00",
            active = true,
        )

        val window = dto.toDomain()

        assertEquals(3, window.id)
        assertEquals(DayOfWeek.SUNDAY, window.weekday)
        assertEquals(LocalTime.of(19, 0), window.startTime)
        assertEquals(LocalTime.of(21, 0), window.endTime)
    }

    @Test
    fun `time parsing accepts both the seconds form and the short form`() {
        assertEquals(LocalTime.of(19, 30), parseTime("19:30:00"))
        assertEquals(LocalTime.of(19, 30), parseTime("19:30"))
    }

    @Test
    fun `draft writes the wire weekday and the short time form`() {
        val draft = ServiceWindowDraft(
            name = "  Culto de Oração  ",
            weekday = DayOfWeek.WEDNESDAY,
            startTime = LocalTime.of(19, 30),
            endTime = LocalTime.of(21, 0),
        )

        val dto = draft.toWriteDto()

        assertEquals("Culto de Oração", dto.name)
        assertEquals(2, dto.weekday)
        assertEquals("19:30", dto.startTime)
        assertEquals("21:00", dto.endTime)
    }

    @Test
    fun `domain window round-trips into a draft`() {
        val window = ServiceWindowDto(
            id = 7,
            name = "Culto de Oração",
            weekday = 2,
            startTime = "19:30:00",
            endTime = "21:00:00",
            active = false,
        ).toDomain()

        val draft = window.toDraft()

        assertEquals(7, draft.id)
        assertEquals(DayOfWeek.WEDNESDAY, draft.weekday)
        assertEquals(false, draft.active)
    }
}
