package com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.data.repository

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.BaseSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.Logger
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.data.dto.LyricsDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.data.mapper.toDomain
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.domain.model.Lyrics
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.domain.repository.LyricsRepository
import javax.inject.Inject

class LyricsRepositoryImpl @Inject constructor(
    cache: SnapshotCache<List<LyricsDto>>,
    fetcher: SnapshotFetcher<List<LyricsDto>>,
    logger: Logger,
) : LyricsRepository,
    BaseSnapshotRepository<List<LyricsDto>, List<Lyrics>>(
        cache   = cache,
        fetcher = fetcher,
        mapper  = { dtos -> dtos.toDomain() },
        logger  = logger,
        tag     = "LyricsSnapshot",
    )
