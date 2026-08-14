package com.ipb.castelobranco.features.admin.reports.hymnal.data.repository

import com.ipb.castelobranco.core.domain.error.AppError
import com.ipb.castelobranco.core.domain.error.mapError
import com.ipb.castelobranco.core.network.error.toAppError
import com.ipb.castelobranco.features.admin.reports.hymnal.data.api.HymnalReportApi
import com.ipb.castelobranco.features.admin.reports.hymnal.data.mapper.toDomain
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BucketGranularity
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.DateRange
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.OccurrenceReport
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.TopHymn
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.repository.HymnalReportRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HymnalReportRepositoryImpl @Inject constructor(
    private val api: HymnalReportApi,
) : HymnalReportRepository {

    override suspend fun getOccurrences(
        range: DateRange,
        granularity: BucketGranularity,
    ): Result<OccurrenceReport> = runCatching {
        val response = api.getOccurrences(
            from = range.from.toString(),
            to = range.to.toString(),
            groupBy = granularity.wireValue,
        )
        if (!response.isSuccessful) throw response.toAppError()
        val body = response.body() ?: throw AppError.Unknown(message = EMPTY_BODY)
        body.toDomain(range = range, granularity = granularity)
    }.mapError()

    override suspend fun getAllTimeTopHymns(): Result<List<TopHymn>> = runCatching {
        val response = api.getAllTimeTopHymns()
        if (!response.isSuccessful) throw response.toAppError()
        val body = response.body() ?: throw AppError.Unknown(message = EMPTY_BODY)
        body.hymns.map { it.toDomain() }
    }.mapError()

    private companion object {
        const val EMPTY_BODY = "Resposta vazia do servidor"
    }
}
