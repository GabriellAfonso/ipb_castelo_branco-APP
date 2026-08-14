package com.ipb.castelobranco.features.admin.reports.hymnal.domain.model

import java.time.LocalDate
import java.time.YearMonth

/**
 * What each reading of the report produces. Every label, sentence and proportion here is written
 * in the domain and rendered as-is: the presentation layer never assembles text from numbers.
 */

/**
 * One bar of any chart in this feature.
 *
 * [value] is always the occurrence count — the primary metric — and [fraction] is derived from
 * it. Device reach lives in [secondaryLabel] and nowhere else, so no chart can accidentally
 * encode reach as a length.
 */
data class BarPoint(
    val label: String,
    val value: Int,
    val secondaryLabel: String,
    val fraction: Float,
    val hymnNumber: String? = null,
) {
    companion object {
        /** A value of 1 next to a value of 400 must still be visible. */
        const val MIN_FRACTION = 0.02f
    }
}

/**
 * A ranking of hymns.
 *
 * @param bars capped for readability; [totalHymns] is the true count so the screen can offer the
 *   rest without deciding for itself what to hide.
 */
data class HymnRanking(
    val bars: List<BarPoint>,
    val totalHymns: Int,
) {
    val isCapped: Boolean get() = totalHymns > bars.size

    companion object {
        const val DEFAULT_LIMIT = 25
    }
}

/** Volume over the period, in the chronological order the service returned. */
data class EvolutionSeries(
    val bars: List<BarPoint>,
    val granularity: BucketGranularity,
)

/** One service's repertoire, read like that Sunday's bulletin. */
data class ServiceBulletin(
    val date: LocalDate,
    /** `null` marks use outside any service — rendered as such, never disguised as a service. */
    val serviceName: String?,
    val hymns: List<BulletinHymn>,
    val totalReach: Int,
)

data class BulletinHymn(
    val number: String,
    val title: String,
    val deviceCount: Int,
)

/** One month of the period as a grid of days shaded by volume. */
data class CalendarMonth(
    val month: YearMonth,
    val label: String,
    val days: List<CalendarDay>,
)

data class CalendarDay(
    val date: LocalDate,
    val occurrenceCount: Int,
    /** 0f when nothing happened, scaled against the busiest day of the same period. */
    val intensity: Float,
)

/** What the congregation sings together, beside what it opens alone during the week. */
data class InVsOutside(
    val inService: HymnRanking,
    val outsideService: HymnRanking,
    val onlyInService: List<String>,
    val onlyOutsideService: List<String>,
)

/** A short statement about the period, with its change against the preceding one. */
data class Highlight(
    val label: String,
    val value: String,
    /** `null` when the preceding period has no data — no comparison is invented. */
    val delta: HighlightDelta?,
)

data class HighlightDelta(
    val direction: Direction,
    val text: String,
) {
    enum class Direction { UP, DOWN, FLAT }
}

/**
 * What the church has and has not sung, across all recorded history.
 *
 * Never-sung comes from the catalogue minus the all-time ranking; the dates in [forgotten] come
 * from the occurrences the app can still see. Two sources, two different facts, never one number.
 */
data class HymnalCoverage(
    val catalogSize: Int,
    val everSungCount: Int,
    val proportionText: String,
    val neverSung: List<CatalogHymn>,
    val forgotten: List<ForgottenHymn>,
)

data class CatalogHymn(
    val number: String,
    val title: String,
)

/**
 * @param lastSung `null` when the most recent occurrence is older than the app can see. The app
 *   then says so instead of inventing a date it does not have.
 */
data class ForgottenHymn(
    val number: String,
    val title: String,
    val lastSung: LocalDate?,
    val text: String,
)

/** Everything the feature knows about one hymn, each fact labelled with the history it covers. */
data class HymnProfile(
    val number: String,
    val title: String,
    val allTimeCount: Int,
    val firstSeen: LocalDate?,
    val lastSeen: LocalDate?,
    val services: List<ServiceShare>,
    val typicalReach: Int,
    val recurrence: HymnRecurrence?,
    val neverRecorded: Boolean,
)

data class ServiceShare(
    val serviceName: String?,
    val occurrenceCount: Int,
)

/** "Cantado em 6 dos últimos 8 domingos à noite", plus one mark per instance. */
data class HymnRecurrence(
    val serviceName: String,
    val text: String,
    val marks: List<Boolean>,
)
