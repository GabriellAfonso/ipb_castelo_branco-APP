package com.ipb.castelobranco.core.data.snapshot

import com.ipb.castelobranco.core.data.local.SnapshotStorage
import com.ipb.castelobranco.core.domain.snapshot.SnapshotCache

class LocalSnapshotCache<T>(
    private val storage: SnapshotStorage,
    private val codec: SnapshotCodec<T>,
    private val key: String
) : SnapshotCache<T> {

    override suspend fun load(): T? =
        storage.loadOrNull(key)?.let(codec::decode)

    override suspend fun save(dto: T, etag: String?) {
        storage.save(key, codec.encode(dto))
        etag?.let { storage.saveETag(key, it) }
    }

    override suspend fun loadETag(): String? =
        storage.loadETagOrNull(key)

    override suspend fun clear() {
        storage.clear(key)
    }
}
