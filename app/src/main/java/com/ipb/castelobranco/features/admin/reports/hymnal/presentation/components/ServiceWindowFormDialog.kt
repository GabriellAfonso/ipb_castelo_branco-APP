package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindowDraft
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase.ServiceWindowFields
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.util.WeekdayLabels
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.util.toHourMinute
import java.time.LocalTime

/**
 * Create and edit share one form.
 *
 * The weekday is a chip row rather than a free field, so an out-of-range weekday is impossible to
 * express; the name and the times are validated in the domain before anything is sent.
 */
@Composable
fun ServiceWindowFormDialog(
    draft: ServiceWindowDraft,
    fieldErrors: Map<String, String>,
    isSaving: Boolean,
    onDraftChanged: (ServiceWindowDraft) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (draft.id == null) "Novo culto" else "Editar culto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { onDraftChanged(draft.copy(name = it)) },
                    label = { Text("Nome") },
                    isError = fieldErrors.containsKey(ServiceWindowFields.NAME),
                    supportingText = {
                        fieldErrors[ServiceWindowFields.NAME]?.let { Text(it) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Dia da semana", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    WeekdayLabels.CALENDAR_ORDER.forEach { day ->
                        FilterChip(
                            selected = draft.weekday == day,
                            onClick = { onDraftChanged(draft.copy(weekday = day)) },
                            label = { Text(WeekdayLabels.short(day)) },
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextButton(onClick = { pickingStart = true }) {
                        Text("Início: ${draft.startTime.toHourMinute()}")
                    }
                    TextButton(onClick = { pickingEnd = true }) {
                        Text("Fim: ${draft.endTime.toHourMinute()}")
                    }
                }
                fieldErrors[ServiceWindowFields.END_TIME]?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
                fieldErrors[ServiceWindowFields.START_TIME]?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Ativo", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = draft.active,
                        onCheckedChange = { onDraftChanged(draft.copy(active = it)) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(enabled = !isSaving, onClick = onSave) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )

    if (pickingStart) {
        ServiceTimePickerDialog(
            initial = draft.startTime,
            onDismiss = { pickingStart = false },
            onConfirm = {
                onDraftChanged(draft.copy(startTime = it))
                pickingStart = false
            },
        )
    }

    if (pickingEnd) {
        ServiceTimePickerDialog(
            initial = draft.endTime,
            onDismiss = { pickingEnd = false },
            onConfirm = {
                onDraftChanged(draft.copy(endTime = it))
                pickingEnd = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceTimePickerDialog(
    initial: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = true,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text("OK")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Preview(showBackground = true)
@Composable
private fun ServiceWindowFormDialogPreview() {
    ServiceWindowFormDialog(
        draft = ServiceWindowDraft(name = "Culto de Oração"),
        fieldErrors = mapOf(
            ServiceWindowFields.END_TIME to "O horário de término precisa ser depois do de início.",
        ),
        isSaving = false,
        onDraftChanged = {},
        onDismiss = {},
        onSave = {},
    )
}
