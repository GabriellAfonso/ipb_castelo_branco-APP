package com.gabrielafonso.ipb.castelobranco.core.domain.snapshot

import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

inline fun logTime(tag: String, message: String) {
    Log.d(tag, "[${SystemClock.elapsedRealtime()} ms] $message")
}
abstract class BaseSnapshotRepository<Dto, Domain>(
    private val cache: SnapshotCache<Dto>,
    private val fetcher: SnapshotFetcher<Dto>,
    private val mapper: (Dto) -> Domain,
    private val logger: Logger = Logger.Noop,
    private val tag: String
) {
    // Estado quente que mantém o dado vivo enquanto o App estiver aberto
    private val _state = MutableStateFlow<SnapshotState<Domain>>(SnapshotState.Loading)

    fun observe(): StateFlow<SnapshotState<Domain>> = _state.asStateFlow()

    // Método vital para o MainViewModel carregar o cache logo no boot
    suspend fun preload() {
        withContext(Dispatchers.IO) {
            val cached = cache.load()
            if (cached != null) {
                _state.value = SnapshotState.Data(mapper(cached))
            }
        }
    }

    fun getCurrentState(): SnapshotState<Domain> = _state.value

    suspend fun refresh(): RefreshResult {
        return try {
            val etag = withContext(Dispatchers.IO) { cache.loadETag() }
            when (val result = fetcher.fetch(etag)) {
                is NetworkResult.NotModified -> {
                    if (_state.value is SnapshotState.Loading) preload()
                    RefreshResult.NotModified
                }
                is NetworkResult.Success -> {
                    withContext(Dispatchers.IO) { cache.save(result.body, result.etag) }
                    val newData = mapper(result.body)
                    _state.value = SnapshotState.Data(newData)
                    RefreshResult.Updated
                }
                is NetworkResult.Failure -> {
                    val cached = withContext(Dispatchers.IO) { cache.load() }
                    if (cached != null) {
                        _state.value = SnapshotState.Data(mapper(cached))
                        RefreshResult.CacheUsed
                    } else RefreshResult.Error(result.throwable)
                }
            }
        } catch (t: Throwable) {
            RefreshResult.Error(t)
        }
    }
}