package com.ipb.castelobranco.features.bible.domain.usecase

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ipb.castelobranco.features.bible.data.work.BibleDownloadWorker
import com.ipb.castelobranco.features.bible.domain.model.BibleTranslation
import com.ipb.castelobranco.features.bible.domain.repository.BibleRepository
import javax.inject.Inject

class BibleAutoDownloadUseCase @Inject constructor(
    private val workManager: WorkManager,
    private val repository: BibleRepository,
) {
    /**
     * Enfileira o download (WiFi only) se ainda falta alguma tradução em cache.
     * Deve rodar APÓS [BibleRepository.preload] para que `cachedTranslationsFlow` reflita o disco.
     */
    fun triggerIfNeeded() {
        val cached = repository.cachedTranslationsFlow.value
        val missing = BibleTranslation.entries.any { it !in cached }
        if (missing) {
            enqueueWifiOnly(policy = ExistingWorkPolicy.KEEP)
        }
    }

    fun enqueueWifiOnly(
        policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP,
        force: Boolean = false,
    ) {
        val request = OneTimeWorkRequestBuilder<BibleDownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build()
            )
            .setInputData(forceData(force))
            .build()
        workManager.enqueueUniqueWork(BibleDownloadWorker.WORK_NAME, policy, request)
    }

    fun enqueueAnyNetwork(force: Boolean = false) {
        val request = OneTimeWorkRequestBuilder<BibleDownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(forceData(force))
            .build()
        workManager.enqueueUniqueWork(
            BibleDownloadWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private fun forceData(force: Boolean): Data =
        workDataOf(BibleDownloadWorker.INPUT_FORCE to force)
}
