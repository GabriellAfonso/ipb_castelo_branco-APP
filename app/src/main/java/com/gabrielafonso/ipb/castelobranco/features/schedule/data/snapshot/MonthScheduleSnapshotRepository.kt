package com.gabrielafonso.ipb.castelobranco.features.schedule.data.snapshot

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.BaseSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.Logger
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.dto.MonthScheduleDto
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.mapper.MonthScheduleMapper
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.MonthSchedule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonthScheduleSnapshotRepository @Inject constructor(
    cache: SnapshotCache<MonthScheduleDto>,
    fetcher: SnapshotFetcher<MonthScheduleDto>,
    logger: Logger,
    mapper: MonthScheduleMapper,
) : BaseSnapshotRepository<MonthScheduleDto, MonthSchedule>(
    cache = cache,
    fetcher = fetcher,
    mapper = mapper::map,
    logger = logger,
    tag = "ScheduleSnapshot"
)