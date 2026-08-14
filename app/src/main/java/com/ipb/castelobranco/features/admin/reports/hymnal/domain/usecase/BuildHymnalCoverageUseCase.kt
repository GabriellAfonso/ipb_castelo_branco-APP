package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.core.domain.model.HymnCatalogEntry
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.CatalogHymn
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ForgottenHymn
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnOccurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnalCoverage
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.TopHymn
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * What the church has never sung, and what it has stopped singing.
 *
 * Two sources feed two different facts, never one number:
 *
 * - **Never sung** is the local catalogue minus the all-time ranking. The service cannot compute
 *   it — hymns with no occurrence simply do not appear in its response — so filling the gap is
 *   the client's job.
 * - **Last sung** comes from the occurrences the app can still see, which reach back one year at
 *   most. A hymn that was sung before that has no date here, and the text says exactly that
 *   instead of inventing precision.
 */
class BuildHymnalCoverageUseCase @Inject constructor() {

    operator fun invoke(
        catalog: List<HymnCatalogEntry>,
        allTime: List<TopHymn>,
        visibleWindow: List<HymnOccurrence>,
    ): HymnalCoverage {
        val everSung = allTime.mapTo(mutableSetOf()) { it.number }
        val neverSung = catalog
            .filter { it.number !in everSung }
            .map { CatalogHymn(number = it.number, title = it.title) }

        val lastSungByHymn = visibleWindow
            .groupBy { it.hymnNumber }
            .mapValues { (_, its) -> its.maxOf { it.occurredOn } }

        val forgotten = allTime
            .map { hymn ->
                val lastSung = lastSungByHymn[hymn.number]
                ForgottenHymn(
                    number = hymn.number,
                    title = hymn.title,
                    lastSung = lastSung,
                    text = lastSung?.let { "Cantado pela última vez em ${it.format(DATE)}" }
                        ?: OVER_A_YEAR,
                )
            }
            // Most forgotten first: the ones the app can only say are older than a year, then
            // the dated ones from oldest to most recent.
            .sortedWith(compareBy(nullsFirst(), ForgottenHymn::lastSung))

        return HymnalCoverage(
            catalogSize = catalog.size,
            everSungCount = everSung.size,
            proportionText = proportionText(catalog.size, everSung.size),
            neverSung = neverSung,
            forgotten = forgotten,
        )
    }

    private fun proportionText(catalogSize: Int, everSung: Int): String {
        if (catalogSize == 0) return "O hinário do aplicativo está vazio."

        val percent = (everSung.toFloat() / catalogSize * PERCENT).roundToInt()
        return "A igreja já cantou $everSung dos $catalogSize hinos do hinário ($percent%)."
    }

    private companion object {
        const val PERCENT = 100
        const val OVER_A_YEAR = "Não é cantado há mais de um ano"
        val DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }
}
