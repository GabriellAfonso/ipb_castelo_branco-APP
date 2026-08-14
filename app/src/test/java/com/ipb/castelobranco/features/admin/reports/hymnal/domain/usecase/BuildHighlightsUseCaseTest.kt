package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.TODAY
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HighlightDelta
import com.ipb.castelobranco.features.admin.reports.hymnal.occurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.outsideOccurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildHighlightsUseCaseTest {

    private val useCase = BuildHighlightsUseCase()

    private val current = listOf(
        occurrence(number = "50", title = "Grandioso És Tu", on = TODAY, deviceCount = 20),
        occurrence(number = "50", title = "Grandioso És Tu", on = TODAY.minusDays(7), deviceCount = 10),
        occurrence(number = "12", on = TODAY, deviceCount = 6),
        outsideOccurrence(number = "120", on = TODAY.minusDays(3), deviceCount = 4),
    )

    @Test
    fun `the four statements are produced`() {
        val highlights = useCase(current, preceding = null)

        assertEquals(
            listOf(
                "Hino mais cantado",
                "Hinos diferentes",
                "Cultos com registro",
                "Alcance médio por ocorrência",
            ),
            highlights.map { it.label },
        )
    }

    @Test
    fun `the most sung hymn names its number and title`() {
        val highlights = useCase(current, preceding = null)

        assertEquals("50 · Grandioso És Tu", highlights.first().value)
    }

    @Test
    fun `distinct hymns counts each hymn once`() {
        val highlights = useCase(current, preceding = null)

        assertEquals("3 hinos", highlights[1].value)
    }

    @Test
    fun `services with records counts date-plus-service groups, ignoring outside use`() {
        val highlights = useCase(current, preceding = null)

        assertEquals("2 cultos", highlights[2].value)
    }

    @Test
    fun `average reach is the mean device count per occurrence`() {
        val highlights = useCase(current, preceding = null)

        // (20 + 10 + 6 + 4) / 4 = 10
        assertEquals("10 aparelhos", highlights[3].value)
    }

    @Test
    fun `no preceding period means no delta is invented`() {
        val highlights = useCase(current, preceding = null)

        assertTrue(highlights.all { it.delta == null })
    }

    @Test
    fun `a smaller preceding period produces upward deltas`() {
        val preceding = listOf(occurrence(number = "50", on = TODAY.minusDays(30)))

        val highlights = useCase(current, preceding)

        assertEquals(HighlightDelta.Direction.UP, highlights[1].delta?.direction)
        assertEquals("+2 hinos em relação ao período anterior", highlights[1].delta?.text)
    }

    @Test
    fun `a larger preceding period produces downward deltas`() {
        val preceding = listOf(
            occurrence(number = "50", on = TODAY.minusDays(30)),
            occurrence(number = "12", on = TODAY.minusDays(31)),
            occurrence(number = "99", on = TODAY.minusDays(32)),
            occurrence(number = "77", on = TODAY.minusDays(33)),
        )

        val highlights = useCase(current, preceding)

        assertEquals(HighlightDelta.Direction.DOWN, highlights[1].delta?.direction)
        assertEquals("−1 hino em relação ao período anterior", highlights[1].delta?.text)
    }

    @Test
    fun `an identical preceding period reads as unchanged`() {
        val highlights = useCase(current, preceding = current)

        assertEquals(HighlightDelta.Direction.FLAT, highlights[1].delta?.direction)
        assertEquals("Igual ao período anterior", highlights[1].delta?.text)
    }

    @Test
    fun `the most sung hymn is compared against its own count before`() {
        val preceding = listOf(occurrence(number = "50", on = TODAY.minusDays(30)))

        val highlights = useCase(current, preceding)

        assertEquals("+1 vez em relação ao período anterior", highlights.first().delta?.text)
    }

    @Test
    fun `an empty period produces no highlights at all`() {
        assertTrue(useCase(emptyList(), preceding = current).isEmpty())
    }

    @Test
    fun `singular wording is used for a single hymn`() {
        val highlights = useCase(listOf(occurrence(number = "50", deviceCount = 1)), null)

        assertEquals("1 hino", highlights[1].value)
        assertEquals("1 culto", highlights[2].value)
        assertEquals("1 aparelho", highlights[3].value)
        assertNull(highlights[3].delta)
    }
}
