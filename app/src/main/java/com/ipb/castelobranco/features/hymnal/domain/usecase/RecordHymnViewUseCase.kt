package com.ipb.castelobranco.features.hymnal.domain.usecase

import com.ipb.castelobranco.core.data.local.DeviceIdProvider
import com.ipb.castelobranco.core.di.AppVersionName
import com.ipb.castelobranco.core.di.PlatformName
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewEvent
import com.ipb.castelobranco.features.hymnal.domain.repository.HymnViewHistoryRepository
import com.ipb.castelobranco.features.hymnal.domain.sync.HymnViewSyncScheduler
import timber.log.Timber
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * Records one qualifying hymn view and asks for it to be delivered.
 *
 * Silent by contract: a hymn without a server id produces nothing, and any failure is swallowed.
 * Nothing here may reach the member.
 */
class RecordHymnViewUseCase @Inject constructor(
    private val repository: HymnViewHistoryRepository,
    private val deviceIdProvider: DeviceIdProvider,
    private val scheduler: HymnViewSyncScheduler,
    @param:AppVersionName private val appVersion: String,
    @param:PlatformName private val platform: String,
) {

    suspend operator fun invoke(hymnId: Int?, durationSeconds: Long) {
        // Snapshots cached before the server exposed hymn ids cannot be reported. Skip silently;
        // the next snapshot refresh fixes it.
        if (hymnId == null) {
            Timber.d("Hymn view not recorded: hymn has no server id")
            return
        }

        runCatching {
            val event = HymnViewEvent(
                clientEventId = UUID.randomUUID().toString(),
                hymnId = hymnId,
                deviceId = deviceIdProvider.get(),
                viewedAt = OffsetDateTime.now(),
                durationSeconds = durationSeconds.coerceAtLeast(0L),
                appVersion = appVersion,
                platform = platform,
            )
            repository.record(event)
            scheduler.scheduleSync()
        }.onFailure { Timber.w(it, "Failed to record hymn view") }
    }
}
