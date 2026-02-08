// app/src/main/java/com/gabrielafonso/ipb/castelobranco/ui/screens/main/MainScreen.kt
package com.gabrielafonso.ipb.castelobranco.ui.screens.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.ui.components.CustomButton
import com.gabrielafonso.ipb.castelobranco.ui.components.Highlight
import com.gabrielafonso.ipb.castelobranco.ui.screens.auth.AuthActivity
import com.gabrielafonso.ipb.castelobranco.ui.screens.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.ui.screens.hymnal.HymnalActivity
import com.gabrielafonso.ipb.castelobranco.ui.screens.monthschedule.MonthScheduleActivity
import com.gabrielafonso.ipb.castelobranco.ui.screens.settings.SettingsActivity
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.WorshipHubActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon

import androidx.compose.runtime.key

@Composable
fun NavigationDrawer(
    actions: MainActions,
    isLoggedIn: Boolean,
    onLogout: () -> Unit,
    content: @Composable (openDrawer: () -> Unit) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Força recomposição do conteúdo do drawer quando isLoggedIn mudar
            key(isLoggedIn) {
                ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.8f)) {
                    DrawerContent(
                        actions = actions,
                        isLoggedIn = isLoggedIn,
                        onLogout = onLogout
                    ) { action ->
                        scope.launch {
                            drawerState.close()
                            action()
                        }
                    }
                }
            }
        },
        content = { content { scope.launch { drawerState.open() } } }
    )
}

@Composable
fun MainView(
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val actions = remember(context) { MainActions(context) }
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()


    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MainViewModel.MainEvent.LogoutSuccess -> {
                    (context as? Activity)?.goToMainAsRoot(message = "Você saiu da conta")
                }
            }
        }
    }
    MainScreen(
        actions = actions,
        isLoggedIn = isLoggedIn,
        onLogout = viewModel::logout
    )
}

@Composable
fun MainScreen(
    actions: MainActions,
    isLoggedIn: Boolean,
    onLogout: () -> Unit
) {
    NavigationDrawer(
        actions = actions,
        isLoggedIn = isLoggedIn,
        onLogout = onLogout
    ) { openDrawer ->
        BaseScreen(
            tabName = "IPB Castelo Branco",
            onMenuClick = openDrawer
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                Highlight()
                Spacer(modifier = Modifier.height(60.dp))
                ButtonGrid(actions = actions)
            }
        }
    }
}

class MainActions(private val context: Context) {
    fun openWorshipHub() = openActivity(WorshipHubActivity::class.java)
    fun openSchedule() = openActivity(MonthScheduleActivity::class.java)
    fun openHymnal() = openActivity(HymnalActivity::class.java)
    fun openAuth() = openActivity(AuthActivity::class.java)
    fun openSettings() = openActivity(SettingsActivity::class.java)

    private fun <T> openActivity(activity: Class<T>) {
        context.startActivity(Intent(context, activity))
    }
}

data class ButtonInfo(
    val iconRes: Int,
    val label: String,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun ButtonGrid(actions: MainActions) {
    val iconColor = MaterialTheme.colorScheme.primaryContainer

    val buttons = remember {
        listOf(
            ButtonInfo(R.drawable.ic_worshiphub, "Min. Louvor", iconColor, actions::openWorshipHub),
            ButtonInfo(R.drawable.ic_schedule, "Escala", iconColor, actions::openSchedule),
            ButtonInfo(R.drawable.ic_galery, "Galeria", iconColor) { println("Galeria clicked") },
            ButtonInfo(R.drawable.ic_sarca_ipb, "Hinário", iconColor, actions::openHymnal),
            ButtonInfo(R.drawable.ic_in_development, "In Dev", iconColor) { },
            ButtonInfo(R.drawable.ic_in_development, "In Dev", iconColor) { println("Sample 2 clicked") }
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ButtonRow(buttons.subList(0, 3))
        ButtonRow(buttons.subList(3, 6))
    }
}

@Composable
private fun ButtonRow(rowButtons: List<ButtonInfo>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(25.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        rowButtons.forEach { button ->
            CustomButton(
                image = painterResource(id = button.iconRes),
                text = button.label,
                backgroundColor = button.color,
                onClick = button.onClick
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    iconRes: Int,
    label: String,
    textColor: Color,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = textColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = textColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = textColor
            )
        }
    }
}

@Composable
fun DrawerContent(
    actions: MainActions,
    isLoggedIn: Boolean,
    onLogout: () -> Unit,
    onItemClick: (action: () -> Unit) -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.padding(16.dp)) {

        if (!isLoggedIn) {
            DrawerMenuItem(
                iconRes = R.drawable.ic_login, // troque pelo seu drawable
                label = "Entrar",
                textColor = textColor
            ) {
                onItemClick { actions.openAuth() }
            }
        }

        DrawerMenuItem(
            iconRes = R.drawable.ic_settings, // troque pelo seu drawable
            label = "Configurações",
            textColor = textColor
        ) {
            onItemClick { actions.openSettings() }
        }

        if (isLoggedIn) {
            DrawerMenuItem(
                iconRes = R.drawable.ic_logout, // troque pelo seu drawable
                label = "Logout",
                textColor = textColor
            ) {
                onItemClick { onLogout() }
            }
        }
    }
}