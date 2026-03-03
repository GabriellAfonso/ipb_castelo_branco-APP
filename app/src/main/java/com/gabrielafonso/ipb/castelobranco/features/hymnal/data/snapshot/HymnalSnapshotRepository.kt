package com.gabrielafonso.ipb.castelobranco.features.hymnal.data.snapshot

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.BaseSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.Logger
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.dto.HymnDto
import com.gabrielafonso.ipb.castelobranco.features.hymnal.data.mapper.HymnMapper
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.Hymn
import javax.inject.Inject

class HymnalSnapshotRepository @Inject constructor(
    cache: SnapshotCache<List<HymnDto>>,
    fetcher: SnapshotFetcher<List<HymnDto>>,
    logger: Logger,
    mapper: HymnMapper
) : BaseSnapshotRepository<List<HymnDto>, List<Hymn>>(
    cache = cache,
    fetcher = fetcher,
    mapper = mapper::map,
    logger = logger,
    tag = "HymnalSnapshot"
)
