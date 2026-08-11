package com.ipb.castelobranco.features.hymnal.domain.usecase

import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewCollectionSettings
import com.ipb.castelobranco.features.hymnal.domain.repository.HymnViewHistoryRepository
import javax.inject.Inject

/**
 * Current collection settings: cached values when they exist, built-in defaults otherwise.
 * Never touches the network — the startup refresh does that.
 */
class GetHymnViewSettingsUseCase @Inject constructor(
    private val repository: HymnViewHistoryRepository,
) {
    suspend operator fun invoke(): HymnViewCollectionSettings = repository.currentSettings()
}
