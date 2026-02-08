package com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.domain.model.SuggestedSong
import com.gabrielafonso.ipb.castelobranco.domain.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.domain.model.TopSong
import com.gabrielafonso.ipb.castelobranco.domain.model.TopTone
import com.gabrielafonso.ipb.castelobranco.ui.screens.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.WorshipHubViewModel
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.tabs.LastSundaysTab
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.tabs.SuggestionsTab
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.tabs.TopSongsTab
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.tabs.TopTonesTab

data class WorshipSongsUiState(
    val sundays: List<SundaySet> = emptyList(),
    val topSongs: List<TopSong> = emptyList(),
    val topTones: List<TopTone> = emptyList(),
    val suggestedSongs: List<SuggestedSong> = emptyList(),
    val isRefreshingSuggestions: Boolean = false
)


data class WorshipSongsActions(
    val onBackClick: () -> Unit,
    val onRefreshSuggestions: () -> Unit
)

@Composable
fun WorshipSongsTableView(
    onBackClick: () -> Unit,
    viewModel: WorshipHubViewModel
) {
    val state = WorshipSongsUiState(
        sundays = viewModel.lastSundays.collectAsStateWithLifecycle().value,
        topSongs = viewModel.topSongs.collectAsStateWithLifecycle().value,
        topTones = viewModel.topTones.collectAsStateWithLifecycle().value,
        suggestedSongs = viewModel.suggestedSongs.collectAsStateWithLifecycle().value,
        isRefreshingSuggestions = viewModel.isRefreshingSuggestedSongs.collectAsStateWithLifecycle().value
    )

    val fixedByPosition = viewModel.fixedByPosition.collectAsStateWithLifecycle().value

    val actions = WorshipSongsActions(
        onBackClick = onBackClick,
        onRefreshSuggestions = viewModel::refreshSuggestedSongs
    )

    WorshipSongsTableScreen(
        state = state,
        actions = actions,
        fixedByPosition = fixedByPosition,
        onToggleFixed = viewModel::toggleFixed
    )
}

@Composable
fun WorshipSongsTableScreen(
    state: WorshipSongsUiState,
    actions: WorshipSongsActions,
    fixedByPosition: Map<Int, Int>,
    onToggleFixed: (com.gabrielafonso.ipb.castelobranco.domain.model.SuggestedSong) -> Unit
) {
    val tabs = listOf(
        "Ultimos Domingos",
        "Mais tocadas",
        "Top tons",
        "Sugestões"
    )
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val barColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val indicatorColor = MaterialTheme.colorScheme.secondary

    BaseScreen(
        tabName = "Tabelas",
        logoRes =  R.drawable.ic_table,
        showBackArrow = true,
        onBackClick = actions.onBackClick
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = barColor,
                contentColor = Color.Black,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = indicatorColor,
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSingleWord = !title.contains(" ")

                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                softWrap = !isSingleWord,
                                maxLines = if (isSingleWord) 1 else 2,
                            )
                        }
                    )
                }


            }
            SelectionContainer {
                when (selectedTabIndex) {
                    0 -> LastSundaysTab(sundays = state.sundays)
                    1 -> TopSongsTab(topSongs = state.topSongs)
                    2 -> TopTonesTab(topTones = state.topTones)
                    3 -> SuggestionsTab(
                        suggestedSongs = state.suggestedSongs,
                        isRefreshing = state.isRefreshingSuggestions,
                        fixedByPosition = fixedByPosition,
                        onToggleFixed = onToggleFixed,
                        onRefreshClick = actions.onRefreshSuggestions
                    )
                }
            }
        }
    }
}