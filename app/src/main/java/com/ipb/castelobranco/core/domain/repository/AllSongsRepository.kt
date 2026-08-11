package com.ipb.castelobranco.core.domain.repository

import com.ipb.castelobranco.core.domain.model.Song
import com.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import kotlinx.coroutines.flow.Flow

interface AllSongsRepository {
    fun observeAllSongs(): Flow<SnapshotState<List<Song>>>
    suspend fun refreshAllSongs(): RefreshResult
}
