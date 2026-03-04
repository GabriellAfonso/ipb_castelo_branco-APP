package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.repository

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.NetworkResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.SuggestedSongDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.fetcher.SuggestedSongsFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.mapper.SuggestedSongsMapper
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.snapshot.AllSongsSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.snapshot.SongsBySundaySnapshotRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.snapshot.TopSongsSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.snapshot.TopTonesSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.Song
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SuggestedSong
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.TopSong
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.TopTone
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class SongsRepositoryImpl @Inject constructor(
    private val songsBySundaySnapshot: SongsBySundaySnapshotRepository,
    private val topSongsSnapshot: TopSongsSnapshotRepository,
    private val topTonesSnapshot: TopTonesSnapshotRepository,
    private val allSongsSnapshot: AllSongsSnapshotRepository,
    private val suggestedSongsCache: SnapshotCache<List<SuggestedSongDto>>,
    private val suggestedSongsFetcher: SuggestedSongsFetcher,
    private val suggestedSongsMapper: SuggestedSongsMapper,
) : SongsRepository {

    private val _suggestedSongsState =
        MutableStateFlow<SnapshotState<List<SuggestedSong>>>(SnapshotState.Loading)
    private val didLoadSuggestedCacheOnce = AtomicBoolean(false)

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
        _suggestedSongsState.asStateFlow()

    override suspend fun refreshSuggestedSongs(): RefreshResult =
        refreshSuggestedSongs(fixedByPosition = emptyMap())

    override suspend fun refreshSuggestedSongs(fixedByPosition: Map<Int, Int>): RefreshResult {
        if (didLoadSuggestedCacheOnce.compareAndSet(false, true)) {
            suggestedSongsCache.load()?.let { cached ->
                _suggestedSongsState.value = SnapshotState.Data(suggestedSongsMapper.map(cached))
            }
        }
        _suggestedSongsState.value = SnapshotState.Loading
        return when (val result = suggestedSongsFetcher.fetch(fixedByPosition)) {
            is NetworkResult.Success -> {
                suggestedSongsCache.save(result.body, result.etag)
                _suggestedSongsState.value = SnapshotState.Data(suggestedSongsMapper.map(result.body))
                RefreshResult.Updated
            }
            is NetworkResult.NotModified -> RefreshResult.NotModified
            is NetworkResult.Failure -> {
                _suggestedSongsState.value = SnapshotState.Error(result.throwable)
                if (suggestedSongsCache.load() != null) RefreshResult.CacheUsed
                else RefreshResult.Error(result.throwable)
            }
        }
    }

    override fun observeAllSongs(): Flow<SnapshotState<List<Song>>> =
        allSongsSnapshot.observe()

    override suspend fun refreshAllSongs(): RefreshResult =
        allSongsSnapshot.refresh()
}
