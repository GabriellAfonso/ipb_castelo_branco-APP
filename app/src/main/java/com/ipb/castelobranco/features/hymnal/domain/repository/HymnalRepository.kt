package com.ipb.castelobranco.features.hymnal.domain.repository

import com.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.hymnal.domain.model.Hymn
import kotlinx.coroutines.flow.Flow

interface HymnalRepository {
    fun observeHymnal(): Flow<SnapshotState<List<Hymn>>>
    suspend fun refreshHymnal(): RefreshResult
}