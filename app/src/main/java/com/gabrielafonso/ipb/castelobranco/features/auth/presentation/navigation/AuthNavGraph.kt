package com.gabrielafonso.ipb.castelobranco.features.auth.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gabrielafonso.ipb.castelobranco.features.auth.presentation.viewmodel.AuthViewModel
import com.gabrielafonso.ipb.castelobranco.features.auth.presentation.screens.AuthScreen
import com.gabrielafonso.ipb.castelobranco.features.auth.presentation.screens.RegisterScreen

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
            AuthScreen(
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
            RegisterScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
    }
}
