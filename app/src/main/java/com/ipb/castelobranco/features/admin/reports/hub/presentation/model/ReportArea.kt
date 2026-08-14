package com.ipb.castelobranco.features.admin.reports.hub.presentation.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One area of church life that has a report.
 *
 * Nothing here is specific to the hymnal: the hub, its state and its item are written against
 * this type, so adding the schedule, attendance, members or gallery later is one more entry in a
 * list — not a rewrite. Equally, no generic reporting machinery exists behind it; the hymnal is
 * the only instance today and that is on purpose.
 */
data class ReportArea(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val onClick: () -> Unit,
)
