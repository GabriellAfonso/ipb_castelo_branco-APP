package com.ipb.castelobranco.features.admin.reports.hymnal.data.api

import com.ipb.castelobranco.core.network.ApiConstants

/**
 * Paths of the hymnal history administration API.
 *
 * `features/hymnal` declares the two paths the invisible collection uses
 * (`HymnalHistoryEndpoints`). These are the administrative ones, and they are deliberately a
 * separate declaration: collection reads settings anonymously, this feature reads them as an
 * administrator, and the two must not end up sharing a Retrofit interface by accident.
 */
object HymnalReportEndpoints {
    private const val ROOT = "${ApiConstants.BASE_PATH}hymnal-history/"

    const val OCCURRENCES_PATH = "${ROOT}occurrences/"
    const val TOP_HYMNS_PATH = "${ROOT}top-hymns/"
    const val SETTINGS_PATH = "${ROOT}settings/"
    const val SERVICE_WINDOWS_PATH = "${ROOT}service-windows/"
    const val SERVICE_WINDOW_DETAIL_PATH = "${ROOT}service-windows/{id}/"
}
