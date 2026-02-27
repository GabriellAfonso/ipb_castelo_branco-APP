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
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.views.AlbumScreen
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.views.GalleryView
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.views.PhotoScreen

@Stable
data class GalleryNav(
    val back: () -> Unit,
    val toAlbum: (Long) -> Unit,
    val toPhoto: (Long, Int) -> Unit
)

object GalleryRoutes {
    const val Gallery = "GalleryMain"
    fun Album(albumId: Long) = "GalleryAlbum/$albumId"
    fun Photo(albumId: Long, photoIndex: Int) = "GalleryPhoto/$albumId/$photoIndex"
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
    val nav = GalleryNav(
        back = { popOrFinish() },
        toAlbum = { albumId -> navController.navigate(GalleryRoutes.Album(albumId)) },
        toPhoto = { albumId, photoIndex -> navController.navigate(GalleryRoutes.Photo(albumId, photoIndex)) }
    )

    NavHost(navController = navController, startDestination = GalleryRoutes.Gallery) {

        composable(GalleryRoutes.Gallery) {
            val viewModel = viewModel
            GalleryView(
                nav = nav,
                viewModel = viewModel,
                onBackClick = { popOrFinish() }
            )
        }
        composable(
            "GalleryAlbum/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) { backStackEntry ->
            val viewModel = viewModel
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: 0L
            AlbumScreen(albumId, viewModel, nav)
        }
        composable(
            "GalleryPhoto/{albumId}/{photoIndex}",
            arguments = listOf(
                navArgument("albumId") { type = NavType.LongType },
                navArgument("photoIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val viewModel = viewModel
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: 0L
            val photoIndex = backStackEntry.arguments?.getInt("photoIndex") ?: 0
            PhotoScreen(albumId, photoIndex, viewModel, nav)
        }
    }
}