package com.ipb.castelobranco.features.hymnal.domain.usecase

import com.ipb.castelobranco.features.hymnal.domain.model.SyncOutcome
import com.ipb.castelobranco.features.hymnal.domain.repository.HymnViewHistoryRepository
import javax.inject.Inject

/**
 * Drains the queue in chunks until it is empty or a chunk has to be retried later.
 *
 * Returns true when everything that could be delivered was. The caller (a background worker)
 * turns false into a retry. Never throws.
 */
class SyncHymnViewsUseCase @Inject constructor(
    private val repository: HymnViewHistoryRepository,
) {

    suspend operator fun invoke(): Boolean {
        var pass = 0
        while (pass < MAX_PASSES) {
            if (repository.queuedCount() == 0) return true

            when (repository.syncOnce()) {
                // A chunk was reconciled either way — keep draining.
                is SyncOutcome.Delivered, is SyncOutcome.Discarded -> pass++
                // Throttled, server error, or offline. Everything stays queued.
                is SyncOutcome.Deferred -> return false
            }
        }
        // Bounded so a pathological queue cannot spin a worker forever; the remainder goes out
        // on the next run.
        return repository.queuedCount() == 0
    }

    private companion object {
        const val MAX_PASSES = 100
    }
}
