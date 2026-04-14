package com.ipb.castelobranco.features.gallery.domain.usecase

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ipb.castelobranco.features.gallery.data.work.GalleryDownloadWorker
import com.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import javax.inject.Inject

class GalleryAutoDownloadUseCase @Inject constructor(
    private val workManager: WorkManager,
    private val repository: GalleryRepository,
) {
    /**
     * Enfileira o download automático (WiFi only) se a galeria estiver vazia localmente.
     * Deve ser chamado APÓS preloadDataUseCase() para que albumsFlow reflita o disco.
     */
    fun triggerIfNeeded() {
        if (repository.albumsFlow.value.isEmpty()) {
            enqueueWifiOnly(policy = ExistingWorkPolicy.KEEP)
        }
    }

    /** Download manual via botão — WiFi only, mantém se já estiver rodando. */
    fun enqueueWifiOnly(policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP) {
        val request = OneTimeWorkRequestBuilder<GalleryDownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build()
            )
            .build()
        workManager.enqueueUniqueWork(GalleryDownloadWorker.WORK_NAME, policy, request)
    }

    /** Download forçado com dados móveis — substitui qualquer trabalho pendente. */
    fun enqueueAnyNetwork() {
        val request = OneTimeWorkRequestBuilder<GalleryDownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        workManager.enqueueUniqueWork(
            GalleryDownloadWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
