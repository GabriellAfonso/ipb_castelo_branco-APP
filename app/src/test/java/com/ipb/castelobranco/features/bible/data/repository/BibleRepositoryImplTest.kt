package com.ipb.castelobranco.features.bible.data.repository

import com.ipb.castelobranco.core.data.local.SnapshotStorage
import com.ipb.castelobranco.core.domain.snapshot.SnapshotCache
import com.ipb.castelobranco.features.bible.data.dto.BibleBookDto
import com.ipb.castelobranco.features.bible.data.local.BiblePreferences
import com.ipb.castelobranco.features.bible.domain.model.BibleReadingPosition
import com.ipb.castelobranco.features.bible.domain.model.BibleTranslation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BibleRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private val naaCache: SnapshotCache<List<BibleBookDto>> = mockk(relaxed = true)
    private val araCache: SnapshotCache<List<BibleBookDto>> = mockk(relaxed = true)
    private val preferences: BiblePreferences = mockk(relaxed = true)
    private val storage: SnapshotStorage = mockk(relaxed = true)

    private val caches = mapOf(
        BibleTranslation.NAA to naaCache,
        BibleTranslation.ARA to araCache,
    )

    private val genesisDto = BibleBookDto(
        abbrev = "gn",
        name = "Gênesis",
        chapters = listOf(listOf("No princípio...", "A terra era sem forma...")),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { preferences.translationFlow } returns flowOf(BibleTranslation.NAA)
        every { preferences.positionFlow } returns flowOf(BibleReadingPosition.default())
        every { preferences.fontSizeFlow } returns flowOf(BiblePreferences.DEFAULT_FONT_SIZE)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildRepository(): BibleRepositoryImpl {
        return BibleRepositoryImpl(caches, preferences, storage, Dispatchers.Unconfined)
    }

    // region preload

    @Test
    fun `preload loads books for active translation`() = runTest {
        coEvery { naaCache.load() } returns listOf(genesisDto)
        coEvery { araCache.load() } returns null

        val repo = buildRepository()
        repo.preload()
        advanceUntilIdle()

        assertEquals(1, repo.booksFlow.value.size)
        assertEquals("gn", repo.booksFlow.value[0].abbrev)
    }

    @Test
    fun `preload detects cached translations`() = runTest {
        coEvery { naaCache.load() } returns listOf(genesisDto)
        coEvery { araCache.load() } returns listOf(genesisDto)

        val repo = buildRepository()
        repo.preload()
        advanceUntilIdle()

        assertEquals(setOf(BibleTranslation.NAA, BibleTranslation.ARA), repo.cachedTranslationsFlow.value)
    }

    @Test
    fun `preload with no cache returns empty books`() = runTest {
        coEvery { naaCache.load() } returns null
        coEvery { araCache.load() } returns null

        val repo = buildRepository()
        repo.preload()
        advanceUntilIdle()

        assertTrue(repo.booksFlow.value.isEmpty())
    }

    // endregion

    // region setActiveTranslation

    @Test
    fun `setActiveTranslation delegates to preferences`() = runTest {
        coEvery { naaCache.load() } returns null
        coEvery { araCache.load() } returns null

        val repo = buildRepository()
        repo.setActiveTranslation(BibleTranslation.ARA)

        coVerify { preferences.setTranslation(BibleTranslation.ARA) }
    }

    // endregion

    // region savePosition

    @Test
    fun `savePosition delegates to preferences`() = runTest {
        coEvery { naaCache.load() } returns null

        val repo = buildRepository()
        repo.savePosition("ex", 3, 14)

        coVerify { preferences.setPosition("ex", 3, 14) }
    }

    // endregion

    // region setFontSize

    @Test
    fun `setFontSize delegates to preferences`() = runTest {
        coEvery { naaCache.load() } returns null

        val repo = buildRepository()
        repo.setFontSize(24f)

        coVerify { preferences.setFontSize(24f) }
    }

    // endregion

    // region clearAll

    @Test
    fun `clearAll clears all caches`() = runTest {
        coEvery { naaCache.load() } returns null
        coEvery { araCache.load() } returns null

        val repo = buildRepository()
        repo.clearAll()

        coVerify { naaCache.clear() }
        coVerify { araCache.clear() }
    }

    @Test
    fun `clearAll resets preferences`() = runTest {
        coEvery { naaCache.load() } returns null
        coEvery { araCache.load() } returns null

        val repo = buildRepository()
        repo.clearAll()

        coVerify { preferences.resetAll() }
    }

    @Test
    fun `clearAll empties booksFlow and cachedTranslationsFlow`() = runTest {
        coEvery { naaCache.load() } returns listOf(genesisDto)
        coEvery { araCache.load() } returns listOf(genesisDto)

        val repo = buildRepository()
        repo.preload()
        advanceUntilIdle()

        // Verify they had data
        assertTrue(repo.booksFlow.value.isNotEmpty())
        assertTrue(repo.cachedTranslationsFlow.value.isNotEmpty())

        repo.clearAll()

        assertTrue(repo.booksFlow.value.isEmpty())
        assertTrue(repo.cachedTranslationsFlow.value.isEmpty())
    }

    // endregion
}
