package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.snapshot

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.BaseSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.Logger
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.AllSongDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.mapper.AllSongsMapper
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.Song
import javax.inject.Inject

class AllSongsSnapshotRepository @Inject constructor(
    cache: SnapshotCache<List<AllSongDto>>,
    fetcher: SnapshotFetcher<List<AllSongDto>>,
    logger: Logger,
    mapper: AllSongsMapper
) : BaseSnapshotRepository<List<AllSongDto>, List<Song>>(
    cache = cache,
    fetcher = fetcher,
    mapper = mapper::map,
    logger = logger,
    tag = "AllSongsSnapshot"
)