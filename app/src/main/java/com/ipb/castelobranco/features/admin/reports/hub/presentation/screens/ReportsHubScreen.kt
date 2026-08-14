package com.ipb.castelobranco.features.admin.reports.hub.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ipb.castelobranco.R
import com.ipb.castelobranco.core.presentation.base.BaseScreen
import com.ipb.castelobranco.core.presentation.theme.BrandColors
import com.ipb.castelobranco.features.admin.reports.hub.presentation.model.ReportArea

/**
 * The entry point of the reports area, reached from the Admin Panel's "Relatórios" card.
 *
 * Built like `AdminScreen`: a list, one item composable, no ViewModel and no data layer. The
 * hymnal is the only area today, and nothing in this file knows that.
 */
@Composable
fun ReportsHubScreen(
    onBack: () -> Unit,
    onOpenHymnalReport: () -> Unit,
) {
    val areas = listOf(
        ReportArea(
            title = "Histórico do hinário",
            description = "O que a igreja canta, nos cultos e durante a semana",
            icon = Icons.Filled.MusicNote,
            accentColor = BrandColors.Green,
            onClick = onOpenHymnalReport,
        ),
    )

    BaseScreen(
        tabName = "Relatórios",
        logoRes = R.drawable.ic_sarca_ipb,
        showBackArrow = true,
        onBackClick = onBack,
    ) { innerPadding ->
        ReportsHubContent(areas = areas, innerPadding = innerPadding)
    }
}

@Composable
fun ReportsHubContent(
    areas: List<ReportArea>,
    innerPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Áreas com relatório",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        areas.forEach { area -> ReportAreaCard(area = area) }
    }
}

@Composable
private fun ReportAreaCard(area: ReportArea) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = area.onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(area.accentColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = area.icon,
                contentDescription = area.title,
                tint = area.accentColor,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = area.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = area.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportsHubContentPreview() {
    ReportsHubContent(
        areas = listOf(
            ReportArea(
                title = "Histórico do hinário",
                description = "O que a igreja canta, nos cultos e durante a semana",
                icon = Icons.Filled.MusicNote,
                accentColor = BrandColors.Green,
                onClick = {},
            ),
        ),
        innerPadding = PaddingValues(0.dp),
    )
}
