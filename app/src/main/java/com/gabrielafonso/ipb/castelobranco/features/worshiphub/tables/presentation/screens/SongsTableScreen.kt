package com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.presentation.screens

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
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SuggestedSong
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.TopSong
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.domain.model.TopTone
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.presentation.viewmodel.SongsTableViewModel
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.presentation.tabs.LastSundaysTab
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.presentation.tabs.SuggestionsTab
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.presentation.tabs.TopSongsTab
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.presentation.tabs.TopTonesTab
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor

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
fun WorshipSongsTableScreen(
    onBackClick: () -> Unit,
    viewModel: SongsTableViewModel
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

    WorshipSongsTableContent(
        state = state,
        actions = actions,
        fixedByPosition = fixedByPosition,
        onToggleFixed = viewModel::toggleFixed
    )
}

@Composable
fun WorshipSongsTableContent(
    state: WorshipSongsUiState,
    actions: WorshipSongsActions,
    fixedByPosition: Map<Int, Int>,
    onToggleFixed: (SuggestedSong) -> Unit
) {
    val tabs = listOf("Ultimos Domingos", "Mais tocadas", "Top tons", "Sugestões")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val barColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val indicatorColor = MaterialTheme.colorScheme.secondary

    LaunchedEffect(showSearch) {
        if (showSearch) focusRequester.requestFocus()
    }

    BaseScreen(
        tabName = "Tabelas",
        logoRes = R.drawable.ic_table,
        showBackArrow = true,
        onBackClick = actions.onBackClick
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.surfaceDim),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barra de busca (aparece quando showSearch = true)
                AnimatedVisibility(
                    visible = showSearch,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(barColor)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface), // ← adiciona isso
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Buscar música, tom, artista, data...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                inner()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

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
                        0 -> LastSundaysTab(sundays = state.sundays, searchQuery = searchQuery)
                        1 -> TopSongsTab(topSongs = state.topSongs)
                        2 -> TopTonesTab(topTones = state.topTones)
                        3 -> SuggestionsTab(
                            suggestedSongs = state.suggestedSongs,
                            isRefreshing = state.isRefreshingSuggestions,
                            fixedByPosition = fixedByPosition,
                            onToggleFixed = onToggleFixed,
                            onRefreshClick = actions.onRefreshSuggestions,

                            )
                    }
                }
            }

            // FAB de busca — sobreposto no canto inferior direito
            FloatingActionButton(
                onClick = {
                    if (showSearch) {
                        showSearch = false
                        searchQuery = ""
                    } else {
                        showSearch = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(
                    imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = "Buscar"
                )
            }
        }
    }
}