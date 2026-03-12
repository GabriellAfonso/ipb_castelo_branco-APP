package com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.domain.usecase

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.domain.model.ChordChart
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.domain.repository.ChordChartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChordChartsUseCase @Inject constructor(
    private val repository: ChordChartRepository,
) {
    fun observe(): Flow<SnapshotState<List<ChordChart>>> = repository.observe()
    suspend fun refresh(): RefreshResult = repository.refresh()
}
