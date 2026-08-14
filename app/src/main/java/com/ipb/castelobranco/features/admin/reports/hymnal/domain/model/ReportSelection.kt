package com.ipb.castelobranco.features.admin.reports.hymnal.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * The three independent choices an administrator makes over one report.
 *
 * Only [ReportPeriod] costs a request. [ReportSlice] and [ReportReading] are recomputed over
 * occurrences already in memory — that is the whole design of this feature.
 */

/** Which stretch of time the report covers. */
sealed interface ReportPeriod {
    data object ThisWeek : ReportPeriod
    data object ThisMonth : ReportPeriod
    data object ThisYear : ReportPeriod
    data class Custom(val from: LocalDate, val to: LocalDate) : ReportPeriod
}

/**
 * Which occurrences of that period are counted.
 *
 * [Service] and [Weekday] are independent filters, both applied on loaded data.
 * [OutsideService] is not "no filter" — it is the occurrences that fell outside every active
 * service, which is the reading the church never had.
 */
sealed interface ReportSlice {
    data object All : ReportSlice
    data class Service(val id: Int, val name: String) : ReportSlice
    data object OutsideService : ReportSlice
    data class Weekday(val day: DayOfWeek) : ReportSlice
}

/** Which reading of that slice is on screen. */
enum class ReportReading {
    HIGHLIGHTS,
    RANKING,
    EVOLUTION,
    SERVICES,
    CALENDAR,
    IN_VS_OUTSIDE,
    COVERAGE,
}

/**
 * The bucket granularity the evolution chart uses, and the `group_by` the request carries.
 *
 * The service also accepts `service`, but the app never asks for it: `group_by` changes only the
 * bucket label, and the services reading groups by date plus service window on its own.
 */
enum class BucketGranularity(val wireValue: String) {
    DAY("day"),
    WEEK("week"),
    MONTH("month"),
}
