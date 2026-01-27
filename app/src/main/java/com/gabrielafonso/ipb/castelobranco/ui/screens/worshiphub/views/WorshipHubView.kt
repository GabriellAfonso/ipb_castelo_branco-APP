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
fun WorshipHubView(onTablesClick: () -> Unit) {
    val activity = LocalContext.current.findActivity()

    BaseScreen(
        tabName = "Louvor",
        logo = painterResource(id = R.drawable.louvor_icon),
        showBackArrow = true,
        onBackClick = { activity?.finish() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
//            Highlight()
            Spacer(modifier = Modifier.height(60.dp))

            CustomButton(
                image = painterResource(id = R.drawable.table_icon),
                onClick = onTablesClick,
                text = "Tabelas",
                backgroundColor = Color(0xFF157C53),
                size = 100.dp
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
