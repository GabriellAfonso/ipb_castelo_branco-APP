package com.ipb.castelobranco.features.bible.domain.usecase

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ipb.castelobranco.features.bible.data.work.BibleDownloadWorker
import com.ipb.castelobranco.features.bible.domain.model.BibleTranslation
import com.ipb.castelobranco.features.bible.domain.repository.BibleRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

class BibleAutoDownloadUseCaseTest {

    private val workManager: WorkManager = mockk(relaxed = true)
    private val repository: BibleRepository = mockk(relaxed = true)

    @Test
    fun `triggerIfNeeded enqueues when cache is missing some translation`() {
        every { repository.cachedTranslationsFlow } returns MutableStateFlow(setOf(BibleTranslation.NAA))
        every { workManager.enqueueUniqueWork(any<String>(), any<ExistingWorkPolicy>(), any<OneTimeWorkRequest>()) } returns mockk(relaxed = true)

        val useCase = BibleAutoDownloadUseCase(workManager, repository)
        useCase.triggerIfNeeded()

        verify(exactly = 1) {
            workManager.enqueueUniqueWork(
                BibleDownloadWorker.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                any<OneTimeWorkRequest>(),
            )
        }
    }

    @Test
    fun `triggerIfNeeded skips when both translations are cached`() {
        every { repository.cachedTranslationsFlow } returns MutableStateFlow(BibleTranslation.entries.toSet())

        val useCase = BibleAutoDownloadUseCase(workManager, repository)
        useCase.triggerIfNeeded()

        verify(exactly = 0) {
            workManager.enqueueUniqueWork(any<String>(), any<ExistingWorkPolicy>(), any<OneTimeWorkRequest>())
        }
    }
}
