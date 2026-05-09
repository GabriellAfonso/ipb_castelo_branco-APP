package com.ipb.castelobranco.features.gallery.domain.usecase

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ipb.castelobranco.features.gallery.data.work.GalleryDownloadWorker
import com.ipb.castelobranco.features.gallery.domain.model.Album
import com.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class GalleryAutoDownloadUseCaseTest {

    private lateinit var workManager: WorkManager
    private lateinit var repository: GalleryRepository
    private lateinit var useCase: GalleryAutoDownloadUseCase

    private val albumsFlow = MutableStateFlow<List<Album>>(emptyList())

    @Before
    fun setup() {
        workManager = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        every { repository.albumsFlow } returns albumsFlow
        useCase = GalleryAutoDownloadUseCase(workManager, repository)
    }

    @Test
    fun `triggerIfNeeded enqueues with KEEP when gallery is empty`() {
        albumsFlow.value = emptyList()

        useCase.triggerIfNeeded()

        verify(exactly = 1) {
            workManager.enqueueUniqueWork(
                GalleryDownloadWorker.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `triggerIfNeeded does not enqueue when gallery is not empty`() {
        albumsFlow.value = listOf(mockk())

        useCase.triggerIfNeeded()

        verify(exactly = 0) { workManager.enqueueUniqueWork(any<String>(), any<ExistingWorkPolicy>(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `triggerIfNeeded called twice with empty gallery enqueues twice`() {
        albumsFlow.value = emptyList()

        useCase.triggerIfNeeded()
        useCase.triggerIfNeeded()

        verify(exactly = 2) {
            workManager.enqueueUniqueWork(
                GalleryDownloadWorker.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `enqueueWifiOnly calls enqueueUniqueWork with KEEP policy`() {
        useCase.enqueueWifiOnly()

        verify(exactly = 1) {
            workManager.enqueueUniqueWork(
                GalleryDownloadWorker.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `enqueueAnyNetwork calls enqueueUniqueWork with REPLACE policy`() {
        useCase.enqueueAnyNetwork()

        verify(exactly = 1) {
            workManager.enqueueUniqueWork(
                GalleryDownloadWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }
}
