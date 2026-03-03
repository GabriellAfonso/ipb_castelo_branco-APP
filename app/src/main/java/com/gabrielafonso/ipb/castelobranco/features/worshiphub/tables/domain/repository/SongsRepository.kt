package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.repository

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.Song
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SuggestedSong
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.TopSong
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.TopTone
import kotlinx.coroutines.flow.Flow

interface SongsRepository {

    fun observeSongsBySunday(): Flow<SnapshotState<List<SundaySet>>>
    suspend fun refreshSongsBySunday(): RefreshResult

    fun observeTopSongs(): Flow<SnapshotState<List<TopSong>>>
    suspend fun refreshTopSongs(): RefreshResult

    fun observeTopTones(): Flow<SnapshotState<List<TopTone>>>
    suspend fun refreshTopTones(): RefreshResult

    fun observeSuggestedSongs(): Flow<SnapshotState<List<SuggestedSong>>>
    suspend fun refreshSuggestedSongs(): RefreshResult
    suspend fun refreshSuggestedSongs(fixedByPosition: Map<Int, Int>): RefreshResult

    fun observeAllSongs(): Flow<SnapshotState<List<Song>>>
    suspend fun refreshAllSongs(): RefreshResult
}