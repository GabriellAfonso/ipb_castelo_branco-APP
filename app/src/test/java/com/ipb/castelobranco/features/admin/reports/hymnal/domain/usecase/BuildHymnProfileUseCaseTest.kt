package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.SUNDAY_NIGHT_NAME
import com.ipb.castelobranco.features.admin.reports.hymnal.TODAY
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.TopHymn
import com.ipb.castelobranco.features.admin.reports.hymnal.occurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.outsideOccurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildHymnProfileUseCaseTest {

    private val useCase = BuildHymnProfileUseCase()

    private val allTime = listOf(TopHymn("50", "Grandioso És Tu", occurrenceCount = 42))

    /** Eight Sundays with records; hymn 50 appears on six of them. */
    private val window = buildList {
        (0..7).forEach { week ->
            val date = TODAY.minusWeeks(week.toLong())
            add(occurrence(number = "99", title = "Outro", on = date, deviceCount = 5))
            if (week != 2 && week != 5) {
                add(occurrence(number = "50", on = date, deviceCount = 10 + week))
            }
        }
    }

    @Test
    fun `the total comes from the all-time source`() {
        val profile = useCase("50", allTime, window)

        assertEquals(42, profile.allTimeCount)
        assertFalse(profile.neverRecorded)
    }

    @Test
    fun `first and last come from the visible window`() {
        val profile = useCase("50", allTime, window)

        assertEquals(TODAY.minusWeeks(7), profile.firstSeen)
        assertEquals(TODAY, profile.lastSeen)
    }

    @Test
    fun `the services it appears in are ordered by how often`() {
        val profile = useCase(
            "50",
            allTime,
            window + outsideOccurrence(number = "50", on = TODAY.minusDays(2)),
        )

        assertEquals(SUNDAY_NIGHT_NAME, profile.services.first().serviceName)
        assertEquals(6, profile.services.first().occurrenceCount)
        assertNull(profile.services.last().serviceName)
    }

    @Test
    fun `typical reach is the mean of the occurrences in the window`() {
        val profile = useCase(
            "50",
            allTime,
            listOf(
                occurrence(number = "50", on = TODAY, deviceCount = 10),
                occurrence(number = "50", on = TODAY.minusWeeks(1), deviceCount = 20),
            ),
        )

        assertEquals(15, profile.typicalReach)
    }

    @Test
    fun `recurrence counts the service instances it appeared in`() {
        val profile = useCase("50", allTime, window)

        val recurrence = requireNotNull(profile.recurrence)
        assertEquals(SUNDAY_NIGHT_NAME, recurrence.serviceName)
        assertEquals(8, recurrence.marks.size)
        assertEquals(6, recurrence.marks.count { it })
        assertEquals(
            "Cantado em 6 dos últimos 8 registros de \"$SUNDAY_NIGHT_NAME\"",
            recurrence.text,
        )
    }

    @Test
    fun `a hymn only ever opened outside services has no recurrence`() {
        val profile = useCase(
            "120",
            listOf(TopHymn("120", "Saudosa Lembrança", 3)),
            listOf(outsideOccurrence(number = "120", on = TODAY.minusDays(3))),
        )

        assertNull(profile.recurrence)
    }

    @Test
    fun `a hymn never recorded shows no dates and no counts`() {
        val profile = useCase("7", allTime, window, fallbackTitle = "Hino 7")

        assertTrue(profile.neverRecorded)
        assertEquals(0, profile.allTimeCount)
        assertEquals(0, profile.typicalReach)
        assertNull(profile.firstSeen)
        assertNull(profile.lastSeen)
        assertNull(profile.recurrence)
        assertEquals("Hino 7", profile.title)
    }

    @Test
    fun `the title falls back to the window when the all-time source has none`() {
        val profile = useCase("99", allTime = emptyList(), visibleWindow = window)

        assertEquals("Outro", profile.title)
    }
}
