package com.ipb.castelobranco.features.admin.reports.hymnal.data.mapper

import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.ServiceWindowDto
import com.ipb.castelobranco.features.admin.reports.hymnal.data.dto.ServiceWindowWriteDto
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindow
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindowDraft
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * The one place the two weekday conventions meet.
 *
 * The service uses Python's `datetime.weekday()`: **`0 = Monday … 6 = Sunday`. Sunday is 6, not
 * 0.** `java.time.DayOfWeek` uses `1 = Monday … 7 = Sunday`. Treating `0` as Sunday rotates the
 * whole week and mislabels every service, which is why the conversion lives here alone and is
 * covered by a round-trip test over all seven values.
 */
private const val WEEKDAY_WIRE_OFFSET = 1

/** `"19:00:00"` on read, `"19:00"` accepted on write — both parse. */
private const val TIME_SECONDS_LENGTH = 8

fun Int.toDayOfWeek(): DayOfWeek = DayOfWeek.of(this + WEEKDAY_WIRE_OFFSET)

fun DayOfWeek.toWireWeekday(): Int = value - WEEKDAY_WIRE_OFFSET

fun ServiceWindowDto.toDomain(): ServiceWindow = ServiceWindow(
    id = id,
    name = name,
    weekday = weekday.toDayOfWeek(),
    startTime = parseTime(startTime),
    endTime = parseTime(endTime),
    active = active,
)

fun ServiceWindowDraft.toWriteDto(): ServiceWindowWriteDto = ServiceWindowWriteDto(
    name = name.trim(),
    weekday = weekday.toWireWeekday(),
    startTime = formatTime(startTime),
    endTime = formatTime(endTime),
    active = active,
)

fun ServiceWindow.toDraft(): ServiceWindowDraft = ServiceWindowDraft(
    id = id,
    name = name,
    weekday = weekday,
    startTime = startTime,
    endTime = endTime,
    active = active,
)

/** Tolerates both `"19:00"` and `"19:00:00"` — the service returns seconds, its writes do not. */
internal fun parseTime(raw: String): LocalTime =
    if (raw.length >= TIME_SECONDS_LENGTH) LocalTime.parse(raw.take(TIME_SECONDS_LENGTH))
    else LocalTime.parse(raw)

internal fun formatTime(time: LocalTime): String =
    "%02d:%02d".format(time.hour, time.minute)
