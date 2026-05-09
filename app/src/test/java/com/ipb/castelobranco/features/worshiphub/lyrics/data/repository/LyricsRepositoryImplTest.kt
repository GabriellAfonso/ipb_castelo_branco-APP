package com.ipb.castelobranco.features.worshiphub.lyrics.data.repository

import com.ipb.castelobranco.core.domain.snapshot.Logger
import com.ipb.castelobranco.core.domain.snapshot.NetworkResult
import com.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.ipb.castelobranco.core.domain.snapshot.SnapshotFetcher
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.lyrics.data.dto.LyricsDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LyricsRepositoryImplTest {

    private lateinit var cache: SnapshotCache<List<LyricsDto>>
    private lateinit var fetcher: SnapshotFetcher<List<LyricsDto>>
    private lateinit var repository: LyricsRepositoryImpl

    @Before
    fun setup() {
        cache = mockk(relaxed = true)
        fetcher = mockk()
        repository = LyricsRepositoryImpl(cache, fetcher, Logger.Noop)
    }

    @Test
    fun `observe returns StateFlow starting with Loading`() {
        val state = repository.observe().value
        assertTrue(state is SnapshotState.Loading)
    }

    @Test
    fun `refresh on Success returns Updated and maps DTOs to domain`() = runTest {
        val dtos = listOf(
            LyricsDto(id = 1, songId = 10, content = "Verso 1\nRefrão", updatedAt = "2025-01-01")
        )
        coEvery { cache.loadETag() } returns null
        coEvery { fetcher.fetch(any()) } returns NetworkResult.Success(dtos, "v1")
        coEvery { cache.save(any(), any()) } returns Unit

        val result = repository.refresh()

        assertEquals(RefreshResult.Updated, result)
        val state = repository.getCurrentState()
        assertTrue(state is SnapshotState.Data)
        val data = (state as SnapshotState.Data).value
        assertEquals(1, data.size)
        assertEquals(1, data[0].id)
        assertEquals("Verso 1\nRefrão", data[0].content)
    }

    @Test
    fun `refresh on Failure with no cache returns Error`() = runTest {
        val error = RuntimeException("network error")
        coEvery { cache.loadETag() } returns null
        coEvery { fetcher.fetch(any()) } returns NetworkResult.Failure(error)
        coEvery { cache.load() } returns null

        val result = repository.refresh()

        assertTrue(result is RefreshResult.Error)
    }
}
