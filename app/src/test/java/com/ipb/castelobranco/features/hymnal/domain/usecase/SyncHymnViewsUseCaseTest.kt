package com.ipb.castelobranco.features.hymnal.domain.usecase

import com.ipb.castelobranco.core.domain.error.AppError
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewCollectionSettings
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewEvent
import com.ipb.castelobranco.features.hymnal.domain.model.SyncOutcome
import com.ipb.castelobranco.features.hymnal.domain.repository.HymnViewHistoryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncHymnViewsUseCaseTest {

    @Test
    fun `drains a multi-chunk queue to empty`() = runTest {
        val repository = FakeRepository(queued = 120, drainPerSync = 50)

        val result = SyncHymnViewsUseCase(repository)()

        assertTrue(result)
        assertEquals(0, repository.queuedCount())
        assertEquals(3, repository.syncCalls)
    }

    @Test
    fun `an empty queue succeeds without a sync call`() = runTest {
        val repository = FakeRepository(queued = 0, drainPerSync = 50)

        val result = SyncHymnViewsUseCase(repository)()

        assertTrue(result)
        assertEquals(0, repository.syncCalls)
    }

    @Test
    fun `a deferred chunk stops the drain and reports failure`() = runTest {
        val repository = FakeRepository(queued = 120, drainPerSync = 50, deferOnCall = 2)

        val result = SyncHymnViewsUseCase(repository)()

        assertFalse(result)
        // The first chunk went out; the rest stays queued for the retry.
        assertEquals(70, repository.queuedCount())
        assertEquals(2, repository.syncCalls)
    }

    @Test
    fun `a discarded chunk still counts as progress`() = runTest {
        val repository = FakeRepository(queued = 60, drainPerSync = 50, discardOnCall = 1)

        val result = SyncHymnViewsUseCase(repository)()

        assertTrue(result)
        assertEquals(0, repository.queuedCount())
    }

    @Test
    fun `defers immediately when the very first chunk fails`() = runTest {
        val repository = FakeRepository(queued = 10, drainPerSync = 50, deferOnCall = 1)

        val result = SyncHymnViewsUseCase(repository)()

        assertFalse(result)
        assertEquals(10, repository.queuedCount())
    }

    private class FakeRepository(
        private var queued: Int,
        private val drainPerSync: Int,
        private val deferOnCall: Int? = null,
        private val discardOnCall: Int? = null,
    ) : HymnViewHistoryRepository {

        var syncCalls = 0

        override suspend fun record(event: HymnViewEvent) { queued++ }
        override suspend fun queuedCount(): Int = queued
        override suspend fun refreshSettings() = Unit
        override suspend fun currentSettings(): HymnViewCollectionSettings =
            HymnViewCollectionSettings.DEFAULT

        override suspend fun syncOnce(): SyncOutcome {
            syncCalls++
            if (syncCalls == deferOnCall) return SyncOutcome.Deferred(AppError.Network())

            val drained = minOf(drainPerSync, queued)
            queued -= drained
            val ids = (0 until drained).map { "id-$syncCalls-$it" }.toSet()

            return if (syncCalls == discardOnCall) {
                SyncOutcome.Discarded(ids)
            } else {
                SyncOutcome.Delivered(ids)
            }
        }
    }
}
