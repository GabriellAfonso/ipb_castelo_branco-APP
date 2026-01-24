package com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gabrielafonso.ipb.castelobranco.domain.model.SundaySet
import com.gabrielafonso.ipb.castelobranco.domain.model.SundaySetItem

@Composable
fun Header(titles: List<Pair<String, Float>>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFc7dbd2))
            .padding(vertical = 10.dp)
    ) {
        titles.forEach { (title, weight) ->
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(weight)
                    .padding(start = 10.dp)

            )
        }
    }
}