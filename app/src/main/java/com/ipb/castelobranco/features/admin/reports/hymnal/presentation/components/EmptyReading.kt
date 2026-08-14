package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ReportEmptyReason

/**
 * A reading with nothing to show says why, in words the domain chose.
 *
 * An empty chart looks like a bug and teaches the leadership nothing; the difference between
 * "there is no history at all", "not in this period" and "this service was not in force" is the
 * difference between waiting, changing the period, and fixing a setting.
 */
@Composable
fun EmptyReading(
    reason: ReportEmptyReason,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(20.dp),
    ) {
        Text(
            text = reason.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = reason.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyReadingPreview() {
    EmptyReading(
        reason = ReportEmptyReason.ServiceWithoutRecords("Culto de Domingo à Noite"),
        modifier = Modifier.padding(16.dp),
    )
}
