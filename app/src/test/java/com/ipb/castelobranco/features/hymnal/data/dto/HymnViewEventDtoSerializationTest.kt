package com.ipb.castelobranco.features.hymnal.data.dto

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime

/**
 * The collection service forbids unknown fields: any extra key makes the event fail parsing and
 * come back as `invalid_event`, silently losing it. Since the shared `Json` sets
 * `encodeDefaults = true`, every declared property does reach the wire — so the declaration is
 * the contract, and this test is what holds it.
 */
class HymnViewEventDtoSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        explicitNulls = false
    }

    private val contractFields = setOf(
        "client_event_id",
        "hymn_id",
        "device_id",
        "viewed_at",
        "duration_seconds",
        "app_version",
        "platform",
    )

    private val dto = HymnViewEventDto(
        clientEventId = "0b7f2c1e-6a3d-4f89-9b21-4c0f5e6d7a88",
        hymnId = 42,
        deviceId = "8f1c9e40-2b77-4d3a-9a5e-6f0b1c2d3e4f",
        viewedAt = "2026-08-09T19:34:12-03:00",
        durationSeconds = 47,
        appVersion = "0.9.6",
        platform = "android",
    )

    @Test
    fun `serializes exactly the seven contract fields`() {
        val keys = json.encodeToJsonElement(dto).jsonObject.keys

        assertEquals(contractFields, keys)
    }

    @Test
    fun `blank optional fields are still emitted rather than omitted`() {
        val keys = json.encodeToJsonElement(dto.copy(appVersion = "", platform = ""))
            .jsonObject.keys

        assertEquals(contractFields, keys)
    }

    @Test
    fun `request body wraps events under the events key only`() {
        val element = json.encodeToJsonElement(IngestRequestDto(events = listOf(dto)))

        assertEquals(setOf("events"), element.jsonObject.keys)
    }

    @Test
    fun `viewed at round-trips through OffsetDateTime without losing the offset`() {
        val original = OffsetDateTime.parse("2026-08-09T19:34:12-03:00")

        val reparsed = OffsetDateTime.parse(original.toString())

        assertEquals(original.toInstant(), reparsed.toInstant())
        assertEquals(original.offset, reparsed.offset)
    }

    @Test
    fun `ingest response parses when the service adds a new field`() {
        val raw = """
            {"accepted":["a","b"],"rejected":[{"client_event_id":"c","reason":"unknown_hymn"}],
             "server_note":"something new"}
        """.trimIndent()

        val response = json.decodeFromString<IngestResponseDto>(raw)

        assertEquals(listOf("a", "b"), response.accepted)
        assertEquals("unknown_hymn", response.rejected.single().reason)
    }

    @Test
    fun `ingest response parses when a list is missing entirely`() {
        val response = json.decodeFromString<IngestResponseDto>("""{"accepted":["a"]}""")

        assertEquals(listOf("a"), response.accepted)
        assertTrue(response.rejected.isEmpty())
    }

    @Test
    fun `settings payload parses with fields missing`() {
        val settings = json.decodeFromString<HistorySettingsDto>("""{"min_seconds_to_count":45}""")

        assertEquals(45, settings.minSecondsToCount)
        assertEquals(50, settings.maxBatchSize)
    }
}
