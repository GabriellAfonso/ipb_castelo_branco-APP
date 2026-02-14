package com.gabrielafonso.ipb.castelobranco.features.hymnal.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gabrielafonso.ipb.castelobranco.features.hymnal.presentation.viewmodel.HymnalViewModel
import com.gabrielafonso.ipb.castelobranco.features.hymnal.presentation.views.HymnDetailView
import com.gabrielafonso.ipb.castelobranco.features.hymnal.presentation.views.HymnalView


object HymnalRoutes {
    const val List = "hymnal_list"
    const val Detail = "hymn_detail"
    const val ArgHymnId = "hymnId"

    fun detailRoute(hymnId: String) = "$Detail/$hymnId"
}

@Composable
fun HymnalNavGraph(
    navController: NavHostController,
    onFinish: () -> Unit,
    viewModel: HymnalViewModel = hiltViewModel()
) {
    NavHost(navController = navController, startDestination = HymnalRoutes.List) {
        composable(HymnalRoutes.List) {
            HymnalView(
                viewModel = viewModel,
                onHymnClick = { hymnId ->
                    navController.navigate(HymnalRoutes.detailRoute(hymnId))
                },
                onBackClick = {
                    val popped = navController.popBackStack()
                    if (!popped) onFinish()
                }
            )
        }

        composable(
            route = "${HymnalRoutes.Detail}/{${HymnalRoutes.ArgHymnId}}",
            arguments = listOf(navArgument(HymnalRoutes.ArgHymnId) { type = NavType.StringType })
        ) { backStackEntry ->
            val hymnId = backStackEntry.arguments?.getString(HymnalRoutes.ArgHymnId).orEmpty()

            HymnDetailView(
                hymnId = hymnId,
                onBack = {
                    val popped = navController.popBackStack()
                    if (!popped) onFinish()
                },
                viewModel = viewModel
            )
        }
    }
}
