package com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.viewmodel.GalleryViewModel
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.screens.AlbumScreen
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.screens.GalleryScreen
import com.gabrielafonso.ipb.castelobranco.features.gallery.presentation.screens.PhotoScreen

@Stable
data class GalleryNav(
    val back: () -> Unit,
    val toAlbum: (Long) -> Unit,
    val toPhoto: (Long, Int) -> Unit
)

object GalleryRoutes {
    const val Gallery = "GalleryMain"
    fun Album(albumId: Long) = "Album/$albumId"
    fun Photo(albumId: Long, photoIndex: Int) = "Photo/$albumId/$photoIndex"
}

@Composable
fun GalleryNavGraph(
    navController: NavHostController,
    onFinish: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val albums by viewModel.albums.collectAsState()

    fun popOrFinish() {
        if (!navController.popBackStack()) onFinish()
    }

    val nav = GalleryNav(
        back = { popOrFinish() },
        toAlbum = { albumId -> navController.navigate(GalleryRoutes.Album(albumId)) },
        toPhoto = { albumId, photoIndex -> navController.navigate(GalleryRoutes.Photo(albumId, photoIndex)) }
    )

    NavHost(navController = navController, startDestination = GalleryRoutes.Gallery) {

        composable(GalleryRoutes.Gallery) {
            GalleryScreen(nav = nav, viewModel = viewModel, albums = albums)
        }

        composable(
            route = "Album/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: 0L
            AlbumScreen(albumId = albumId, viewModel = viewModel, nav = nav)
        }

        composable(
            route = "Photo/{albumId}/{photoIndex}",
            arguments = listOf(
                navArgument("albumId") { type = NavType.LongType },
                navArgument("photoIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: 0L
            val photoIndex = backStackEntry.arguments?.getInt("photoIndex") ?: 0
            PhotoScreen(albumId = albumId, photoIndex = photoIndex, viewModel = viewModel, nav = nav)
        }
    }
}