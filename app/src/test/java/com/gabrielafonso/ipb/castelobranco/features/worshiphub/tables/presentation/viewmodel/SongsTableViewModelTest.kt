package com.ipb.castelobranco.features.worshiphub.tables.presentation.viewmodel

import com.ipb.castelobranco.core.domain.snapshot.RefreshResult
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.Song
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.SuggestedSong
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.SundaySet
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.SundaySetItem
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.TopSong
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.TopTone
import com.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongsTableViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: SongsRepository
    private lateinit var viewModel: SongsTableViewModel

    private val fakeSongs = listOf(
        Song(id = 1, title = "Oceans", artist = "Hillsong", categoryName = "Louvor"),
        Song(id = 2, title = "Way Maker", artist = "Sinach", categoryName = "Adoração"),
    )
    private val fakeSundays = listOf(
        SundaySet(date = "07/04/2024", songs = listOf(SundaySetItem(1, "Oceans", "Hillsong", "D")))
    )
    private val fakeTopSongs = listOf(TopSong(title = "Oceans", playCount = 10))
    private val fakeTopTones = listOf(TopTone(tone = "D", count = 8))
    private val fakeSuggested = listOf(
        SuggestedSong(id = 1, songId = 1, title = "Oceans", artist = "Hillsong", date = "07/04/2024", tone = "D", position = 1)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()

        every { repository.observeAllSongs() } returns flowOf(SnapshotState.Loading)
        every { repository.observeSongsBySunday() } returns flowOf(SnapshotState.Loading)
        every { repository.observeTopSongs() } returns flowOf(SnapshotState.Loading)
        every { repository.observeTopTones() } returns flowOf(SnapshotState.Loading)
        every { repository.observeSuggestedSongs() } returns flowOf(SnapshotState.Loading)
        coEvery { repository.refreshSongsBySunday() } returns RefreshResult.Updated
        coEvery { repository.refreshTopSongs() } returns RefreshResult.Updated
        coEvery { repository.refreshTopTones() } returns RefreshResult.Updated
        coEvery { repository.refreshAllSongs() } returns RefreshResult.Updated
        coEvery { repository.refreshSuggestedSongs(any<Map<Int, Int>>()) } returns RefreshResult.Updated

        viewModel = SongsTableViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region allSongs

    @Test
    fun `allSongs emits empty list when state is Loading`() = runTest {
        advanceUntilIdle()
        assertTrue(viewModel.allSongs.value.isEmpty())
    }

    @Test
    fun `allSongs emits list when state is Data`() = runTest {
        every { repository.observeAllSongs() } returns flowOf(SnapshotState.Data(fakeSongs))
        viewModel = SongsTableViewModel(repository)

        val job = launch { viewModel.allSongs.collect { } }
        advanceUntilIdle()
        job.cancel()

        assertEquals(fakeSongs, viewModel.allSongs.value)
    }

    @Test
    fun `allSongs emits empty list when state is Error`() = runTest {
        every { repository.observeAllSongs() } returns flowOf(SnapshotState.Error(RuntimeException()))
        viewModel = SongsTableViewModel(repository)

        val job = launch { viewModel.allSongs.collect { } }
        advanceUntilIdle()
        job.cancel()

        assertTrue(viewModel.allSongs.value.isEmpty())
    }

    // endregion

    // region lastSundays

    @Test
    fun `lastSundays emits empty list when state is Loading`() = runTest {
        advanceUntilIdle()
        assertTrue(viewModel.lastSundays.value.isEmpty())
    }

    @Test
    fun `lastSundays emits list when state is Data`() = runTest {
        every { repository.observeSongsBySunday() } returns flowOf(SnapshotState.Data(fakeSundays))
        viewModel = SongsTableViewModel(repository)

        val job = launch { viewModel.lastSundays.collect { } }
        advanceUntilIdle()
        job.cancel()

        assertEquals(fakeSundays, viewModel.lastSundays.value)
    }

    // endregion

    // region topSongs

    @Test
    fun `topSongs emits empty list when state is Loading`() = runTest {
        advanceUntilIdle()
        assertTrue(viewModel.topSongs.value.isEmpty())
    }

    @Test
    fun `topSongs emits list when state is Data`() = runTest {
        every { repository.observeTopSongs() } returns flowOf(SnapshotState.Data(fakeTopSongs))
        viewModel = SongsTableViewModel(repository)

        val job = launch { viewModel.topSongs.collect { } }
        advanceUntilIdle()
        job.cancel()

        assertEquals(fakeTopSongs, viewModel.topSongs.value)
    }

    // endregion

    // region topTones

    @Test
    fun `topTones emits empty list when state is Loading`() = runTest {
        advanceUntilIdle()
        assertTrue(viewModel.topTones.value.isEmpty())
    }

    @Test
    fun `topTones emits list when state is Data`() = runTest {
        every { repository.observeTopTones() } returns flowOf(SnapshotState.Data(fakeTopTones))
        viewModel = SongsTableViewModel(repository)

        val job = launch { viewModel.topTones.collect { } }
        advanceUntilIdle()
        job.cancel()

        assertEquals(fakeTopTones, viewModel.topTones.value)
    }

    // endregion

    // region suggestedSongs

    @Test
    fun `suggestedSongs emits empty list when state is Loading`() = runTest {
        advanceUntilIdle()
        assertTrue(viewModel.suggestedSongs.value.isEmpty())
    }

    @Test
    fun `suggestedSongs emits list when state is Data`() = runTest {
        every { repository.observeSuggestedSongs() } returns flowOf(SnapshotState.Data(fakeSuggested))
        viewModel = SongsTableViewModel(repository)

        val job = launch { viewModel.suggestedSongs.collect { } }
        advanceUntilIdle()
        job.cancel()

        assertEquals(fakeSuggested, viewModel.suggestedSongs.value)
    }

    // endregion

    // region refreshCurrentTab

    @Test
    fun `refreshCurrentTab tab 0 calls refreshSongsBySunday`() = runTest {
        viewModel.refreshCurrentTab(tabIndex = 0, minDurationMs = 0L)
        advanceUntilIdle()
        coVerify { repository.refreshSongsBySunday() }
    }

    @Test
    fun `refreshCurrentTab tab 1 calls refreshTopSongs`() = runTest {
        viewModel.refreshCurrentTab(tabIndex = 1, minDurationMs = 0L)
        advanceUntilIdle()
        coVerify { repository.refreshTopSongs() }
    }

    @Test
    fun `refreshCurrentTab tab 2 calls refreshTopTones`() = runTest {
        viewModel.refreshCurrentTab(tabIndex = 2, minDurationMs = 0L)
        advanceUntilIdle()
        coVerify { repository.refreshTopTones() }
    }

    @Test
    fun `refreshCurrentTab tab 3 does not call any repository method`() = runTest {
        viewModel.refreshCurrentTab(tabIndex = 3, minDurationMs = 0L)
        advanceUntilIdle()
        coVerify(exactly = 0) { repository.refreshTopSongs() }
        coVerify(exactly = 0) { repository.refreshTopTones() }
        coVerify(exactly = 0) { repository.refreshSongsBySunday() }
    }

    @Test
    fun `isRefreshing is false after refreshCurrentTab completes`() = runTest {
        viewModel.refreshCurrentTab(tabIndex = 0, minDurationMs = 0L)
        advanceUntilIdle()
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `refreshCurrentTab guard prevents concurrent refresh`() = runTest {
        coEvery { repository.refreshSongsBySunday() } coAnswers {
            kotlinx.coroutines.delay(5_000); RefreshResult.Updated
        }
        viewModel.refreshCurrentTab(tabIndex = 0) // schedules coroutine
        runCurrent() // run it up to the delay — sets _isRefreshing = true
        viewModel.refreshCurrentTab(tabIndex = 0) // guarded
        advanceUntilIdle()
        coVerify(exactly = 1) { repository.refreshSongsBySunday() }
    }

    // endregion

    // region toggleFixed

    @Test
    fun `toggleFixed adds song to fixedByPosition when not present`() = runTest {
        val song = fakeSuggested[0]
        viewModel.toggleFixed(song)
        assertEquals(mapOf(song.position to song.id), viewModel.fixedByPosition.value)
    }

    @Test
    fun `toggleFixed removes song from fixedByPosition when already present`() = runTest {
        val song = fakeSuggested[0]
        viewModel.toggleFixed(song)
        viewModel.toggleFixed(song)
        assertTrue(viewModel.fixedByPosition.value.isEmpty())
    }

    @Test
    fun `toggleFixed replaces position when different song is at same position`() = runTest {
        val song1 = fakeSuggested[0]
        val song2 = song1.copy(id = 99)
        viewModel.toggleFixed(song1)
        viewModel.toggleFixed(song2)
        assertEquals(mapOf(song1.position to song2.id), viewModel.fixedByPosition.value)
    }

    // endregion

    // region refreshSuggestedSongs

    @Test
    fun `refreshSuggestedSongs calls repository with current fixedByPosition`() = runTest {
        val song = fakeSuggested[0]
        viewModel.toggleFixed(song)
        val expectedFixed = mapOf(song.position to song.id)

        viewModel.refreshSuggestedSongs(minDurationMs = 0L)
        advanceUntilIdle()

        coVerify { repository.refreshSuggestedSongs(expectedFixed) }
    }

    @Test
    fun `isRefreshingSuggestedSongs is false after refresh completes`() = runTest {
        viewModel.refreshSuggestedSongs(minDurationMs = 0L)
        advanceUntilIdle()
        assertFalse(viewModel.isRefreshingSuggestedSongs.value)
    }

    @Test
    fun `refreshSuggestedSongs guard prevents concurrent refresh`() = runTest {
        coEvery { repository.refreshSuggestedSongs(any<Map<Int, Int>>()) } coAnswers {
            kotlinx.coroutines.delay(5_000); RefreshResult.Updated
        }
        viewModel.refreshSuggestedSongs() // schedules coroutine
        runCurrent() // advance to where _isRefreshingSuggestedSongs = true
        viewModel.refreshSuggestedSongs() // guarded
        advanceUntilIdle()
        coVerify(exactly = 1) { repository.refreshSuggestedSongs(any<Map<Int, Int>>()) }
    }

    // endregion

    // region initialize

    @Test
    fun `initialize calls refreshSongsBySunday`() = runTest {
        viewModel.initialize()
        advanceUntilIdle()
        coVerify(atLeast = 1) { repository.refreshSongsBySunday() }
    }

    // endregion
}
