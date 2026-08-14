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
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ReportReading

/** Which reading of the slice is on screen. Changing it never fetches. */
@Composable
fun ReadingSelector(
    selected: ReportReading,
    onReadingSelected: (ReportReading) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReportReading.entries.forEach { reading ->
            FilterChip(
                selected = selected == reading,
                onClick = { onReadingSelected(reading) },
                label = { Text(reading.label()) },
            )
        }
    }
}

fun ReportReading.label(): String = when (this) {
    ReportReading.HIGHLIGHTS -> "Destaques"
    ReportReading.RANKING -> "Ranking"
    ReportReading.EVOLUTION -> "Evolução"
    ReportReading.SERVICES -> "Cultos"
    ReportReading.CALENDAR -> "Calendário"
    ReportReading.IN_VS_OUTSIDE -> "Culto × fora"
    ReportReading.COVERAGE -> "Cobertura"
}

@Preview(showBackground = true)
@Composable
private fun ReadingSelectorPreview() {
    ReadingSelector(
        selected = ReportReading.HIGHLIGHTS,
        onReadingSelected = {},
        modifier = Modifier.padding(vertical = 12.dp),
    )
}
