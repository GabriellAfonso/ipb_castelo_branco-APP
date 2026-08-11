package com.ipb.castelobranco.features.hymnal.data.local

import kotlinx.serialization.Serializable

/**
 * On-disk form of a recorded view.
 *
 * Kept separate from the wire DTO on purpose: the collection service rejects any event carrying
 * an unknown field, so a local-only addition here (a retry counter, an enqueue timestamp) must
 * not be able to reach the request body. The mapper is where that separation is enforced.
 *
 * [viewedAt] is an ISO-8601 string with offset — `kotlinx.serialization` has no built-in
 * `OffsetDateTime` serializer, and the string round-trips losslessly.
 */
@Serializable
data class QueuedHymnViewEvent(
    val clientEventId: String,
    val hymnId: Int,
    val deviceId: String,
    val viewedAt: String,
    val durationSeconds: Long,
    val appVersion: String,
    val platform: String,
)
