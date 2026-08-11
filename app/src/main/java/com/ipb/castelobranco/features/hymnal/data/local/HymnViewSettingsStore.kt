package com.ipb.castelobranco.features.hymnal.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.ipb.castelobranco.core.di.SettingsPrefs
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewCollectionSettings
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caches the two collection settings the app consumes, so the feature keeps working offline and
 * on a fresh install. Absent keys mean "never fetched" and fall through to the defaults.
 */
@Singleton
class HymnViewSettingsStore @Inject constructor(
    @param:SettingsPrefs private val dataStore: DataStore<Preferences>,
) {

    suspend fun read(): HymnViewCollectionSettings {
        val prefs = dataStore.data.first()
        val minSeconds = prefs[MIN_SECONDS_KEY]
        val maxBatch = prefs[MAX_BATCH_KEY]

        if (minSeconds == null || maxBatch == null) return HymnViewCollectionSettings.DEFAULT

        return HymnViewCollectionSettings.sanitized(minSeconds, maxBatch)
    }

    suspend fun write(settings: HymnViewCollectionSettings) {
        val sanitized = HymnViewCollectionSettings.sanitized(
            minSecondsToCount = settings.minSecondsToCount,
            maxBatchSize = settings.maxBatchSize,
        )
        dataStore.edit { prefs ->
            prefs[MIN_SECONDS_KEY] = sanitized.minSecondsToCount
            prefs[MAX_BATCH_KEY] = sanitized.maxBatchSize
        }
    }

    private companion object {
        val MIN_SECONDS_KEY = intPreferencesKey("hymn_history_min_seconds")
        val MAX_BATCH_KEY = intPreferencesKey("hymn_history_max_batch")
    }
}
