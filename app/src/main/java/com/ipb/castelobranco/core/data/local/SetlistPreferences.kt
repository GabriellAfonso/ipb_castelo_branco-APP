package com.ipb.castelobranco.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
        val CHORD_CHART_IDS = stringPreferencesKey("setlist_chord_chart_ids_v2")
        val LYRICS_IDS      = stringPreferencesKey("setlist_lyrics_ids_v2")
    }

    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    val pinnedChordChartIds: Flow<List<Int>> = dataStore.data.map { prefs ->
        if (prefs[Keys.DATE] != today()) emptyList()
        else prefs[Keys.CHORD_CHART_IDS].decodeIds()
    }

    val pinnedLyricsIds: Flow<List<Int>> = dataStore.data.map { prefs ->
        if (prefs[Keys.DATE] != today()) emptyList()
        else prefs[Keys.LYRICS_IDS].decodeIds()
    }

    suspend fun toggleChordChart(id: Int) {
        dataStore.edit { prefs ->
            ensureToday(prefs)
            val updated = prefs[Keys.CHORD_CHART_IDS].decodeIds().toggle(id)
            prefs[Keys.CHORD_CHART_IDS] = updated.encodeIds()
        }
    }

    suspend fun toggleLyrics(id: Int) {
        dataStore.edit { prefs ->
            ensureToday(prefs)
            val updated = prefs[Keys.LYRICS_IDS].decodeIds().toggle(id)
            prefs[Keys.LYRICS_IDS] = updated.encodeIds()
        }
    }

    private fun ensureToday(prefs: androidx.datastore.preferences.core.MutablePreferences) {
        val today = today()
        if (prefs[Keys.DATE] != today) {
            prefs[Keys.DATE]            = today
            prefs[Keys.CHORD_CHART_IDS] = ""
            prefs[Keys.LYRICS_IDS]      = ""
        }
    }

    private fun String?.decodeIds(): List<Int> =
        if (isNullOrBlank()) emptyList()
        else split(",").mapNotNull { it.toIntOrNull() }

    private fun List<Int>.encodeIds(): String = joinToString(",")

    private fun List<Int>.toggle(id: Int): List<Int> =
        if (id in this) this - id else this + id
}
