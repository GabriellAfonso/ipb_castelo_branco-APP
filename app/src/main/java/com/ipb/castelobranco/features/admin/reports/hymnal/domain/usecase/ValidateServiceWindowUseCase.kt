package com.ipb.castelobranco.features.admin.reports.hymnal.domain.usecase

import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceWindowDraft
import javax.inject.Inject

/**
 * Field names as the service names them, so a local error and a server `field_errors` key land on
 * the same field of the same form.
 */
object ServiceWindowFields {
    const val NAME = "name"
    const val START_TIME = "start_time"
    const val END_TIME = "end_time"
    const val WEEKDAY = "weekday"
}

/**
 * The service's own rules, applied before the request: a non-empty name within the accepted
 * length, and an end strictly after the start.
 */
class ValidateServiceWindowUseCase @Inject constructor() {

    operator fun invoke(draft: ServiceWindowDraft): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        val name = draft.name.trim()
        when {
            name.isEmpty() -> errors[ServiceWindowFields.NAME] = NAME_REQUIRED
            name.length > ServiceWindowDraft.MAX_NAME_LENGTH ->
                errors[ServiceWindowFields.NAME] = NAME_TOO_LONG
        }

        // Strictly after: a service that ends when it starts would match nothing at all.
        if (!draft.endTime.isAfter(draft.startTime)) {
            errors[ServiceWindowFields.END_TIME] = END_BEFORE_START
        }

        return errors
    }

    private companion object {
        const val NAME_REQUIRED = "Informe o nome do culto."
        val NAME_TOO_LONG =
            "O nome pode ter no máximo ${ServiceWindowDraft.MAX_NAME_LENGTH} caracteres."
        const val END_BEFORE_START = "O horário de término precisa ser depois do de início."
    }
}
