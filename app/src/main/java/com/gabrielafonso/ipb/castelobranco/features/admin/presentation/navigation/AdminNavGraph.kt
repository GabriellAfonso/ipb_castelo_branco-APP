package com.gabrielafonso.ipb.castelobranco.features.admin.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gabrielafonso.ipb.castelobranco.features.admin.presentation.views.AdminView
import com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.views.MusicRegistrationView

@Stable
data class AdminNav(
    val back: () -> Unit,
    val register: () -> Unit,
)

object AdminRoutes {
    const val ADMIN = "AdminMain"
    const val REGISTER = "AdminRegister"
}

@Composable
fun AdminNavGraph(
    navController: NavHostController,
    onFinish: () -> Unit,
) {

    fun popOrFinish() {
        if (!navController.popBackStack()) onFinish()
    }

    val nav = AdminNav(
        back = { popOrFinish() },
        register = { navController.navigate(AdminRoutes.REGISTER) },
    )

    NavHost(navController = navController, startDestination = AdminRoutes.ADMIN) {

        composable(AdminRoutes.ADMIN) {
            AdminView(nav = nav)
        }

        composable(AdminRoutes.REGISTER) {
            MusicRegistrationView(nav)
        }
    }
}