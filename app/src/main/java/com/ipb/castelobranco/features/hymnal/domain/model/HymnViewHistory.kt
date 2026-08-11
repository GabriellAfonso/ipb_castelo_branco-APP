package com.ipb.castelobranco.features.hymnal.domain.model

import com.ipb.castelobranco.core.domain.error.AppError
import java.time.OffsetDateTime

/**
 * One occasion on which a member kept a hymn on screen long enough for it to count.
 *
 * Created only when the accumulated foreground time crosses the configured threshold, and only
 * for hymns that carry a server [Hymn.id]. Once the collection service has answered for it, it
 * has no further use on the device.
 */
data class HymnViewEvent(
    val clientEventId: String,
    val hymnId: Int,
    val deviceId: String,
    val viewedAt: OffsetDateTime,
    val durationSeconds: Long,
    val appVersion: String,
    val platform: String,
)

/**
 * The church-controlled collection parameters the app consumes. The service exposes more; only
 * these two affect client behaviour.
 */
data class HymnViewCollectionSettings(
    val minSecondsToCount: Int,
    val maxBatchSize: Int,
) {
    companion object {
        const val DEFAULT_MIN_SECONDS_TO_COUNT = 30
        const val DEFAULT_MAX_BATCH_SIZE = 50

        private const val MAX_ALLOWED_BATCH_SIZE = 500

        /** Used on a fresh install that has never reached the settings endpoint. */
        val DEFAULT = HymnViewCollectionSettings(
            minSecondsToCount = DEFAULT_MIN_SECONDS_TO_COUNT,
            maxBatchSize = DEFAULT_MAX_BATCH_SIZE,
        )

        /** Guards against a malformed or absurd server value making the feature misbehave. */
        fun sanitized(minSecondsToCount: Int, maxBatchSize: Int): HymnViewCollectionSettings =
            HymnViewCollectionSettings(
                minSecondsToCount = if (minSecondsToCount > 0) {
                    minSecondsToCount
                } else {
                    DEFAULT_MIN_SECONDS_TO_COUNT
                },
                maxBatchSize = maxBatchSize.coerceIn(1, MAX_ALLOWED_BATCH_SIZE),
            )
    }
}

/**
 * Why the collection service refused an event. The four named codes are a stable contract;
 * [Unrecognised] keeps a future addition loggable instead of fatal.
 */
sealed class RejectionReason(val code: String) {
    data object UnknownHymn : RejectionReason(CODE_UNKNOWN_HYMN)
    data object ViewedAtInFuture : RejectionReason(CODE_VIEWED_AT_IN_FUTURE)
    data object ViewedAtTooOld : RejectionReason(CODE_VIEWED_AT_TOO_OLD)
    data object InvalidEvent : RejectionReason(CODE_INVALID_EVENT)
    data class Unrecognised(val raw: String) : RejectionReason(raw)

    companion object {
        const val CODE_UNKNOWN_HYMN = "unknown_hymn"
        const val CODE_VIEWED_AT_IN_FUTURE = "viewed_at_in_future"
        const val CODE_VIEWED_AT_TOO_OLD = "viewed_at_too_old"
        const val CODE_INVALID_EVENT = "invalid_event"

        fun fromCode(raw: String): RejectionReason = when (raw) {
            CODE_UNKNOWN_HYMN -> UnknownHymn
            CODE_VIEWED_AT_IN_FUTURE -> ViewedAtInFuture
            CODE_VIEWED_AT_TOO_OLD -> ViewedAtTooOld
            CODE_INVALID_EVENT -> InvalidEvent
            else -> Unrecognised(raw)
        }
    }
}

/** What one submission attempt produced. */
sealed class SyncOutcome {
    /** The service answered; every id in [removedIds] has been dropped from the queue. */
    data class Delivered(val removedIds: Set<String>) : SyncOutcome()

    /** The chunk can never succeed (HTTP 400); dropped rather than retried forever. */
    data class Discarded(val removedIds: Set<String>) : SyncOutcome()

    /** Nothing was removed. Throttled, server error, or offline — retry later. */
    data class Deferred(val error: AppError) : SyncOutcome()
}
