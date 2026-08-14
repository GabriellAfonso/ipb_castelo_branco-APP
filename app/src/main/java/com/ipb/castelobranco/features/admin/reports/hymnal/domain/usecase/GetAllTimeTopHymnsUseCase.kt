package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.TopHymn
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.repository.HymnalReportRepository
import javax.inject.Inject

/**
 * The ranking across all recorded history — the only source in the app for an all-time statement,
 * because the occurrences endpoint reaches back one year at most.
 *
 * It cannot slice by service or weekday, so no sliced reading is ever built from it.
 */
class GetAllTimeTopHymnsUseCase @Inject constructor(
    private val repository: HymnalReportRepository,
) {
    suspend operator fun invoke(): Result<List<TopHymn>> = repository.getAllTimeTopHymns()
}
