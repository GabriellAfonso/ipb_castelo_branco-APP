package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.snapshot

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.BaseSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.Logger
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.TopToneDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.mapper.TopTonesMapper
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.TopTone
import javax.inject.Inject

class TopTonesSnapshotRepository @Inject constructor(
    cache: SnapshotCache<List<TopToneDto>>,
    fetcher: SnapshotFetcher<List<TopToneDto>>,
    logger: Logger,
    mapper: TopTonesMapper
) : BaseSnapshotRepository<List<TopToneDto>, List<TopTone>>(
    cache = cache,
    fetcher = fetcher,
    mapper = mapper::map,
    logger = logger,
    tag = "TopTonesSnapshot"
)