package com.gabrielafonso.ipb.castelobranco.features.admin.panel.presentation.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gabrielafonso.ipb.castelobranco.features.admin.panel.presentation.screens.AdminScreen
import com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.screens.MusicRegistrationScreen
import com.gabrielafonso.ipb.castelobranco.features.admin.schedule.presentation.screens.AdminScheduleScreen

@Stable
data class AdminNav(
    val back: () -> Unit,
    val register: () -> Unit,
    val schedule: () -> Unit,
)

object AdminRoutes {
    const val ADMIN = "AdminMain"
    const val REGISTER = "AdminRegister"
    const val SCHEDULE = "AdminSchedule"
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
        schedule = { navController.navigate(AdminRoutes.SCHEDULE) },
    )

    NavHost(navController = navController, startDestination = AdminRoutes.ADMIN) {

        composable(AdminRoutes.ADMIN) {
            AdminScreen(nav = nav)
        }

        composable(AdminRoutes.REGISTER) {
            MusicRegistrationScreen(nav)
        }

        composable(AdminRoutes.SCHEDULE) {
            AdminScheduleScreen(
                nav = nav,
                onShare = { text ->
                    // mesmo Intent que você já usa no schedule normal
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    navController.context.startActivity(
                        Intent.createChooser(intent, "Compartilhar escala")
                    )
                }
            )
        }
    }
}
