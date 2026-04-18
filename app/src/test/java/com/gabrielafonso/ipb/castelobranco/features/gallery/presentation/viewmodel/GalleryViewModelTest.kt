package com.ipb.castelobranco.features.gallery.presentation.viewmodel

import androidx.work.WorkManager
import com.ipb.castelobranco.core.data.NetworkConnectivityObserver
import com.ipb.castelobranco.features.gallery.domain.model.Album
import com.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import com.ipb.castelobranco.features.gallery.domain.usecase.GalleryAutoDownloadUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class GalleryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: GalleryRepository
    private lateinit var autoDownload: GalleryAutoDownloadUseCase
    private lateinit var connectivityObserver: NetworkConnectivityObserver
    private lateinit var workManager: WorkManager
    private lateinit var viewModel: GalleryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        repository = mockk()
        autoDownload = mockk(relaxed = true)
        connectivityObserver = mockk()
        workManager = mockk()

        every { repository.albumsFlow } returns MutableStateFlow(emptyList())
        every { repository.thumbnailsFlow } returns MutableStateFlow(emptyMap())
        every { connectivityObserver.isOnWifi } returns flowOf(false)
        every { workManager.getWorkInfosForUniqueWorkFlow(any()) } returns flowOf(emptyList())

        viewModel = GalleryViewModel(repository, autoDownload, connectivityObserver, workManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region albums / thumbnails exposure

    @Test
    fun `albums exposes repository albumsFlow`() = runTest {
        val albums = listOf(Album(1L, "Conferência 2024"), Album(2L, "Culto Especial"))
        every { repository.albumsFlow } returns MutableStateFlow(albums)
        viewModel = GalleryViewModel(repository, autoDownload, connectivityObserver, workManager)
        advanceUntilIdle()
        assertEquals(albums, viewModel.albums.value)
    }

    @Test
    fun `thumbnails exposes repository thumbnailsFlow`() = runTest {
        val file = mockk<File>()
        val thumbnails = mapOf(1L to file, 2L to null)
        every { repository.thumbnailsFlow } returns MutableStateFlow(thumbnails)
        viewModel = GalleryViewModel(repository, autoDownload, connectivityObserver, workManager)
        advanceUntilIdle()
        assertEquals(thumbnails, viewModel.thumbnails.value)
    }

    // endregion

    // region downloadState

    @Test
    fun `downloadState defaults when no work info is available`() = runTest {
        every { workManager.getWorkInfosForUniqueWorkFlow(any()) } returns flowOf(emptyList())
        viewModel = GalleryViewModel(repository, autoDownload, connectivityObserver, workManager)
        advanceUntilIdle()
        val state = viewModel.downloadState.value
        assertFalse(state.isDownloading)
        assertFalse(state.isPending)
        assertNull(state.error)
    }

    // endregion

    // region isOnWifi

    @Test
    fun `isOnWifi is false when connectivity observer emits false`() = runTest {
        every { connectivityObserver.isOnWifi } returns flowOf(false)
        viewModel = GalleryViewModel(repository, autoDownload, connectivityObserver, workManager)
        advanceUntilIdle()
        assertFalse(viewModel.isOnWifi.value)
    }

    @Test
    fun `isOnWifi is true when connectivity observer emits true`() = runTest {
        every { connectivityObserver.isOnWifi } returns flowOf(true)
        viewModel = GalleryViewModel(repository, autoDownload, connectivityObserver, workManager)

        val job = launch { viewModel.isOnWifi.collect { } }
        advanceUntilIdle()
        job.cancel()

        assertTrue(viewModel.isOnWifi.value)
    }

    // endregion

    // region downloadAllPhotos / downloadWithMobileData

    @Test
    fun `downloadAllPhotos delegates to autoDownload enqueueWifiOnly`() = runTest {
        viewModel.downloadAllPhotos()
        verify { autoDownload.enqueueWifiOnly() }
    }

    @Test
    fun `downloadWithMobileData delegates to autoDownload enqueueAnyNetwork`() = runTest {
        viewModel.downloadWithMobileData()
        verify { autoDownload.enqueueAnyNetwork() }
    }

    // endregion

    // region clearGallery

    @Test
    fun `clearGallery delegates to repository clearAllPhotos`() = runTest {
        coEvery { repository.clearAllPhotos() } returns Unit
        viewModel.clearGallery()
        advanceUntilIdle()
        coVerify { repository.clearAllPhotos() }
    }

    // endregion

    // region getLocalPhotos / getPhotoName

    @Test
    fun `getLocalPhotos delegates to repository`() = runTest {
        val photos = listOf(mockk<File>())
        coEvery { repository.getLocalPhotos(1L) } returns photos
        val result = viewModel.getLocalPhotos(1L)
        assertEquals(photos, result)
    }

    @Test
    fun `getPhotoName returns repository value when found`() = runTest {
        coEvery { repository.getPhotoName(1L, 42L) } returns "Batismo João"
        val result = viewModel.getPhotoName(1L, 42L)
        assertEquals("Batismo João", result)
    }

    @Test
    fun `getPhotoName returns Foto when repository returns null`() = runTest {
        coEvery { repository.getPhotoName(1L, 99L) } returns null
        val result = viewModel.getPhotoName(1L, 99L)
        assertEquals("Foto", result)
    }

    // endregion
}
