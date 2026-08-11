package com.ipb.castelobranco.features.hymnal.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ipb.castelobranco.features.hymnal.domain.usecase.SyncHymnViewsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Delivers queued hymn views in the background.
 *
 * Nothing this worker does may reach the member: every failure becomes a retry, and the whole
 * body is guarded so an unexpected throwable cannot crash the process.
 */
@HiltWorker
class HymnViewSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncHymnViews: SyncHymnViewsUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        runCatching {
            if (syncHymnViews()) Result.success() else Result.retry()
        }.getOrElse {
            Timber.d(it, "Hymn view sync worker failed")
            Result.retry()
        }

    companion object {
        const val WORK_NAME = "hymn_view_sync"
    }
}
