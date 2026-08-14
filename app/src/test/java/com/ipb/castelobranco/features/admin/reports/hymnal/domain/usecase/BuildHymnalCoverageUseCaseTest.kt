package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.TODAY
import com.ipb.castelobranco.features.admin.reports.hymnal.catalog
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.TopHymn
import com.ipb.castelobranco.features.admin.reports.hymnal.occurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildHymnalCoverageUseCaseTest {

    private val useCase = BuildHymnalCoverageUseCase()

    private val fullCatalog = catalog("1", "2", "3", "50", "120")
    private val allTime = listOf(
        TopHymn(number = "50", title = "Grandioso És Tu", occurrenceCount = 42),
        TopHymn(number = "120", title = "Saudosa Lembrança", occurrenceCount = 3),
    )

    @Test
    fun `never sung is the catalogue minus the all-time ranking`() {
        val coverage = useCase(fullCatalog, allTime, visibleWindow = emptyList())

        assertEquals(listOf("1", "2", "3"), coverage.neverSung.map { it.number })
    }

    @Test
    fun `the proportion names both numbers and the percentage`() {
        val coverage = useCase(fullCatalog, allTime, visibleWindow = emptyList())

        assertEquals(5, coverage.catalogSize)
        assertEquals(2, coverage.everSungCount)
        assertEquals("A igreja já cantou 2 dos 5 hinos do hinário (40%).", coverage.proportionText)
    }

    @Test
    fun `an empty ranking makes the whole catalogue never sung`() {
        val coverage = useCase(fullCatalog, allTime = emptyList(), visibleWindow = emptyList())

        assertEquals(5, coverage.neverSung.size)
        assertEquals(0, coverage.everSungCount)
        assertTrue(coverage.forgotten.isEmpty())
    }

    @Test
    fun `a hymn sung inside the visible window shows its exact last date`() {
        val coverage = useCase(
            fullCatalog,
            allTime,
            visibleWindow = listOf(
                occurrence(number = "50", on = TODAY.minusDays(30)),
                occurrence(number = "50", on = TODAY.minusDays(200)),
            ),
        )

        val hymn = coverage.forgotten.first { it.number == "50" }
        assertEquals(TODAY.minusDays(30), hymn.lastSung)
        assertTrue(hymn.text.startsWith("Cantado pela última vez em "))
    }

    @Test
    fun `a hymn whose last occurrence is outside the window shows no date at all`() {
        val coverage = useCase(
            fullCatalog,
            allTime,
            visibleWindow = listOf(occurrence(number = "50", on = TODAY.minusDays(30))),
        )

        val hymn = coverage.forgotten.first { it.number == "120" }
        assertNull(hymn.lastSung)
        assertEquals("Não é cantado há mais de um ano", hymn.text)
    }

    @Test
    fun `undated hymns sort ahead of every dated one`() {
        val coverage = useCase(
            fullCatalog,
            allTime,
            visibleWindow = listOf(occurrence(number = "50", on = TODAY.minusDays(30))),
        )

        assertEquals(listOf("120", "50"), coverage.forgotten.map { it.number })
    }

    @Test
    fun `dated hymns sort from most forgotten to least`() {
        val allThree = allTime + TopHymn(number = "1", title = "Hino 1", occurrenceCount = 5)

        val coverage = useCase(
            fullCatalog,
            allThree,
            visibleWindow = listOf(
                occurrence(number = "50", on = TODAY.minusDays(10)),
                occurrence(number = "120", on = TODAY.minusDays(300)),
                occurrence(number = "1", on = TODAY.minusDays(100)),
            ),
        )

        assertEquals(listOf("120", "1", "50"), coverage.forgotten.map { it.number })
    }

    @Test
    fun `an empty catalogue says so instead of dividing by zero`() {
        val coverage = useCase(emptyList(), allTime, visibleWindow = emptyList())

        assertEquals("O hinário do aplicativo está vazio.", coverage.proportionText)
        assertTrue(coverage.neverSung.isEmpty())
    }
}
