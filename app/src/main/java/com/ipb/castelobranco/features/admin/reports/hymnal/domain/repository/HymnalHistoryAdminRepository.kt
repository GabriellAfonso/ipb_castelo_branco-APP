package com.ipb.castelobranco.features.admin.reports.hymnal.domain.repository

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.CollectionSettings
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindow
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindowDraft

/**
 * Administration of the collection itself: the six parameters and the weekly services.
 *
 * Every call here is made as an administrator, including the settings read. The anonymous read
 * used by the invisible collection lives in `features/hymnal` and is a different caller.
 */
interface HymnalHistoryAdminRepository {

    suspend fun getSettings(): Result<CollectionSettings>

    /** Partial by construction: only the fields that differ from [current] are sent. */
    suspend fun updateSettings(
        current: CollectionSettings,
        updated: CollectionSettings,
    ): Result<CollectionSettings>

    suspend fun getServiceWindows(): Result<List<ServiceWindow>>

    /** Creates when [draft] has no id, updates otherwise. */
    suspend fun saveServiceWindow(draft: ServiceWindowDraft): Result<ServiceWindow>

    suspend fun deleteServiceWindow(id: Int): Result<Unit>
}
