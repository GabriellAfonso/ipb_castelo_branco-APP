package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ReportSlice
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.util.WeekdayLabels

/**
 * Which occurrences of the loaded period are counted. Changing it never fetches.
 *
 * Only **active** services are offered: a deactivated one would produce an empty slice that looks
 * like the congregation stopped singing.
 */
@Composable
fun SliceSelector(
    selected: ReportSlice,
    services: List<ReportSlice.Service>,
    onSliceSelected: (ReportSlice) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == ReportSlice.All,
            onClick = { onSliceSelected(ReportSlice.All) },
            label = { Text("Todas") },
        )

        services.forEach { service ->
            FilterChip(
                selected = selected == service,
                onClick = { onSliceSelected(service) },
                label = { Text(service.name) },
            )
        }

        FilterChip(
            selected = selected == ReportSlice.OutsideService,
            onClick = { onSliceSelected(ReportSlice.OutsideService) },
            label = { Text("Fora do culto") },
        )

        WeekdayLabels.CALENDAR_ORDER.forEach { day ->
            val slice = ReportSlice.Weekday(day)
            FilterChip(
                selected = selected == slice,
                onClick = { onSliceSelected(slice) },
                label = { Text(WeekdayLabels.short(day)) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SliceSelectorPreview() {
    SliceSelector(
        selected = ReportSlice.All,
        services = listOf(ReportSlice.Service(id = 3, name = "Culto de Domingo à Noite")),
        onSliceSelected = {},
        modifier = Modifier.padding(vertical = 12.dp),
    )
}
