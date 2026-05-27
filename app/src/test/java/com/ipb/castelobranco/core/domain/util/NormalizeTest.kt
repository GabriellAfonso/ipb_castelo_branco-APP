package com.ipb.castelobranco.core.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class NormalizeTest {

    @Test
    fun `strips diacritical marks`() {
        assertEquals("Joao", "João".normalize())
    }

    @Test
    fun `strips multiple accented characters`() {
        assertEquals("acucar", "açúcar".normalize())
    }

    @Test
    fun `strips punctuation`() {
        assertEquals("Santo Santo", "Santo, Santo!".normalize())
    }

    @Test
    fun `strips accents and punctuation together`() {
        assertEquals("Gloria ao Pai", "Glória, ao Pai!".normalize())
    }

    @Test
    fun `plain ASCII string unchanged`() {
        assertEquals("hello world", "hello world".normalize())
    }

    @Test
    fun `empty string returns empty`() {
        assertEquals("", "".normalize())
    }
}
