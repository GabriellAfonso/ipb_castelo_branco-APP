package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ipb.castelobranco.core.presentation.theme.BrandColors
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BarPoint

/**
 * Volume over time, in the chronological order the service returned.
 *
 * Vertical bars need a shared baseline, which layout weights express badly, so this one is drawn
 * on a [Canvas] — with the labels beneath it as ordinary text. The heights come from
 * [BarPoint.fraction], already computed in the domain, so swapping this drawing for something
 * else later touches only this file.
 */
@Composable
fun VerticalBarChart(
    bars: List<BarPoint>,
    modifier: Modifier = Modifier,
) {
    if (bars.isEmpty()) return

    val barColor = BrandColors.Green
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val chartWidth = (bars.size * SLOT_WIDTH_DP).dp

    Column(
        modifier = modifier.horizontalScroll(rememberScrollState()),
    ) {
        Canvas(
            modifier = Modifier
                .width(chartWidth)
                .height(CHART_HEIGHT_DP.dp),
        ) {
            val slot = size.width / bars.size
            val barWidth = slot * BAR_WIDTH_RATIO
            val corner = CornerRadius(barWidth / 2, barWidth / 2)

            bars.forEachIndexed { index, bar ->
                val left = index * slot + (slot - barWidth) / 2
                val barHeight = size.height * bar.fraction

                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = corner,
                )
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = corner,
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.width(chartWidth)) {
            bars.forEach { bar ->
                Column(
                    modifier = Modifier.width(SLOT_WIDTH_DP.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = bar.value.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = bar.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

private const val CHART_HEIGHT_DP = 140
private const val SLOT_WIDTH_DP = 44
private const val BAR_WIDTH_RATIO = 0.55f

@Preview(showBackground = true)
@Composable
private fun VerticalBarChartPreview() {
    VerticalBarChart(
        bars = listOf(
            BarPoint("09/08", 6, "84 aparelhos", 1f),
            BarPoint("12/08", 2, "9 aparelhos", 0.33f),
            BarPoint("16/08", 5, "77 aparelhos", 0.83f),
        ),
        modifier = Modifier.padding(16.dp),
    )
}
