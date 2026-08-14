package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Portuguese labels for the week and the short date formats the report uses.
 *
 * The conversion between the service's weekday integer and [DayOfWeek] happens in
 * `ServiceWindowMapper`, never here: this file only names a day the domain already resolved.
 */
object WeekdayLabels {

    private val FULL = mapOf(
        DayOfWeek.MONDAY to "Segunda-feira",
        DayOfWeek.TUESDAY to "Terça-feira",
        DayOfWeek.WEDNESDAY to "Quarta-feira",
        DayOfWeek.THURSDAY to "Quinta-feira",
        DayOfWeek.FRIDAY to "Sexta-feira",
        DayOfWeek.SATURDAY to "Sábado",
        DayOfWeek.SUNDAY to "Domingo",
    )

    private val SHORT = mapOf(
        DayOfWeek.MONDAY to "Seg",
        DayOfWeek.TUESDAY to "Ter",
        DayOfWeek.WEDNESDAY to "Qua",
        DayOfWeek.THURSDAY to "Qui",
        DayOfWeek.FRIDAY to "Sex",
        DayOfWeek.SATURDAY to "Sáb",
        DayOfWeek.SUNDAY to "Dom",
    )

    /** Week order as the calendar grid draws it, Sunday first as Brazilian calendars do. */
    val CALENDAR_ORDER: List<DayOfWeek> = listOf(
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
    )

    fun full(day: DayOfWeek): String = FULL.getValue(day)

    fun short(day: DayOfWeek): String = SHORT.getValue(day)
}

private val DAY_MONTH = DateTimeFormatter.ofPattern("dd/MM")
private val FULL_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun LocalDate.toDayMonth(): String = format(DAY_MONTH)

fun LocalDate.toFullDate(): String = format(FULL_DATE)

fun LocalTime.toHourMinute(): String = "%02d:%02d".format(hour, minute)
