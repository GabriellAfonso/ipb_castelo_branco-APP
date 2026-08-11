package com.ipb.castelobranco.features.hymnal.data.local

import com.ipb.castelobranco.core.data.local.SnapshotStorage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Durable, ordered queue of recorded views awaiting delivery.
 *
 * Built on the same [SnapshotStorage] the hymnal and schedule caches use, but the access pattern
 * is different: this is an append-and-remove queue, not a replace-whole-blob cache. Since the
 * underlying store only knows how to write a whole file, every mutation is a read-modify-write —
 * and every one of them is held under a single [Mutex] for its full duration. Guarding only the
 * write would let two concurrent appends read the same list and lose one of the two events.
 *
 * Ordered oldest-first. On overflow the oldest is dropped, which loses nothing of value: the
 * service refuses anything older than its retention window anyway.
 */
@Singleton
class HymnViewQueueStore @Inject constructor(
    private val storage: SnapshotStorage,
    private val json: Json,
) {

    private val mutex = Mutex()
    private val serializer = ListSerializer(QueuedHymnViewEvent.serializer())

    suspend fun append(event: QueuedHymnViewEvent) = mutex.withLock {
        val current = readUnlocked()
        val appended = current + event
        val capped = if (appended.size > MAX_QUEUE_SIZE) {
            val dropped = appended.size - MAX_QUEUE_SIZE
            Timber.d("Hymn view queue full, dropping %d oldest event(s)", dropped)
            appended.takeLast(MAX_QUEUE_SIZE)
        } else {
            appended
        }
        writeUnlocked(capped)
    }

    suspend fun readAll(): List<QueuedHymnViewEvent> = mutex.withLock { readUnlocked() }

    suspend fun count(): Int = mutex.withLock { readUnlocked().size }

    suspend fun remove(ids: Set<String>) {
        if (ids.isEmpty()) return
        mutex.withLock {
            val remaining = readUnlocked().filterNot { it.clientEventId in ids }
            writeUnlocked(remaining)
        }
    }

    private suspend fun readUnlocked(): List<QueuedHymnViewEvent> {
        val raw = storage.loadOrNull(STORAGE_KEY) ?: return emptyList()
        return runCatching { json.decodeFromString(serializer, raw) }
            .onFailure { Timber.w(it, "Hymn view queue unreadable, treating as empty") }
            .getOrDefault(emptyList())
    }

    private suspend fun writeUnlocked(events: List<QueuedHymnViewEvent>) {
        runCatching { storage.save(STORAGE_KEY, json.encodeToString(serializer, events)) }
            .onFailure { Timber.w(it, "Failed to persist hymn view queue") }
    }

    companion object {
        const val STORAGE_KEY = "hymn_view_queue"
        const val MAX_QUEUE_SIZE = 2000
    }
}
