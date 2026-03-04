package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.snapshot

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.BaseSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.Logger
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.SongsBySundayDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.mapper.toDomain
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SundaySet
import javax.inject.Inject

class SongsBySundaySnapshotRepository @Inject constructor(
    cache: SnapshotCache<List<SongsBySundayDto>>,
    fetcher: SnapshotFetcher<List<SongsBySundayDto>>,
    logger: Logger,
) : BaseSnapshotRepository<List<SongsBySundayDto>, List<SundaySet>>(
    cache = cache,
    fetcher = fetcher,
    mapper = { it.toDomain() },
    logger = logger,
    tag = "SongsBySundaySnapshot"
)
