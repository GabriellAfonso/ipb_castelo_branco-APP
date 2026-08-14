package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ipb.castelobranco.core.presentation.theme.BrandColors
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.BarPoint

/**
 * One bar per hymn, drawn with layout rather than a canvas so the number and title stay real,
 * selectable, accessible text.
 *
 * The bar length is [BarPoint.fraction], which the domain derived from the occurrence count.
 * [BarPoint.secondaryLabel] carries device reach and is never encoded as a length — one singing
 * followed by twenty phones is still one singing.
 */
@Composable
fun HorizontalBarChart(
    bars: List<BarPoint>,
    modifier: Modifier = Modifier,
    onBarClick: ((BarPoint) -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        bars.forEach { bar ->
            HorizontalBar(bar = bar, onClick = onBarClick?.let { { it(bar) } })
        }
    }
}

@Composable
private fun HorizontalBar(
    bar: BarPoint,
    onClick: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 4.dp, horizontal = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = bar.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${bar.value}×",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = BrandColors.Green,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BAR_HEIGHT_DP.dp)
                .clip(RoundedCornerShape(BAR_CORNER_DP.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(bar.fraction)
                    .height(BAR_HEIGHT_DP.dp)
                    .clip(RoundedCornerShape(BAR_CORNER_DP.dp))
                    .background(BrandColors.Green),
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = bar.secondaryLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

private const val BAR_HEIGHT_DP = 10
private const val BAR_CORNER_DP = 5

@Preview(showBackground = true)
@Composable
private fun HorizontalBarChartPreview() {
    HorizontalBarChart(
        bars = listOf(
            BarPoint("50 · Grandioso És Tu", 12, "148 aparelhos", 1f, "50"),
            BarPoint("12 · Firme nas Promessas", 7, "63 aparelhos", 0.58f, "12"),
            BarPoint("120 · Saudosa Lembrança", 1, "2 aparelhos", 0.08f, "120"),
        ),
        modifier = Modifier.padding(16.dp),
    )
}
