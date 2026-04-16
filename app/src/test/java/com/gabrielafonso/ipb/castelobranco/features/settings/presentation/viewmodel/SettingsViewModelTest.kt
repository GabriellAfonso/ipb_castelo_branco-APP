package com.ipb.castelobranco.features.settings.presentation.viewmodel

import com.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import com.ipb.castelobranco.features.settings.domain.model.ThemeMode
import com.ipb.castelobranco.features.settings.domain.repository.SettingsRepository
import app.cash.turbine.test
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var galleryRepository: GalleryRepository
    private lateinit var viewModel: SettingsViewModel

    private val themeModeFlow = MutableSharedFlow<ThemeMode>(replay = 1)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mockk(relaxed = true)
        galleryRepository = mockk(relaxed = true)
        every { settingsRepository.themeModeFlow } returns themeModeFlow
        viewModel = SettingsViewModel(settingsRepository, galleryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState initial value is default SettingsUiState before flow emits`() {
        assertEquals(SettingsUiState(), viewModel.uiState.value)
    }

    @Test
    fun `uiState reflects DARK when themeModeFlow emits DARK`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial SettingsUiState()
            themeModeFlow.emit(ThemeMode.DARK)
            val state = awaitItem()
            assertEquals(ThemeMode.DARK, state.themeMode)
            assertTrue(state.darkMode == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState reflects LIGHT when themeModeFlow emits LIGHT`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial SettingsUiState()
            themeModeFlow.emit(ThemeMode.LIGHT)
            val state = awaitItem()
            assertEquals(ThemeMode.LIGHT, state.themeMode)
            assertEquals(false, state.darkMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState reflects FOLLOW_SYSTEM with null darkMode when themeModeFlow emits FOLLOW_SYSTEM`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial SettingsUiState()
            themeModeFlow.emit(ThemeMode.DARK) // move away from default first
            awaitItem()
            themeModeFlow.emit(ThemeMode.FOLLOW_SYSTEM)
            val state = awaitItem()
            assertEquals(ThemeMode.FOLLOW_SYSTEM, state.themeMode)
            assertNull(state.darkMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearGallery delegates to galleryRepository clearAllPhotos`() = runTest {
        viewModel.clearGallery()
        advanceUntilIdle()

        coVerify(exactly = 1) { galleryRepository.clearAllPhotos() }
    }
}
