package com.ipb.castelobranco.features.admin.reports.hymnal.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * One hymn sung once by the congregation — **not** once per person.
 *
 * Twenty phones opening hymn 50 during Sunday service form one occurrence with a
 * [deviceCount] of 20. Counting occurrences answers *how many times it was sung*; summing
 * [deviceCount] answers *how many devices followed along*. The first is the primary metric
 * everywhere in this feature; the second is never a bar length and never an ordering.
 *
 * @param serviceWindowId `null` means the views fell outside every active service and collapsed
 *   by calendar day instead. That is meaning, not missing data: it is the "Fora do culto" slice.
 */
data class HymnOccurrence(
    val hymnNumber: String,
    val hymnTitle: String,
    val occurredOn: LocalDate,
    val serviceWindowId: Int?,
    val serviceWindowName: String?,
    val bucket: String,
    val deviceCount: Int,
)

/** A closed period of occurrences, exactly as one request returned it. */
data class OccurrenceReport(
    val range: DateRange,
    val granularity: BucketGranularity,
    val occurrences: List<HymnOccurrence>,
)

/** A hymn and how many occurrences it has, across all recorded history. */
data class TopHymn(
    val number: String,
    val title: String,
    val occurrenceCount: Int,
)

/** An inclusive day range, both ends counted. */
data class DateRange(val from: LocalDate, val to: LocalDate) {

    /** Inclusive length: a range whose ends are the same day is one day, not zero. */
    val days: Int get() = ChronoUnit.DAYS.between(from, to).toInt() + 1

    /** The range of the same length immediately before this one, used for period comparisons. */
    fun preceding(): DateRange = DateRange(
        from = from.minusDays(days.toLong()),
        to = from.minusDays(1),
    )

    companion object {
        /** The service refuses anything longer, so the app refuses it first. */
        const val MAX_DAYS = 366
    }
}
