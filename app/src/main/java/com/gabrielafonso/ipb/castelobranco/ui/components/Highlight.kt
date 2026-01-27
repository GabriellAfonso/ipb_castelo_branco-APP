package com.gabrielafonso.ipb.castelobranco.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Highlight() {
    val corner = 24.dp
    val background = Color(0xFFF7F7F7)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(250.dp)
            .background(background)
            .drawWithContent {
                drawContent()

                val cr = corner.toPx()
                val inset = (size.minDimension * 0.12f).coerceIn(18f, 48f)

                val clip = Path().apply {
                    addRoundRect(
                        androidx.compose.ui.geometry.RoundRect(
                            rect = androidx.compose.ui.geometry.Rect(Offset.Zero, size),
                            cornerRadius = CornerRadius(cr, cr)
                        )
                    )
                }

                clipPath(clip) {
                    drawInsetEdges(inset = inset, cornerPx = cr)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "DESTAQUE",
            color = Color(0xFF6B6B6B),
            fontWeight = FontWeight.Bold
        )
    }
}

private fun DrawScope.drawInsetEdges(
    inset: Float,
    cornerPx: Float,
) {
    // Darken nas bordas indo para o centro (efeito "buraco")
    val edgeColor = Color(0x2A000000)
    val transparent = Color.Transparent

    // Top (escuro no topo, transparente para baixo)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(edgeColor, transparent),
            startY = 0f,
            endY = inset
        ),
        topLeft = Offset(0f, 0f),
        size = Size(size.width, inset),
        cornerRadius = CornerRadius(cornerPx, cornerPx),
        blendMode = BlendMode.Multiply
    )

    // Bottom (escuro em baixo, transparente para cima)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(transparent, edgeColor),
            startY = size.height - inset,
            endY = size.height
        ),
        topLeft = Offset(0f, size.height - inset),
        size = Size(size.width, inset),
        cornerRadius = CornerRadius(cornerPx, cornerPx),
        blendMode = BlendMode.Multiply
    )

    // Left (escuro à esquerda, transparente para direita)
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(edgeColor, transparent),
            startX = 0f,
            endX = inset
        ),
        topLeft = Offset(0f, 0f),
        size = Size(inset, size.height),
        cornerRadius = CornerRadius(cornerPx, cornerPx),
        blendMode = BlendMode.Multiply
    )

    // Right (escuro à direita, transparente para esquerda)
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(transparent, edgeColor),
            startX = size.width - inset,
            endX = size.width
        ),
        topLeft = Offset(size.width - inset, 0f),
        size = Size(inset, size.height),
        cornerRadius = CornerRadius(cornerPx, cornerPx),
        blendMode = BlendMode.Multiply
    )

    // Leve realce interno no centro (opcional, ajuda a "cavar")
    val centerLight = Brush.radialGradient(
        colors = listOf(Color(0x14FFFFFF), Color.Transparent),
        center = Offset(size.width * 0.5f, size.height * 0.5f),
        radius = size.minDimension * 0.75f
    )
    drawRoundRect(
        brush = centerLight,
        topLeft = Offset.Zero,
        size = size,
        cornerRadius = CornerRadius(cornerPx, cornerPx),
        blendMode = BlendMode.Screen
    )
}
