package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindow
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.repository.HymnalHistoryAdminRepository
import javax.inject.Inject

/**
 * The services, for the slice selector and for the management screen.
 *
 * The report needs this even though the management screen is a separate surface, which is why it
 * lives with the use cases and not inside that screen.
 */
class GetServiceWindowsUseCase @Inject constructor(
    private val repository: HymnalHistoryAdminRepository,
) {
    suspend operator fun invoke(): Result<List<ServiceWindow>> = repository.getServiceWindows()
}
