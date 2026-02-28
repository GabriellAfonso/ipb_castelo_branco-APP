package com.gabrielafonso.ipb.castelobranco.features.worshiphub.hub.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gabrielafonso.ipb.castelobranco.core.ui.components.InDevelopmentScreen
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.hub.presentation.views.WorshipHubView
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.presentation.viewmodel.SongsTableViewModel
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.presentation.views.WorshipSongsTableView

@Stable
data class WorshipHubNav(
    val tables: () -> Unit,
    val songs: () -> Unit,
    val button3: () -> Unit,
    val button4: () -> Unit,
    val button5: () -> Unit,
    val button6: () -> Unit,
    val button7: () -> Unit,
    val button8: () -> Unit,
    val back: () -> Unit
)

object WorshipHubRoutes {
    const val Hub = "WorshipMain"
    const val Tables = "WorshipTables"
    const val Songs = "songs"
    const val Button3 = "button_3"
    const val Button4 = "button_4"
    const val Button5 = "button_5"
    const val Button6 = "button_6"
    const val Button7 = "button_7"
    const val Button8 = "button_8"
}

@Composable
fun WorshipHubNavGraph(
    navController: NavHostController,
    onFinish: () -> Unit,
) {
    fun popOrFinish() {
        val popped = navController.popBackStack()
        if (!popped) onFinish()
    }

    val nav =
        WorshipHubNav(
            tables = { navController.navigate(WorshipHubRoutes.Tables) },
            songs = { navController.navigate(WorshipHubRoutes.Songs) },
            button3 = { navController.navigate(WorshipHubRoutes.Button3) },
            button4 = { navController.navigate(WorshipHubRoutes.Button4) },
            button5 = { navController.navigate(WorshipHubRoutes.Button5) },
            button6 = { navController.navigate(WorshipHubRoutes.Button6) },
            button7 = { navController.navigate(WorshipHubRoutes.Button7) },
            button8 = { navController.navigate(WorshipHubRoutes.Button8) },
            back = { popOrFinish() }
        )

    NavHost(
        navController = navController,
        startDestination = WorshipHubRoutes.Hub
    ) {

        composable(WorshipHubRoutes.Hub) {

            WorshipHubView(
                nav = nav
            )
        }

        composable(WorshipHubRoutes.Tables) {
            val viewModel: SongsTableViewModel = hiltViewModel()
            WorshipSongsTableView(
                onBackClick = { popOrFinish() },
                viewModel = viewModel
            )
        }

        composable(WorshipHubRoutes.Songs) { InDevelopmentScreen(onBack = ::popOrFinish) }
        composable(WorshipHubRoutes.Button4) { InDevelopmentScreen(onBack = ::popOrFinish) }
        composable(WorshipHubRoutes.Button5) { InDevelopmentScreen(onBack = ::popOrFinish) }
        composable(WorshipHubRoutes.Button6) { InDevelopmentScreen(onBack = ::popOrFinish) }
        composable(WorshipHubRoutes.Button7) { InDevelopmentScreen(onBack = ::popOrFinish) }
        composable(WorshipHubRoutes.Button8) { InDevelopmentScreen(onBack = ::popOrFinish) }
    }
}