// app/src/main/java/com/gabrielafonso/ipb/castelobranco/ui/screens/worshiphub/WorshipSongsTableScreen.kt
package com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.domain.model.TopSong
import com.gabrielafonso.ipb.castelobranco.ui.screens.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.tabs.LastSundaysTab
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.tabs.SuggestionsTab
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.tabs.TopSongsTab
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.tabs.TopTonesTab


@Composable
fun WorshipSongsTableScreen(
    onBack: () -> Unit,
    viewModel: WorshipHubViewModel
) {
    val sundays by viewModel.lastSundays.collectAsStateWithLifecycle()
    val topSongs by viewModel.topSongs.collectAsStateWithLifecycle()
    val topTones by viewModel.topTones.collectAsStateWithLifecycle()

    WorshipSongsTableUi(
        onBack = onBack,
        sundays = sundays,
        topSongs = topSongs,
        topTones = topTones
    )
}
@Composable
fun WorshipSongsTableUi(
    onBack: () -> Unit,
    sundays: List<com.gabrielafonso.ipb.castelobranco.domain.model.SundaySet>,
    topSongs: List<com.gabrielafonso.ipb.castelobranco.domain.model.TopSong>,
    topTones: List<com.gabrielafonso.ipb.castelobranco.domain.model.TopTone>
) {
    val tabs = listOf(
        "Ultimos Domingos",
        "Mais tocadas",
        "Top tons",
        "Sugestões"
    )
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val barColor = Color(0xFFc7dbd2)
    val indicatorColor = Color(0xFF2E7D6C)

    BaseScreen(
        tabName = "Tabelas",
        logo = painterResource(id = R.drawable.louvor_icon),
        showBackArrow = true,
        onBackClick = onBack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFc7dbd8))
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
                divider = { }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> LastSundaysTab(sundays = sundays)
                1 -> TopSongsTab(topSongs = topSongs)
                2 -> TopTonesTab(topTones = topTones)
                3 -> SuggestionsTab()
            }
        }
    }
}