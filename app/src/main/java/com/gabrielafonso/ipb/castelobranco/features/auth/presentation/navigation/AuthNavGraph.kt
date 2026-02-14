package com.gabrielafonso.ipb.castelobranco.features.auth.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gabrielafonso.ipb.castelobranco.features.auth.presentation.viewmodel.AuthViewModel
import com.gabrielafonso.ipb.castelobranco.features.auth.presentation.views.AuthView
import com.gabrielafonso.ipb.castelobranco.features.auth.presentation.views.RegisterView

object AuthRoutes {
    const val AUTH = "AuthView"
    const val REGISTER = "RegisterView"
}

@Composable
fun AuthNavGraph(
    navController: NavHostController,
    onFinish: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    NavHost(navController = navController, startDestination = AuthRoutes.AUTH) {
        composable(AuthRoutes.AUTH) {
            AuthView(
                viewModel = viewModel,
                onBackClick = {
                    val popped = navController.popBackStack()
                    if (!popped) onFinish()
                },
                onNavigateToRegister = {
                    navController.navigate(AuthRoutes.REGISTER)
                }
            )
        }
        composable(AuthRoutes.REGISTER) {
            RegisterView(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
    }
}
