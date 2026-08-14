package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.CollectionSettings
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.SettingField
import javax.inject.Inject

/**
 * @param values `null` when any field failed, so a caller cannot accidentally send a half-valid
 *   settings object.
 */
data class SettingsValidation(
    val values: CollectionSettings?,
    val errors: Map<SettingField, String>,
) {
    val isValid: Boolean get() = errors.isEmpty() && values != null
}

/**
 * The same ranges the service enforces, checked here first so the administrator gets the answer
 * immediately and no doomed request is issued. The ranges themselves live on [SettingField], the
 * single table that also routes a server-side `field_errors` key back to its field.
 */
class ValidateCollectionSettingsUseCase @Inject constructor() {

    operator fun invoke(raw: Map<SettingField, String>): SettingsValidation {
        val errors = mutableMapOf<SettingField, String>()
        val parsed = mutableMapOf<SettingField, Int>()

        SettingField.entries.forEach { field ->
            val text = raw[field]?.trim().orEmpty()
            val value = text.toIntOrNull()

            when {
                text.isEmpty() -> errors[field] = REQUIRED
                value == null -> errors[field] = NOT_A_NUMBER
                !field.accepts(value) -> errors[field] = outOfRange(field)
                else -> parsed[field] = value
            }
        }

        val values = if (errors.isEmpty()) {
            CollectionSettings(
                minSecondsToCount = parsed.getValue(SettingField.MIN_SECONDS_TO_COUNT),
                collapseWindowMinutes = parsed.getValue(SettingField.COLLAPSE_WINDOW_MINUTES),
                maxBatchSize = parsed.getValue(SettingField.MAX_BATCH_SIZE),
                maxPastDays = parsed.getValue(SettingField.MAX_PAST_DAYS),
                futureToleranceMinutes = parsed.getValue(SettingField.FUTURE_TOLERANCE_MINUTES),
                windowGraceMinutes = parsed.getValue(SettingField.WINDOW_GRACE_MINUTES),
            )
        } else {
            null
        }

        return SettingsValidation(values = values, errors = errors)
    }

    private companion object {
        const val REQUIRED = "Informe um valor."
        const val NOT_A_NUMBER = "Informe um número inteiro."

        fun outOfRange(field: SettingField): String =
            "Informe um número entre ${field.min} e ${field.max}."
    }
}
