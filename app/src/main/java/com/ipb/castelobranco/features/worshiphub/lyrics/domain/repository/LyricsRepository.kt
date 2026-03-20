package com.ipb.castelobranco.features.worshiphub.lyrics.domain.repository

import com.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.lyrics.domain.model.Lyrics
import kotlinx.coroutines.flow.Flow

interface LyricsRepository {
    fun observe(): Flow<SnapshotState<List<Lyrics>>>
    suspend fun preload()
    suspend fun refresh(): RefreshResult
}
