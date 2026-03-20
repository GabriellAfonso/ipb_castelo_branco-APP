package com.ipb.castelobranco.features.worshiphub.chordcharts.domain.repository

import com.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.model.ChordChart
import kotlinx.coroutines.flow.Flow

interface ChordChartRepository {
    fun observe(): Flow<SnapshotState<List<ChordChart>>>
    suspend fun preload()
    suspend fun refresh(): RefreshResult
}
