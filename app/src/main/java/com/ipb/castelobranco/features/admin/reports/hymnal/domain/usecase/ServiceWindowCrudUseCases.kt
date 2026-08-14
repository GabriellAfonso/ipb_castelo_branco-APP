package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindow
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindowDraft
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.repository.HymnalHistoryAdminRepository
import javax.inject.Inject

/** Creates when the draft has no id, updates otherwise — one action for one form. */
class SaveServiceWindowUseCase @Inject constructor(
    private val repository: HymnalHistoryAdminRepository,
) {
    suspend operator fun invoke(draft: ServiceWindowDraft): Result<ServiceWindow> =
        repository.saveServiceWindow(draft)
}

/**
 * Deleting a service **never deletes history**. Occurrences are derived at read time, so the
 * events grouped under it simply regroup by calendar day on the next reading.
 */
class DeleteServiceWindowUseCase @Inject constructor(
    private val repository: HymnalHistoryAdminRepository,
) {
    suspend operator fun invoke(id: Int): Result<Unit> = repository.deleteServiceWindow(id)
}
