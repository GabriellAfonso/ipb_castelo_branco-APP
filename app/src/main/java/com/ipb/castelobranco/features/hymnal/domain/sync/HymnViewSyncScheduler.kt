package com.ipb.castelobranco.features.hymnal.domain.sync

/**
 * Asks for delivery of the queued views to happen when the network allows.
 *
 * An interface so `domain/` stays free of WorkManager. Implementations must be idempotent —
 * this is called on every recorded view and must not spawn a job per call.
 */
fun interface HymnViewSyncScheduler {
    fun scheduleSync()
}
