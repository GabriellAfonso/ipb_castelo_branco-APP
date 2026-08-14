package com.ipb.castelobranco.features.admin.reports.hymnal.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire types for the hymnal history administration API, named exactly as the service names them.
 *
 * Every field carries an explicit `@SerialName`: the shared `Json` does not rename automatically,
 * so a missing annotation is a field that silently never arrives.
 */

// region reporting

@Serializable
data class OccurrencesResponseDto(
    @SerialName("from") val from: String,
    @SerialName("to") val to: String,
    @SerialName("group_by") val groupBy: String,
    @SerialName("occurrences") val occurrences: List<OccurrenceDto> = emptyList(),
)

/**
 * @param serviceWindowId `null` when the views fell outside every active window and collapsed by
 *   calendar day. Nullable on purpose — it is the "Fora do culto" reading.
 * @param deviceCount reach, not a count of singings. One occurrence is one congregational
 *   singing regardless of how many devices contributed to it.
 */
@Serializable
data class OccurrenceDto(
    @SerialName("hymn_number") val hymnNumber: String,
    @SerialName("hymn_title") val hymnTitle: String,
    @SerialName("occurred_on") val occurredOn: String,
    @SerialName("service_window_id") val serviceWindowId: Int? = null,
    @SerialName("service_window_name") val serviceWindowName: String? = null,
    @SerialName("bucket") val bucket: String = "",
    @SerialName("device_count") val deviceCount: Int = 0,
)

@Serializable
data class TopHymnsResponseDto(
    @SerialName("from") val from: String? = null,
    @SerialName("to") val to: String? = null,
    @SerialName("hymns") val hymns: List<TopHymnDto> = emptyList(),
)

@Serializable
data class TopHymnDto(
    @SerialName("hymn_number") val hymnNumber: String,
    @SerialName("hymn_title") val hymnTitle: String,
    @SerialName("occurrence_count") val occurrenceCount: Int = 0,
)

// endregion

// region settings

@Serializable
data class CollectionSettingsDto(
    @SerialName("min_seconds_to_count") val minSecondsToCount: Int,
    @SerialName("collapse_window_minutes") val collapseWindowMinutes: Int,
    @SerialName("max_batch_size") val maxBatchSize: Int,
    @SerialName("max_past_days") val maxPastDays: Int,
    @SerialName("future_tolerance_minutes") val futureToleranceMinutes: Int,
    @SerialName("window_grace_minutes") val windowGraceMinutes: Int,
)

/**
 * The partial patch. Every field is nullable and defaults to `null`; the shared `Json` is built
 * with `explicitNulls = false`, so a null field is **omitted from the body** rather than sent as
 * `null`. That is what makes "send only what changed" true on the wire and not merely in intent.
 */
@Serializable
data class CollectionSettingsPatchDto(
    @SerialName("min_seconds_to_count") val minSecondsToCount: Int? = null,
    @SerialName("collapse_window_minutes") val collapseWindowMinutes: Int? = null,
    @SerialName("max_batch_size") val maxBatchSize: Int? = null,
    @SerialName("max_past_days") val maxPastDays: Int? = null,
    @SerialName("future_tolerance_minutes") val futureToleranceMinutes: Int? = null,
    @SerialName("window_grace_minutes") val windowGraceMinutes: Int? = null,
) {
    val isEmpty: Boolean
        get() = minSecondsToCount == null &&
            collapseWindowMinutes == null &&
            maxBatchSize == null &&
            maxPastDays == null &&
            futureToleranceMinutes == null &&
            windowGraceMinutes == null
}

// endregion

// region service windows

/** `weekday` is the service's convention: `0 = Monday … 6 = Sunday`. Sunday is 6, not 0. */
@Serializable
data class ServiceWindowDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("weekday") val weekday: Int,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("active") val active: Boolean = true,
)

@Serializable
data class ServiceWindowListDto(
    @SerialName("service_windows") val serviceWindows: List<ServiceWindowDto> = emptyList(),
)

@Serializable
data class ServiceWindowWriteDto(
    @SerialName("name") val name: String,
    @SerialName("weekday") val weekday: Int,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    @SerialName("active") val active: Boolean = true,
)

// endregion
