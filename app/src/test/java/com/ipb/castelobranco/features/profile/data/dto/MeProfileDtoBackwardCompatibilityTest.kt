package com.ipb.castelobranco.features.profile.data.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards `GET /api/me/profile/` against the `active` field being dropped by the backend.
 *
 * `active` was vestigial on both sides: no permission class read it, and the app never rendered it.
 * The backend will stop sending it. While [MeProfileDto] declared `active` as a non-nullable
 * `Boolean` without a default, decoding a payload without the key threw `MissingFieldException`
 * and the profile screen broke. The app therefore drops the field first, and stays compatible with
 * both the current backend (which still sends it) and the future one (which will not).
 */
class MeProfileDtoBackwardCompatibilityTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun `decodes a payload without active`() {
        val raw = """
            {"name":"Joao Silva","is_member":true,"is_admin":false,"photo_url":null}
        """.trimIndent()

        val dto = json.decodeFromString<MeProfileDto>(raw)

        assertEquals("Joao Silva", dto.name)
        assertTrue(dto.isMember)
        assertFalse(dto.isAdmin)
        assertNull(dto.photoUrl)
    }

    @Test
    fun `decodes a payload still carrying active`() {
        val raw = """
            {"name":"Joao Silva","active":true,"is_member":true,"is_admin":false,
             "photo_url":"https://example.com/photo.jpg"}
        """.trimIndent()

        val dto = json.decodeFromString<MeProfileDto>(raw)

        assertEquals("Joao Silva", dto.name)
        assertEquals("https://example.com/photo.jpg", dto.photoUrl)
    }

    @Test
    fun `decodes a payload without photo_url`() {
        val raw = """{"name":"Joao Silva","is_member":false,"is_admin":false}"""

        val dto = json.decodeFromString<MeProfileDto>(raw)

        assertNull(dto.photoUrl)
    }
}
