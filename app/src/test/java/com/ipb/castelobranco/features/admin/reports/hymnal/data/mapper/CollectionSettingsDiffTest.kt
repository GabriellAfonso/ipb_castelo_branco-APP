package com.ipb.castelobranco.features.admin.reports.hymnal.data.mapper

import com.ipb.castelobranco.features.admin.reports.hymnal.defaultSettings
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * "Send only what changed" has to be true on the wire, not merely in intent — a full overwrite
 * would quietly reset five settings the administrator never touched.
 */
class CollectionSettingsDiffTest {

    /** Same configuration as the app's shared `Json`: nulls are omitted, not encoded. */
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun `a single change produces a patch with only that field`() {
        val current = defaultSettings()
        val updated = current.copy(minSecondsToCount = 45)

        val patch = current.diff(updated)

        assertEquals(45, patch.minSecondsToCount)
        assertEquals(null, patch.collapseWindowMinutes)
        assertEquals(null, patch.maxBatchSize)
        assertEquals(null, patch.maxPastDays)
        assertEquals(null, patch.futureToleranceMinutes)
        assertEquals(null, patch.windowGraceMinutes)
    }

    @Test
    fun `the serialized body carries exactly the changed key`() {
        val current = defaultSettings()
        val updated = current.copy(minSecondsToCount = 45)

        val body = json.encodeToString(current.diff(updated))

        assertEquals("""{"min_seconds_to_count":45}""", body)
    }

    @Test
    fun `an unchanged settings object produces an empty patch and an empty body`() {
        val current = defaultSettings()

        val patch = current.diff(current)

        assertTrue(patch.isEmpty)
        assertEquals("{}", json.encodeToString(patch))
    }

    @Test
    fun `several changes are all carried`() {
        val current = defaultSettings()
        val updated = current.copy(windowGraceMinutes = 45, maxBatchSize = 500)

        val patch = current.diff(updated)

        assertFalse(patch.isEmpty)
        assertEquals(45, patch.windowGraceMinutes)
        assertEquals(500, patch.maxBatchSize)
        assertEquals(null, patch.minSecondsToCount)
    }
}
