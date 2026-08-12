package com.ipb.castelobranco.features.worshiphub.songs.domain.usecase

import com.ipb.castelobranco.core.domain.error.AppError
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.model.ChordChart
import com.ipb.castelobranco.features.worshiphub.chordcharts.domain.repository.ChordChartRepository
import com.ipb.castelobranco.features.worshiphub.lyrics.domain.model.Lyrics
import com.ipb.castelobranco.features.worshiphub.lyrics.domain.repository.LyricsRepository
import com.ipb.castelobranco.core.domain.model.Song
import com.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class SongDetail(
    val song: Song,
    val playCount: Int,
    val tones: List<String>,
    val lastSundays: List<String>,
    val chordCharts: List<ChordChart>,
    val lyrics: Lyrics?,
)

class GetSongDetailUseCase @Inject constructor(
    private val songsRepository: SongsRepository,
    private val chordChartRepository: ChordChartRepository,
    private val lyricsRepository: LyricsRepository,
) {

    fun observe(songId: Int): Flow<SnapshotState<SongDetail>> = combine(
        songsRepository.observeAllSongs(),
        songsRepository.observeSongsBySunday(),
        chordChartRepository.observe(),
        lyricsRepository.observe(),
    ) { songsState, sundaysState, chartsState, lyricsState ->

        val songs = (songsState as? SnapshotState.Data)?.value
        val sundays = (sundaysState as? SnapshotState.Data)?.value
        val charts = (chartsState as? SnapshotState.Data)?.value
        val allLyrics = (lyricsState as? SnapshotState.Data)?.value

        if (songs == null || sundays == null || charts == null || allLyrics == null) {
            val error = listOf(songsState, sundaysState, chartsState, lyricsState)
                .filterIsInstance<SnapshotState.Error>()
                .firstOrNull()
            if (error != null) return@combine SnapshotState.Error(error.error)
            return@combine SnapshotState.Loading
        }

        val song = songs.firstOrNull { it.id == songId }
            ?: return@combine SnapshotState.Error(
                AppError.Unknown(message = "Song $songId not found", userMessage = "Música não encontrada")
            )

        val sundayItems = sundays.flatMap { set ->
            set.songs.filter { it.songId == songId }.map { set.date to it }
        }

        val playCount = sundayItems.size
        val tones = sundayItems.map { it.second.tone }.distinct()
        val lastSundays = sundayItems.map { it.first }.distinct().take(3)

        val songCharts = charts.filter { it.songId == songId }
        val songLyrics = allLyrics.firstOrNull { it.songId == songId }

        SnapshotState.Data(
            SongDetail(
                song = song,
                playCount = playCount,
                tones = tones,
                lastSundays = lastSundays,
                chordCharts = songCharts,
                lyrics = songLyrics,
            )
        )
    }
}
