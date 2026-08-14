package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.TODAY
import com.ipb.castelobranco.features.admin.reports.hymnal.occurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.outsideOccurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildInVsOutsideUseCaseTest {

    private val useCase = BuildInVsOutsideUseCase(BuildHymnRankingUseCase())

    private val mixed = listOf(
        occurrence(number = "50", on = TODAY),
        occurrence(number = "12", on = TODAY),
        outsideOccurrence(number = "12", on = TODAY.minusDays(3)),
        outsideOccurrence(number = "120", on = TODAY.minusDays(4)),
    )

    @Test
    fun `both sides get their own ranking`() {
        val comparison = useCase(mixed)

        assertEquals(listOf("12", "50"), comparison.inService.bars.map { it.hymnNumber })
        assertEquals(listOf("12", "120"), comparison.outsideService.bars.map { it.hymnNumber })
    }

    @Test
    fun `hymns exclusive to each side are called out`() {
        val comparison = useCase(mixed)

        assertEquals(listOf("50"), comparison.onlyInService)
        assertEquals(listOf("120"), comparison.onlyOutsideService)
    }

    @Test
    fun `a hymn on both sides is exclusive to neither`() {
        val comparison = useCase(mixed)

        assertTrue("12" !in comparison.onlyInService)
        assertTrue("12" !in comparison.onlyOutsideService)
    }

    @Test
    fun `an empty outside side leaves the service side intact`() {
        val comparison = useCase(listOf(occurrence(number = "50", on = TODAY)))

        assertEquals(1, comparison.inService.bars.size)
        assertTrue(comparison.outsideService.bars.isEmpty())
        assertEquals(listOf("50"), comparison.onlyInService)
        assertTrue(comparison.onlyOutsideService.isEmpty())
    }

    @Test
    fun `an empty service side leaves the outside side intact`() {
        val comparison = useCase(listOf(outsideOccurrence(number = "120")))

        assertTrue(comparison.inService.bars.isEmpty())
        assertEquals(1, comparison.outsideService.bars.size)
    }
}
