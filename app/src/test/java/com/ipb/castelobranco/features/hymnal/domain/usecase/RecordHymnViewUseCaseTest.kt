package com.ipb.castelobranco.features.hymnal.domain.usecase

import com.ipb.castelobranco.core.data.local.DeviceIdProvider
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewCollectionSettings
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewEvent
import com.ipb.castelobranco.features.hymnal.domain.model.SyncOutcome
import com.ipb.castelobranco.features.hymnal.domain.repository.HymnViewHistoryRepository
import com.ipb.castelobranco.features.hymnal.domain.sync.HymnViewSyncScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class RecordHymnViewUseCaseTest {

    private lateinit var repository: FakeRepository
    private lateinit var scheduler: RecordingScheduler
    private lateinit var useCase: RecordHymnViewUseCase

    @Before
    fun setUp() {
        repository = FakeRepository()
        scheduler = RecordingScheduler()
        useCase = RecordHymnViewUseCase(
            repository = repository,
            deviceIdProvider = DeviceIdProvider { "test-device-id" },
            scheduler = scheduler,
            appVersion = "1.2.3",
            platform = "android",
        )
    }

    @Test
    fun `records an event and schedules delivery`() = runTest {
        useCase(hymnId = 42, durationSeconds = 30)

        assertEquals(1, repository.recorded.size)
        val event = repository.recorded.single()
        assertEquals(42, event.hymnId)
        assertEquals("test-device-id", event.deviceId)
        assertEquals(30L, event.durationSeconds)
        assertEquals("1.2.3", event.appVersion)
        assertEquals("android", event.platform)
        assertTrue(scheduler.scheduleCount == 1)
    }

    @Test
    fun `client event id is a parseable uuid`() = runTest {
        useCase(hymnId = 42, durationSeconds = 30)

        val parsed = UUID.fromString(repository.recorded.single().clientEventId)

        assertNotNull(parsed)
    }

    @Test
    fun `viewed at carries a utc offset`() = runTest {
        useCase(hymnId = 42, durationSeconds = 30)

        val viewedAt = repository.recorded.single().viewedAt

        // OffsetDateTime always carries one; asserting the serialized form is what the service
        // actually validates.
        assertNotNull(viewedAt.offset)
        val serialized = viewedAt.toString()
        assertTrue(
            "expected an offset in $serialized",
            serialized.endsWith("Z") || serialized.contains('+') || serialized.lastIndexOf('-') > 9,
        )
    }

    @Test
    fun `hymn without a server id records nothing and does not throw`() = runTest {
        useCase(hymnId = null, durationSeconds = 30)

        assertTrue(repository.recorded.isEmpty())
        assertEquals(0, scheduler.scheduleCount)
    }

    @Test
    fun `a queue failure is swallowed`() = runTest {
        repository.failOnRecord = true

        useCase(hymnId = 42, durationSeconds = 30)

        assertTrue(repository.recorded.isEmpty())
        assertEquals(0, scheduler.scheduleCount)
    }

    @Test
    fun `negative duration is clamped to zero`() = runTest {
        useCase(hymnId = 42, durationSeconds = -5)

        assertEquals(0L, repository.recorded.single().durationSeconds)
    }

    @Test
    fun `event carries no personal data beyond the anonymous device id`() = runTest {
        useCase(hymnId = 42, durationSeconds = 30)

        val event = repository.recorded.single()

        // The device id is the only identifier, and it is the anonymous one the provider gave us
        // — never a hardware, advertising, or platform-assigned value.
        assertEquals("test-device-id", event.deviceId)

        // Everything else is a hymn, a moment, a duration, and build metadata. The exact wire
        // shape is pinned separately by HymnViewEventDtoSerializationTest.
        val rendered = listOf(
            event.clientEventId,
            event.hymnId.toString(),
            event.deviceId,
            event.viewedAt.toString(),
            event.durationSeconds.toString(),
            event.appVersion,
            event.platform,
        ).joinToString(" ")
        assertFalse("no email-shaped value may appear", rendered.contains("@"))
    }

    private class FakeRepository : HymnViewHistoryRepository {
        val recorded = mutableListOf<HymnViewEvent>()
        var failOnRecord = false

        override suspend fun record(event: HymnViewEvent) {
            if (failOnRecord) throw IllegalStateException("disk full")
            recorded += event
        }

        override suspend fun queuedCount(): Int = recorded.size
        override suspend fun syncOnce(): SyncOutcome = SyncOutcome.Delivered(emptySet())
        override suspend fun refreshSettings() = Unit
        override suspend fun currentSettings(): HymnViewCollectionSettings =
            HymnViewCollectionSettings.DEFAULT
    }

    private class RecordingScheduler : HymnViewSyncScheduler {
        var scheduleCount = 0
        override fun scheduleSync() { scheduleCount++ }
    }
}
