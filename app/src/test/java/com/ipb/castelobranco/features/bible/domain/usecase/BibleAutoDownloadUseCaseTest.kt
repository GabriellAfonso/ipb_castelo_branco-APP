package com.ipb.castelobranco.features.bible.domain.usecase

import com.ipb.castelobranco.features.bible.domain.download.BibleDownloadScheduler
import com.ipb.castelobranco.features.bible.domain.model.BibleTranslation
import com.ipb.castelobranco.features.bible.domain.repository.BibleRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

class BibleAutoDownloadUseCaseTest {

    private val scheduler: BibleDownloadScheduler = mockk(relaxed = true)
    private val repository: BibleRepository = mockk(relaxed = true)

    @Test
    fun `triggerIfNeeded enqueues when cache is missing some translation`() {
        every { repository.cachedTranslationsFlow } returns MutableStateFlow(setOf(BibleTranslation.NAA))

        val useCase = BibleAutoDownloadUseCase(scheduler, repository)
        useCase.triggerIfNeeded()

        verify(exactly = 1) { scheduler.enqueueWifiOnly(replaceExisting = false, force = false) }
    }

    @Test
    fun `triggerIfNeeded skips when both translations are cached`() {
        every { repository.cachedTranslationsFlow } returns MutableStateFlow(BibleTranslation.entries.toSet())

        val useCase = BibleAutoDownloadUseCase(scheduler, repository)
        useCase.triggerIfNeeded()

        verify(exactly = 0) { scheduler.enqueueWifiOnly(any(), any()) }
    }

    @Test
    fun `enqueueWifiOnly forwards replaceExisting and force to the scheduler`() {
        val useCase = BibleAutoDownloadUseCase(scheduler, repository)

        useCase.enqueueWifiOnly(replaceExisting = true, force = true)

        verify(exactly = 1) { scheduler.enqueueWifiOnly(replaceExisting = true, force = true) }
    }

    @Test
    fun `enqueueAnyNetwork forwards force to the scheduler`() {
        val useCase = BibleAutoDownloadUseCase(scheduler, repository)

        useCase.enqueueAnyNetwork(force = true)

        verify(exactly = 1) { scheduler.enqueueAnyNetwork(force = true) }
    }
}
