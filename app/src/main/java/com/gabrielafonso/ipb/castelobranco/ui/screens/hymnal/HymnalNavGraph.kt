package com.gabrielafonso.ipb.castelobranco.ui.screens.hymnal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gabrielafonso.ipb.castelobranco.domain.model.HymnLyric
import com.gabrielafonso.ipb.castelobranco.domain.model.HymnLyricType
import com.gabrielafonso.ipb.castelobranco.ui.screens.base.BaseScreen

object HymnalRoutes {
    const val List = "hymnal_list"
    const val Detail = "hymn_detail"
    const val ArgHymnId = "hymnId"

    fun detailRoute(hymnId: String) = "$Detail/$hymnId"
}

@Composable
fun HymnalNavGraph(
    navController: NavHostController,
    viewModel: HymnalViewModel
) {
    NavHost(navController = navController, startDestination = HymnalRoutes.List) {
        composable(HymnalRoutes.List) {
            HymnalView(
                // adicione este callback na sua HymnalView
                viewModel= viewModel,
                onHymnClick = { hymnId ->
                    navController.navigate(HymnalRoutes.detailRoute(hymnId))
                }
            )
        }

        composable(
            route = "${HymnalRoutes.Detail}/{${HymnalRoutes.ArgHymnId}}",
            arguments = listOf(navArgument(HymnalRoutes.ArgHymnId) { type = NavType.StringType })
        ) { backStackEntry ->
            val hymnId = backStackEntry.arguments?.getString(HymnalRoutes.ArgHymnId).orEmpty()

            HymnDetailView(
                hymnId = hymnId,
                onBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}
