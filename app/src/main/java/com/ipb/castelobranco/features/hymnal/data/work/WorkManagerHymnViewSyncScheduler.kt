package com.ipb.castelobranco.features.hymnal.data.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ipb.castelobranco.features.hymnal.domain.sync.HymnViewSyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerHymnViewSyncScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : HymnViewSyncScheduler {

    override fun scheduleSync() {
        runCatching {
            val request = OneTimeWorkRequestBuilder<HymnViewSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    BACKOFF_SECONDS,
                    TimeUnit.SECONDS,
                )
                .build()

            // KEEP, so a burst of recorded views cannot spawn a burst of jobs. The known gap —
            // a view recorded while a run is finishing — is closed by the drain loop and by the
            // enqueue in MyApp.onCreate().
            WorkManager.getInstance(context).enqueueUniqueWork(
                HymnViewSyncWorker.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request,
            )
        }.onFailure { Timber.d(it, "Failed to schedule hymn view sync") }
    }

    private companion object {
        const val BACKOFF_SECONDS = 30L
    }
}
