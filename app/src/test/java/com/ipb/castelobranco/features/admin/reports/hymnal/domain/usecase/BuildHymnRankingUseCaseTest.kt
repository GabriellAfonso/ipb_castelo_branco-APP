package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.TODAY
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BarPoint
import com.ipb.castelobranco.features.admin.reports.hymnal.occurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildHymnRankingUseCaseTest {

    private val useCase = BuildHymnRankingUseCase()

    @Test
    fun `hymns are ordered by how many times they were sung`() {
        val ranking = useCase(
            listOf(
                occurrence(number = "50", on = TODAY),
                occurrence(number = "50", on = TODAY.minusDays(7)),
                occurrence(number = "50", on = TODAY.minusDays(14)),
                occurrence(number = "12", on = TODAY),
                occurrence(number = "12", on = TODAY.minusDays(7)),
                occurrence(number = "99", on = TODAY),
            )
        )

        assertEquals(listOf("50", "12", "99"), ranking.bars.map { it.hymnNumber })
        assertEquals(listOf(3, 2, 1), ranking.bars.map { it.value })
    }

    @Test
    fun `ties break by hymn number read as a number, not as text`() {
        val ranking = useCase(
            listOf(
                occurrence(number = "120", on = TODAY),
                occurrence(number = "50", on = TODAY),
            )
        )

        assertEquals(listOf("50", "120"), ranking.bars.map { it.hymnNumber })
    }

    @Test
    fun `one occurrence is one singing regardless of how many devices followed`() {
        val ranking = useCase(
            listOf(
                occurrence(number = "50", on = TODAY, deviceCount = 27),
                occurrence(number = "12", on = TODAY, deviceCount = 1),
                occurrence(number = "12", on = TODAY.minusDays(7), deviceCount = 1),
            )
        )

        // Hymn 12 was sung twice; hymn 50 reached 27 devices once. Count wins the ordering.
        assertEquals(listOf("12", "50"), ranking.bars.map { it.hymnNumber })
    }

    @Test
    fun `device reach is a label and never a bar length`() {
        val ranking = useCase(
            listOf(
                occurrence(number = "50", on = TODAY, deviceCount = 27),
                occurrence(number = "50", on = TODAY.minusDays(7), deviceCount = 13),
            )
        )

        val bar = ranking.bars.single()
        assertEquals(2, bar.value)
        assertEquals("40 aparelhos", bar.secondaryLabel)
        assertEquals(1f, bar.fraction, 0.001f)
    }

    @Test
    fun `a reach of one is written in the singular`() {
        val ranking = useCase(listOf(occurrence(number = "50", deviceCount = 1)))

        assertEquals("1 aparelho", ranking.bars.single().secondaryLabel)
    }

    @Test
    fun `the label carries the hymn number and title`() {
        val ranking = useCase(
            listOf(occurrence(number = "50", title = "Grandioso És Tu"))
        )

        assertEquals("50 · Grandioso És Tu", ranking.bars.single().label)
    }

    @Test
    fun `a tiny value beside a huge one still gets a visible bar`() {
        val many = List(400) { occurrence(number = "50", on = TODAY.minusDays(it.toLong())) }
        val one = occurrence(number = "12", on = TODAY)

        val ranking = useCase(many + one)

        val smallest = ranking.bars.last()
        assertEquals(1, smallest.value)
        assertEquals(BarPoint.MIN_FRACTION, smallest.fraction, 0.0001f)
    }

    @Test
    fun `the ranking is capped but reports the true total`() {
        val occurrences = (1..40).map { occurrence(number = it.toString(), on = TODAY) }

        val ranking = useCase(occurrences)

        assertEquals(25, ranking.bars.size)
        assertEquals(40, ranking.totalHymns)
        assertTrue(ranking.isCapped)
    }

    @Test
    fun `a custom limit is honoured`() {
        val occurrences = (1..10).map { occurrence(number = it.toString(), on = TODAY) }

        val ranking = useCase(occurrences, limit = 3)

        assertEquals(3, ranking.bars.size)
        assertEquals(10, ranking.totalHymns)
    }

    @Test
    fun `a ranking that fits is not reported as capped`() {
        val ranking = useCase(listOf(occurrence(number = "50")))

        assertFalse(ranking.isCapped)
    }

    @Test
    fun `no occurrences yields an empty ranking rather than a crash`() {
        val ranking = useCase(emptyList())

        assertTrue(ranking.bars.isEmpty())
        assertEquals(0, ranking.totalHymns)
    }
}
