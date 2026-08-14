package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnOccurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnProfile
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnRecurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceShare
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.TopHymn
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Everything the feature can say about one hymn, and nothing it cannot.
 *
 * The total comes from the all-time ranking and is labelled as covering all history. Dates,
 * services, reach and recurrence come from the occurrences the app can still see — one year at
 * most — and are labelled as such. Mixing the two would produce a card whose numbers do not add
 * up against each other.
 */
class BuildHymnProfileUseCase @Inject constructor() {

    operator fun invoke(
        hymnNumber: String,
        allTime: List<TopHymn>,
        visibleWindow: List<HymnOccurrence>,
        fallbackTitle: String? = null,
    ): HymnProfile {
        val allTimeEntry = allTime.firstOrNull { it.number == hymnNumber }
        val mine = visibleWindow.filter { it.hymnNumber == hymnNumber }

        val title = allTimeEntry?.title
            ?: mine.firstOrNull()?.hymnTitle
            ?: fallbackTitle
            ?: ""

        return HymnProfile(
            number = hymnNumber,
            title = title,
            allTimeCount = allTimeEntry?.occurrenceCount ?: 0,
            firstSeen = mine.minOfOrNull { it.occurredOn },
            lastSeen = mine.maxOfOrNull { it.occurredOn },
            services = mine.groupBy { it.serviceWindowName }
                .map { (name, its) -> ServiceShare(serviceName = name, occurrenceCount = its.size) }
                .sortedByDescending { it.occurrenceCount },
            typicalReach = if (mine.isEmpty()) 0
            else (mine.sumOf { it.deviceCount }.toFloat() / mine.size).roundToInt(),
            recurrence = recurrence(mine, visibleWindow),
            neverRecorded = allTimeEntry == null && mine.isEmpty(),
        )
    }

    /**
     * How regularly the hymn shows up in the service it belongs to most.
     *
     * "Registros" and not "cultos": the app knows which services produced records, never whether
     * a service happened at all.
     */
    private fun recurrence(
        mine: List<HymnOccurrence>,
        visibleWindow: List<HymnOccurrence>,
    ): HymnRecurrence? {
        val dominant = mine
            .filter { it.serviceWindowId != null }
            .groupBy { it.serviceWindowId }
            .maxByOrNull { (_, its) -> its.size }
            ?: return null

        val serviceId = dominant.key
        val serviceName = dominant.value.first().serviceWindowName ?: return null

        val instances = visibleWindow
            .filter { it.serviceWindowId == serviceId }
            .map { it.occurredOn }
            .distinct()
            .sorted()
            .takeLast(MAX_INSTANCES)

        if (instances.isEmpty()) return null

        val sungOn = dominant.value.mapTo(mutableSetOf()) { it.occurredOn }
        val marks = instances.map { it in sungOn }
        val hits = marks.count { it }

        return HymnRecurrence(
            serviceName = serviceName,
            text = "Cantado em $hits dos últimos ${instances.size} registros de \"$serviceName\"",
            marks = marks,
        )
    }

    private companion object {
        const val MAX_INSTANCES = 8
    }
}
