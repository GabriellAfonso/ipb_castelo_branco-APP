package com.gabrielafonso.ipb.castelobranco.core.domain.snapshot

interface SnapshotFetcher<Dto> {
    suspend fun fetch(etag: String?): NetworkResult<Dto>
}
