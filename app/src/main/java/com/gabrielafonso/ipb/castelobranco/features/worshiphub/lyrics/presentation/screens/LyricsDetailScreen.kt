package com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.presentation.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.parser.LyricsPage
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.parser.LyricsStanza
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.state.LyricsDetailUiState
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.lyrics.presentation.viewmodel.LyricsDetailViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement

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
            state.isLoading       -> LoadingState(Modifier.padding(innerPadding))
            state.error != null   -> ErrorState(state.error, Modifier.padding(innerPadding))
            state.pages.isEmpty() -> ErrorState("No content available", Modifier.padding(innerPadding))
            else -> LyricsPager(
                pages    = state.pages,
                songName = state.songName,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun LyricsPager(
    pages: List<LyricsPage>,
    songName: String,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(modifier = modifier.fillMaxSize()) {
        if (songName.isNotEmpty()) {
            Text(
                text      = songName,
                color     = TitleColor,
                style     = MaterialTheme.typography.titleLarge,
                modifier  = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }

        HorizontalPager(
            state    = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { index ->
            AutoScaledContent(modifier = Modifier.fillMaxSize()) {
                LyricsPageContent(page = pages[index])
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
private fun LyricsPageContent(page: LyricsPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        page.stanzas.forEachIndexed { index, stanza ->
            if (index > 0) Spacer(modifier = Modifier.height(16.dp))
            StanzaBlock(stanza = stanza)
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
        val placeable  = measurable.measure(Constraints())

        val scale = if (placeable.height > constraints.maxHeight && placeable.height > 0) {
            constraints.maxHeight.toFloat() / placeable.height.toFloat()
        } else 1f

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeWithLayer(0, 0) {
                scaleX          = scale
                scaleY          = scale
                transformOrigin = TransformOrigin(0f, 0f)
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
