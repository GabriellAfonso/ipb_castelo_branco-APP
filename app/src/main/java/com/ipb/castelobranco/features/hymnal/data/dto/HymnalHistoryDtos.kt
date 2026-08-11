package com.ipb.castelobranco.features.hymnal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One view, exactly as the collection service expects it.
 *
 * **Seven fields, no more.** The service forbids unknown keys: any extra property makes the
 * event fail parsing and come back as `invalid_event`, silently losing it. The shared `Json`
 * sets `encodeDefaults = true`, so every declared property does reach the wire — there is no
 * omission to hide behind. Local-only fields belong on `QueuedHymnViewEvent`, never here.
 */
@Serializable
data class HymnViewEventDto(
    @SerialName("client_event_id") val clientEventId: String,
    @SerialName("hymn_id") val hymnId: Int,
    @SerialName("device_id") val deviceId: String,
    @SerialName("viewed_at") val viewedAt: String,
    @SerialName("duration_seconds") val durationSeconds: Long,
    @SerialName("app_version") val appVersion: String,
    @SerialName("platform") val platform: String,
)

@Serializable
data class IngestRequestDto(
    @SerialName("events") val events: List<HymnViewEventDto>,
)

@Serializable
data class IngestResponseDto(
    @SerialName("accepted") val accepted: List<String> = emptyList(),
    @SerialName("rejected") val rejected: List<RejectedEventDto> = emptyList(),
)

@Serializable
data class RejectedEventDto(
    @SerialName("client_event_id") val clientEventId: String,
    @SerialName("reason") val reason: String,
)

/**
 * All six service settings are declared with defaults so a payload change cannot break parsing.
 * Only [minSecondsToCount] and [maxBatchSize] are mapped into the domain; the rest document the
 * contract and are discarded.
 */
@Serializable
data class HistorySettingsDto(
    @SerialName("min_seconds_to_count") val minSecondsToCount: Int = 30,
    @SerialName("collapse_window_minutes") val collapseWindowMinutes: Int = 10,
    @SerialName("max_batch_size") val maxBatchSize: Int = 50,
    @SerialName("max_past_days") val maxPastDays: Int = 30,
    @SerialName("future_tolerance_minutes") val futureToleranceMinutes: Int = 5,
    @SerialName("window_grace_minutes") val windowGraceMinutes: Int = 15,
)
