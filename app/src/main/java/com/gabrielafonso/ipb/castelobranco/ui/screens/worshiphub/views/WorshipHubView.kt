package com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.views

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.ui.components.CustomButton
import com.gabrielafonso.ipb.castelobranco.ui.screens.base.BaseScreen


@Composable
fun WorshipHubView(
    onTablesClick: () -> Unit,
    onBackClick: () -> Unit
) {
    BaseScreen(
        tabName = "Louvor",
        logoRes = R.drawable.louvor_icon,
        showBackArrow = true,
        onBackClick = onBackClick
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Spacer(modifier = Modifier.height(60.dp))

            CustomButton(
                image = painterResource(id = R.drawable.table_icon),
                onClick = onTablesClick,
                text = "Tabelas",
                size = 100.dp
            )
        }
    }
}
