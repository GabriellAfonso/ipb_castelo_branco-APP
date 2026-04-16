package com.ipb.castelobranco.core.utils

import android.content.Context
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class DiskCacheTest {

    private lateinit var context: Context
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "disk_cache_test_${System.nanoTime()}")
        tempDir.mkdirs()

        context = mockk()
        every { context.cacheDir } returns tempDir
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `save and load round-trip returns stored data`() {
        DiskCache.save(context, "key1", "my data")

        val result = DiskCache.load(context, "key1")

        assertEquals("my data", result)
    }

    @Test
    fun `load returns null when file does not exist`() {
        val result = DiskCache.load(context, "missing_key")

        assertNull(result)
    }

    @Test
    fun `isCacheValidToday returns true for freshly saved data`() {
        DiskCache.save(context, "today_key", "content")

        val result = DiskCache.isCacheValidToday(context, "today_key")

        assertTrue(result)
    }

    @Test
    fun `isCacheValidToday returns false when file does not exist`() {
        val result = DiskCache.isCacheValidToday(context, "no_file")

        assertFalse(result)
    }

    @Test
    fun `isCacheValidWithinMinutes returns true when saved just now`() {
        DiskCache.save(context, "fresh_key", "data")

        val result = DiskCache.isCacheValidWithinMinutes(context, "fresh_key", minutes = 30)

        assertTrue(result)
    }

    @Test
    fun `isCacheValidWithinMinutes returns false when timestamp is expired`() {
        val expiredTimestamp = System.currentTimeMillis() - (60 * 60 * 1000) // 1 hour ago
        val wrapper = CacheWrapper(timestamp = expiredTimestamp, data = "old data")
        File(tempDir, "expired_key").writeText(Gson().toJson(wrapper))

        val result = DiskCache.isCacheValidWithinMinutes(context, "expired_key", minutes = 30)

        assertFalse(result)
    }
}
