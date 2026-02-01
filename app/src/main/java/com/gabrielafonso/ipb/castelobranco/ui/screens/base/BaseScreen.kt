package com.gabrielafonso.ipb.castelobranco.ui.screens.base


import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.DrawableRes
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
    @DrawableRes logoRes: Int = R.drawable.sarca_ipb,
    @DrawableRes accountImageRes: Int = R.drawable.ic_account,
    showBackArrow: Boolean = false,
    onMenuClick: () -> Unit = {}, // talvez vai ter apenas um menu, definir aqui
    onBackClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable (innerPadding: PaddingValues) -> Unit,
) {
    val logo: Painter = painterResource(id = logoRes)
    val accountImage: Painter = painterResource(id = accountImageRes)

    Scaffold(
        containerColor =  containerColor,
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
