package com.ipb.castelobranco.features.hymnal.data.repository

import com.ipb.castelobranco.core.domain.auth.AuthStatusProvider
import com.ipb.castelobranco.core.domain.error.toAppError
import com.ipb.castelobranco.core.network.error.toAppError
import com.ipb.castelobranco.features.hymnal.data.api.HymnalHistoryApi
import com.ipb.castelobranco.features.hymnal.data.dto.IngestRequestDto
import com.ipb.castelobranco.features.hymnal.data.dto.RejectedEventDto
import com.ipb.castelobranco.features.hymnal.data.local.HymnViewQueueStore
import com.ipb.castelobranco.features.hymnal.data.local.HymnViewSettingsStore
import com.ipb.castelobranco.features.hymnal.data.mapper.toDomain
import com.ipb.castelobranco.features.hymnal.data.mapper.toQueued
import com.ipb.castelobranco.features.hymnal.data.mapper.toRejectionReason
import com.ipb.castelobranco.features.hymnal.data.mapper.toWireDto
import com.ipb.castelobranco.features.hymnal.di.AuthLessHistoryApi
import com.ipb.castelobranco.features.hymnal.di.AuthedHistoryApi
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewCollectionSettings
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewEvent
import com.ipb.castelobranco.features.hymnal.domain.model.SyncOutcome
import com.ipb.castelobranco.features.hymnal.domain.repository.HymnViewHistoryRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HymnViewHistoryRepositoryImpl @Inject constructor(
    @param:AuthedHistoryApi private val authedApi: HymnalHistoryApi,
    @param:AuthLessHistoryApi private val authLessApi: HymnalHistoryApi,
    private val queueStore: HymnViewQueueStore,
    private val settingsStore: HymnViewSettingsStore,
    private val authStatusProvider: AuthStatusProvider,
) : HymnViewHistoryRepository {

    override suspend fun record(event: HymnViewEvent) {
        queueStore.append(event.toQueued())
    }

    override suspend fun queuedCount(): Int = queueStore.count()

    override suspend fun currentSettings(): HymnViewCollectionSettings = settingsStore.read()

    override suspend fun refreshSettings() {
        runCatching {
            // Public read: no token needed, and using the authed client here would risk a
            // refresh-or-logout cycle for no benefit.
            val response = authLessApi.getSettings()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                settingsStore.write(body.toDomain())
            } else {
                Timber.d("Hymn history settings fetch failed: HTTP %d", response.code())
            }
        }.onFailure { Timber.d(it, "Hymn history settings fetch failed") }
    }

    override suspend fun syncOnce(): SyncOutcome {
        val settings = settingsStore.read()
        val chunk = queueStore.readAll().take(settings.maxBatchSize)
        if (chunk.isEmpty()) return SyncOutcome.Delivered(emptySet())

        val submittedIds = chunk.map { it.clientEventId }.toSet()
        val request = IngestRequestDto(events = chunk.map { it.toWireDto() })

        return runCatching {
            // Only attach credentials when the access token is actually still valid. Sending an
            // expired one makes the server reply 401, which drives TokenAuthenticator into a
            // refresh attempt that clears the token store on failure — a silent logout caused by
            // background telemetry.
            val api = if (authStatusProvider.hasValidAccessToken()) authedApi else authLessApi
            val response = api.submitEvents(request)

            when {
                response.isSuccessful -> {
                    logRejections(response.body()?.rejected.orEmpty())
                    // Reconcile against what was SUBMITTED, not what was answered. An event the
                    // service silently dropped appears in neither list; removing only the
                    // answered ids would leave it in the queue forever.
                    queueStore.remove(submittedIds)
                    SyncOutcome.Delivered(submittedIds)
                }

                response.code() == HTTP_BAD_REQUEST -> {
                    val error = response.toAppError()
                    Timber.w("Hymn view chunk rejected wholesale (%s); discarding", error.message)
                    queueStore.remove(submittedIds)
                    SyncOutcome.Discarded(submittedIds)
                }

                else -> SyncOutcome.Deferred(response.toAppError())
            }
        }.getOrElse { throwable ->
            Timber.d(throwable, "Hymn view sync deferred")
            SyncOutcome.Deferred(throwable.toAppError())
        }
    }

    private fun logRejections(rejected: List<RejectedEventDto>) {
        rejected.forEach {
            Timber.d("Hymn view rejected: %s", it.reason.toRejectionReason().code)
        }
    }

    private companion object {
        const val HTTP_BAD_REQUEST = 400
    }
}
