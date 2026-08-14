package com.ipb.castelobranco.core.domain.repository

import com.ipb.castelobranco.core.domain.model.HymnCatalogEntry
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import kotlinx.coroutines.flow.Flow

/**
 * Read-only view of the hymn catalogue, shared through `core/` so administrative features can
 * cross the catalogue with report data without importing `features/hymnal`.
 *
 * Implemented by the hymnal feature's own repository and bound in `HymnalModule`, exactly as
 * `SongsRepository` implements [AllSongsRepository]. There is no second cache and no second
 * fetch: this observes the snapshot the hymnal already preloads at startup.
 */
interface HymnCatalogRepository {
    fun observeHymnCatalog(): Flow<SnapshotState<List<HymnCatalogEntry>>>
}
