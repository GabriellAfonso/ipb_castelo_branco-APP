package com.ipb.castelobranco.core.domain.usecase

import com.ipb.castelobranco.core.domain.startup.Preloadable
import com.ipb.castelobranco.core.domain.startup.Refreshable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PreloadDataUseCase @Inject constructor(
    private val preloadables: Set<@JvmSuppressWildcards Preloadable>,
    private val refreshables: Set<@JvmSuppressWildcards Refreshable>,
) {
    suspend operator fun invoke() {
        preloadCachesFromDisk()
        refreshDataFromNetwork()
    }

    private suspend fun preloadCachesFromDisk() = withContext(Dispatchers.IO) {
        supervisorScope {
            preloadables
                .map { launch { runCatching { it.preload() } } }
                .joinAll()
        }
    }

    private suspend fun refreshDataFromNetwork() = withContext(Dispatchers.IO) {
        supervisorScope {
            refreshables
                .map { async { runCatching { it.refresh() } } }
                .forEach { it.await() }
        }
    }
}
