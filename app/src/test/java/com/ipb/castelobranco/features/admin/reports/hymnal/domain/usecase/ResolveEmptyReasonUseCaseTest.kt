package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.SUNDAY_NIGHT_ID
import com.ipb.castelobranco.features.admin.reports.hymnal.SUNDAY_NIGHT_NAME
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ReportEmptyReason
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ReportSlice
import com.ipb.castelobranco.features.admin.reports.hymnal.occurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.serviceWindow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveEmptyReasonUseCaseTest {

    private val useCase = ResolveEmptyReasonUseCase()

    private val sundayNight = ReportSlice.Service(SUNDAY_NIGHT_ID, SUNDAY_NIGHT_NAME)
    private val windows = listOf(serviceWindow())

    @Test
    fun `a slice with data has no empty reason`() {
        val reason = useCase(
            periodOccurrences = listOf(occurrence()),
            slicedOccurrences = listOf(occurrence()),
            slice = ReportSlice.All,
            serviceWindows = windows,
        )

        assertNull(reason)
    }

    @Test
    fun `an empty period with an empty history reads as no collection at all`() {
        val reason = useCase(
            periodOccurrences = emptyList(),
            slicedOccurrences = emptyList(),
            slice = ReportSlice.All,
            serviceWindows = windows,
            collectionIsEmpty = true,
        )

        assertEquals(ReportEmptyReason.NoCollectionAtAll, reason)
    }

    @Test
    fun `an empty period with history elsewhere points at the period`() {
        val reason = useCase(
            periodOccurrences = emptyList(),
            slicedOccurrences = emptyList(),
            slice = ReportSlice.All,
            serviceWindows = windows,
            collectionIsEmpty = false,
        )

        assertEquals(ReportEmptyReason.NoRecordsInPeriod, reason)
    }

    @Test
    fun `an empty period never claims there was never any collection on a guess`() {
        val reason = useCase(
            periodOccurrences = emptyList(),
            slicedOccurrences = emptyList(),
            slice = ReportSlice.All,
            serviceWindows = windows,
            collectionIsEmpty = null,
        )

        assertEquals(ReportEmptyReason.NoRecordsInPeriod, reason)
    }

    @Test
    fun `a non-service slice with no data points at the slice`() {
        val reason = useCase(
            periodOccurrences = listOf(occurrence()),
            slicedOccurrences = emptyList(),
            slice = ReportSlice.OutsideService,
            serviceWindows = windows,
        )

        assertEquals(ReportEmptyReason.NoRecordsInSlice, reason)
    }

    @Test
    fun `a deactivated service says it was not in force, not that nobody sang`() {
        val reason = useCase(
            periodOccurrences = listOf(occurrence()),
            slicedOccurrences = emptyList(),
            slice = sundayNight,
            serviceWindows = listOf(serviceWindow(active = false)),
        )

        assertEquals(
            ReportEmptyReason.ServiceInactiveOrAbsentInPeriod(SUNDAY_NIGHT_NAME),
            reason,
        )
    }

    @Test
    fun `a service that no longer exists is treated the same way`() {
        val reason = useCase(
            periodOccurrences = listOf(occurrence()),
            slicedOccurrences = emptyList(),
            slice = sundayNight,
            serviceWindows = emptyList(),
        )

        assertEquals(
            ReportEmptyReason.ServiceInactiveOrAbsentInPeriod(SUNDAY_NIGHT_NAME),
            reason,
        )
    }

    @Test
    fun `an active service with no records names both possibilities and asserts neither`() {
        val reason = useCase(
            periodOccurrences = listOf(occurrence()),
            slicedOccurrences = emptyList(),
            slice = sundayNight,
            serviceWindows = windows,
        )

        assertEquals(ReportEmptyReason.ServiceWithoutRecords(SUNDAY_NIGHT_NAME), reason)
        val message = reason!!.message
        assertTrue(message.contains("não tenha acontecido"))
        assertTrue(message.contains("ninguém tenha usado o hinário"))
    }
}
