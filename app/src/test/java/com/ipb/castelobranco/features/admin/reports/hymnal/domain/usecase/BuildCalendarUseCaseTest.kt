package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.DateRange
import com.ipb.castelobranco.features.admin.reports.hymnal.occurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BuildCalendarUseCaseTest {

    private val useCase = BuildCalendarUseCase()

    @Test
    fun `a month-long period is one grid`() {
        val range = DateRange(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))

        val months = useCase(emptyList(), range)

        assertEquals(1, months.size)
        assertEquals("agosto de 2026", months.single().label)
        assertEquals(31, months.single().days.size)
    }

    @Test
    fun `a year-long period is twelve grids`() {
        val range = DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))

        val months = useCase(emptyList(), range)

        assertEquals(12, months.size)
        assertEquals("janeiro de 2026", months.first().label)
        assertEquals("dezembro de 2026", months.last().label)
    }

    @Test
    fun `a period crossing a year boundary keeps both years`() {
        val range = DateRange(LocalDate.of(2025, 11, 15), LocalDate.of(2026, 2, 10))

        val months = useCase(emptyList(), range)

        assertEquals(4, months.size)
        assertEquals("novembro de 2025", months.first().label)
        assertEquals("fevereiro de 2026", months.last().label)
    }

    @Test
    fun `intensity is scaled against the busiest day of the whole period`() {
        val busy = LocalDate.of(2026, 8, 9)
        val quiet = LocalDate.of(2026, 8, 12)
        val range = DateRange(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))

        val days = useCase(
            listOf(
                occurrence(number = "1", on = busy),
                occurrence(number = "2", on = busy),
                occurrence(number = "3", on = busy),
                occurrence(number = "4", on = quiet),
            ),
            range,
        ).single().days

        assertEquals(1f, days.first { it.date == busy }.intensity, 0.001f)
        assertEquals(1f / 3f, days.first { it.date == quiet }.intensity, 0.001f)
    }

    @Test
    fun `days with no occurrence are empty rather than faintly shaded`() {
        val range = DateRange(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))

        val days = useCase(listOf(occurrence(on = LocalDate.of(2026, 8, 9))), range).single().days

        val silent = days.first { it.date == LocalDate.of(2026, 8, 10) }
        assertEquals(0, silent.occurrenceCount)
        assertEquals(0f, silent.intensity, 0.0001f)
    }

    @Test
    fun `a period with no occurrences still renders its grid`() {
        val range = DateRange(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))

        val days = useCase(emptyList(), range).single().days

        assertTrue(days.all { it.intensity == 0f })
    }
}
