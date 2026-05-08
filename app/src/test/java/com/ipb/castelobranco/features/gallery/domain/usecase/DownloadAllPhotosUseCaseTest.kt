package com.ipb.castelobranco.features.gallery.domain.usecase

import com.ipb.castelobranco.core.domain.download.DownloadProgress
import com.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DownloadAllPhotosUseCaseTest {

    private lateinit var repository: GalleryRepository
    private lateinit var useCase: DownloadAllPhotosUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = DownloadAllPhotosUseCase(repository)
    }

    @Test
    fun `downloadProgress returns flow from repository`() = runTest {
        val progress = listOf(
            DownloadProgress(downloaded = 0, total = 10),
            DownloadProgress(downloaded = 5, total = 10),
            DownloadProgress(downloaded = 10, total = 10),
        )
        every { repository.downloadAllPhotos() } returns flowOf(*progress.toTypedArray())

        val result = useCase.downloadProgress().toList()

        assertEquals(3, result.size)
        assertEquals(0, result[0].percentage)
        assertEquals(50, result[1].percentage)
        assertEquals(100, result[2].percentage)
    }

    @Test
    fun `preload delegates to repository`() = runTest {
        coEvery { repository.preload() } returns Unit

        useCase.preload()

        coVerify { repository.preload() }
    }
}
