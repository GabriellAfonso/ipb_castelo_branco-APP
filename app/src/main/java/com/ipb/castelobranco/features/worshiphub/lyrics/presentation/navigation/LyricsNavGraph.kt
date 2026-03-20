package com.ipb.castelobranco.features.worshiphub.lyrics.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.ipb.castelobranco.core.presentation.navigation.safePopBackStack
import com.ipb.castelobranco.features.worshiphub.hub.presentation.navigation.WorshipHubRoutes
import com.ipb.castelobranco.features.worshiphub.lyrics.presentation.screens.LyricsDetailScreen
import com.ipb.castelobranco.features.worshiphub.lyrics.presentation.screens.LyricsScreen
import com.ipb.castelobranco.features.worshiphub.lyrics.presentation.viewmodel.LyricsDetailViewModel
import com.ipb.castelobranco.features.worshiphub.lyrics.presentation.viewmodel.LyricsViewModel

private object LyricsRoutes {
    const val List   = "lyrics_list"
    const val Detail = "lyrics_detail/{lyricsId}"

    fun detail(id: Int) = "lyrics_detail/$id"
}

fun NavGraphBuilder.lyricsGraph(navController: NavHostController) {
    navigation(
        route            = WorshipHubRoutes.Button4,
        startDestination = LyricsRoutes.List,
    ) {
        composable(LyricsRoutes.List) {
            val viewModel: LyricsViewModel = hiltViewModel()
            LyricsScreen(
                viewModel     = viewModel,
                onLyricsClick = { id -> navController.navigate(LyricsRoutes.detail(id)) },
                onBackClick   = { navController.safePopBackStack() },
            )
        }

        composable(
            route     = LyricsRoutes.Detail,
            arguments = listOf(navArgument("lyricsId") { type = NavType.IntType }),
        ) {
            val viewModel: LyricsDetailViewModel = hiltViewModel()
            LyricsDetailScreen(
                viewModel   = viewModel,
                onBackClick = { navController.safePopBackStack() },
            )
        }
    }
}
