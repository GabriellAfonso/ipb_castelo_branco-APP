package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ipb.castelobranco.R
import com.ipb.castelobranco.core.presentation.base.BaseScreen
import com.ipb.castelobranco.core.presentation.theme.BrandColors
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnProfile
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.HymnRecurrence
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.ServiceShare
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.util.toFullDate
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.viewmodel.HymnalReportViewModel
import java.time.LocalDate

/**
 * Where every list in this feature leads.
 *
 * Each fact is labelled with the history it covers: the total comes from all recorded history,
 * the dates and the recurrence from the last year — the furthest back the app can see.
 */
@Composable
fun HymnCardScreen(
    viewModel: HymnalReportViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BaseScreen(
        tabName = "Ficha do hino",
        logoRes = R.drawable.ic_sarca_ipb,
        showBackArrow = true,
        onBackClick = onBack,
    ) { innerPadding ->
        when {
            state.isHymnProfileLoading -> LoadingScreen(innerPadding)
            state.hymnProfileError != null -> ErrorScreen(state.hymnProfileError, innerPadding)
            state.hymnProfile != null -> HymnCardContent(
                profile = requireNotNull(state.hymnProfile),
                innerPadding = innerPadding,
            )
        }
    }
}

@Composable
fun HymnCardContent(
    profile: HymnProfile,
    innerPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text(
                text = profile.number,
                style = MaterialTheme.typography.labelLarge,
                color = BrandColors.Green,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = profile.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (profile.neverRecorded) {
            InfoCard(
                label = "Nunca registrado",
                value = "Este hino nunca foi aberto por ninguém desde o início da coleta.",
            )
            return@Column
        }

        InfoCard(
            label = "Em todo o histórico",
            value = if (profile.allTimeCount == 1) "Cantado 1 vez"
            else "Cantado ${profile.allTimeCount} vezes",
        )

        InfoCard(
            label = "No último ano",
            value = buildString {
                append(profile.firstSeen?.let { "Primeira vez em ${it.toFullDate()}" } ?: "Sem registro")
                profile.lastSeen?.let { append("\nÚltima vez em ${it.toFullDate()}") }
                append("\nAlcance típico de ${profile.typicalReach} aparelhos")
            },
        )

        profile.recurrence?.let { RecurrenceCard(it) }

        if (profile.services.isNotEmpty()) {
            Column {
                Text(
                    text = "Onde costuma aparecer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(6.dp))
                profile.services.forEach { share -> ServiceShareRow(share) }
            }
        }
    }
}

@Composable
private fun RecurrenceCard(recurrence: HymnRecurrence) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
    ) {
        Text(
            text = recurrence.text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            recurrence.marks.forEach { sung ->
                Box(
                    modifier = Modifier
                        .size(MARK_SIZE_DP.dp)
                        .clip(CircleShape)
                        .background(
                            if (sung) BrandColors.Green
                            else MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                )
            }
        }
    }
}

@Composable
private fun ServiceShareRow(share: ServiceShare) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = share.serviceName ?: "Fora de culto",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "${share.occurrenceCount}×",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun LoadingScreen(innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(message: String?, innerPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = message.orEmpty(), style = MaterialTheme.typography.bodyMedium)
    }
}

private const val MARK_SIZE_DP = 12

@Preview(showBackground = true)
@Composable
private fun HymnCardContentPreview() {
    HymnCardContent(
        profile = HymnProfile(
            number = "50",
            title = "Grandioso És Tu",
            allTimeCount = 42,
            firstSeen = LocalDate.of(2025, 9, 7),
            lastSeen = LocalDate.of(2026, 8, 16),
            services = listOf(ServiceShare("Culto de Domingo à Noite", 12)),
            typicalReach = 24,
            recurrence = HymnRecurrence(
                serviceName = "Culto de Domingo à Noite",
                text = "Cantado em 6 dos últimos 8 registros de \"Culto de Domingo à Noite\"",
                marks = listOf(true, true, false, true, true, false, true, true),
            ),
            neverRecorded = false,
        ),
        innerPadding = PaddingValues(0.dp),
    )
}
