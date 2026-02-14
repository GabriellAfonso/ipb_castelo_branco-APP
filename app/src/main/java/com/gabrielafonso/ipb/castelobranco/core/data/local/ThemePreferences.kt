package com.gabrielafonso.ipb.castelobranco.core.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class ThemePreferences @Inject constructor(
    @param: ApplicationContext private val context: Context
) {
    private val darkModeKey = booleanPreferencesKey("dark_mode")

    val darkModeFlow: Flow<Boolean?> = context.settingsDataStore.data
        .map { prefs -> if (prefs.contains(darkModeKey)) prefs[darkModeKey] else null }

    suspend fun setDarkMode(value: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[darkModeKey] = value
        }
    }
}
