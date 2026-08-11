package com.ipb.castelobranco.features.hymnal.presentation.viewmodel

import com.ipb.castelobranco.core.data.local.DeviceIdProvider
import com.ipb.castelobranco.core.domain.util.MonotonicClock
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewCollectionSettings
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewEvent
import com.ipb.castelobranco.features.hymnal.domain.model.SyncOutcome
import com.ipb.castelobranco.features.hymnal.domain.repository.HymnViewHistoryRepository
import com.ipb.castelobranco.features.hymnal.domain.sync.HymnViewSyncScheduler
import com.ipb.castelobranco.features.hymnal.domain.usecase.GetHymnViewSettingsUseCase
import com.ipb.castelobranco.features.hymnal.domain.usecase.RecordHymnViewUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HymnViewTrackingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    /**
     * The timer's clock is the test scheduler's virtual clock, so `advanceTimeBy` moves both the
     * coroutine that waits for the threshold and the measurement of how long the hymn was on
     * screen. Keeping them independent would let one drift past the other and make the test
     * assert nothing.
     */
    private val clock = MonotonicClock { testDispatcher.scheduler.currentTime }

    private lateinit var repository: FakeRepository
    private lateinit var recordHymnView: RecordHymnViewUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeRepository()
        recordHymnView = RecordHymnViewUseCase(
            repository = repository,
            deviceIdProvider = DeviceIdProvider { "device" },
            scheduler = HymnViewSyncScheduler { },
            appVersion = "1.0.0",
            platform = "android",
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = HymnViewTrackingViewModel(
        recordHymnView = recordHymnView,
        getSettings = GetHymnViewSettingsUseCase(repository),
        clock = clock,
    )

    @Test
    fun `records one view once the threshold is reached`() = runTest(testDispatcher) {
        val vm = viewModel()

        vm.onHymnVisible(hymnId = 42)
        advanceTimeBy(30_001)
        runCurrent()

        assertEquals(1, repository.recorded.size)
        assertEquals(42, repository.recorded.single().hymnId)
        assertEquals(30L, repository.recorded.single().durationSeconds)
    }

    @Test
    fun `records nothing before the threshold`() = runTest(testDispatcher) {
        val vm = viewModel()

        vm.onHymnVisible(hymnId = 42)
        advanceTimeBy(29_000)
        vm.onHymnHidden()
        runCurrent()

        assertTrue(repository.recorded.isEmpty())
    }

    @Test
    fun `time in the background does not count toward the threshold`() = runTest(testDispatcher) {
        val vm = viewModel()

        vm.onHymnVisible(hymnId = 42)
        advanceTimeBy(20_000)
        vm.onHymnHidden()

        // Ten minutes with the app backgrounded or the screen off.
        advanceTimeBy(600_000)
        runCurrent()
        assertTrue("must not fire while backgrounded", repository.recorded.isEmpty())

        vm.onHymnVisible(hymnId = 42)
        advanceTimeBy(10_001)
        runCurrent()

        assertEquals(1, repository.recorded.size)
        // 20 s before the pause plus 10 s after it — not the 620 s of wall-clock time.
        assertEquals(30L, repository.recorded.single().durationSeconds)
    }

    @Test
    fun `records only once however long the visit continues`() = runTest(testDispatcher) {
        val vm = viewModel()

        vm.onHymnVisible(hymnId = 42)
        advanceTimeBy(30_001)
        runCurrent()
        assertEquals(1, repository.recorded.size)

        vm.onHymnHidden()
        vm.onHymnVisible(hymnId = 42)
        advanceTimeBy(300_000)
        runCurrent()

        assertEquals(1, repository.recorded.size)
    }

    @Test
    fun `a fresh instance starts a new count`() = runTest(testDispatcher) {
        val first = viewModel()
        first.onHymnVisible(hymnId = 42)
        advanceTimeBy(30_001)
        runCurrent()
        assertEquals(1, repository.recorded.size)
        first.onHymnHidden()

        // Leaving and re-entering the destination gives a new ViewModel, and legitimately a
        // second event — the service collapses these, the app deliberately does not.
        val second = viewModel()
        second.onHymnVisible(hymnId = 42)
        advanceTimeBy(30_001)
        runCurrent()

        assertEquals(2, repository.recorded.size)
    }

    @Test
    fun `falls back to the default threshold when settings cannot be read`() =
        runTest(testDispatcher) {
            repository.failOnSettings = true
            val vm = viewModel()

            vm.onHymnVisible(hymnId = 42)
            advanceTimeBy(30_001)
            runCurrent()

            assertEquals(1, repository.recorded.size)
        }

    @Test
    fun `a hymn without a server id records nothing`() = runTest(testDispatcher) {
        val vm = viewModel()

        vm.onHymnVisible(hymnId = null)
        advanceTimeBy(30_001)
        runCurrent()

        assertTrue(repository.recorded.isEmpty())
    }

    @Test
    fun `honours a longer server-configured threshold`() = runTest(testDispatcher) {
        repository.settings = HymnViewCollectionSettings(minSecondsToCount = 60, maxBatchSize = 50)
        val vm = viewModel()

        vm.onHymnVisible(hymnId = 42)
        advanceTimeBy(30_001)
        runCurrent()
        assertTrue("must not fire at the default 30 s", repository.recorded.isEmpty())

        advanceTimeBy(30_000)
        runCurrent()

        assertEquals(1, repository.recorded.size)
        assertEquals(60L, repository.recorded.single().durationSeconds)
    }

    private class FakeRepository : HymnViewHistoryRepository {
        val recorded = mutableListOf<HymnViewEvent>()
        var failOnSettings = false
        var settings = HymnViewCollectionSettings.DEFAULT

        override suspend fun record(event: HymnViewEvent) { recorded += event }
        override suspend fun queuedCount(): Int = recorded.size
        override suspend fun syncOnce(): SyncOutcome = SyncOutcome.Delivered(emptySet())
        override suspend fun refreshSettings() = Unit

        override suspend fun currentSettings(): HymnViewCollectionSettings {
            if (failOnSettings) throw IllegalStateException("settings unreadable")
            return settings
        }
    }
}
