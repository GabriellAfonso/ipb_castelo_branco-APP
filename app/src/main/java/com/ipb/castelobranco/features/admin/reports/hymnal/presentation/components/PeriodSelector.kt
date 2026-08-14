package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ipb.castelobranco.core.presentation.components.DateFieldWithPicker
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ReportPeriod
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.util.toFullDate
import java.time.LocalDate

/**
 * The one selection that costs a request. Everything else on the report recomputes in memory,
 * which is why the period sits apart from the other two selectors.
 */
@Composable
fun PeriodSelector(
    selected: ReportPeriod,
    rangeLabel: String,
    rangeError: String?,
    onPeriodSelected: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PRESETS.forEach { (period, label) ->
                FilterChip(
                    selected = selected == period,
                    onClick = { onPeriodSelected(period) },
                    label = { Text(label) },
                )
            }
            FilterChip(
                selected = selected is ReportPeriod.Custom,
                onClick = { showCustomDialog = true },
                label = { Text(CUSTOM_LABEL) },
            )
        }

        if (rangeLabel.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = rangeLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (rangeError != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = rangeError,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }

    if (showCustomDialog) {
        CustomRangeDialog(
            initial = selected as? ReportPeriod.Custom,
            onDismiss = { showCustomDialog = false },
            onConfirm = { from, to ->
                showCustomDialog = false
                onPeriodSelected(ReportPeriod.Custom(from = from, to = to))
            },
        )
    }
}

/**
 * Two dates and nothing more. The range rules — start before end, at most a year — are checked in
 * the domain and reported on the selector, so an impossible range never becomes a request.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRangeDialog(
    initial: ReportPeriod.Custom?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit,
) {
    var from by remember { mutableStateOf(initial?.from) }
    var to by remember { mutableStateOf(initial?.to) }
    var picking by remember { mutableStateOf<PickTarget?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Período personalizado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Início", style = MaterialTheme.typography.labelMedium)
                DateFieldWithPicker(
                    dateBr = from?.toFullDate().orEmpty(),
                    onOpenPicker = { picking = PickTarget.FROM },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Fim", style = MaterialTheme.typography.labelMedium)
                DateFieldWithPicker(
                    dateBr = to?.toFullDate().orEmpty(),
                    onOpenPicker = { picking = PickTarget.TO },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = from != null && to != null,
                onClick = {
                    val start = from ?: return@TextButton
                    val end = to ?: return@TextButton
                    onConfirm(start, end)
                },
            ) { Text("Aplicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )

    picking?.let { target ->
        ReportDatePickerDialog(
            initialDate = if (target == PickTarget.FROM) from else to,
            onDismiss = { picking = null },
            onConfirm = { date ->
                if (target == PickTarget.FROM) from = date else to = date
                picking = null
            },
        )
    }
}

private enum class PickTarget { FROM, TO }

private val PRESETS: List<Pair<ReportPeriod, String>> = listOf(
    ReportPeriod.ThisWeek to "Esta semana",
    ReportPeriod.ThisMonth to "Este mês",
    ReportPeriod.ThisYear to "Este ano",
)

private const val CUSTOM_LABEL = "Personalizado"

@Preview(showBackground = true)
@Composable
private fun PeriodSelectorPreview() {
    PeriodSelector(
        selected = ReportPeriod.ThisMonth,
        rangeLabel = "01/08/2026 – 16/08/2026",
        rangeError = null,
        onPeriodSelected = {},
        modifier = Modifier.padding(vertical = 12.dp),
    )
}
