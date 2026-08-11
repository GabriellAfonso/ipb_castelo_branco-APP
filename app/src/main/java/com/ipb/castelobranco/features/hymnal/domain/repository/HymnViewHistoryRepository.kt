package com.ipb.castelobranco.features.hymnal.domain.repository

import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewCollectionSettings
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewEvent
import com.ipb.castelobranco.features.hymnal.domain.model.SyncOutcome

/**
 * Collection of hymn view history. Only domain types cross this boundary — no Retrofit
 * [retrofit2.Response], no DTO, and no raw HTTP exception.
 */
interface HymnViewHistoryRepository {

    /** Appends an event to the local queue and schedules delivery. Never throws. */
    suspend fun record(event: HymnViewEvent)

    /** How many events are waiting to be delivered. */
    suspend fun queuedCount(): Int

    /** Submits one chunk. Returns what happened; never throws. */
    suspend fun syncOnce(): SyncOutcome

    /** Reads collection settings from the service and caches them. Never throws. */
    suspend fun refreshSettings()

    /** Cached settings, falling back to [HymnViewCollectionSettings.DEFAULT]. */
    suspend fun currentSettings(): HymnViewCollectionSettings
}
