package com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.presentation.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.parser.ChordBlock
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.parser.ChordLine
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.parser.ChordPage
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
            state.pages.isEmpty()  -> ErrorState("No content available", Modifier.padding(innerPadding))
            else -> ChordPager(
                pages    = state.pages,
                tone     = state.tone,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun ChordPager(
    pages: List<ChordPage>,
    tone: String,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(modifier = modifier.fillMaxSize()) {
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
                text  = "${pagerState.currentPage + 1} / ${pages.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalPager(
            state    = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { index ->
            AutoScaledContent(modifier = Modifier.fillMaxSize()) {
                ChordPageContent(page = pages[index])
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
private fun ChordPageContent(page: ChordPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        page.blocks.forEachIndexed { index, block ->
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
        Spacer(modifier = Modifier.height(8.dp))
    }
    block.lines.forEach { line ->
        ChordLineRow(line = line)
        Spacer(modifier = Modifier.height(2.dp))
    }
}

private data class ChordLyricGroup(val chord: String?, val lyrics: String)

private fun groupTokens(tokens: List<LineToken>): List<ChordLyricGroup> {
    val groups = mutableListOf<ChordLyricGroup>()
    var currentChord: String? = null
    val currentLyrics = StringBuilder()
    var hasStarted = false

    for (token in tokens) {
        when (token) {
            is LineToken.Chord -> {
                if (hasStarted) {
                    groups += ChordLyricGroup(currentChord, currentLyrics.toString())
                    currentLyrics.clear()
                }
                currentChord = token.value
                hasStarted = true
            }
            is LineToken.Lyrics -> {
                currentLyrics.append(token.value)
                hasStarted = true
            }
        }
    }

    if (hasStarted) groups += ChordLyricGroup(currentChord, currentLyrics.toString())

    return groups
}

@Composable
private fun ChordLineRow(line: ChordLine) {
    val lyricColor = MaterialTheme.colorScheme.onSurface
    val groups = groupTokens(line.tokens)

    Row {
        groups.forEach { group ->
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text       = group.chord ?: "",
                    color      = ChordColor,
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text  = group.lyrics,
                    color = lyricColor,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun AutoScaledContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        val measurable = subcompose("content", content)[0]
        val placeable = measurable.measure(Constraints())

        val scale = if (placeable.height > constraints.maxHeight && placeable.height > 0) {
            constraints.maxHeight.toFloat() / placeable.height.toFloat()
        } else 1f

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale
                scaleY = scale
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
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
