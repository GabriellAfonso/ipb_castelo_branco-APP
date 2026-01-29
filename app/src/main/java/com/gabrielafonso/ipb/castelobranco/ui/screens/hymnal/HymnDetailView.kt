package com.gabrielafonso.ipb.castelobranco.ui.screens.hymnal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gabrielafonso.ipb.castelobranco.domain.model.Hymn
import com.gabrielafonso.ipb.castelobranco.domain.model.HymnLyric
import com.gabrielafonso.ipb.castelobranco.domain.model.HymnLyricType
import com.gabrielafonso.ipb.castelobranco.ui.screens.base.BaseScreen




@Composable
fun HymnDetailView(
    hymnId: String,
    viewModel: HymnalViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val hymn = remember(hymnId, state.hymns) {
        state.hymns.firstOrNull { it.number == hymnId }
    }

    HymnDetailScreen(
        hymn = hymn,
        onBack = onBack
    )
}

@Composable
fun HymnDetailScreen(
    hymn: Hymn?,
    onBack: () -> Unit
) {

    val pageBg = Color(0xFFE9E9E9)
    val headerGreen = Color(0xFF0F6B5C)

    BaseScreen(
        tabName = "Hinário",
        showBackArrow = true,
        onBackClick = onBack,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            if (hymn == null) {
                Text(
                    text = "Hino não encontrado",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = headerGreen,
                    style = MaterialTheme.typography.titleMedium
                )
                return@BaseScreen
            }

            Text(
                text = "${hymn.number} \u2022 ${hymn.title}",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = headerGreen,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.headlineSmall
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(
                    items = hymn.lyrics,
                    key = { index, _ -> "${hymn.number}-$index" }
                ) { _, lyric ->
                    LyricCard(
                        lyric = lyric,
                        fontSizeSp = 22f
                    )
                }
            }
        }
    }
}

@Composable
private fun LyricCard(
    lyric: HymnLyric,
    fontSizeSp: Float
) {
    val container = Color(0xFFFFFFFF)
    val stripe = when (lyric.type) {
        HymnLyricType.VERSE -> Color(0xFFF2A300)
        HymnLyricType.CHORUS -> Color(0xFF0F6B5C)
        HymnLyricType.OTHER -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container),
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Spacer(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(stripe, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = lyric.text.trim(),
                color = Color(0xFF111111),
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp + 10f).sp,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}