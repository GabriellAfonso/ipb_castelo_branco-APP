package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.viewmodel

import com.ipb.castelobranco.core.domain.error.AppError
import com.ipb.castelobranco.features.admin.reports.hymnal.FakeHymnalHistoryAdminRepository
import com.ipb.castelobranco.features.admin.reports.hymnal.defaultSettings
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.SettingField
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase.GetCollectionSettingsUseCase
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase.UpdateCollectionSettingsUseCase
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase.ValidateCollectionSettingsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeHymnalHistoryAdminRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeHymnalHistoryAdminRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = CollectionSettingsViewModel(
        getSettings = GetCollectionSettingsUseCase(repository),
        updateSettings = UpdateCollectionSettingsUseCase(repository),
        validate = ValidateCollectionSettingsUseCase(),
    )

    @Test
    fun `loading fills every field with the current value`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals("30", viewModel.uiState.value.values[SettingField.MIN_SECONDS_TO_COUNT])
        assertEquals("200", viewModel.uiState.value.values[SettingField.MAX_BATCH_SIZE])
    }

    @Test
    fun `a failed load surfaces a message`() = runTest {
        repository.settingsResult = Result.failure(AppError.Server(code = 500))

        val viewModel = viewModel()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)
    }

    @Test
    fun `an out-of-range value is refused locally and issues no request`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onValueChanged(SettingField.MIN_SECONDS_TO_COUNT, "0")
        viewModel.onSave()
        advanceUntilIdle()

        assertEquals(
            "Informe um número entre 1 e 3600.",
            viewModel.uiState.value.fieldErrors[SettingField.MIN_SECONDS_TO_COUNT],
        )
        assertNull(repository.patched)
    }

    @Test
    fun `saving sends the loaded baseline so the patch stays partial`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onValueChanged(SettingField.MIN_SECONDS_TO_COUNT, "45")
        viewModel.onSave()
        advanceUntilIdle()

        val (current, updated) = requireNotNull(repository.patched)
        assertEquals(defaultSettings(), current)
        assertEquals(45, updated.minSecondsToCount)
        assertEquals(defaultSettings().maxBatchSize, updated.maxBatchSize)
    }

    @Test
    fun `a server field error lands on the field the service named`() = runTest {
        repository.updateResult = Result.failure(
            AppError.Server(
                code = 400,
                errorCode = "VALIDATION_ERROR",
                fieldErrors = mapOf(
                    "window_grace_minutes" to listOf("Value 0 is out of range."),
                ),
            )
        )

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onValueChanged(SettingField.WINDOW_GRACE_MINUTES, "45")
        viewModel.onSave()
        advanceUntilIdle()

        assertEquals(
            "Value 0 is out of range.",
            viewModel.uiState.value.fieldErrors[SettingField.WINDOW_GRACE_MINUTES],
        )
    }

    @Test
    fun `a field error key the app does not know falls back to the generic message`() = runTest {
        repository.updateResult = Result.failure(
            AppError.Server(
                code = 400,
                errorCode = "VALIDATION_ERROR",
                fieldErrors = mapOf("campo_novo" to listOf("Algo mudou no servidor.")),
            )
        )

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onValueChanged(SettingField.MAX_PAST_DAYS, "120")
        viewModel.onSave()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.fieldErrors.isEmpty())
    }

    @Test
    fun `editing marks the form dirty and clears that field's error`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onValueChanged(SettingField.MAX_PAST_DAYS, "abc")
        viewModel.onSave()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.fieldErrors[SettingField.MAX_PAST_DAYS])

        viewModel.onValueChanged(SettingField.MAX_PAST_DAYS, "120")

        assertNull(viewModel.uiState.value.fieldErrors[SettingField.MAX_PAST_DAYS])
        assertTrue(viewModel.uiState.value.isDirty)
    }
}
