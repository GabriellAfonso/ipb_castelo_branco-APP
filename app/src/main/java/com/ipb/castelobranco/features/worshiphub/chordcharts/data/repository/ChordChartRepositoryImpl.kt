package com.ipb.castelobranco.features.worshiphub.chordcharts.data.repository

import com.ipb.castelobranco.core.domain.snapshot.BaseSnapshotRepository
import com.ipb.castelobranco.core.domain.snapshot.Logger
import com.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.ipb.castelobranco.features.worshiphub.chordcharts.data.dto.ChordChartDto
import com.ipb.castelobranco.features.worshiphub.chordcharts.data.mapper.toDomain
import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.model.ChordChart
import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.repository.ChordChartRepository
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
