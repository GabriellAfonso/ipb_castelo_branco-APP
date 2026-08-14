package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.state

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindow
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindowDraft

/**
 * @param fieldErrors keyed by the field name the API uses, so a local check and a server
 *   `field_errors` key land on the same field of the same form.
 */
data class ServiceWindowsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val windows: List<ServiceWindow> = emptyList(),
    val editing: ServiceWindowDraft? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val pendingDelete: ServiceWindow? = null,
    val isSaving: Boolean = false,
)

sealed interface ServiceWindowsEvent {
    data class ShowMessage(val message: String) : ServiceWindowsEvent

    /** Something changed, so the report's slice selector must be refreshed. */
    data object WindowsChanged : ServiceWindowsEvent
}
