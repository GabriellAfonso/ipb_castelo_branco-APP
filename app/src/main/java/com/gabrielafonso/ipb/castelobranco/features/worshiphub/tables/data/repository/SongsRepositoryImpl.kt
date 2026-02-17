package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.repository

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.snapshot.AllSongsSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.snapshot.SongsBySundaySnapshotRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.snapshot.SuggestedSongsSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.snapshot.TopSongsSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.snapshot.TopTonesSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.model.Song
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.model.SuggestedSong
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.model.TopSong
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.model.TopTone
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SongsRepositoryImpl @Inject constructor(
    private val songsBySundaySnapshot: SongsBySundaySnapshotRepository,
    private val topSongsSnapshot: TopSongsSnapshotRepository,
    private val topTonesSnapshot: TopTonesSnapshotRepository,
    private val allSongsSnapshot: AllSongsSnapshotRepository,
    private val suggestedSongsSnapshot: SuggestedSongsSnapshotRepository
) : SongsRepository {

    override fun observeSongsBySunday(): Flow<SnapshotState<List<SundaySet>>> =
        songsBySundaySnapshot.observe()

    override suspend fun refreshSongsBySunday(): RefreshResult =
        songsBySundaySnapshot.refresh()

    override fun observeTopSongs(): Flow<SnapshotState<List<TopSong>>> =
        topSongsSnapshot.observe()

    override suspend fun refreshTopSongs(): RefreshResult =
        topSongsSnapshot.refresh()

    override fun observeTopTones(): Flow<SnapshotState<List<TopTone>>> =
        topTonesSnapshot.observe()

    override suspend fun refreshTopTones(): RefreshResult =
        topTonesSnapshot.refresh()

    override fun observeSuggestedSongs(): Flow<SnapshotState<List<SuggestedSong>>> =
        suggestedSongsSnapshot.observeState()

    override suspend fun refreshSuggestedSongs(): RefreshResult =
        suggestedSongsSnapshot.refresh(fixedByPosition = emptyMap())

    override suspend fun refreshSuggestedSongs(fixedByPosition: Map<Int, Int>): RefreshResult =
        suggestedSongsSnapshot.refresh(fixedByPosition)

    override fun observeAllSongs(): Flow<SnapshotState<List<Song>>> =
        allSongsSnapshot.observe()

    override suspend fun refreshAllSongs(): RefreshResult =
        allSongsSnapshot.refresh()
}