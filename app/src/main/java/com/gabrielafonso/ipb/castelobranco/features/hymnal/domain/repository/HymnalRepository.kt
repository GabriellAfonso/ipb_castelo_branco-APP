package com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.repository

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.model.Hymn
import kotlinx.coroutines.flow.Flow

interface HymnalRepository {
    fun observeHymnal(): Flow<SnapshotState<List<Hymn>>>
    suspend fun refreshHymnal(): RefreshResult
}