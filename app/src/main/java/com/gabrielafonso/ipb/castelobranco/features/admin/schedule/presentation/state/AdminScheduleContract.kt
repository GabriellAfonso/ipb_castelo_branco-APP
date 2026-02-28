package com.gabrielafonso.ipb.castelobranco.features.admin.schedule.presentation.state

import androidx.compose.runtime.Immutable
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.domain.model.Member
import java.time.LocalDate

@Immutable
data class EditableScheduleItem(
    val date: String,
    val day: Int,
    val scheduleTypeName: String,
    val scheduleTypeId: Int = 0,   // <─ novo campo
    val selectedMember: Member? = null,
    val memberQuery: String = ""
)
@Immutable
data class AdminScheduleUiState(
    val year: Int = LocalDate.now().year,
    val month: Int = LocalDate.now().plusMonths(1).monthValue,
    val members: List<Member> = emptyList(),
    val isLoadingMembers: Boolean = false,
    val items: List<EditableScheduleItem> = emptyList(),
    val isGenerating: Boolean = false,
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
) {
    val canSave: Boolean
        get() = items.isNotEmpty() && items.all { it.selectedMember != null }

    val monthLabel: String
        get() = MONTHS[month - 1] + " $year"

    companion object {
        private val MONTHS = listOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )
    }
}

sealed interface AdminScheduleEvent {
    data object LoadMembers : AdminScheduleEvent
    data class MonthChanged(val year: Int, val month: Int) : AdminScheduleEvent
    data class MemberQueryChanged(val itemIndex: Int, val query: String) : AdminScheduleEvent
    data class MemberSelected(val itemIndex: Int, val member: Member) : AdminScheduleEvent
    data object GenerateSchedule : AdminScheduleEvent
    data object SaveSchedule : AdminScheduleEvent
    data object SnackbarShown : AdminScheduleEvent
}
