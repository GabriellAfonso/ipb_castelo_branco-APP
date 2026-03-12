package com.gabrielafonso.ipb.castelobranco.core.domain.usecase

import com.gabrielafonso.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import com.gabrielafonso.ipb.castelobranco.features.hymnal.domain.repository.HymnalRepository
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.repository.ScheduleRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.domain.repository.ChordChartRepository
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.repository.SongsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PreloadDataUseCase @Inject constructor(
    private val songsRepository: SongsRepository,
    private val hymnalRepository: HymnalRepository,
    private val scheduleRepository: ScheduleRepository,
    private val galleryRepository: GalleryRepository,
    private val chordChartRepository: ChordChartRepository,
) {
    suspend operator fun invoke() {
        preloadCachesFromDisk()
        refreshDataFromNetwork()
    }

    private suspend fun preloadCachesFromDisk() = withContext(Dispatchers.IO) {
        supervisorScope {
            val preloads = listOf(
//              launch { songsRepository.preload() },
//              launch { hymnalRepository.preload() },
                launch { scheduleRepository.preload() },
                launch { galleryRepository.preload() },
                launch { chordChartRepository.preload() },
//              launch { profileRepository.preload() }
            )
            preloads.joinAll()
        }
    }

    private suspend fun refreshDataFromNetwork() = withContext(Dispatchers.IO) {
        supervisorScope {
            val jobs = listOf(
                async { songsRepository.refreshAllSongs() },
                async { songsRepository.refreshSongsBySunday() },
                async { songsRepository.refreshTopSongs() },
                async { songsRepository.refreshTopTones() },
                async { songsRepository.refreshSuggestedSongs() },
                async { hymnalRepository.refreshHymnal() },
                async { scheduleRepository.refreshMonthSchedule() },
                async { chordChartRepository.refresh() },
            )

            jobs.forEach { deferred ->
                runCatching { deferred.await() }
            }
        }
    }
}
