package com.ipb.castelobranco.features.settings.presentation.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import com.ipb.castelobranco.features.bible.domain.usecase.DeleteAndRedownloadBibleUseCase
import com.ipb.castelobranco.features.gallery.domain.repository.GalleryRepository
import com.ipb.castelobranco.features.settings.domain.model.ThemeMode
import com.ipb.castelobranco.features.settings.domain.repository.SettingsRepository
import app.cash.turbine.test
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
    private lateinit var deleteAndRedownloadBible: DeleteAndRedownloadBibleUseCase
    private lateinit var viewModel: SettingsViewModel

    private val themeModeFlow = MutableSharedFlow<ThemeMode>(replay = 1)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mockk(relaxed = true)
        galleryRepository = mockk(relaxed = true)
        deleteAndRedownloadBible = mockk(relaxed = true)
        every { settingsRepository.themeModeFlow } returns themeModeFlow
        viewModel = SettingsViewModel(settingsRepository, galleryRepository, deleteAndRedownloadBible)
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

    // region toggleDarkMode

    @Test
    fun `toggleDarkMode when current mode is DARK sets theme to LIGHT`() = runTest {
        mockkStatic(AppCompatDelegate::class)
        every { AppCompatDelegate.setDefaultNightMode(any()) } just Runs

        viewModel.uiState.test {
            awaitItem()
            themeModeFlow.emit(ThemeMode.DARK)
            awaitItem()

            viewModel.toggleDarkMode()
            advanceUntilIdle()

            coVerify { settingsRepository.setThemeMode(ThemeMode.LIGHT) }
            cancelAndIgnoreRemainingEvents()
        }

        unmockkStatic(AppCompatDelegate::class)
    }

    @Test
    fun `toggleDarkMode when current mode is LIGHT sets theme to DARK`() = runTest {
        mockkStatic(AppCompatDelegate::class)
        every { AppCompatDelegate.setDefaultNightMode(any()) } just Runs

        viewModel.uiState.test {
            awaitItem()
            themeModeFlow.emit(ThemeMode.LIGHT)
            awaitItem()

            viewModel.toggleDarkMode()
            advanceUntilIdle()

            coVerify { settingsRepository.setThemeMode(ThemeMode.DARK) }
            cancelAndIgnoreRemainingEvents()
        }

        unmockkStatic(AppCompatDelegate::class)
    }

    @Test
    fun `toggleDarkMode when FOLLOW_SYSTEM and system is dark sets theme to LIGHT`() = runTest {
        mockkStatic(AppCompatDelegate::class)
        every { AppCompatDelegate.getDefaultNightMode() } returns AppCompatDelegate.MODE_NIGHT_YES
        every { AppCompatDelegate.setDefaultNightMode(any()) } just Runs

        viewModel.toggleDarkMode()
        advanceUntilIdle()

        coVerify { settingsRepository.setThemeMode(ThemeMode.LIGHT) }

        unmockkStatic(AppCompatDelegate::class)
    }

    @Test
    fun `toggleDarkMode when FOLLOW_SYSTEM and system is light sets theme to DARK`() = runTest {
        mockkStatic(AppCompatDelegate::class)
        every { AppCompatDelegate.getDefaultNightMode() } returns AppCompatDelegate.MODE_NIGHT_NO
        every { AppCompatDelegate.setDefaultNightMode(any()) } just Runs

        viewModel.toggleDarkMode()
        advanceUntilIdle()

        coVerify { settingsRepository.setThemeMode(ThemeMode.DARK) }

        unmockkStatic(AppCompatDelegate::class)
    }

    @Test
    fun `toggleDarkMode emits event after successful toggle`() = runTest {
        mockkStatic(AppCompatDelegate::class)
        every { AppCompatDelegate.getDefaultNightMode() } returns AppCompatDelegate.MODE_NIGHT_NO
        every { AppCompatDelegate.setDefaultNightMode(any()) } just Runs

        viewModel.events.test {
            viewModel.toggleDarkMode()
            advanceUntilIdle()
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        unmockkStatic(AppCompatDelegate::class)
    }

    // endregion
}
