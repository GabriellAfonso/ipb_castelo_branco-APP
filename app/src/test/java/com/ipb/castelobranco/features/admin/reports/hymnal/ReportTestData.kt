package com.ipb.castelobranco.features.admin.reports.hymnal

import com.ipb.castelobranco.core.domain.model.HymnCatalogEntry
import com.ipb.castelobranco.core.domain.repository.HymnCatalogRepository
import com.ipb.castelobranco.core.domain.snapshot.SnapshotState
import com.ipb.castelobranco.core.domain.util.DateProvider
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BucketGranularity
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.CollectionSettings
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.DateRange
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnOccurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.OccurrenceReport
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindow
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindowDraft
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.TopHymn
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.repository.HymnalHistoryAdminRepository
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.repository.HymnalReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Shared builders and fakes. Every date assertion in this feature is deterministic because the
 * clock is a parameter, never `LocalDate.now()`.
 */

/** A Sunday, so weekday slices and service windows have something obvious to match. */
val TODAY: LocalDate = LocalDate.of(2026, 8, 16)

fun fixedDateProvider(today: LocalDate = TODAY): DateProvider = DateProvider { today }

fun occurrence(
    number: String = "50",
    title: String = "Grandioso És Tu",
    on: LocalDate = TODAY,
    serviceWindowId: Int? = SUNDAY_NIGHT_ID,
    serviceWindowName: String? = SUNDAY_NIGHT_NAME,
    deviceCount: Int = 10,
    bucket: String = "$on:${serviceWindowId ?: "none"}",
): HymnOccurrence = HymnOccurrence(
    hymnNumber = number,
    hymnTitle = title,
    occurredOn = on,
    serviceWindowId = serviceWindowId,
    serviceWindowName = serviceWindowName,
    bucket = bucket,
    deviceCount = deviceCount,
)

fun outsideOccurrence(
    number: String = "120",
    title: String = "Saudosa Lembrança",
    on: LocalDate = TODAY.minusDays(3),
    deviceCount: Int = 2,
): HymnOccurrence = occurrence(
    number = number,
    title = title,
    on = on,
    serviceWindowId = null,
    serviceWindowName = null,
    deviceCount = deviceCount,
)

fun report(
    occurrences: List<HymnOccurrence>,
    range: DateRange = DateRange(TODAY.minusDays(29), TODAY),
    granularity: BucketGranularity = BucketGranularity.DAY,
): OccurrenceReport = OccurrenceReport(
    range = range,
    granularity = granularity,
    occurrences = occurrences,
)

fun serviceWindow(
    id: Int = SUNDAY_NIGHT_ID,
    name: String = SUNDAY_NIGHT_NAME,
    weekday: DayOfWeek = DayOfWeek.SUNDAY,
    start: LocalTime = LocalTime.of(19, 0),
    end: LocalTime = LocalTime.of(21, 0),
    active: Boolean = true,
): ServiceWindow = ServiceWindow(id, name, weekday, start, end, active)

fun defaultSettings(): CollectionSettings = CollectionSettings(
    minSecondsToCount = 30,
    collapseWindowMinutes = 10,
    maxBatchSize = 200,
    maxPastDays = 90,
    futureToleranceMinutes = 5,
    windowGraceMinutes = 30,
)

const val SUNDAY_NIGHT_ID = 3
const val SUNDAY_NIGHT_NAME = "Culto de Domingo à Noite"

/**
 * Answers with whatever was staged for the requested range, and counts calls so a test can prove
 * that changing a slice or a reading never reaches the network.
 */
class FakeHymnalReportRepository(
    var occurrencesResult: (DateRange) -> Result<OccurrenceReport> = {
        Result.success(report(emptyList(), it))
    },
    var topHymnsResult: Result<List<TopHymn>> = Result.success(emptyList()),
) : HymnalReportRepository {

    val requestedRanges = mutableListOf<DateRange>()
    var topHymnsCalls = 0
        private set

    override suspend fun getOccurrences(
        range: DateRange,
        granularity: BucketGranularity,
    ): Result<OccurrenceReport> {
        requestedRanges += range
        return occurrencesResult(range)
    }

    override suspend fun getAllTimeTopHymns(): Result<List<TopHymn>> {
        topHymnsCalls++
        return topHymnsResult
    }
}

class FakeHymnalHistoryAdminRepository(
    var settingsResult: Result<CollectionSettings> = Result.success(defaultSettings()),
    var windowsResult: Result<List<ServiceWindow>> = Result.success(listOf(serviceWindow())),
) : HymnalHistoryAdminRepository {

    var savedDraft: ServiceWindowDraft? = null
        private set
    var deletedId: Int? = null
        private set
    var patched: Pair<CollectionSettings, CollectionSettings>? = null
        private set
    var saveResult: Result<ServiceWindow>? = null
    var deleteResult: Result<Unit> = Result.success(Unit)
    var updateResult: Result<CollectionSettings>? = null

    override suspend fun getSettings(): Result<CollectionSettings> = settingsResult

    override suspend fun updateSettings(
        current: CollectionSettings,
        updated: CollectionSettings,
    ): Result<CollectionSettings> {
        patched = current to updated
        return updateResult ?: Result.success(updated)
    }

    override suspend fun getServiceWindows(): Result<List<ServiceWindow>> = windowsResult

    override suspend fun saveServiceWindow(draft: ServiceWindowDraft): Result<ServiceWindow> {
        savedDraft = draft
        return saveResult ?: Result.success(
            serviceWindow(
                id = draft.id ?: 99,
                name = draft.name,
                weekday = draft.weekday,
                start = draft.startTime,
                end = draft.endTime,
                active = draft.active,
            )
        )
    }

    override suspend fun deleteServiceWindow(id: Int): Result<Unit> {
        deletedId = id
        return deleteResult
    }
}

class FakeHymnCatalogRepository(
    private val state: SnapshotState<List<HymnCatalogEntry>>,
) : HymnCatalogRepository {

    constructor(entries: List<HymnCatalogEntry>) : this(SnapshotState.Data(entries))

    override fun observeHymnCatalog(): Flow<SnapshotState<List<HymnCatalogEntry>>> = flowOf(state)
}

fun catalog(vararg numbers: String): List<HymnCatalogEntry> =
    numbers.map { HymnCatalogEntry(number = it, title = "Hino $it") }
