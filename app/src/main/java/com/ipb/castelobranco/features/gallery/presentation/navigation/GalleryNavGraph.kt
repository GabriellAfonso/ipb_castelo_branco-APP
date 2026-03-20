package com.ipb.castelobranco.features.gallery.presentation.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.ipb.castelobranco.core.presentation.navigation.AppRoutes
import com.ipb.castelobranco.core.presentation.navigation.safePopBackStack
import com.ipb.castelobranco.features.gallery.presentation.screens.AlbumScreen
import com.ipb.castelobranco.features.gallery.presentation.screens.GalleryScreen
import com.ipb.castelobranco.features.gallery.presentation.screens.PhotoScreen
import com.ipb.castelobranco.features.gallery.presentation.viewmodel.GalleryViewModel

@Stable
data class GalleryNav(
    val back: () -> Unit,
    val toAlbum: (Long) -> Unit,
    val toPhoto: (Long, Int) -> Unit,
)

object GalleryRoutes {
    const val Gallery = "GalleryMain"
    fun Album(albumId: Long) = "Album/$albumId"
    fun Photo(albumId: Long, photoIndex: Int) = "Photo/$albumId/$photoIndex"
}

fun NavGraphBuilder.galleryGraph(navController: NavHostController) {
    fun nav() = GalleryNav(
        back    = { navController.safePopBackStack() },
        toAlbum = { albumId -> navController.navigate(GalleryRoutes.Album(albumId)) },
        toPhoto = { albumId, idx -> navController.navigate(GalleryRoutes.Photo(albumId, idx)) },
    )

    navigation(
        route            = AppRoutes.GALLERY_GRAPH,
        startDestination = GalleryRoutes.Gallery,
    ) {
        composable(GalleryRoutes.Gallery) { entry ->
            val graphEntry = remember(entry) { navController.getBackStackEntry(AppRoutes.GALLERY_GRAPH) }
            val viewModel: GalleryViewModel = hiltViewModel(graphEntry)
            val albums by viewModel.albums.collectAsState()
            GalleryScreen(nav = nav(), viewModel = viewModel, albums = albums)
        }

        composable(
            route     = "Album/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val albumId    = backStackEntry.arguments?.getLong("albumId") ?: 0L
            val graphEntry = remember(backStackEntry) { navController.getBackStackEntry(AppRoutes.GALLERY_GRAPH) }
            val viewModel: GalleryViewModel = hiltViewModel(graphEntry)
            AlbumScreen(albumId = albumId, viewModel = viewModel, nav = nav())
        }

        composable(
            route     = "Photo/{albumId}/{photoIndex}",
            arguments = listOf(
                navArgument("albumId") { type = NavType.LongType },
                navArgument("photoIndex") { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val albumId    = backStackEntry.arguments?.getLong("albumId") ?: 0L
            val photoIndex = backStackEntry.arguments?.getInt("photoIndex") ?: 0
            val graphEntry = remember(backStackEntry) { navController.getBackStackEntry(AppRoutes.GALLERY_GRAPH) }
            val viewModel: GalleryViewModel = hiltViewModel(graphEntry)
            PhotoScreen(albumId = albumId, photoIndex = photoIndex, viewModel = viewModel, nav = nav())
        }
    }
}
