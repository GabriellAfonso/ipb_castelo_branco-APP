package com.ipb.castelobranco.features.admin.reports.hymnal.data.repository

import com.ipb.castelobranco.core.domain.error.AppError
import com.ipb.castelobranco.core.domain.error.mapError
import com.ipb.castelobranco.core.network.error.toAppError
import com.ipb.castelobranco.features.admin.reports.hymnal.data.api.HymnalHistoryAdminApi
import com.ipb.castelobranco.features.admin.reports.hymnal.data.mapper.diff
import com.ipb.castelobranco.features.admin.reports.hymnal.data.mapper.toDomain
import com.ipb.castelobranco.features.admin.reports.hymnal.data.mapper.toWriteDto
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.CollectionSettings
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindow
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindowDraft
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.repository.HymnalHistoryAdminRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HymnalHistoryAdminRepositoryImpl @Inject constructor(
    private val api: HymnalHistoryAdminApi,
) : HymnalHistoryAdminRepository {

    override suspend fun getSettings(): Result<CollectionSettings> = runCatching {
        val response = api.getSettings()
        if (!response.isSuccessful) throw response.toAppError()
        val body = response.body() ?: throw AppError.Unknown(message = EMPTY_BODY)
        body.toDomain()
    }.mapError()

    /**
     * Nothing changed means nothing is sent: an empty patch would be a request whose only effect
     * is to make the screen wait.
     */
    override suspend fun updateSettings(
        current: CollectionSettings,
        updated: CollectionSettings,
    ): Result<CollectionSettings> = runCatching {
        val patch = current.diff(updated)
        if (patch.isEmpty) return@runCatching current

        val response = api.patchSettings(patch)
        if (!response.isSuccessful) throw response.toAppError()
        val body = response.body() ?: throw AppError.Unknown(message = EMPTY_BODY)
        body.toDomain()
    }.mapError()

    override suspend fun getServiceWindows(): Result<List<ServiceWindow>> = runCatching {
        val response = api.getServiceWindows()
        if (!response.isSuccessful) throw response.toAppError()
        val body = response.body() ?: throw AppError.Unknown(message = EMPTY_BODY)
        body.serviceWindows.map { it.toDomain() }
    }.mapError()

    override suspend fun saveServiceWindow(draft: ServiceWindowDraft): Result<ServiceWindow> =
        runCatching {
            val payload = draft.toWriteDto()
            val id = draft.id
            val response =
                if (id == null) api.createServiceWindow(payload)
                else api.updateServiceWindow(id = id, body = payload)

            if (!response.isSuccessful) throw response.toAppError()
            val body = response.body() ?: throw AppError.Unknown(message = EMPTY_BODY)
            body.toDomain()
        }.mapError()

    override suspend fun deleteServiceWindow(id: Int): Result<Unit> = runCatching {
        val response = api.deleteServiceWindow(id)
        if (!response.isSuccessful) throw response.toAppError()
    }.mapError()

    private companion object {
        const val EMPTY_BODY = "Resposta vazia do servidor"
    }
}
