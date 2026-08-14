package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.state

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.SettingField

/**
 * @param values raw text per field, not parsed numbers, so a typo survives long enough for the
 *   administrator to see the error next to what they actually typed.
 */
data class CollectionSettingsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val values: Map<SettingField, String> = emptyMap(),
    val fieldErrors: Map<SettingField, String> = emptyMap(),
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
)

sealed interface CollectionSettingsEvent {
    data class ShowMessage(val message: String) : CollectionSettingsEvent
}
