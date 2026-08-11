package com.ipb.castelobranco.features.gallery.domain.usecase

import com.ipb.castelobranco.features.gallery.domain.download.GalleryDownloadScheduler
import com.ipb.castelobranco.features.gallery.domain.model.Album
import com.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class GalleryAutoDownloadUseCaseTest {

    private lateinit var scheduler: GalleryDownloadScheduler
    private lateinit var repository: GalleryRepository
    private lateinit var useCase: GalleryAutoDownloadUseCase

    private val albumsFlow = MutableStateFlow<List<Album>>(emptyList())

    @Before
    fun setup() {
        scheduler = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        every { repository.albumsFlow } returns albumsFlow
        useCase = GalleryAutoDownloadUseCase(scheduler, repository)
    }

    @Test
    fun `triggerIfNeeded enqueues keeping existing work when gallery is empty`() {
        albumsFlow.value = emptyList()

        useCase.triggerIfNeeded()

        verify(exactly = 1) { scheduler.enqueueWifiOnly(replaceExisting = false) }
    }

    @Test
    fun `triggerIfNeeded does not enqueue when gallery is not empty`() {
        albumsFlow.value = listOf(mockk())

        useCase.triggerIfNeeded()

        verify(exactly = 0) { scheduler.enqueueWifiOnly(any()) }
    }

    @Test
    fun `triggerIfNeeded called twice with empty gallery enqueues twice`() {
        albumsFlow.value = emptyList()

        useCase.triggerIfNeeded()
        useCase.triggerIfNeeded()

        verify(exactly = 2) { scheduler.enqueueWifiOnly(replaceExisting = false) }
    }

    @Test
    fun `enqueueWifiOnly keeps existing work by default`() {
        useCase.enqueueWifiOnly()

        verify(exactly = 1) { scheduler.enqueueWifiOnly(replaceExisting = false) }
    }

    @Test
    fun `enqueueAnyNetwork delegates to the scheduler`() {
        useCase.enqueueAnyNetwork()

        verify(exactly = 1) { scheduler.enqueueAnyNetwork() }
    }
}
