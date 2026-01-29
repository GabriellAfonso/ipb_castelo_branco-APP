package com.gabrielafonso.ipb.castelobranco.ui.screens.base


import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.PaddingValues

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.gabrielafonso.ipb.castelobranco.R


import com.gabrielafonso.ipb.castelobranco.ui.components.TopBar
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
@Composable
fun BaseScreen(
    tabName: String,
    logo: Painter = painterResource(id = R.drawable.sarca_ipb),
    accountImage: Painter = painterResource(id = R.drawable.ic_account),
    showBackArrow: Boolean = false,
    onMenuClick: () -> Unit = {}, // talvez vai ter apenas um menu, definir aqui
    onBackClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    content: @Composable (innerPadding: PaddingValues) -> Unit
) {
    Scaffold(
        containerColor =  MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                tabName = tabName,
                logo = logo,
                accountImage = accountImage,
                showBackArrow = showBackArrow,
                onMenuClick = onMenuClick,
                onBackClick = onBackClick,
                onAccountClick = onAccountClick
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}
