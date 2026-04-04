package com.ipb.castelobranco.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.ipb.castelobranco.core.di.SetlistPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class SetlistPreferences @Inject constructor(
    @param:SetlistPrefs private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val DATE            = stringPreferencesKey("setlist_date")
        val CHORD_CHART_IDS = stringSetPreferencesKey("setlist_chord_chart_ids")
        val LYRICS_IDS      = stringSetPreferencesKey("setlist_lyrics_ids")
    }

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    val pinnedChordChartIds: Flow<Set<Int>> = dataStore.data.map { prefs ->
        if (prefs[Keys.DATE] != today()) emptySet()
        else prefs[Keys.CHORD_CHART_IDS].orEmpty().mapNotNull { it.toIntOrNull() }.toSet()
    }

    val pinnedLyricsIds: Flow<Set<Int>> = dataStore.data.map { prefs ->
        if (prefs[Keys.DATE] != today()) emptySet()
        else prefs[Keys.LYRICS_IDS].orEmpty().mapNotNull { it.toIntOrNull() }.toSet()
    }

    suspend fun toggleChordChart(id: Int) {
        val today = today()
        dataStore.edit { prefs ->
            if (prefs[Keys.DATE] != today) {
                prefs[Keys.DATE]            = today
                prefs[Keys.CHORD_CHART_IDS] = emptySet()
                prefs[Keys.LYRICS_IDS]      = emptySet()
            }
            val current = prefs[Keys.CHORD_CHART_IDS].orEmpty()
                .mapNotNull { it.toIntOrNull() }.toMutableSet()
            if (id in current) current.remove(id) else current.add(id)
            prefs[Keys.CHORD_CHART_IDS] = current.map { it.toString() }.toSet()
        }
    }

    suspend fun toggleLyrics(id: Int) {
        val today = today()
        dataStore.edit { prefs ->
            if (prefs[Keys.DATE] != today) {
                prefs[Keys.DATE]            = today
                prefs[Keys.CHORD_CHART_IDS] = emptySet()
                prefs[Keys.LYRICS_IDS]      = emptySet()
            }
            val current = prefs[Keys.LYRICS_IDS].orEmpty()
                .mapNotNull { it.toIntOrNull() }.toMutableSet()
            if (id in current) current.remove(id) else current.add(id)
            prefs[Keys.LYRICS_IDS] = current.map { it.toString() }.toSet()
        }
    }
}
