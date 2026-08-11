package com.ipb.castelobranco.features.worshiphub.songs.domain.usecase

import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.model.ChordChart
import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.repository.ChordChartRepository
import com.ipb.castelobranco.features.worshiphub.lyrics.domain.model.Lyrics
import com.ipb.castelobranco.features.worshiphub.lyrics.domain.repository.LyricsRepository
import com.ipb.castelobranco.core.domain.model.Song
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.SundaySet
import com.ipb.castelobranco.features.worshiphub.tables.domain.model.SundaySetItem
import com.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetSongDetailUseCaseTest {

    private lateinit var songsRepository: SongsRepository
    private lateinit var chordChartRepository: ChordChartRepository
    private lateinit var lyricsRepository: LyricsRepository
    private lateinit var useCase: GetSongDetailUseCase

    private val fakeSongs = listOf(
        Song(id = 1, title = "Oceans", artist = "Hillsong", categoryName = "Louvor", youtubeLink = "https://youtube.com/oceans"),
        Song(id = 2, title = "Way Maker", artist = "Sinach", categoryName = "Adoração"),
    )

    private val fakeSundays = listOf(
        SundaySet(
            date = "25/05/2025",
            songs = listOf(
                SundaySetItem(position = 1, title = "Oceans", songId = 1, artist = "Hillsong", tone = "D"),
                SundaySetItem(position = 2, title = "Way Maker", songId = 2, artist = "Sinach", tone = "G"),
            ),
        ),
        SundaySet(
            date = "18/05/2025",
            songs = listOf(
                SundaySetItem(position = 1, title = "Oceans", songId = 1, artist = "Hillsong", tone = "C"),
            ),
        ),
    )

    private val fakeCharts = listOf(
        ChordChart(id = 10, songId = 1, content = "...", tone = "D", instrument = "violão"),
        ChordChart(id = 11, songId = 1, content = "...", tone = "C", instrument = "teclado"),
        ChordChart(id = 12, songId = 2, content = "...", tone = "G", instrument = "violão"),
    )

    private val fakeLyrics = listOf(
        Lyrics(id = 20, songId = 1, content = "You call me out..."),
    )

    @Before
    fun setup() {
        songsRepository = mockk()
        chordChartRepository = mockk()
        lyricsRepository = mockk()

        every { songsRepository.observeAllSongs() } returns flowOf(SnapshotState.Data(fakeSongs))
        every { songsRepository.observeSongsBySunday() } returns flowOf(SnapshotState.Data(fakeSundays))
        every { chordChartRepository.observe() } returns flowOf(SnapshotState.Data(fakeCharts))
        every { lyricsRepository.observe() } returns flowOf(SnapshotState.Data(fakeLyrics))

        useCase = GetSongDetailUseCase(songsRepository, chordChartRepository, lyricsRepository)
    }

    @Test
    fun `happy path - song with everything`() = runTest {
        val result = useCase.observe(songId = 1).first()

        assertTrue(result is SnapshotState.Data)
        val detail = (result as SnapshotState.Data).value

        assertEquals("Oceans", detail.song.title)
        assertEquals(2, detail.playCount)
        assertEquals(listOf("D", "C"), detail.tones)
        assertEquals(listOf("25/05/2025", "18/05/2025"), detail.lastSundays)
        assertEquals(2, detail.chordCharts.size)
        assertEquals("You call me out...", detail.lyrics?.content)
    }

    @Test
    fun `song without chord charts or lyrics`() = runTest {
        every { chordChartRepository.observe() } returns flowOf(SnapshotState.Data(emptyList()))
        every { lyricsRepository.observe() } returns flowOf(SnapshotState.Data(emptyList()))
        useCase = GetSongDetailUseCase(songsRepository, chordChartRepository, lyricsRepository)

        val result = useCase.observe(songId = 2).first()

        assertTrue(result is SnapshotState.Data)
        val detail = (result as SnapshotState.Data).value

        assertEquals("Way Maker", detail.song.title)
        assertTrue(detail.chordCharts.isEmpty())
        assertNull(detail.lyrics)
    }

    @Test
    fun `song never played`() = runTest {
        every { songsRepository.observeSongsBySunday() } returns flowOf(SnapshotState.Data(emptyList()))
        useCase = GetSongDetailUseCase(songsRepository, chordChartRepository, lyricsRepository)

        val result = useCase.observe(songId = 1).first()

        assertTrue(result is SnapshotState.Data)
        val detail = (result as SnapshotState.Data).value

        assertEquals(0, detail.playCount)
        assertTrue(detail.tones.isEmpty())
        assertTrue(detail.lastSundays.isEmpty())
    }

    @Test
    fun `returns error when song not found`() = runTest {
        val result = useCase.observe(songId = 999).first()

        assertTrue(result is SnapshotState.Error)
    }

    @Test
    fun `returns loading when any source still loading`() = runTest {
        every { songsRepository.observeAllSongs() } returns flowOf(SnapshotState.Loading)
        useCase = GetSongDetailUseCase(songsRepository, chordChartRepository, lyricsRepository)

        val result = useCase.observe(songId = 1).first()

        assertTrue(result is SnapshotState.Loading)
    }
}
