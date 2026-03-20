package com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ipb.castelobranco.R
import com.ipb.castelobranco.core.presentation.base.BaseScreen
import com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.state.ChordChartListItem
import com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.state.ChordChartsUiState
import com.ipb.castelobranco.features.worshiphub.chordcharts.presentation.viewmodel.ChordChartsViewModel

private val Orange = Color(0xFFF2A300)
private val Green = Color(0xFF0F6B5C)

@Composable
fun ChordChartsScreen(
    viewModel: ChordChartsViewModel,
    onChordChartClick: (id: Int) -> Unit,
    onBackClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ChordChartsContent(
        state            = state,
        onQueryChange    = viewModel::onQueryChange,
        onChordChartClick = onChordChartClick,
        onBackClick      = onBackClick,
    )
}

@Composable
private fun ChordChartsContent(
    state: ChordChartsUiState,
    onQueryChange: (String) -> Unit,
    onChordChartClick: (id: Int) -> Unit,
    onBackClick: () -> Unit,
) {
    BaseScreen(
        tabName       = "Cifras",
        logoRes       = R.drawable.ic_sarca_ipb,
        showBackArrow = true,
        onBackClick   = onBackClick,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SearchCard(
                query         = state.query,
                onQueryChange = onQueryChange,
                resultsCount  = state.filteredCharts.size,
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.filteredCharts.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.filteredCharts, key = { it.id }) { item ->
                        ChordChartRow(
                            item    = item,
                            onClick = { onChordChartClick(item.id) },
                        )
                        HorizontalDivider(
                            color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 0.5.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchCard(
    query: String,
    onQueryChange: (String) -> Unit,
    resultsCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape    = RoundedCornerShape(10.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value         = query,
                onValueChange = onQueryChange,
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine    = true,
                placeholder   = { Text("") },
                trailingIcon  = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector        = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint               = Green,
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor      = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor    = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor    = MaterialTheme.colorScheme.inverseSurface,
                    unfocusedBorderColor  = MaterialTheme.colorScheme.inverseSurface,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text  = "Results: $resultsCount",
                color = Green,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun ChordChartRow(
    item: ChordChartListItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(width = 4.dp, height = 34.dp)
                .background(Orange, RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = item.songName,
                color      = Green,
                fontWeight = FontWeight.SemiBold,
                style      = MaterialTheme.typography.titleMedium,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text  = "${item.tone} • ${item.instrument}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier             = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment  = Alignment.CenterHorizontally,
        verticalArrangement  = Arrangement.Center,
    ) {
        Text(
            text  = "No results",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}
