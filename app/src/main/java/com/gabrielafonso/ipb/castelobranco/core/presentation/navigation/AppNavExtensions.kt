package com.gabrielafonso.ipb.castelobranco.core.presentation.navigation

import androidx.navigation.NavHostController

fun NavHostController.safePopBackStack() {
    if (currentBackStackEntry?.destination?.route != AppRoutes.CORE) {
        popBackStack()
    }
}