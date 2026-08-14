package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.Highlight
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HighlightDelta
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnOccurrence
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The three or four short statements the report opens with, each with its change against the
 * period immediately before.
 *
 * When there is no preceding data the delta is `null` and nothing is shown — a church that just
 * installed the collection must not be told it grew infinitely.
 */
class BuildHighlightsUseCase @Inject constructor() {

    operator fun invoke(
        current: List<HymnOccurrence>,
        preceding: List<HymnOccurrence>?,
    ): List<Highlight> {
        if (current.isEmpty()) return emptyList()

        return listOf(
            mostSung(current, preceding),
            distinctHymns(current, preceding),
            servicesWithRecords(current, preceding),
            averageReach(current, preceding),
        )
    }

    private fun mostSung(
        current: List<HymnOccurrence>,
        preceding: List<HymnOccurrence>?,
    ): Highlight {
        val top = current.groupBy { it.hymnNumber }
            .maxByOrNull { (_, its) -> its.size }
            ?: return Highlight(label = MOST_SUNG, value = NONE, delta = null)

        val count = top.value.size
        val before = preceding?.count { it.hymnNumber == top.key }

        return Highlight(
            label = MOST_SUNG,
            value = "${top.key} · ${top.value.first().hymnTitle}",
            delta = delta(count, before, unit = "vez", pluralUnit = "vezes"),
        )
    }

    private fun distinctHymns(
        current: List<HymnOccurrence>,
        preceding: List<HymnOccurrence>?,
    ): Highlight {
        val count = current.distinctBy { it.hymnNumber }.size
        val before = preceding?.distinctBy { it.hymnNumber }?.size

        return Highlight(
            label = DISTINCT_HYMNS,
            value = if (count == 1) "1 hino" else "$count hinos",
            delta = delta(count, before, unit = "hino", pluralUnit = "hinos"),
        )
    }

    /** A service with records is one date-plus-service group, not one service window. */
    private fun servicesWithRecords(
        current: List<HymnOccurrence>,
        preceding: List<HymnOccurrence>?,
    ): Highlight {
        val count = current.countServices()
        val before = preceding?.countServices()

        return Highlight(
            label = SERVICES,
            value = if (count == 1) "1 culto" else "$count cultos",
            delta = delta(count, before, unit = "culto", pluralUnit = "cultos"),
        )
    }

    private fun averageReach(
        current: List<HymnOccurrence>,
        preceding: List<HymnOccurrence>?,
    ): Highlight {
        val average = current.averageReach()
        val before = preceding?.takeIf { it.isNotEmpty() }?.averageReach()

        return Highlight(
            label = AVERAGE_REACH,
            value = if (average == 1) "1 aparelho" else "$average aparelhos",
            delta = delta(average, before, unit = "aparelho", pluralUnit = "aparelhos"),
        )
    }

    private fun List<HymnOccurrence>.countServices(): Int =
        filter { it.serviceWindowId != null }
            .distinctBy { it.occurredOn to it.serviceWindowId }
            .size

    private fun List<HymnOccurrence>.averageReach(): Int =
        if (isEmpty()) 0 else (sumOf { it.deviceCount }.toFloat() / size).roundToInt()

    private fun delta(
        current: Int,
        preceding: Int?,
        unit: String,
        pluralUnit: String,
    ): HighlightDelta? {
        if (preceding == null) return null

        val difference = current - preceding
        val magnitude = abs(difference)
        val noun = if (magnitude == 1) unit else pluralUnit

        return when {
            difference > 0 -> HighlightDelta(
                direction = HighlightDelta.Direction.UP,
                text = "+$magnitude $noun em relação ao período anterior",
            )

            difference < 0 -> HighlightDelta(
                direction = HighlightDelta.Direction.DOWN,
                text = "−$magnitude $noun em relação ao período anterior",
            )

            else -> HighlightDelta(
                direction = HighlightDelta.Direction.FLAT,
                text = "Igual ao período anterior",
            )
        }
    }

    private companion object {
        const val MOST_SUNG = "Hino mais cantado"
        const val DISTINCT_HYMNS = "Hinos diferentes"
        const val SERVICES = "Cultos com registro"
        const val AVERAGE_REACH = "Alcance médio por ocorrência"
        const val NONE = "—"
    }
}
