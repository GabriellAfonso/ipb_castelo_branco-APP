package com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.views.WorshipHubView
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.views.WorshipSongsTableView

object WorshipHubRoutes {
    const val Hub = "worship_hub"
    const val Tables = "tables"
    const val Other = "other"
}

@Composable
fun WorshipHubNavGraph(
    navController: NavHostController,
    onFinish: () -> Unit,
    viewModel: WorshipHubViewModel = hiltViewModel()
) {
    NavHost(navController = navController, startDestination = WorshipHubRoutes.Hub) {
        composable(WorshipHubRoutes.Hub) {
            WorshipHubView(
                onTablesClick = { navController.navigate(WorshipHubRoutes.Tables) },
                onBackClick = {
                    val popped = navController.popBackStack()
                    if (!popped) onFinish()
                }
            )
        }
        composable(WorshipHubRoutes.Tables) {
            WorshipSongsTableView(
                onBackClick = {
                    val popped = navController.popBackStack()
                    if (!popped) onFinish()
                },
                viewModel = viewModel
            )
        }
    }
}