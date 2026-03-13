package com.gabrielafonso.ipb.castelobranco.core.presentation.navigation

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gabrielafonso.ipb.castelobranco.features.admin.panel.presentation.navigation.adminGraph
import com.gabrielafonso.ipb.castelobranco.features.auth.presentation.navigation.authGraph
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.navigation.galleryGraph
import com.gabrielafonso.ipb.castelobranco.features.hymnal.presentation.navigation.hymnalGraph
import com.gabrielafonso.ipb.castelobranco.core.presentation.screens.CoreView
import com.gabrielafonso.ipb.castelobranco.features.profile.presentation.screens.ProfileScreen
import com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.screens.MonthScheduleScreen
import com.gabrielafonso.ipb.castelobranco.features.settings.presentation.screens.SettingsScreen
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.hub.presentation.navigation.worshipHubGraph

@Composable
fun AppNavHost(navController: NavHostController) {
    val context = LocalContext.current

    val appNavigator = remember(navController) {
        AppNavigator(
            navigateToProfile = { navController.navigate(AppRoutes.PROFILE) },
            navigateToAuth    = { navController.navigate(AppRoutes.AUTH_GRAPH) },
        )
    }

    CompositionLocalProvider(LocalAppNavigator provides appNavigator) {
        NavHost(navController = navController, startDestination = AppRoutes.CORE) {

            composable(AppRoutes.CORE) {
                BackHandler {}
                CoreView(
                    onNavigateToAuth       = { navController.navigate(AppRoutes.AUTH_GRAPH) },
                    onNavigateToWorshipHub = { navController.navigate(AppRoutes.WORSHIP_HUB_GRAPH) },
                    onNavigateToSchedule   = { navController.navigate(AppRoutes.SCHEDULE) },
                    onNavigateToGallery    = { navController.navigate(AppRoutes.GALLERY_GRAPH) },
                    onNavigateToHymnal     = { navController.navigate(AppRoutes.HYMNAL_GRAPH) },
                    onNavigateToSettings   = { navController.navigate(AppRoutes.SETTINGS) },
                    onNavigateToAdmin      = { navController.navigate(AppRoutes.ADMIN_GRAPH) },
                    onLogoutSuccess        = {},
                )
            }

            authGraph(
                navController = navController,
                onAuthSuccess = {
                    navController.navigate(AppRoutes.CORE) {
                        popUpTo(AppRoutes.AUTH_GRAPH) { inclusive = true }
                    }
                    Toast.makeText(context, "Sessão iniciada", Toast.LENGTH_SHORT).show()
                },
            )

            adminGraph(navController)
            worshipHubGraph(navController)
            galleryGraph(navController)
            hymnalGraph(navController)

            composable(AppRoutes.SCHEDULE) {
                MonthScheduleScreen(
                    onBackClick = { navController.safePopBackStack() },
                    onShare     = { text ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartilhar"))
                    },
                )
            }

            composable(AppRoutes.SETTINGS) {
                SettingsScreen(onBackClick = { navController.safePopBackStack() })
            }

            composable(AppRoutes.PROFILE) {
                ProfileScreen(onBackClick = { navController.safePopBackStack() })
            }
        }
    }
}
