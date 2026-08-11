package com.ipb.castelobranco.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceIdProviderTest {

    private val testScope = TestScope(UnconfinedTestDispatcher() + Job())

    private lateinit var tempFile: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var provider: DataStoreDeviceIdProvider

    @Before
    fun setUp() {
        tempFile = File.createTempFile("device_id_test", ".preferences_pb")
            .also { it.deleteOnExit() }
        dataStore = PreferenceDataStoreFactory.create(scope = testScope, produceFile = { tempFile })
        provider = DataStoreDeviceIdProvider(dataStore)
    }

    @After
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `generates a value on first read`() = testScope.runTest {
        val id = provider.get()

        assertTrue(id.isNotBlank())
    }

    @Test
    fun `returns the same value on every call`() = testScope.runTest {
        val first = provider.get()
        val second = provider.get()

        assertEquals(first, second)
    }

    @Test
    fun `the value is a random uuid, not a device identifier`() = testScope.runTest {
        val id = provider.get()

        assertNotNull(UUID.fromString(id))
    }

    @Test
    fun `the value fits the 64 character contract limit`() = testScope.runTest {
        val id = provider.get()

        assertTrue("device_id was ${id.length} chars", id.length <= 64)
    }

    @Test
    fun `the value survives a new provider over the same store`() = testScope.runTest {
        val first = provider.get()

        val restarted = DataStoreDeviceIdProvider(dataStore)

        assertEquals(first, restarted.get())
    }

    @Test
    fun `concurrent first reads agree on one value`() = testScope.runTest {
        val ids = (0 until 20).map { async { provider.get() } }.awaitAll()

        assertEquals(1, ids.toSet().size)
    }

    @Test
    fun `two independent installs get different values`() = testScope.runTest {
        val otherFile = File.createTempFile("device_id_test_other", ".preferences_pb")
            .also { it.deleteOnExit() }
        val otherStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { otherFile },
        )

        val first = provider.get()
        val second = DataStoreDeviceIdProvider(otherStore).get()

        assertTrue(first != second)
        otherFile.delete()
    }
}
