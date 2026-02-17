package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.snapshot

import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.BaseSnapshotRepository
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.Logger
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.NetworkResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.gabrielafonso.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.dto.SuggestedSongDto
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.fetcher.SuggestedSongsFetcher
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.data.mapper.SuggestedSongsMapper
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.model.SuggestedSong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class SuggestedSongsSnapshotRepository @Inject constructor(
    private val cacheRef: SnapshotCache<List<SuggestedSongDto>>,
    fetcher: SnapshotFetcher<List<SuggestedSongDto>>,
    logger: Logger,
    private val mapperRef: SuggestedSongsMapper,
    private val suggestedSongsFetcher: SuggestedSongsFetcher
) : BaseSnapshotRepository<List<SuggestedSongDto>, List<SuggestedSong>>(
    cache = cacheRef,
    fetcher = fetcher,
    mapper = mapperRef::map,
    logger = logger,
    tag = "SuggestedSongsSnapshot"
) {
    private val didLoadCacheOnce = AtomicBoolean(false)

    private val _state =
        MutableStateFlow<SnapshotState<List<SuggestedSong>>>(SnapshotState.Loading)

    fun observeState(): Flow<SnapshotState<List<SuggestedSong>>> = _state.asStateFlow()

    /**
     * Refresh parametrizado: sempre rede e sempre sobrescreve o MESMO snapshot
     * (último resultado), usando o fixed atual.
     *
     * Importante: aqui a gente também atualiza o _state, senão a UI não muda
     * (porque BaseSnapshotRepository.observe() é "flow frio" e não re-emite após refresh()).
     */
    suspend fun refresh(fixedByPosition: Map<Int, Int>): RefreshResult {
        // carrega cache na primeira interação (opcional, mas melhora UX)
        if (didLoadCacheOnce.compareAndSet(false, true)) {
            cacheRef.load()?.let { cached ->
                _state.value = SnapshotState.Data(mapperRef.map(cached))
            }
        }

        _state.value = SnapshotState.Loading

        return when (val result = suggestedSongsFetcher.fetch(fixedByPosition)) {
            is NetworkResult.Success<List<SuggestedSongDto>> -> {
                cacheRef.save(result.body, result.etag)
                _state.value = SnapshotState.Data(mapperRef.map(result.body))
                RefreshResult.Updated
            }

            NetworkResult.NotModified -> {
                RefreshResult.NotModified
            }

            is NetworkResult.Failure -> {
                _state.value = SnapshotState.Error(result.throwable)
                if (cacheRef.load() != null) RefreshResult.CacheUsed
                else RefreshResult.Error(result.throwable)
            }
        }
    }
}