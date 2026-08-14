package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.CalendarDay
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.CalendarMonth
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.DateRange
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnOccurrence
import java.time.YearMonth
import javax.inject.Inject

/**
 * The rhythm of the church at a glance: one grid per month of the period, each day shaded by how
 * many occurrences fell on it.
 *
 * Intensity is scaled against the busiest day of the whole period, not of each month, so a quiet
 * month reads as quiet instead of being stretched to look busy.
 */
class BuildCalendarUseCase @Inject constructor() {

    operator fun invoke(
        occurrences: List<HymnOccurrence>,
        range: DateRange,
    ): List<CalendarMonth> {
        val countByDate = occurrences.groupingBy { it.occurredOn }.eachCount()
        val busiest = countByDate.values.maxOrNull() ?: 0

        val months = generateSequence(YearMonth.from(range.from)) { it.plusMonths(1) }
            .takeWhile { !it.isAfter(YearMonth.from(range.to)) }

        return months.map { month ->
            CalendarMonth(
                month = month,
                label = "${MONTH_NAMES[month.monthValue - 1]} de ${month.year}",
                days = (1..month.lengthOfMonth()).map { day ->
                    val date = month.atDay(day)
                    val count = countByDate[date] ?: 0
                    CalendarDay(
                        date = date,
                        occurrenceCount = count,
                        intensity = if (busiest == 0) 0f else count.toFloat() / busiest,
                    )
                },
            )
        }.toList()
    }

    private companion object {
        val MONTH_NAMES = listOf(
            "janeiro", "fevereiro", "março", "abril", "maio", "junho",
            "julho", "agosto", "setembro", "outubro", "novembro", "dezembro",
        )
    }
}
