package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.SUNDAY_NIGHT_ID
import com.ipb.castelobranco.features.admin.reports.hymnal.TODAY
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ReportSlice
import com.ipb.castelobranco.features.admin.reports.hymnal.occurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.outsideOccurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class FilterOccurrencesUseCaseTest {

    private val useCase = FilterOccurrencesUseCase()

    private val prayerServiceId = 7

    /** A Sunday service, a Wednesday prayer service, and one weekday view outside any service. */
    private val occurrences = listOf(
        occurrence(number = "50", on = TODAY),
        occurrence(number = "12", on = TODAY),
        occurrence(
            number = "200",
            on = TODAY.minusDays(4),
            serviceWindowId = prayerServiceId,
            serviceWindowName = "Culto de Oração",
        ),
        outsideOccurrence(number = "120", on = TODAY.minusDays(3)),
    )

    @Test
    fun `All keeps every occurrence`() {
        assertEquals(occurrences, useCase(occurrences, ReportSlice.All))
    }

    @Test
    fun `a service slice keeps only that service`() {
        val filtered = useCase(
            occurrences,
            ReportSlice.Service(id = SUNDAY_NIGHT_ID, name = "Culto de Domingo à Noite"),
        )

        assertEquals(listOf("50", "12"), filtered.map { it.hymnNumber })
    }

    @Test
    fun `another service slice keeps only its own`() {
        val filtered = useCase(
            occurrences,
            ReportSlice.Service(id = prayerServiceId, name = "Culto de Oração"),
        )

        assertEquals(listOf("200"), filtered.map { it.hymnNumber })
    }

    @Test
    fun `the outside-service slice keeps only occurrences with no service`() {
        val filtered = useCase(occurrences, ReportSlice.OutsideService)

        assertEquals(listOf("120"), filtered.map { it.hymnNumber })
        assertTrue(filtered.all { it.serviceWindowId == null })
    }

    @Test
    fun `a weekday slice derives the day from the date, across services`() {
        val filtered = useCase(occurrences, ReportSlice.Weekday(DayOfWeek.SUNDAY))

        assertEquals(listOf("50", "12"), filtered.map { it.hymnNumber })
    }

    @Test
    fun `a weekday slice also catches occurrences outside any service`() {
        val thursday = TODAY.minusDays(3)
        val filtered = useCase(occurrences, ReportSlice.Weekday(thursday.dayOfWeek))

        assertEquals(listOf("120"), filtered.map { it.hymnNumber })
    }

    @Test
    fun `a slice that matches nothing yields an empty list rather than everything`() {
        val filtered = useCase(occurrences, ReportSlice.Service(id = 999, name = "Inexistente"))

        assertTrue(filtered.isEmpty())
    }
}
