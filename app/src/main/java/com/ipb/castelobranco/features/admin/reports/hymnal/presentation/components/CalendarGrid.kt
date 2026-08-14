package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ipb.castelobranco.core.presentation.theme.BrandColors
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.CalendarDay
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.CalendarMonth
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.util.WeekdayLabels
import java.time.LocalDate
import java.time.YearMonth

/**
 * A month of days shaded by how much the hymnal was used, so the rhythm of the church — dense
 * Sundays, thin midweek, dead weeks — is visible at a glance.
 *
 * Deliberately not a chart: a grid of boxes with varying opacity says the same thing with none of
 * the machinery.
 */
@Composable
fun CalendarGrid(
    month: CalendarMonth,
    modifier: Modifier = Modifier,
    onDayClick: (LocalDate) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = month.label.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            WeekdayLabels.CALENDAR_ORDER.forEach { day ->
                Text(
                    text = WeekdayLabels.short(day),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Sunday-first weeks, with the leading blanks so the first day lands on its column.
        val leadingBlanks = month.days.first().date.dayOfWeek.value % WEEK_LENGTH
        val cells = List(leadingBlanks) { null } + month.days

        cells.chunked(WEEK_LENGTH).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                week.forEach { day ->
                    DayCell(
                        day = day,
                        onDayClick = onDayClick,
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(WEEK_LENGTH - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay?,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (day == null) {
        Spacer(modifier = modifier)
        return
    }

    val hasRecords = day.occurrenceCount > 0
    val background = if (hasRecords) {
        BrandColors.Green.copy(alpha = MIN_ALPHA + day.intensity * (1f - MIN_ALPHA))
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .clickable(enabled = hasRecords) { onDayClick(day.date) },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (day.intensity > CONTRAST_FLIP) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            },
        )
    }
}

private const val WEEK_LENGTH = 7
private const val MIN_ALPHA = 0.15f
private const val CONTRAST_FLIP = 0.55f

@Preview(showBackground = true)
@Composable
private fun CalendarGridPreview() {
    val month = YearMonth.of(2026, 8)
    CalendarGrid(
        month = CalendarMonth(
            month = month,
            label = "agosto de 2026",
            days = (1..month.lengthOfMonth()).map { day ->
                val date = month.atDay(day)
                val count = if (date.dayOfWeek.value == 7) 6 else day % 3
                CalendarDay(date = date, occurrenceCount = count, intensity = count / 6f)
            },
        ),
        modifier = Modifier.padding(16.dp),
    )
}
