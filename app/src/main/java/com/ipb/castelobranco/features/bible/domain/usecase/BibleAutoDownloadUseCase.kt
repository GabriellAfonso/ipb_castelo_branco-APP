package com.ipb.castelobranco.features.bible.domain.usecase

import com.ipb.castelobranco.features.bible.domain.download.BibleDownloadScheduler
import com.ipb.castelobranco.features.bible.domain.model.BibleTranslation
import com.ipb.castelobranco.features.bible.domain.repository.BibleRepository
import javax.inject.Inject

class BibleAutoDownloadUseCase @Inject constructor(
    private val scheduler: BibleDownloadScheduler,
    private val repository: BibleRepository,
) {
    /**
     * Enfileira o download (WiFi only) se ainda falta alguma tradução em cache.
     * Deve rodar APÓS [BibleRepository.preload] para que `cachedTranslationsFlow` reflita o disco.
     */
    fun triggerIfNeeded() {
        val cached = repository.cachedTranslationsFlow.value
        val missing = BibleTranslation.entries.any { it !in cached }
        if (missing) scheduler.enqueueWifiOnly()
    }

    fun enqueueWifiOnly(replaceExisting: Boolean = false, force: Boolean = false) =
        scheduler.enqueueWifiOnly(replaceExisting, force)

    fun enqueueAnyNetwork(force: Boolean = false) = scheduler.enqueueAnyNetwork(force)
}
