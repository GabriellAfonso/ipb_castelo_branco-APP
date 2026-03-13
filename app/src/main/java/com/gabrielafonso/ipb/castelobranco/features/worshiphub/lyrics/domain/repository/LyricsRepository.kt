package com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.domain.repository

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.domain.model.Lyrics
import kotlinx.coroutines.flow.Flow

interface LyricsRepository {
    fun observe(): Flow<SnapshotState<List<Lyrics>>>
    suspend fun preload()
    suspend fun refresh(): RefreshResult
}
