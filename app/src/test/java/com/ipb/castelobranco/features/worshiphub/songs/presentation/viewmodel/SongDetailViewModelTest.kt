package com.ipb.castelobranco.features.worshiphub.songs.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.model.ChordChart
import com.ipb.castelobranco.features.worshiphub.lyrics.domain.model.Lyrics
import com.ipb.castelobranco.features.worshiphub.songs.domain.usecase.GetSongDetailUseCase
import com.ipb.castelobranco.features.worshiphub.songs.domain.usecase.SongDetail
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.Song
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getSongDetailUseCase: GetSongDetailUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: SongDetailViewModel

    private val fakeSong = Song(id = 1, title = "Oceans", artist = "Hillsong", categoryName = "Louvor", youtubeLink = "https://youtube.com/oceans")

    private val fakeDetail = SongDetail(
        song        = fakeSong,
        playCount   = 5,
        tones       = listOf("D", "C"),
        lastSundays = listOf("25/05/2025", "18/05/2025"),
        chordCharts = listOf(
            ChordChart(id = 10, songId = 1, content = "...", tone = "D", instrument = "violão"),
        ),
        lyrics = Lyrics(id = 20, songId = 1, content = "..."),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getSongDetailUseCase = mockk()
        savedStateHandle = SavedStateHandle(mapOf("songId" to 1))

        every { getSongDetailUseCase.observe(1) } returns flowOf(SnapshotState.Loading)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SongDetailViewModel {
        return SongDetailViewModel(savedStateHandle, getSongDetailUseCase)
    }

    private fun TestScope.subscribeAndAdvance(vm: SongDetailViewModel) {
        val job = launch { vm.uiState.collect { } }
        advanceUntilIdle()
        job.cancel()
    }

    @Test
    fun `uiState initial value has isLoading true`() = runTest {
        viewModel = createViewModel()
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `uiState maps SongDetail to UiState correctly`() = runTest {
        every { getSongDetailUseCase.observe(1) } returns flowOf(SnapshotState.Data(fakeDetail))
        viewModel = createViewModel()

        subscribeAndAdvance(viewModel)

        val state = viewModel.uiState.value
        assertEquals("Oceans", state.songName)
        assertEquals("Hillsong", state.artist)
        assertEquals(5, state.playCount)
        assertEquals(listOf("D", "C"), state.tones)
        assertEquals(listOf("25/05/2025", "18/05/2025"), state.lastSundays)
        assertEquals(1, state.chordCharts.size)
        assertTrue(state.hasLyrics)
        assertEquals(20, state.lyricsId)
        assertEquals("https://youtube.com/oceans", state.youtubeLink)
    }

    @Test
    fun `uiState sets error when use case emits Error`() = runTest {
        every { getSongDetailUseCase.observe(1) } returns flowOf(SnapshotState.Error(RuntimeException("fail")))
        viewModel = createViewModel()

        subscribeAndAdvance(viewModel)

        assertEquals("fail", viewModel.uiState.value.error)
    }

    @Test
    fun `uiState has no lyrics when detail lyrics is null`() = runTest {
        val detailNoLyrics = fakeDetail.copy(lyrics = null)
        every { getSongDetailUseCase.observe(1) } returns flowOf(SnapshotState.Data(detailNoLyrics))
        viewModel = createViewModel()

        subscribeAndAdvance(viewModel)

        val state = viewModel.uiState.value
        assertEquals(false, state.hasLyrics)
        assertNull(state.lyricsId)
    }
}
