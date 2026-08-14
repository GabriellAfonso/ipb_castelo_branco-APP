package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnOccurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ReportEmptyReason
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ReportSlice
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindow
import javax.inject.Inject

/**
 * Why a reading is empty, in the administrator's terms.
 *
 * The difference between "there is no history at all", "this period has none", "this slice has
 * none" and "this service was not in force" is the difference between waiting, changing the
 * period, changing the slice, and fixing a service window. A blank chart says none of that.
 *
 * @param collectionIsEmpty `true` only when the all-time ranking is known to be empty; `null`
 *   while it has not been loaded, so the report never claims "nunca houve coleta" on a guess.
 */
class ResolveEmptyReasonUseCase @Inject constructor() {

    operator fun invoke(
        periodOccurrences: List<HymnOccurrence>,
        slicedOccurrences: List<HymnOccurrence>,
        slice: ReportSlice,
        serviceWindows: List<ServiceWindow>,
        collectionIsEmpty: Boolean? = null,
    ): ReportEmptyReason? {
        if (slicedOccurrences.isNotEmpty()) return null

        if (periodOccurrences.isEmpty()) {
            return if (collectionIsEmpty == true) ReportEmptyReason.NoCollectionAtAll
            else ReportEmptyReason.NoRecordsInPeriod
        }

        if (slice !is ReportSlice.Service) return ReportEmptyReason.NoRecordsInSlice

        val window = serviceWindows.firstOrNull { it.id == slice.id }

        // A window that is gone or switched off did not fail to be sung at — it was not in force.
        // Saying "ninguém abriu o hinário" there would blame the congregation for a setting.
        return if (window == null || !window.active) {
            ReportEmptyReason.ServiceInactiveOrAbsentInPeriod(window?.name ?: slice.name)
        } else {
            ReportEmptyReason.ServiceWithoutRecords(window.name)
        }
    }
}
