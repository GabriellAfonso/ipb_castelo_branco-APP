package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.CollectionSettings
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.repository.HymnalHistoryAdminRepository
import javax.inject.Inject

class GetCollectionSettingsUseCase @Inject constructor(
    private val repository: HymnalHistoryAdminRepository,
) {
    suspend operator fun invoke(): Result<CollectionSettings> = repository.getSettings()
}

/**
 * Sends only what changed. The comparison against [current] is what makes the request partial,
 * so a screen that forgets to pass the loaded values would overwrite five settings nobody
 * touched.
 */
class UpdateCollectionSettingsUseCase @Inject constructor(
    private val repository: HymnalHistoryAdminRepository,
) {
    suspend operator fun invoke(
        current: CollectionSettings,
        updated: CollectionSettings,
    ): Result<CollectionSettings> = repository.updateSettings(current, updated)
}
