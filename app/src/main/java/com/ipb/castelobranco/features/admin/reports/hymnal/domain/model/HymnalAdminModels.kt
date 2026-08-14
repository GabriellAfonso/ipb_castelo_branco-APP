package com.ipb.castelobranco.features.admin.reports.hymnal.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * A recurring weekly service. It is the grouping key for occurrences and the definition of a
 * report slice.
 *
 * [weekday] is a `DayOfWeek` here on purpose: the service's integer convention
 * (`0 = Monday … 6 = Sunday`) never escapes `data/`, so no screen can rotate the week by
 * treating `0` as Sunday.
 */
data class ServiceWindow(
    val id: Int,
    val name: String,
    val weekday: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val active: Boolean,
)

/** Create and edit share one draft; a `null` [id] means create. */
data class ServiceWindowDraft(
    val id: Int? = null,
    val name: String = "",
    val weekday: DayOfWeek = DayOfWeek.SUNDAY,
    val startTime: LocalTime = LocalTime.of(19, 0),
    val endTime: LocalTime = LocalTime.of(21, 0),
    val active: Boolean = true,
) {
    companion object {
        const val MAX_NAME_LENGTH = 100
    }
}

/** The six numbers that govern collection. */
data class CollectionSettings(
    val minSecondsToCount: Int,
    val collapseWindowMinutes: Int,
    val maxBatchSize: Int,
    val maxPastDays: Int,
    val futureToleranceMinutes: Int,
    val windowGraceMinutes: Int,
) {
    fun valueOf(field: SettingField): Int = when (field) {
        SettingField.MIN_SECONDS_TO_COUNT -> minSecondsToCount
        SettingField.COLLAPSE_WINDOW_MINUTES -> collapseWindowMinutes
        SettingField.MAX_BATCH_SIZE -> maxBatchSize
        SettingField.MAX_PAST_DAYS -> maxPastDays
        SettingField.FUTURE_TOLERANCE_MINUTES -> futureToleranceMinutes
        SettingField.WINDOW_GRACE_MINUTES -> windowGraceMinutes
    }
}

/**
 * One collection parameter: its accepted range, its label, its explanation, and the name the API
 * uses for it.
 *
 * [wireName] is the single point that ties the local range check to the server's `field_errors`
 * key, so the two cannot drift into disagreeing about which field failed.
 */
enum class SettingField(
    val wireName: String,
    val min: Int,
    val max: Int,
    val label: String,
    val explanation: String,
) {
    MIN_SECONDS_TO_COUNT(
        wireName = "min_seconds_to_count",
        min = 1,
        max = 3600,
        label = "Tempo mínimo de leitura (s)",
        explanation = "Vale só para as visualizações coletadas a partir de agora. " +
            "As que já foram registradas continuam como estão.",
    ),
    COLLAPSE_WINDOW_MINUTES(
        wireName = "collapse_window_minutes",
        min = 1,
        max = 1440,
        label = "Janela de agrupamento (min)",
        explanation = "Vale só para as visualizações coletadas a partir de agora. " +
            "Nenhum relatório já existente muda.",
    ),
    MAX_BATCH_SIZE(
        wireName = "max_batch_size",
        min = 1,
        max = 1000,
        label = "Tamanho máximo do lote",
        explanation = "Quantas visualizações o aplicativo envia de uma vez.",
    ),
    MAX_PAST_DAYS(
        wireName = "max_past_days",
        min = 1,
        max = 3650,
        label = "Idade máxima do evento (dias)",
        explanation = "Visualizações mais antigas que isso são recusadas no envio.",
    ),
    FUTURE_TOLERANCE_MINUTES(
        wireName = "future_tolerance_minutes",
        min = 1,
        max = 1440,
        label = "Tolerância de futuro (min)",
        explanation = "Folga para relógios de aparelhos adiantados.",
    ),
    WINDOW_GRACE_MINUTES(
        wireName = "window_grace_minutes",
        min = 1,
        max = 1440,
        label = "Tolerância após o culto (min)",
        explanation = "Altera como o histórico já gravado é lido: um hino aberto às 21h20 entra " +
            "ou sai do culto conforme este valor. Nenhum registro é apagado.",
    );

    fun accepts(value: Int): Boolean = value in min..max

    val rangeText: String get() = "Entre $min e $max"

    companion object {
        fun byWireName(name: String): SettingField? = entries.firstOrNull { it.wireName == name }
    }
}
