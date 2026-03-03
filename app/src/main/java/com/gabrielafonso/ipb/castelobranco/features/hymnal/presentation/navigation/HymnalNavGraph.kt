package com.gabrielafonso.ipb.castelobranco.features.hymnal.presentation.navigation

import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.gabrielafonso.ipb.castelobranco.core.presentation.navigation.AppRoutes
import com.gabrielafonso.ipb.castelobranco.features.hymnal.presentation.screens.HymnDetailScreen
import com.gabrielafonso.ipb.castelobranco.features.hymnal.presentation.screens.HymnalScreen
import com.gabrielafonso.ipb.castelobranco.features.hymnal.presentation.viewmodel.HymnalViewModel

object HymnalRoutes {
    const val List      = "hymnal_list"
    const val Detail    = "hymn_detail"
    const val ArgHymnId = "hymnId"

    fun detailRoute(hymnId: String) = "$Detail/$hymnId"
}

fun NavGraphBuilder.hymnalGraph(navController: NavHostController) {
    navigation(
        route            = AppRoutes.HYMNAL_GRAPH,
        startDestination = HymnalRoutes.List,
    ) {
        composable(HymnalRoutes.List) { entry ->
            val graphEntry = remember(entry) { navController.getBackStackEntry(AppRoutes.HYMNAL_GRAPH) }
            val viewModel: HymnalViewModel = hiltViewModel(graphEntry)
            HymnalScreen(
                viewModel   = viewModel,
                onHymnClick = { hymnId -> navController.navigate(HymnalRoutes.detailRoute(hymnId)) },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(
            route     = "${HymnalRoutes.Detail}/{${HymnalRoutes.ArgHymnId}}",
            arguments = listOf(navArgument(HymnalRoutes.ArgHymnId) { type = NavType.StringType }),
        ) { backStackEntry ->
            val hymnId     = backStackEntry.arguments?.getString(HymnalRoutes.ArgHymnId).orEmpty()
            val graphEntry = remember(backStackEntry) { navController.getBackStackEntry(AppRoutes.HYMNAL_GRAPH) }
            val viewModel: HymnalViewModel = hiltViewModel(graphEntry)
            HymnDetailScreen(
                hymnId    = hymnId,
                onBack    = { navController.popBackStack() },
                viewModel = viewModel,
            )
        }
    }
}
