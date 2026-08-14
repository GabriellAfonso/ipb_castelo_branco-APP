package com.ipb.castelobranco.features.gallery.domain.download

/**
 * Schedules the gallery download job.
 *
 * An interface so `domain/` stays free of WorkManager. [replaceExisting] stands in for the
 * scheduling policy: `false` keeps a job that is already queued, `true` replaces it.
 */
interface GalleryDownloadScheduler {
    fun enqueueWifiOnly(replaceExisting: Boolean = false)
    fun enqueueAnyNetwork()

    /**
     * Cancels the job and, with it, any finished state it left behind — a failed job stays in the
     * scheduler's records and would keep being reported to the UI long after it ran.
     */
    fun cancel()
}
