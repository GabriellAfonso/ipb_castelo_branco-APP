package com.ipb.castelobranco.core.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SetlistPreferencesTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())

    private lateinit var tempFile: File
    private lateinit var dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
    private lateinit var preferences: SetlistPreferences

    private val keyDate = stringPreferencesKey("setlist_date")
    private val keyChordIds = stringPreferencesKey("setlist_chord_chart_ids_v2")
    private val keyLyricsIds = stringPreferencesKey("setlist_lyrics_ids_v2")

    @Before
    fun setUp() {
        tempFile = File.createTempFile("setlist_prefs_test", ".preferences_pb")
            .also { it.deleteOnExit() }

        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tempFile }
        )
        preferences = SetlistPreferences(dataStore)
    }

    @After
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `pinnedChordChartIds returns empty list when no data stored`() = testScope.runTest {
        val result = preferences.pinnedChordChartIds.first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `toggleChordChart adds id when not present`() = testScope.runTest {
        preferences.toggleChordChart(42)

        val result = preferences.pinnedChordChartIds.first()

        assertEquals(listOf(42), result)
    }

    @Test
    fun `toggleChordChart removes id when already present`() = testScope.runTest {
        preferences.toggleChordChart(42)
        preferences.toggleChordChart(42)

        val result = preferences.pinnedChordChartIds.first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `toggleChordChart preserves pin insertion order`() = testScope.runTest {
        preferences.toggleChordChart(3)
        preferences.toggleChordChart(1)
        preferences.toggleChordChart(2)

        val result = preferences.pinnedChordChartIds.first()

        assertEquals(listOf(3, 1, 2), result)
    }

    @Test
    fun `toggleChordChart re-pin appends id at the end`() = testScope.runTest {
        preferences.toggleChordChart(1)
        preferences.toggleChordChart(2)
        preferences.toggleChordChart(3)
        preferences.toggleChordChart(2) // unpin
        preferences.toggleChordChart(2) // re-pin

        val result = preferences.pinnedChordChartIds.first()

        assertEquals(listOf(1, 3, 2), result)
    }

    @Test
    fun `toggleLyrics adds and removes id correctly`() = testScope.runTest {
        preferences.toggleLyrics(10)
        assertEquals(listOf(10), preferences.pinnedLyricsIds.first())

        preferences.toggleLyrics(10)
        assertTrue(preferences.pinnedLyricsIds.first().isEmpty())
    }

    @Test
    fun `toggleLyrics preserves pin insertion order`() = testScope.runTest {
        preferences.toggleLyrics(7)
        preferences.toggleLyrics(4)
        preferences.toggleLyrics(9)

        val result = preferences.pinnedLyricsIds.first()

        assertEquals(listOf(7, 4, 9), result)
    }

    @Test
    fun `pinnedChordChartIds returns empty list when stored date differs from today`() = testScope.runTest {
        dataStore.edit { prefs ->
            prefs[keyDate] = "2000-01-01"
            prefs[keyChordIds] = "1,2,3"
        }

        val result = preferences.pinnedChordChartIds.first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `toggleChordChart on stale date resets all data and adds the new id`() = testScope.runTest {
        dataStore.edit { prefs ->
            prefs[keyDate] = "2000-01-01"
            prefs[keyChordIds] = "1,2"
            prefs[keyLyricsIds] = "5,6"
        }

        preferences.toggleChordChart(99)

        assertEquals(listOf(99), preferences.pinnedChordChartIds.first())
        assertTrue(preferences.pinnedLyricsIds.first().isEmpty())
    }
}
