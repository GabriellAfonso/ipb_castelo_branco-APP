package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BulletinHymn
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnOccurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceBulletin
import javax.inject.Inject

/**
 * The repertoire of each service, most recent first, read like that Sunday's bulletin.
 *
 * Occurrences with no service are kept and grouped by calendar day, marked as what they are —
 * weekday use outside any service. Dropping them would hide the very signal the collection
 * added; disguising them as a service would invent one.
 */
class BuildServiceBulletinsUseCase @Inject constructor() {

    operator fun invoke(occurrences: List<HymnOccurrence>): List<ServiceBulletin> =
        occurrences
            .groupBy { it.occurredOn to it.serviceWindowId }
            .map { (key, its) ->
                val (date, _) = key
                ServiceBulletin(
                    date = date,
                    serviceName = its.first().serviceWindowName,
                    hymns = its.map { hymn ->
                        BulletinHymn(
                            number = hymn.hymnNumber,
                            title = hymn.hymnTitle,
                            deviceCount = hymn.deviceCount,
                        )
                    },
                    totalReach = its.sumOf { it.deviceCount },
                )
            }
            // Newest first, and within one day the services before the loose weekday use.
            .sortedWith(
                compareByDescending<ServiceBulletin> { it.date }
                    .thenBy { it.serviceName == null }
                    .thenBy { it.serviceName.orEmpty() }
            )
}
