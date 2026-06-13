package com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.viewmodel

import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.repository.ChordChartRepository
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.Song
import com.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChordChartCreateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var chordChartRepository: ChordChartRepository
    private lateinit var songsRepository: SongsRepository

    private val fakeSongs = listOf(
        Song(id = 1, title = "Oceans", artist = "Hillsong", categoryName = "Louvor"),
        Song(id = 2, title = "Way Maker", artist = "Sinach", categoryName = "Adoração"),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        chordChartRepository = mockk()
        songsRepository = mockk()
        every { songsRepository.observeAllSongs() } returns flowOf(SnapshotState.Data(fakeSongs))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ChordChartCreateViewModel(chordChartRepository, songsRepository)

    @Test
    fun `initial state has empty fields`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("", state.songQuery)
        assertEquals("", state.content)
        assertEquals("", state.tone)
        assertEquals("", state.instrument)
        assertNull(state.selectedSong)
        assertFalse(state.savedSuccessfully)
    }

    @Test
    fun `songs loaded from repository`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.allSongs.size)
    }

    @Test
    fun `onSongQueryChange filters songs by title`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onSongQueryChange("oceans")
        advanceUntilIdle()

        val filtered = vm.uiState.value.filteredSongs
        assertEquals(1, filtered.size)
        assertEquals("Oceans", filtered.first().title)
    }

    @Test
    fun `onSongSelected sets selectedSong and clears list`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onSongQueryChange("oceans")
        advanceUntilIdle()
        vm.onSongSelected(fakeSongs.first())
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(fakeSongs.first(), state.selectedSong)
        assertEquals("Oceans", state.songQuery)
        assertTrue(state.filteredSongs.isEmpty())
    }

    @Test
    fun `onContentChange updates content`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onContentChange("[G]Amazing [D]grace")
        advanceUntilIdle()

        assertEquals("[G]Amazing [D]grace", vm.uiState.value.content)
    }

    @Test
    fun `onToneChange updates tone`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onToneChange("G")
        advanceUntilIdle()

        assertEquals("G", vm.uiState.value.tone)
    }

    @Test
    fun `onInstrumentChange updates instrument`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onInstrumentChange("Violão")
        advanceUntilIdle()

        assertEquals("Violão", vm.uiState.value.instrument)
    }

    @Test
    fun `onSave success sets savedSuccessfully true`() = runTest {
        coEvery { chordChartRepository.createChordChart(any(), any(), any(), any()) } returns Result.success(Unit)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onSongSelected(fakeSongs.first())
        vm.onContentChange("[G]Amazing")
        vm.onToneChange("G")
        vm.onInstrumentChange("Violão")
        vm.onSave()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.savedSuccessfully)
        assertNull(vm.uiState.value.saveError)
    }

    @Test
    fun `onSave failure sets saveError`() = runTest {
        coEvery { chordChartRepository.createChordChart(any(), any(), any(), any()) } returns
            Result.failure(RuntimeException("server error"))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onSongSelected(fakeSongs.first())
        vm.onContentChange("[G]Amazing")
        vm.onToneChange("G")
        vm.onInstrumentChange("Violão")
        vm.onSave()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.savedSuccessfully)
        assertNotNull(vm.uiState.value.saveError)
        assertEquals("server error", vm.uiState.value.saveError)
    }

    @Test
    fun `onSave does nothing when no song selected`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onContentChange("[G]Amazing")
        vm.onToneChange("G")
        vm.onInstrumentChange("Violão")
        vm.onSave()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.savedSuccessfully)
        assertNull(vm.uiState.value.saveError)
    }

    @Test
    fun `onSave does nothing when tone is blank`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onSongSelected(fakeSongs.first())
        vm.onContentChange("[G]Amazing")
        vm.onInstrumentChange("Violão")
        vm.onSave()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.savedSuccessfully)
        assertNull(vm.uiState.value.saveError)
    }

    @Test
    fun `onSave does nothing when instrument is blank`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onSongSelected(fakeSongs.first())
        vm.onContentChange("[G]Amazing")
        vm.onToneChange("G")
        vm.onSave()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.savedSuccessfully)
        assertNull(vm.uiState.value.saveError)
    }
}
