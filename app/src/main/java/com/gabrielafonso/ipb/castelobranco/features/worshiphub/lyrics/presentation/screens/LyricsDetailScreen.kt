package com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.screens

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.presentation.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.core.presentation.modifier.tapToPaginate
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.parser.LyricsStanza
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.state.LyricsDetailUiState
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.viewmodel.LyricsDetailViewModel

private val DotColor   = Color(0xFF1565C0)
private val TitleColor = Color(0xFFF2A300)

@Composable
fun LyricsDetailScreen(
    viewModel: LyricsDetailViewModel,
    onBackClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LyricsDetailContent(state = state, onBackClick = onBackClick)
}

@Composable
private fun LyricsDetailContent(
    state: LyricsDetailUiState,
    onBackClick: () -> Unit,
) {
    BaseScreen(
        tabName       = "Letra",
        logoRes       = R.drawable.ic_sarca_ipb,
        showBackArrow = true,
        onBackClick   = onBackClick,
    ) { innerPadding ->
        when {
            state.isLoading          -> LoadingState(Modifier.padding(innerPadding))
            state.error != null      -> ErrorState(state.error, Modifier.padding(innerPadding))
            state.stanzas.isEmpty()  -> ErrorState("No content available", Modifier.padding(innerPadding))
            else -> LyricsPager(
                stanzas  = state.stanzas,
                songName = state.songName,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

/**
 * Measures each stanza's height, computes page splits, then subcomposes the full
 * pager UI with the pages already known — same pattern as ChordChartDetailScreen.
 */
@Composable
private fun LyricsPager(
    stanzas: List<LyricsStanza>,
    songName: String,
    modifier: Modifier = Modifier,
) {
    SubcomposeLayout(modifier = modifier.fillMaxSize()) { constraints ->
        val stanzaSpacingPx = 16.dp.roundToPx()
        val hPaddingPx      = 40.dp.roundToPx()  // matches LyricsPageContent padding(horizontal = 20.dp)
        val vPaddingPx      = 24.dp.roundToPx()  // matches LyricsPageContent padding(vertical = 12.dp)
        val measureWidth    = (constraints.maxWidth - hPaddingPx).coerceAtLeast(0)

        // Phase 1: measure each stanza at the correct render width
        val stanzaHeights = stanzas.mapIndexed { i, stanza ->
            subcompose("m_$i") {
                Column { StanzaBlock(stanza) }
            }.firstOrNull()
                ?.measure(Constraints(maxWidth = measureWidth))
                ?.height ?: 0
        }

        // Phase 2: measure chrome (song title + dots) for available height calculation
        val titleHeight = subcompose("chrome_title") {
            if (songName.isNotEmpty()) SongTitle(songName)
        }.firstOrNull()?.measure(Constraints(maxWidth = constraints.maxWidth))?.height ?: 0

        val dotsHeight = subcompose("chrome_dots") {
            PageDotIndicator(
                pageCount   = 1,
                currentPage = 0,
                modifier    = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            )
        }.first().measure(Constraints(maxWidth = constraints.maxWidth)).height

        // Phase 3: compute pages
        val availableForStanzas = (constraints.maxHeight - titleHeight - dotsHeight - vPaddingPx)
            .coerceAtLeast(1)
        val pages = paginate(stanzas, stanzaHeights, availableForStanzas, stanzaSpacingPx)

        // Phase 4: render full pager — pagerState lives inside this subcomposition
        val contentPlaceable = subcompose("pager") {
            PagerContent(pages = pages, songName = songName)
        }.first().measure(constraints)

        layout(constraints.maxWidth, constraints.maxHeight) {
            contentPlaceable.place(0, 0)
        }
    }
}

private fun paginate(
    stanzas: List<LyricsStanza>,
    heights: List<Int>,
    availableHeight: Int,
    spacingPx: Int,
): List<List<LyricsStanza>> {
    if (stanzas.isEmpty()) return emptyList()

    val pages = mutableListOf<MutableList<LyricsStanza>>()
    var currentPage = mutableListOf<LyricsStanza>()
    var usedHeight = 0

    stanzas.forEachIndexed { i, stanza ->
        val h       = heights[i]
        val spacing = if (currentPage.isEmpty()) 0 else spacingPx
        if (currentPage.isNotEmpty() && usedHeight + spacing + h > availableHeight) {
            pages += currentPage
            currentPage = mutableListOf()
            usedHeight = 0
        }
        currentPage += stanza
        usedHeight += (if (usedHeight == 0) 0 else spacingPx) + h
    }

    if (currentPage.isNotEmpty()) pages += currentPage
    return pages
}

@Composable
private fun PagerContent(
    pages: List<List<LyricsStanza>>,
    songName: String,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope      = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        if (songName.isNotEmpty()) SongTitle(songName)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .tapToPaginate(pagerState, scope),
        ) {
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { index ->
                LyricsPageContent(stanzas = pages[index])
            }
        }

        PageDotIndicator(
            pageCount   = pages.size,
            currentPage = pagerState.currentPage,
            modifier    = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        )
    }
}

@Composable
private fun SongTitle(name: String) {
    Text(
        text     = name,
        color    = TitleColor,
        style    = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

@Composable
private fun LyricsPageContent(stanzas: List<LyricsStanza>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        stanzas.forEachIndexed { index, stanza ->
            if (index > 0) Spacer(modifier = Modifier.height(16.dp))
            StanzaBlock(stanza)
        }
    }
}

@Composable
private fun StanzaBlock(stanza: LyricsStanza) {
    Column {
        stanza.lines.forEach { line ->
            Text(
                text  = line,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
private fun PageDotIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            Surface(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isSelected) 8.dp else 6.dp),
                shape    = CircleShape,
                color    = if (isSelected) DotColor
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            ) {}
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text  = "Loading...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text  = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}
