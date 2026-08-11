package com.ipb.castelobranco.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ipb.castelobranco.core.di.SettingsPrefs
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A stable, anonymous identifier for this installation.
 *
 * Random by construction — no hardware identifier, no advertising id, no `ANDROID_ID`, and no
 * permission. It survives app updates and is regenerated on reinstall, which is acceptable: the
 * only consequence is that the service's collapse window no longer recognises the device.
 */
fun interface DeviceIdProvider {
    suspend fun get(): String
}

@Singleton
class DataStoreDeviceIdProvider @Inject constructor(
    @param:SettingsPrefs private val dataStore: DataStore<Preferences>,
) : DeviceIdProvider {

    override suspend fun get(): String {
        val existing = dataStore.data.first()[DEVICE_ID_KEY]
        if (!existing.isNullOrBlank()) return existing

        // Generating inside edit {} makes the first read atomic: two concurrent callers cannot
        // mint two different ids, because the second sees the value written by the first.
        return dataStore.edit { prefs ->
            if (prefs[DEVICE_ID_KEY].isNullOrBlank()) {
                prefs[DEVICE_ID_KEY] = UUID.randomUUID().toString()
            }
        }[DEVICE_ID_KEY].orEmpty()
    }

    private companion object {
        val DEVICE_ID_KEY = stringPreferencesKey("device_id")
    }
}
