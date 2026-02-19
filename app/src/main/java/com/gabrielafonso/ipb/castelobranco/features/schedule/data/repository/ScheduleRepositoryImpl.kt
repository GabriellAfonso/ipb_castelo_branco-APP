package com.gabrielafonso.ipb.castelobranco.features.schedule.data.repository

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState

import com.gabrielafonso.ipb.castelobranco.features.schedule.data.snapshot.MonthScheduleSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.MonthSchedule
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val snapshot: MonthScheduleSnapshotRepository
) : ScheduleRepository {

    // O snapshot.observe() agora retorna o StateFlow interno da classe base
    override fun observeMonthSchedule(): Flow<SnapshotState<MonthSchedule>> =
        snapshot.observe()

    // Devolve o estado atual que está na memória do snapshot
    override fun getCurrentSnapshot(): SnapshotState<MonthSchedule> =
        snapshot.getCurrentState()

    // Chama o novo método de leitura de disco que criamos na Base
    override suspend fun preload() {
        snapshot.preload()
    }

    override suspend fun refreshMonthSchedule(): RefreshResult =
        snapshot.refresh()
}