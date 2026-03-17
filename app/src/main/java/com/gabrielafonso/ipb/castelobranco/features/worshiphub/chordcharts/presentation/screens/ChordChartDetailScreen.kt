package com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
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
import com.gabrielafonso.ipb.castelobranco.core.presentation.modifier.tapToPaginate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.presentation.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.parser.ChordBlock
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.parser.ChordLine
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.parser.LineToken
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.state.ChordChartDetailUiState
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.viewmodel.ChordChartDetailViewModel

private val ChordColor = Color(0xFFF2A300)
private val SectionTitleColor = Color(0xFF0F6B5C)

@Composable
fun ChordChartDetailScreen(
    viewModel: ChordChartDetailViewModel,
    onBackClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ChordChartDetailContent(state = state, onBackClick = onBackClick)
}

@Composable
private fun ChordChartDetailContent(
    state: ChordChartDetailUiState,
    onBackClick: () -> Unit,
) {
    BaseScreen(
        tabName       = state.songName.ifEmpty { "Chord Chart" },
        logoRes       = R.drawable.ic_sarca_ipb,
        showBackArrow = true,
        onBackClick   = onBackClick,
    ) { innerPadding ->
        when {
            state.isLoading        -> LoadingState(Modifier.padding(innerPadding))
            state.error != null    -> ErrorState(state.error, Modifier.padding(innerPadding))
            state.blocks.isEmpty() -> ErrorState("No content available", Modifier.padding(innerPadding))
            else -> ChordPager(
                blocks   = state.blocks,
                tone     = state.tone,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

/**
 * Measures each block's height, computes page splits via [BlockPaginator], then subcomposes
 * the entire pager UI (including [rememberPagerState]) with the final pages already known.
 * This avoids any state round-trip between the measurement layout and the pager.
 */
@Composable
private fun ChordPager(
    blocks: List<ChordBlock>,
    tone: String,
    modifier: Modifier = Modifier,
) {
    SubcomposeLayout(modifier = modifier.fillMaxSize()) { constraints ->
        val blockSpacingPx = 16.dp.roundToPx()
        // Matches ChordPageContent padding(horizontal = 20.dp, vertical = 12.dp)
        val hPaddingPx = 40.dp.roundToPx()
        val vPaddingPx = 24.dp.roundToPx()
        val measureWidth = (constraints.maxWidth - hPaddingPx).coerceAtLeast(0)

        // Phase 1: measure each block at the correct render width.
        // Column wrapper is required — SectionBlock emits multiple root nodes (Text, Spacers, Rows)
        // with no container, so .firstOrNull() without it would only measure the first child.
        val blockHeights = blocks.mapIndexed { i, block ->
            subcompose("m_$i") {
                Column {
                    if (block.isIntro) IntroBlock(block) else SectionBlock(block)
                }
            }.firstOrNull()
                ?.measure(Constraints(maxWidth = measureWidth))
                ?.height ?: 0
        }

        // Phase 2: measure chrome (header + dots) to determine pager area height
        val headerHeight = subcompose("chrome_header") {
            PagerHeader(tone = tone, currentPage = 0, pageCount = 1)
        }.first().measure(Constraints(maxWidth = constraints.maxWidth)).height

        val dotsHeight = subcompose("chrome_dots") {
            PageDotIndicator(
                pageCount   = 1,
                currentPage = 0,
                modifier    = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            )
        }.first().measure(Constraints(maxWidth = constraints.maxWidth)).height

        // Phase 3: compute pages with the actual available height for block content
        val availableForBlocks = (constraints.maxHeight - headerHeight - dotsHeight - vPaddingPx)
            .coerceAtLeast(1)
        val pages = BlockPaginator.paginate(blocks, blockHeights, availableForBlocks, blockSpacingPx)

        // Phase 4: render the full pager — pagerState lives inside this subcomposition
        val contentPlaceable = subcompose("pager") {
            PagerContent(pages = pages, tone = tone)
        }.first().measure(constraints)

        layout(constraints.maxWidth, constraints.maxHeight) {
            contentPlaceable.place(0, 0)
        }
    }
}

@Composable
private fun PagerContent(
    pages: List<List<ChordBlock>>,
    tone: String,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        PagerHeader(
            tone        = tone,
            currentPage = pagerState.currentPage,
            pageCount   = pages.size,
        )

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
                ChordPageContent(blocks = pages[index])
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
private fun PagerHeader(tone: String, currentPage: Int, pageCount: Int) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text       = "Key: $tone",
            style      = MaterialTheme.typography.bodyMedium,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text  = "${currentPage + 1} / $pageCount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChordPageContent(blocks: List<ChordBlock>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        blocks.forEachIndexed { index, block ->
            if (index > 0) Spacer(modifier = Modifier.height(16.dp))
            if (block.isIntro) IntroBlock(block) else SectionBlock(block)
        }
    }
}

@Composable
private fun IntroBlock(block: ChordBlock) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text       = "Intro:",
            style      = MaterialTheme.typography.titleMedium,
            color      = SectionTitleColor,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(8.dp))
        block.lines.forEach { line ->
            line.tokens.filterIsInstance<LineToken.Chord>().forEach { chord ->
                Text(
                    text       = chord.value,
                    color      = ChordColor,
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun SectionBlock(block: ChordBlock) {
    if (block.title != null) {
        Text(
            text       = block.title,
            style      = MaterialTheme.typography.titleMedium,
            color      = SectionTitleColor,
            fontWeight = FontWeight.Bold,
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    block.lines.forEach { line ->
        ChordLineRow(line = line)
        Spacer(modifier = Modifier.height(2.dp))
    }
}

private data class ChordLyricGroup(val chord: String?, val lyrics: String)

private fun groupTokens(tokens: List<LineToken>): List<ChordLyricGroup> {
    val groups = mutableListOf<ChordLyricGroup>()
    var i = 0
    while (i < tokens.size) {
        val token = tokens[i]
        when (token) {
            is LineToken.Chord -> {
                val lyrics = (tokens.getOrNull(i + 1) as? LineToken.Lyrics)?.value ?: ""
                groups += ChordLyricGroup(token.value, lyrics)
                i += 2
            }
            is LineToken.Lyrics -> {
                groups += ChordLyricGroup(null, token.value)
                i += 1
            }
        }
    }
    return groups
}

@Composable
private fun ChordLineRow(line: ChordLine) {
    val lyricColor = MaterialTheme.colorScheme.onSurface
    val groups = groupTokens(line.tokens)

    FlowRow {
        groups.forEach { group ->
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text       = group.chord ?: "",
                    color      = ChordColor,
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.labelMedium,
                    softWrap   = false,
                    maxLines   = 1,
                )
                Text(
                    text     = group.lyrics,
                    color    = lyricColor,
                    style    = MaterialTheme.typography.bodyLarge,
                    softWrap = false,
                    maxLines = 1,
                )
            }
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
                color    = if (isSelected) ChordColor
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
