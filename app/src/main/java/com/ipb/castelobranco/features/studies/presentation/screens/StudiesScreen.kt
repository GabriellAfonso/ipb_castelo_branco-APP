package com.ipb.castelobranco.features.studies.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ipb.castelobranco.core.presentation.base.BaseScreen

@Composable
fun StudiesScreen(onBackClick: () -> Unit) {
    BaseScreen(
        tabName = "Estudos",
        showBackArrow = true,
        onBackClick = onBackClick,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Em Breve",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
