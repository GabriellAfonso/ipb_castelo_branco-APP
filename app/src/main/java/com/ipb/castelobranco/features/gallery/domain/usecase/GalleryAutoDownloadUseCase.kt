package com.ipb.castelobranco.features.gallery.domain.usecase

import com.ipb.castelobranco.features.gallery.domain.download.GalleryDownloadScheduler
import com.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import javax.inject.Inject

class GalleryAutoDownloadUseCase @Inject constructor(
    private val scheduler: GalleryDownloadScheduler,
    private val repository: GalleryRepository,
) {
    /**
     * Enfileira o download automático (WiFi only) se a galeria estiver vazia localmente.
     * Deve ser chamado APÓS preloadDataUseCase() para que albumsFlow reflita o disco.
     */
    fun triggerIfNeeded() {
        if (repository.albumsFlow.value.isEmpty()) {
            scheduler.enqueueWifiOnly()
        }
    }

    /** Download manual via botão — WiFi only, mantém se já estiver rodando. */
    fun enqueueWifiOnly(replaceExisting: Boolean = false) = scheduler.enqueueWifiOnly(replaceExisting)

    /** Download forçado com dados móveis — substitui qualquer trabalho pendente. */
    fun enqueueAnyNetwork() = scheduler.enqueueAnyNetwork()
}
