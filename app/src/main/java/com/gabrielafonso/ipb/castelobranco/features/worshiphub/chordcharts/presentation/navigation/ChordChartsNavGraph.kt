package com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.screens.ChordChartDetailScreen
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.screens.ChordChartsScreen
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.viewmodel.ChordChartDetailViewModel
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.chordcharts.presentation.viewmodel.ChordChartsViewModel
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.hub.presentation.navigation.WorshipHubRoutes
import com.gabrielafonso.ipb.castelobranco.core.presentation.navigation.safePopBackStack

private object ChordChartsRoutes {
    const val List   = "chord_charts_list"
    const val Detail = "chord_chart_detail/{chordChartId}"

    fun detail(id: Int) = "chord_chart_detail/$id"
}

fun NavGraphBuilder.chordChartsGraph(navController: NavHostController) {
    navigation(
        route            = WorshipHubRoutes.Button3,
        startDestination = ChordChartsRoutes.List,
    ) {
        composable(ChordChartsRoutes.List) {
            val viewModel: ChordChartsViewModel = hiltViewModel()
            ChordChartsScreen(
                viewModel         = viewModel,
                onChordChartClick = { id -> navController.navigate(ChordChartsRoutes.detail(id)) },
                onBackClick       = { navController.safePopBackStack() },
            )
        }

        composable(
            route     = ChordChartsRoutes.Detail,
            arguments = listOf(navArgument("chordChartId") { type = NavType.IntType }),
        ) {
            val viewModel: ChordChartDetailViewModel = hiltViewModel()
            ChordChartDetailScreen(
                viewModel   = viewModel,
                onBackClick = { navController.safePopBackStack() },
            )
        }
    }
}
