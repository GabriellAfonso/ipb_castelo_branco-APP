package com.ipb.castelobranco.features.gallery.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.ipb.castelobranco.core.di.SettingsPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GalleryPreferences @Inject constructor(
    @SettingsPrefs private val dataStore: DataStore<Preferences>,
) {
    private val autoDownloadTriggeredKey = booleanPreferencesKey("gallery_auto_download_triggered")

    val autoDownloadTriggeredFlow: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[autoDownloadTriggeredKey] ?: false }

    suspend fun markAutoDownloadTriggered() {
        dataStore.edit { prefs -> prefs[autoDownloadTriggeredKey] = true }
    }

    suspend fun resetAutoDownloadFlag() {
        dataStore.edit { prefs -> prefs[autoDownloadTriggeredKey] = false }
    }
}
