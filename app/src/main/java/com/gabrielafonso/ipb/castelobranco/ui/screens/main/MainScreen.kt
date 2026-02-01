package com.gabrielafonso.ipb.castelobranco.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.ui.components.Highlight
import com.gabrielafonso.ipb.castelobranco.ui.components.CustomButton
import com.gabrielafonso.ipb.castelobranco.ui.components.ThemeToggle
import com.gabrielafonso.ipb.castelobranco.ui.screens.auth.AuthActivity
import com.gabrielafonso.ipb.castelobranco.ui.screens.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.ui.screens.hymnal.HymnalActions
import com.gabrielafonso.ipb.castelobranco.ui.screens.hymnal.HymnalActivity
import com.gabrielafonso.ipb.castelobranco.ui.screens.hymnal.HymnalScreen
import com.gabrielafonso.ipb.castelobranco.ui.screens.hymnal.HymnalViewModel
import com.gabrielafonso.ipb.castelobranco.ui.screens.monthschedule.MonthScheduleActivity
import com.gabrielafonso.ipb.castelobranco.ui.screens.settings.SettingsActivity
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.WorshipHubActivity
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawer(actions: MainActions, content: @Composable (openDrawer: () -> Unit) -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.8f)) {
                // passa uma função que fecha a drawer e então executa a ação
                DrawerContent(actions) { action ->
                    scope.launch {
                        drawerState.close()
                        action()
                    }
                }
            }
        },
        content = {
            content { scope.launch { drawerState.open() } }
        }
    )
}


@Composable
fun MainView(
    viewModel: MainViewModel = hiltViewModel(),

) {
    val context = LocalContext.current

    val actions =  remember(context) { MainActions(context) }


    MainScreen(
        actions = actions,

    )
}

@Composable
fun MainScreen(
    actions: MainActions
) {

    NavigationDrawer(actions) { openDrawer ->
        BaseScreen(
            tabName = "IPB Castelo Branco",
            onMenuClick = openDrawer // aqui o botão do TopBar chama openDrawer
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
            ButtonInfo(R.drawable.louvor_icon, "Louvor", iconColor, actions::openWorshipHub),
            ButtonInfo(R.drawable.calendar_icon, "Escala", iconColor, actions::openSchedule),
            ButtonInfo(R.drawable.gallery_icon, "Galeria", iconColor) { println("Galeria clicked") },
            ButtonInfo(R.drawable.sarca_ipb, "Hinário", iconColor, actions::openHymnal),
            ButtonInfo(R.drawable.sarca_ipb, "Exemplo", iconColor) { println("Sample 1 clicked") },
            ButtonInfo(R.drawable.sarca_ipb, "Exemplo", iconColor) { println("Sample 2 clicked") }
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

//@Composable
//fun NavigationDrawer(content: @Composable () -> Unit) {
//    val drawerState = rememberDrawerState(DrawerValue.Closed)
//
//    ModalNavigationDrawer(
//        drawerState = drawerState,
//        drawerContent = {
//            ModalDrawerSheet {
//                DrawerContent()
//            }
//        },
//        content = content
//    )
//}
@Composable
fun DrawerContent(actions: MainActions, onItemClick: (action: () -> Unit) -> Unit) {
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.padding(16.dp)) {
        TextButton(
            onClick = { onItemClick { actions.openAuth() } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = textColor)
        ) {
            Text(
                "Entrar",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = textColor
            )
        }

        TextButton(
            onClick = { onItemClick { /* navegar para perfil */ } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = textColor)
        ) {
            Text(
                "Perfil",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = textColor
            )
        }

        TextButton(
            onClick = { onItemClick { actions.openSettings() } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = textColor)
        ) {
            Text(
                "Configurações",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = textColor
            )
        }
    }
}