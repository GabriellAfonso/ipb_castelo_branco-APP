// app/src/main/java/com/gabrielafonso/ipb/castelobranco/ui/screens/monthschedule/ScheduleView.kt
package com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseScreen
import java.util.Locale
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.MonthSchedule
import com.gabrielafonso.ipb.castelobranco.features.schedule.domain.model.ScheduleEntry
import com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.viewmodel.ScheduleViewModel

@Composable
fun MonthScheduleView(
    viewModel: ScheduleViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onShare: (String) -> Unit
) {
    val monthSchedule by viewModel.monthSchedule.collectAsStateWithLifecycle()
    val isLoading by viewModel.isRefreshingMonthSchedule.collectAsStateWithLifecycle()

    val formattedText = monthSchedule?.toWhatsappText().orEmpty()

    BaseScreen(
        tabName = "Escala Mensal",
        logoRes = R.drawable.ic_schedule,
        showBackArrow = true,
        onBackClick = onBackClick,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                val innerScroll = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(innerScroll)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isLoading && formattedText.isBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Carregando escala...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = if (formattedText.isBlank()) "Sem escala disponível."
                            else formattedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* TODO: gerar nova escala depois */ },
                    enabled = false,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                ) {
                    Text(text = "Nova Escala")
                }

                Button(
                    onClick = { onShare(formattedText) },
                    enabled = formattedText.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                ) {
                    Text(text = "Compartilhar")
                }
            }
        }
    }
}

private fun MonthSchedule.toWhatsappText(): String {
    val monthName = monthPtBr(month).uppercase(Locale.forLanguageTag("pt-BR"))

    val sb = StringBuilder()
    sb.append("ESCALA DE ").append(monthName).append(" ").append(year).append(" - DIRIGENTES E RESPONSÁVEIS\n\n")

    val order = listOf("terça", "terca", "quinta", "domingo")

    val entriesSorted = schedule.entries
        .sortedWith(
            compareBy<Map.Entry<String, ScheduleEntry>> { entry ->
                val t = entry.key.trim().lowercase(Locale.forLanguageTag("pt-BR"))
                order.indexOfFirst { t.startsWith(it) }.let { if (it == -1) Int.MAX_VALUE else it }
            }.thenBy { it.key.lowercase(Locale.forLanguageTag("pt-BR")) }
        )

    entriesSorted.forEach { (title, entry) ->
        sb.append(title)
        entry.time.takeIf { it.isNotBlank() }?.let { sb.append(" (").append(it).append(")") }
        sb.append("\n\n")

        entry.items
            .sortedBy { it.day }
            .forEach { item ->
                sb.append(String.format("%02d", item.day)).append("- ").append(item.member).append("\n")
            }

        sb.append("\n")
    }

    sb.append("\\*Cafezinho pós culto de Adoração (Ceia) todo 4° Domingo\n\n")
    sb.append("\\* Aberto a participação de qualquer irmão.\n")
    sb.append("DEUS ABENÇOE")

    return sb.toString().trim()
}

private fun monthPtBr(month: Int): String =
    when (month) {
        1 -> "Janeiro"
        2 -> "Fevereiro"
        3 -> "Março"
        4 -> "Abril"
        5 -> "Maio"
        6 -> "Junho"
        7 -> "Julho"
        8 -> "Agosto"
        9 -> "Setembro"
        10 -> "Outubro"
        11 -> "Novembro"
        12 -> "Dezembro"
        else -> "Mês"
    }
