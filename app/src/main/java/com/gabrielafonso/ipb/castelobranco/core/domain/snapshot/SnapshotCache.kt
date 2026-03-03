package com.gabrielafonso.ipb.castelobranco.core.domain.snapshot

interface SnapshotCache<Dto> {
    suspend fun load(): Dto?
    suspend fun save(dto: Dto, etag: String?)
    suspend fun loadETag(): String?
    suspend fun clear()
}
