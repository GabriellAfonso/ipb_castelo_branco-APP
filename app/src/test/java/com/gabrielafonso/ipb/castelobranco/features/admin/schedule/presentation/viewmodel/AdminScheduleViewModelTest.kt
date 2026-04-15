package com.ipb.castelobranco.features.admin.schedule.presentation.viewmodel

import com.ipb.castelobranco.features.admin.schedule.domain.model.Member
import com.ipb.castelobranco.features.admin.schedule.domain.model.ScheduleItem
import com.ipb.castelobranco.features.admin.schedule.domain.repository.AdminScheduleRepository
import com.ipb.castelobranco.features.admin.schedule.presentation.state.AdminScheduleEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AdminScheduleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: AdminScheduleRepository
    private lateinit var viewModel: AdminScheduleViewModel

    private val member1 = Member(id = 1, name = "João")
    private val member2 = Member(id = 2, name = "Maria")

    private val scheduleItem = ScheduleItem(
        date = "2026-04-01",
        day = 1,
        scheduleTypeName = "Terça de Oração",
        scheduleTypeId = 1,
        selectedMember = member1
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = AdminScheduleViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region generateSchedule

    @Test
    fun `generateSchedule success maps ScheduleItem to EditableScheduleUiState in UI state`() = runTest {
        coEvery { repository.generateSchedule(any(), any()) } returns Result.success(listOf(scheduleItem))

        viewModel.onEvent(AdminScheduleEvent.GenerateSchedule)
        advanceUntilIdle()

        val items = viewModel.uiState.value.items
        assertEquals(1, items.size)
        with(items[0]) {
            assertEquals("2026-04-01", date)
            assertEquals(1, day)
            assertEquals("Terça de Oração", scheduleTypeName)
            assertEquals(1, scheduleTypeId)
            assertEquals(member1, selectedMember)
        }
    }

    @Test
    fun `generateSchedule success clears isGenerating flag`() = runTest {
        coEvery { repository.generateSchedule(any(), any()) } returns Result.success(emptyList())

        viewModel.onEvent(AdminScheduleEvent.GenerateSchedule)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isGenerating)
    }

    @Test
    fun `generateSchedule failure sets snackbar message and clears isGenerating`() = runTest {
        coEvery { repository.generateSchedule(any(), any()) } returns
            Result.failure(Exception("server error"))

        viewModel.onEvent(AdminScheduleEvent.GenerateSchedule)
        advanceUntilIdle()

        assertEquals("Falha ao gerar escala.", viewModel.uiState.value.snackbarMessage)
        assertFalse(viewModel.uiState.value.isGenerating)
    }

    @Test
    fun `generateSchedule uses current year and month from UI state`() = runTest {
        coEvery { repository.generateSchedule(any(), any()) } returns Result.success(emptyList())
        viewModel.onEvent(AdminScheduleEvent.MonthChanged(2027, 6))

        viewModel.onEvent(AdminScheduleEvent.GenerateSchedule)
        advanceUntilIdle()

        coVerify { repository.generateSchedule(year = 2027, month = 6) }
    }

    @Test
    fun `generateSchedule maps multiple items preserving order`() = runTest {
        val item2 = ScheduleItem("2026-04-08", 8, "Quinta de Oração", 2, member2)
        coEvery { repository.generateSchedule(any(), any()) } returns
            Result.success(listOf(scheduleItem, item2))

        viewModel.onEvent(AdminScheduleEvent.GenerateSchedule)
        advanceUntilIdle()

        val items = viewModel.uiState.value.items
        assertEquals(2, items.size)
        assertEquals("2026-04-01", items[0].date)
        assertEquals("2026-04-08", items[1].date)
    }

    // endregion

    // region saveSchedule

    @Test
    fun `saveSchedule calls repository with ScheduleItem converted from UI state items`() = runTest {
        coEvery { repository.generateSchedule(any(), any()) } returns Result.success(listOf(scheduleItem))
        coEvery { repository.saveSchedule(any(), any(), any()) } returns Result.success(Unit)

        viewModel.onEvent(AdminScheduleEvent.GenerateSchedule)
        advanceUntilIdle()
        viewModel.onEvent(AdminScheduleEvent.SaveSchedule)
        advanceUntilIdle()

        coVerify {
            repository.saveSchedule(
                year = any(),
                month = any(),
                items = match { items ->
                    items.size == 1 &&
                        items[0].date == "2026-04-01" &&
                        items[0].scheduleTypeId == 1 &&
                        items[0].selectedMember == member1
                }
            )
        }
    }

    @Test
    fun `saveSchedule success sets success snackbar message and clears isSaving`() = runTest {
        coEvery { repository.generateSchedule(any(), any()) } returns Result.success(listOf(scheduleItem))
        coEvery { repository.saveSchedule(any(), any(), any()) } returns Result.success(Unit)

        viewModel.onEvent(AdminScheduleEvent.GenerateSchedule)
        advanceUntilIdle()
        viewModel.onEvent(AdminScheduleEvent.SaveSchedule)
        advanceUntilIdle()

        assertEquals("Escala salva com sucesso.", viewModel.uiState.value.snackbarMessage)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `saveSchedule failure sets error message from exception`() = runTest {
        coEvery { repository.generateSchedule(any(), any()) } returns Result.success(listOf(scheduleItem))
        coEvery { repository.saveSchedule(any(), any(), any()) } returns
            Result.failure(Exception("Escala já existe"))

        viewModel.onEvent(AdminScheduleEvent.GenerateSchedule)
        advanceUntilIdle()
        viewModel.onEvent(AdminScheduleEvent.SaveSchedule)
        advanceUntilIdle()

        assertEquals("Escala já existe", viewModel.uiState.value.snackbarMessage)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `saveSchedule failure without message falls back to default message`() = runTest {
        coEvery { repository.generateSchedule(any(), any()) } returns Result.success(listOf(scheduleItem))
        coEvery { repository.saveSchedule(any(), any(), any()) } returns
            Result.failure(Exception())

        viewModel.onEvent(AdminScheduleEvent.GenerateSchedule)
        advanceUntilIdle()
        viewModel.onEvent(AdminScheduleEvent.SaveSchedule)
        advanceUntilIdle()

        assertEquals("Falha ao salvar escala.", viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `saveSchedule does not call repository when items list is empty`() = runTest {
        viewModel.onEvent(AdminScheduleEvent.SaveSchedule)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.saveSchedule(any(), any(), any()) }
    }

    @Test
    fun `saveSchedule does not call repository when any item has null selectedMember`() = runTest {
        val itemWithoutMember = scheduleItem.copy(selectedMember = null)
        coEvery { repository.generateSchedule(any(), any()) } returns
            Result.success(listOf(itemWithoutMember))

        viewModel.onEvent(AdminScheduleEvent.GenerateSchedule)
        advanceUntilIdle()
        viewModel.onEvent(AdminScheduleEvent.SaveSchedule)
        advanceUntilIdle()

        coVerify(exactly = 0) { repository.saveSchedule(any(), any(), any()) }
    }

    // endregion

    // region selectMember

    @Test
    fun `selectMember updates selectedMember on the target item only`() = runTest {
        val item2 = scheduleItem.copy(date = "2026-04-08", day = 8, selectedMember = null)
        coEvery { repository.generateSchedule(any(), any()) } returns
            Result.success(listOf(scheduleItem, item2))

        viewModel.onEvent(AdminScheduleEvent.GenerateSchedule)
        advanceUntilIdle()
        viewModel.onEvent(AdminScheduleEvent.MemberSelected(itemIndex = 1, member = member2))

        val items = viewModel.uiState.value.items
        assertEquals(member1, items[0].selectedMember) // unchanged
        assertEquals(member2, items[1].selectedMember) // updated
    }

    @Test
    fun `selectMember on first item does not affect other items`() = runTest {
        val item2 = scheduleItem.copy(date = "2026-04-08", selectedMember = member2)
        coEvery { repository.generateSchedule(any(), any()) } returns
            Result.success(listOf(scheduleItem, item2))

        viewModel.onEvent(AdminScheduleEvent.GenerateSchedule)
        advanceUntilIdle()
        viewModel.onEvent(AdminScheduleEvent.MemberSelected(itemIndex = 0, member = member2))

        assertEquals(member2, viewModel.uiState.value.items[0].selectedMember)
        assertEquals(member2, viewModel.uiState.value.items[1].selectedMember) // unchanged
    }

    // endregion

    // region changeMonth

    @Test
    fun `changeMonth updates year and month in UI state`() = runTest {
        viewModel.onEvent(AdminScheduleEvent.MonthChanged(2027, 3))

        assertEquals(2027, viewModel.uiState.value.year)
        assertEquals(3, viewModel.uiState.value.month)
    }

    @Test
    fun `changeMonth clears items list`() = runTest {
        coEvery { repository.generateSchedule(any(), any()) } returns Result.success(listOf(scheduleItem))
        viewModel.onEvent(AdminScheduleEvent.GenerateSchedule)
        advanceUntilIdle()

        viewModel.onEvent(AdminScheduleEvent.MonthChanged(2027, 3))

        assertTrue(viewModel.uiState.value.items.isEmpty())
    }

    // endregion

    // region loadMembers

    @Test
    fun `loadMembers success updates members and clears isLoadingMembers`() = runTest {
        coEvery { repository.getMembers() } returns Result.success(listOf(member1, member2))

        viewModel.onEvent(AdminScheduleEvent.LoadMembers)
        advanceUntilIdle()

        assertEquals(listOf(member1, member2), viewModel.uiState.value.members)
        assertFalse(viewModel.uiState.value.isLoadingMembers)
    }

    @Test
    fun `loadMembers failure sets snackbar message and clears isLoadingMembers`() = runTest {
        coEvery { repository.getMembers() } returns Result.failure(Exception("error"))

        viewModel.onEvent(AdminScheduleEvent.LoadMembers)
        advanceUntilIdle()

        assertEquals("Falha ao carregar membros.", viewModel.uiState.value.snackbarMessage)
        assertFalse(viewModel.uiState.value.isLoadingMembers)
    }

    @Test
    fun `loadMembers does not call repository when members already loaded`() = runTest {
        coEvery { repository.getMembers() } returns Result.success(listOf(member1))
        viewModel.onEvent(AdminScheduleEvent.LoadMembers)
        advanceUntilIdle()

        viewModel.onEvent(AdminScheduleEvent.LoadMembers)
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.getMembers() }
    }

    // endregion

    // region snackbarShown

    @Test
    fun `SnackbarShown clears snackbar message`() = runTest {
        coEvery { repository.generateSchedule(any(), any()) } returns
            Result.failure(Exception("error"))
        viewModel.onEvent(AdminScheduleEvent.GenerateSchedule)
        advanceUntilIdle()

        viewModel.onEvent(AdminScheduleEvent.SnackbarShown)

        assertNull(viewModel.uiState.value.snackbarMessage)
    }

    // endregion
}
