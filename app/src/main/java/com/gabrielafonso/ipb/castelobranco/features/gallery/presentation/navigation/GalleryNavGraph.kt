package com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.viewmodel.GalleryViewModel
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.views.GalleryView
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.hub.presentation.navigation.WorshipHubNav
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.hub.presentation.navigation.WorshipHubRoutes


@Stable
data class GalleryNav(
    val Gallery: () -> Unit,
    val Album: () -> Unit,
    val Photo: () -> Unit,
    val back: () -> Unit
)

object GalleryRoutes {
    const val Gallery = "GalleryMain"
    const val Album = "GalleryAlbum"
    const val Photo = "GalleryPhoto"

}

@Composable
fun GalleryNavGraph(
    navController: NavHostController,
    onFinish: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()

) {
    fun popOrFinish() {
        val popped = navController.popBackStack()
        if (!popped) onFinish()
    }
    val nav =
        GalleryNav(
            Gallery = { navController.navigate(GalleryRoutes.Gallery) },
            Album = { navController.navigate(GalleryRoutes.Album) },
            Photo = { navController.navigate(GalleryRoutes.Photo) },
            back = { popOrFinish() }
    )

    NavHost(navController = navController, startDestination = GalleryRoutes.Gallery) {

        composable(GalleryRoutes.Gallery) {
            GalleryView(
                nav = nav,
                viewModel = viewModel,
                onBackClick = {
                    val popped = navController.popBackStack()
                    if (!popped) onFinish()
                }
            )
        }
        composable(GalleryRoutes.Album) {

        }
        composable(GalleryRoutes.Photo) {

        }




    }

}
