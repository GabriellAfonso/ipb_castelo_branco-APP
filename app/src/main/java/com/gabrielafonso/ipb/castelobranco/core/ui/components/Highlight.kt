package com.gabrielafonso.ipb.castelobranco.core.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
// ─── Main Highlight Carousel ────────────────────────────────────────────────

@Composable
fun Highlight(
    pages: List<@Composable () -> Unit>,
    autoScrollDuration: Long = 5000L
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })

    LaunchedEffect(Unit) {
        while (true) {
            yield()
            delay(autoScrollDuration)
            if (pages.isNotEmpty()) {
                val nextPage = (pagerState.currentPage + 1) % pages.size
                pagerState.animateScrollToPage(page = nextPage, animationSpec = tween(600))
            }
        }
    }

    val corner = 16.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            // 1. Sombra suave no container externo
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(corner),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            // 2. Container externo arredondado age como máscara (clip)
            .clip(RoundedCornerShape(corner))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            // 3. Cards internos são totalmente quadrados — sem clip, sem arredondamento
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                pages[page]()
            }
        }

        // 4. Efeito de borda interna sutil (opcional, substitui o drawInsetEdges)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    val cr = corner.toPx()
                    val inset = 32f
                    val clip = Path().apply {
                        addRoundRect(RoundRect(Rect(Offset.Zero, size), CornerRadius(cr, cr)))
                    }
                    clipPath(clip) {
                        drawInsetEdges(inset = inset, cornerPx = cr)
                    }
                }
        )
    }
}

// ─── Default Composables ─────────────────────────────────────────────────────

@Composable
fun HighlightEscalaIndisponivel() {
    HighlightPlaceholder(
        icon = "📅",
        title = "Escala",
        message = "Escala indisponível"
    )
}

@Composable
fun HighlightAniversariantes() {
    HighlightPlaceholder(
        icon = "🎂",
        title = "Aniversariantes do Mês",
        message = "Nenhum aniversariante esse mês"
    )
}

@Composable
fun HighlightEventos() {
    HighlightPlaceholder(
        icon = "📌",
        title = "Eventos",
        message = "Nenhum evento esse mês"
    )
}

@Composable
private fun HighlightPlaceholder(
    icon: String,
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 36.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color(0xFFFFFFFF),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            color = Color(0xFF9E9E9E),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = false)
@Composable
private fun HighlightPreview() {
    Highlight(
        pages = listOf(
            { HighlightEscalaIndisponivel() },
            { HighlightAniversariantes() },
            { HighlightEventos() }
        )
    )
}

// ─── Edge drawing ─────────────────────────────────────────────────────────────

private fun DrawScope.drawInsetEdges(inset: Float, cornerPx: Float) {
    val edgeColor = Color(0x2A000000)
    val transparent = Color.Transparent

    drawRoundRect(
        brush = Brush.verticalGradient(listOf(edgeColor, transparent), 0f, inset),
        topLeft = Offset(0f, 0f), size = Size(size.width, inset),
        cornerRadius = CornerRadius(cornerPx), blendMode = BlendMode.Multiply
    )
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(transparent, edgeColor), size.height - inset, size.height),
        topLeft = Offset(0f, size.height - inset), size = Size(size.width, inset),
        cornerRadius = CornerRadius(cornerPx), blendMode = BlendMode.Multiply
    )
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(edgeColor, transparent), 0f, inset),
        topLeft = Offset(0f, 0f), size = Size(inset, size.height),
        cornerRadius = CornerRadius(cornerPx), blendMode = BlendMode.Multiply
    )
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(transparent, edgeColor), size.width - inset, size.width),
        topLeft = Offset(size.width - inset, 0f), size = Size(inset, size.height),
        cornerRadius = CornerRadius(cornerPx), blendMode = BlendMode.Multiply
    )
    drawRoundRect(
        brush = Brush.radialGradient(
            listOf(Color(0x14FFFFFF), Color.Transparent),
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = size.minDimension * 0.75f
        ),
        cornerRadius = CornerRadius(cornerPx), blendMode = BlendMode.Screen
    )
}