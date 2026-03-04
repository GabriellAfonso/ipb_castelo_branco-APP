package com.gabrielafonso.ipb.castelobranco.features.schedule.data.repository

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.BaseSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.Logger
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.dto.MonthScheduleDto
import com.gabrielafonso.ipb.castelobranco.features.schedule.data.mapper.toDomain
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.MonthSchedule
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    cache: SnapshotCache<MonthScheduleDto>,
    fetcher: SnapshotFetcher<MonthScheduleDto>,
    logger: Logger,
) : BaseSnapshotRepository<MonthScheduleDto, MonthSchedule>(
    cache = cache,
    fetcher = fetcher,
    mapper = { it.toDomain() },
    logger = logger,
    tag = "ScheduleSnapshot"
), ScheduleRepository {

    override fun observeMonthSchedule(): Flow<SnapshotState<MonthSchedule>> = observe()

    override fun getCurrentSnapshot(): SnapshotState<MonthSchedule> = getCurrentState()

    // preload() is inherited from BaseSnapshotRepository

    override suspend fun refreshMonthSchedule(): RefreshResult = refresh()
}
