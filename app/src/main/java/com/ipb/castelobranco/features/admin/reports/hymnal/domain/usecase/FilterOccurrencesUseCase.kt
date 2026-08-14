package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnOccurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ReportSlice
import javax.inject.Inject

/**
 * The single implementation of a slice. Every reading goes through it, so "apenas os domingos à
 * noite" means the same thing in the ranking, the bulletins and the calendar.
 *
 * Nothing here touches the network: the slice is applied to occurrences already loaded.
 */
class FilterOccurrencesUseCase @Inject constructor() {

    operator fun invoke(
        occurrences: List<HymnOccurrence>,
        slice: ReportSlice,
    ): List<HymnOccurrence> = when (slice) {
        ReportSlice.All -> occurrences

        is ReportSlice.Service -> occurrences.filter { it.serviceWindowId == slice.id }

        // Not "no filter": these are the views that fell outside every active service, which is
        // the reading the church never had.
        ReportSlice.OutsideService -> occurrences.filter { it.serviceWindowId == null }

        is ReportSlice.Weekday -> occurrences.filter { it.occurredOn.dayOfWeek == slice.day }
    }
}
