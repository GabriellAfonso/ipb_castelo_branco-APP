package com.gabrielafonso.ipb.castelobranco.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.gabrielafonso.ipb.castelobranco.core.di.SettingsPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemePreferences @Inject constructor(
    @param: SettingsPrefs private val dataStore: DataStore<Preferences>
) {

    companion object {
        const val MODE_FOLLOW_SYSTEM: Int = 0
        const val MODE_LIGHT: Int = 1
        const val MODE_DARK: Int = 2
    }

    private val themeModeKey = intPreferencesKey("theme_mode")

    val themeModeFlow: Flow<Int> =
        dataStore.data.map { prefs ->
            prefs[themeModeKey] ?: MODE_FOLLOW_SYSTEM
        }

    suspend fun setThemeMode(mode: Int) {
        require(mode in MODE_FOLLOW_SYSTEM..MODE_DARK) {
            "Invalid theme mode: $mode"
        }
        dataStore.edit { prefs ->
            prefs[themeModeKey] = mode
        }
    }

    suspend fun setFollowSystem() = setThemeMode(MODE_FOLLOW_SYSTEM)
    suspend fun setLightMode() = setThemeMode(MODE_LIGHT)
    suspend fun setDarkMode() = setThemeMode(MODE_DARK)
}