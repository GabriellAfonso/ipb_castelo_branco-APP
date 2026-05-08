package com.ipb.castelobranco.features.studies.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.ipb.castelobranco.core.presentation.navigation.AppRoutes
import com.ipb.castelobranco.core.presentation.navigation.safePopBackStack
import com.ipb.castelobranco.features.studies.presentation.screens.StudiesScreen

object StudiesRoutes {
    const val Hub = "StudiesHub"
}

fun NavGraphBuilder.studiesGraph(navController: NavHostController) {
    navigation(
        route            = AppRoutes.STUDIES_GRAPH,
        startDestination = StudiesRoutes.Hub,
    ) {
        composable(StudiesRoutes.Hub) {
            StudiesScreen(onBackClick = { navController.safePopBackStack() })
        }
    }
}
