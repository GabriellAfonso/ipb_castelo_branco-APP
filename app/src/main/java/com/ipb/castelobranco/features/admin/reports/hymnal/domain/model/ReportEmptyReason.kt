package com.ipb.castelobranco.features.admin.reports.hymnal.domain.model

/**
 * Why a reading has nothing to show.
 *
 * A blank chart tells the leadership nothing; a sentence tells them whether to change the period,
 * fix a service window, or simply wait for the congregation to use the hymnal. Sealed so the
 * compiler forces every new reading to handle every kind of emptiness.
 */
sealed interface ReportEmptyReason {

    val title: String
    val message: String

    /** No occurrence has ever been recorded — a church that just installed the collection. */
    data object NoCollectionAtAll : ReportEmptyReason {
        override val title = "Nenhum registro ainda"
        override val message =
            "A coleta ainda não registrou nenhuma abertura de hino. Assim que a congregação " +
                "começar a usar o hinário, os relatórios aparecem aqui."
    }

    /** History exists, but not inside the chosen period. */
    data object NoRecordsInPeriod : ReportEmptyReason {
        override val title = "Nada neste período"
        override val message =
            "Existem registros no histórico, mas nenhum dentro do período escolhido. " +
                "Experimente ampliar o período."
    }

    /** The period has occurrences; this slice of it has none. */
    data object NoRecordsInSlice : ReportEmptyReason {
        override val title = "Nada neste recorte"
        override val message =
            "Há registros no período, mas nenhum dentro do recorte escolhido."
    }

    /**
     * The slice is empty because the service itself was not in force — created after the period,
     * or deactivated before it. Nobody failed to sing; there was no service to sing at.
     */
    data class ServiceInactiveOrAbsentInPeriod(val serviceName: String) : ReportEmptyReason {
        override val title = "Culto fora do período"
        override val message =
            "O culto \"$serviceName\" não estava ativo durante todo o período escolhido, " +
                "então não há ocorrências agrupadas nele."
    }

    /**
     * The service was in force and has no records.
     *
     * The app collects hymn views and nothing else, so it has no evidence of whether a service
     * happened. Both possibilities are named and neither is asserted.
     */
    data class ServiceWithoutRecords(val serviceName: String) : ReportEmptyReason {
        override val title = "Nenhum hino aberto"
        override val message =
            "Nenhum hino foi aberto no culto \"$serviceName\" neste período. Pode ser que o " +
                "culto não tenha acontecido, ou que ninguém tenha usado o hinário — o " +
                "aplicativo não tem como distinguir os dois casos."
    }

    /** The local hymn catalogue could not be read, so absence cannot be computed. */
    data object CatalogUnavailable : ReportEmptyReason {
        override val title = "Hinário indisponível"
        override val message =
            "Não foi possível ler o hinário do aplicativo, então não dá para saber quais hinos " +
                "nunca foram cantados. As demais leituras continuam funcionando."
    }
}
