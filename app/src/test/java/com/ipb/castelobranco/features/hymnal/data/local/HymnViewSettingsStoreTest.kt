package com.ipb.castelobranco.features.hymnal.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.ipb.castelobranco.features.hymnal.domain.model.HymnViewCollectionSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class HymnViewSettingsStoreTest {

    private val testScope = TestScope(UnconfinedTestDispatcher() + Job())

    private lateinit var tempFile: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var store: HymnViewSettingsStore

    @Before
    fun setUp() {
        tempFile = File.createTempFile("hymn_settings_test", ".preferences_pb")
            .also { it.deleteOnExit() }
        dataStore = PreferenceDataStoreFactory.create(scope = testScope, produceFile = { tempFile })
        store = HymnViewSettingsStore(dataStore)
    }

    @After
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `unwritten store returns the built-in defaults`() = testScope.runTest {
        val settings = store.read()

        assertEquals(30, settings.minSecondsToCount)
        assertEquals(50, settings.maxBatchSize)
    }

    @Test
    fun `written values are read back`() = testScope.runTest {
        store.write(HymnViewCollectionSettings(minSecondsToCount = 45, maxBatchSize = 200))

        val settings = store.read()

        assertEquals(45, settings.minSecondsToCount)
        assertEquals(200, settings.maxBatchSize)
    }

    @Test
    fun `values survive a new store over the same data`() = testScope.runTest {
        store.write(HymnViewCollectionSettings(minSecondsToCount = 45, maxBatchSize = 200))

        val restarted = HymnViewSettingsStore(dataStore)

        assertEquals(45, restarted.read().minSecondsToCount)
    }

    @Test
    fun `a non-positive threshold falls back to the default`() = testScope.runTest {
        store.write(HymnViewCollectionSettings(minSecondsToCount = 0, maxBatchSize = 50))

        assertEquals(30, store.read().minSecondsToCount)
    }

    @Test
    fun `a negative threshold falls back to the default`() = testScope.runTest {
        store.write(HymnViewCollectionSettings(minSecondsToCount = -10, maxBatchSize = 50))

        assertEquals(30, store.read().minSecondsToCount)
    }

    @Test
    fun `an absurd batch size is clamped`() = testScope.runTest {
        store.write(HymnViewCollectionSettings(minSecondsToCount = 30, maxBatchSize = 100_000))

        assertEquals(500, store.read().maxBatchSize)
    }

    @Test
    fun `a zero batch size is clamped up to one`() = testScope.runTest {
        store.write(HymnViewCollectionSettings(minSecondsToCount = 30, maxBatchSize = 0))

        assertEquals(1, store.read().maxBatchSize)
    }
}
