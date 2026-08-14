package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.SUNDAY_NIGHT_NAME
import com.ipb.castelobranco.features.admin.reports.hymnal.TODAY
import com.ipb.castelobranco.features.admin.reports.hymnal.occurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.outsideOccurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildServiceBulletinsUseCaseTest {

    private val useCase = BuildServiceBulletinsUseCase()

    @Test
    fun `one card per date-and-service group, most recent first`() {
        val bulletins = useCase(
            listOf(
                occurrence(number = "50", on = TODAY.minusDays(7)),
                occurrence(number = "12", on = TODAY),
                occurrence(number = "99", on = TODAY),
            )
        )

        assertEquals(2, bulletins.size)
        assertEquals(TODAY, bulletins.first().date)
        assertEquals(TODAY.minusDays(7), bulletins.last().date)
    }

    @Test
    fun `a card lists its hymns with number, title and reach`() {
        val bulletins = useCase(
            listOf(
                occurrence(number = "50", title = "Grandioso És Tu", on = TODAY, deviceCount = 27),
                occurrence(number = "12", title = "Firme nas Promessas", on = TODAY, deviceCount = 13),
            )
        )

        val card = bulletins.single()
        assertEquals(SUNDAY_NIGHT_NAME, card.serviceName)
        assertEquals(listOf("50", "12"), card.hymns.map { it.number })
        assertEquals(listOf(27, 13), card.hymns.map { it.deviceCount })
        assertEquals(40, card.totalReach)
    }

    @Test
    fun `occurrences outside any service are kept, grouped by day and left unnamed`() {
        val bulletins = useCase(
            listOf(
                occurrence(number = "50", on = TODAY),
                outsideOccurrence(number = "120", on = TODAY.minusDays(3)),
                outsideOccurrence(number = "121", on = TODAY.minusDays(3)),
            )
        )

        val loose = bulletins.single { it.serviceName == null }
        assertNull(loose.serviceName)
        assertEquals(2, loose.hymns.size)
    }

    @Test
    fun `on the same day the service comes before the loose weekday use`() {
        val bulletins = useCase(
            listOf(
                outsideOccurrence(number = "120", on = TODAY),
                occurrence(number = "50", on = TODAY),
            )
        )

        assertEquals(SUNDAY_NIGHT_NAME, bulletins.first().serviceName)
        assertNull(bulletins.last().serviceName)
    }

    @Test
    fun `no occurrences yields no cards`() {
        assertTrue(useCase(emptyList()).isEmpty())
    }
}
