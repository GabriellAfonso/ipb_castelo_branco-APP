package com.ipb.castelobranco.features.bible.presentation.navigation

import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.ipb.castelobranco.core.presentation.navigation.AppRoutes
import com.ipb.castelobranco.core.presentation.navigation.safePopBackStack
import com.ipb.castelobranco.features.bible.presentation.screens.BibleIndexScreen
import com.ipb.castelobranco.features.bible.presentation.screens.BibleReaderScreen
import com.ipb.castelobranco.features.bible.presentation.viewmodel.BibleViewModel

object BibleRoutes {
    const val Reader      = "BibleReader"
    const val Index       = "BibleIndex"
    const val ArgInitTab  = "tab"

    fun indexRoute(tab: String = "book") = "$Index?$ArgInitTab=$tab"
}

fun NavGraphBuilder.bibleGraph(navController: NavHostController) {
    navigation(
        route            = AppRoutes.BIBLE_GRAPH,
        startDestination = BibleRoutes.Reader,
    ) {
        composable(BibleRoutes.Reader) { entry ->
            val graphEntry = remember(entry) { navController.getBackStackEntry(AppRoutes.BIBLE_GRAPH) }
            val viewModel: BibleViewModel = hiltViewModel(graphEntry)
            BibleReaderScreen(
                viewModel = viewModel,
                onBack = { navController.safePopBackStack() },
                onOpenIndex = { tab -> navController.navigate(BibleRoutes.indexRoute(tab)) },
            )
        }

        composable(
            route = "${BibleRoutes.Index}?${BibleRoutes.ArgInitTab}={${BibleRoutes.ArgInitTab}}",
            arguments = listOf(
                navArgument(BibleRoutes.ArgInitTab) {
                    type = NavType.StringType
                    defaultValue = "book"
                },
            ),
        ) { entry ->
            val tab = entry.arguments?.getString(BibleRoutes.ArgInitTab) ?: "book"
            val graphEntry = remember(entry) { navController.getBackStackEntry(AppRoutes.BIBLE_GRAPH) }
            val viewModel: BibleViewModel = hiltViewModel(graphEntry)
            BibleIndexScreen(
                viewModel = viewModel,
                initialTabKey = tab,
                onBack = { navController.safePopBackStack() },
            )
        }
    }
}
