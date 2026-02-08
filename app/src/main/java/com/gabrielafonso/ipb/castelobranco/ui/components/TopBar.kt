package com.gabrielafonso.ipb.castelobranco.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.ui.theme.ipbGreen
import com.gabrielafonso.ipb.castelobranco.ui.theme.onPrimaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    tabName: String,
    logo: Painter,
    accountImage: Painter?,
    showBackArrow: Boolean = false,
    onMenuClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onAccountClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ipbGreen,
            titleContentColor = onPrimaryLight,
            navigationIconContentColor = onPrimaryLight,
            actionIconContentColor = onPrimaryLight
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = logo,
                    contentDescription = "Logo",
                    modifier = Modifier.height(24.dp)
                )
                Spacer(modifier = Modifier.width(15.dp))
                TopBarTitle(tabName)
            }
        },
        navigationIcon = { TopBarNavigation(showBackArrow, onMenuClick, onBackClick) },
        actions = {
            if (accountImage != null) {
                TopBarActions(onAccountClick, accountImage)
            }
        }
    )
}

@Composable
private fun TopBarTitle(tabName: String) {
    Text(
        text = tabName,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun TopBarNavigation(
    showBackArrow: Boolean,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showBackArrow) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.back_arrow_icon),
                    contentDescription = "Voltar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
            }
        }
    }
}

@Composable
private fun TopBarActions(onAccountClick: () -> Unit, accountImage: Painter) {
    IconButton(onClick = onAccountClick) {
        Image(
            painter = accountImage,
            contentDescription = "Conta",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
        )
    }
}
