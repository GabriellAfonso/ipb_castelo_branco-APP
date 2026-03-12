package com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.data.repository

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.BaseSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.Logger
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.data.dto.ChordChartDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.data.mapper.toDomain
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.domain.model.ChordChart
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.domain.repository.ChordChartRepository
import javax.inject.Inject

class ChordChartRepositoryImpl @Inject constructor(
    cache: SnapshotCache<List<ChordChartDto>>,
    fetcher: SnapshotFetcher<List<ChordChartDto>>,
    logger: Logger,
) : ChordChartRepository,
    BaseSnapshotRepository<List<ChordChartDto>, List<ChordChart>>(
        cache = cache,
        fetcher = fetcher,
        mapper = { dtos -> dtos.toDomain() },
        logger = logger,
        tag = "ChordChartsSnapshot",
    )
