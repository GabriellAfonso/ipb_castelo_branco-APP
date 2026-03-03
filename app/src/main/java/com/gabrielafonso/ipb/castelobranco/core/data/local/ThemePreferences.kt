package com.gabrielafonso.ipb.castelobranco.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.gabrielafonso.ipb.castelobranco.core.di.SettingsPrefs
import com.gabrielafonso.ipb.castelobranco.features.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemePreferences @Inject constructor(
    @param: SettingsPrefs private val dataStore: DataStore<Preferences>
) {

    companion object {
        private const val MODE_FOLLOW_SYSTEM: Int = 0
        private const val MODE_LIGHT: Int = 1
        private const val MODE_DARK: Int = 2
    }

    private val themeModeKey = intPreferencesKey("theme_mode")

    val themeModeFlow: Flow<ThemeMode> =
        dataStore.data.map { prefs ->
            when (prefs[themeModeKey] ?: MODE_FOLLOW_SYSTEM) {
                MODE_LIGHT -> ThemeMode.LIGHT
                MODE_DARK -> ThemeMode.DARK
                else -> ThemeMode.FOLLOW_SYSTEM
            }
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        val persisted = when (mode) {
            ThemeMode.FOLLOW_SYSTEM -> MODE_FOLLOW_SYSTEM
            ThemeMode.LIGHT -> MODE_LIGHT
            ThemeMode.DARK -> MODE_DARK
        }
        dataStore.edit { prefs ->
            prefs[themeModeKey] = persisted
        }
    }

    suspend fun setFollowSystem() = setThemeMode(ThemeMode.FOLLOW_SYSTEM)
    suspend fun setLightMode() = setThemeMode(ThemeMode.LIGHT)
    suspend fun setDarkMode() = setThemeMode(ThemeMode.DARK)
}