package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.TODAY
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BucketGranularity
import com.ipb.castelobranco.features.admin.reports.hymnal.occurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildEvolutionSeriesUseCaseTest {

    private val useCase = BuildEvolutionSeriesUseCase()

    @Test
    fun `one bar per bucket, in the order the service returned them`() {
        val series = useCase(
            listOf(
                occurrence(number = "50", on = TODAY.minusDays(2), bucket = "2026-08-14"),
                occurrence(number = "12", on = TODAY.minusDays(2), bucket = "2026-08-14"),
                occurrence(number = "99", on = TODAY, bucket = "2026-08-16"),
            ),
            BucketGranularity.DAY,
        )

        assertEquals(listOf("14/08", "16/08"), series.bars.map { it.label })
        assertEquals(listOf(2, 1), series.bars.map { it.value })
    }

    @Test
    fun `a chronologically later bucket returned first is not re-sorted`() {
        val series = useCase(
            listOf(
                occurrence(on = TODAY, bucket = "2026-08-16"),
                occurrence(on = TODAY.minusDays(2), bucket = "2026-08-14"),
            ),
            BucketGranularity.DAY,
        )

        assertEquals(listOf("16/08", "14/08"), series.bars.map { it.label })
    }

    @Test
    fun `week buckets are labelled by their ISO week number`() {
        val series = useCase(
            listOf(occurrence(bucket = "2026-W32")),
            BucketGranularity.WEEK,
        )

        assertEquals("Sem 32", series.bars.single().label)
    }

    @Test
    fun `month buckets are labelled with the abbreviated month`() {
        val series = useCase(
            listOf(
                occurrence(bucket = "2026-08"),
                occurrence(bucket = "2026-01"),
            ),
            BucketGranularity.MONTH,
        )

        assertEquals(listOf("ago", "jan"), series.bars.map { it.label })
    }

    @Test
    fun `an unrecognised bucket is shown as-is rather than mangled`() {
        val series = useCase(
            listOf(occurrence(bucket = "estranho")),
            BucketGranularity.MONTH,
        )

        assertEquals("estranho", series.bars.single().label)
    }

    @Test
    fun `bars are proportional to the busiest bucket`() {
        val series = useCase(
            listOf(
                occurrence(number = "1", bucket = "2026-08-14"),
                occurrence(number = "2", bucket = "2026-08-14"),
                occurrence(number = "3", bucket = "2026-08-14"),
                occurrence(number = "4", bucket = "2026-08-16"),
            ),
            BucketGranularity.DAY,
        )

        assertEquals(1f, series.bars.first().fraction, 0.001f)
        assertEquals(1f / 3f, series.bars.last().fraction, 0.001f)
    }

    @Test
    fun `reach rides along as a label`() {
        val series = useCase(
            listOf(
                occurrence(bucket = "2026-08-16", deviceCount = 20),
                occurrence(number = "12", bucket = "2026-08-16", deviceCount = 7),
            ),
            BucketGranularity.DAY,
        )

        assertEquals("27 aparelhos", series.bars.single().secondaryLabel)
    }

    @Test
    fun `no occurrences yields no bars`() {
        val series = useCase(emptyList(), BucketGranularity.DAY)

        assertTrue(series.bars.isEmpty())
        assertEquals(BucketGranularity.DAY, series.granularity)
    }
}
