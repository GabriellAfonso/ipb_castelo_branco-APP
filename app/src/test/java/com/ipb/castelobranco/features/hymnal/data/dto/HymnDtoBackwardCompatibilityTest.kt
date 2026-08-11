package com.ipb.castelobranco.features.hymnal.data.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Guards the offline hymnal against the `id` field being added.
 *
 * Every existing installation has a `hymnal.json` snapshot written before `id` existed. If
 * [HymnDto.id] were non-nullable without a default, decoding that file would throw
 * `MissingFieldException` and the cached hymnal would be lost on upgrade.
 */
class HymnDtoBackwardCompatibilityTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun `decodes a pre-feature snapshot without id`() {
        val raw = """
            [{"number":"42","title":"Firme nas Promessas","lyrics":[{"type":"verse","text":"a"}]}]
        """.trimIndent()

        val hymns = json.decodeFromString<List<HymnDto>>(raw)

        assertEquals(1, hymns.size)
        assertNull(hymns.first().id)
        assertEquals("42", hymns.first().number)
    }

    @Test
    fun `decodes a payload carrying id`() {
        val raw = """
            [{"id":42,"number":"42","title":"Firme nas Promessas","lyrics":[]}]
        """.trimIndent()

        val hymns = json.decodeFromString<List<HymnDto>>(raw)

        assertEquals(42, hymns.first().id)
    }

    @Test
    fun `decodes a payload with an unknown extra field`() {
        val raw = """
            [{"id":7,"number":"7","title":"T","lyrics":[],"author":"someone new"}]
        """.trimIndent()

        val hymns = json.decodeFromString<List<HymnDto>>(raw)

        assertEquals(7, hymns.first().id)
    }
}
