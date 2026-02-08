package com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.ui.screens.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.views.MusicRegistrationView
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.views.WorshipSongsTableView

object WorshipHubRoutes {
    const val Hub = "worship_hub"
    const val Tables = "tables"
    const val Register = "register"
    const val Button3 = "button_3"
    const val Button4 = "button_4"
    const val Button5 = "button_5"
    const val Button6 = "button_6"
    const val Button7 = "button_7"
    const val Button8 = "button_8"
}

@Composable
fun WorshipHubNavGraph(
    navController: NavHostController,
    onFinish: () -> Unit,
    viewModel: WorshipHubViewModel = hiltViewModel()
) {
    fun popOrFinish() {
        val popped = navController.popBackStack()
        if (!popped) onFinish()
    }

    NavHost(
        navController = navController,
        startDestination = WorshipHubRoutes.Hub
    ) {
        val nav = WorshipHubNav(
            tables = { navController.navigate(WorshipHubRoutes.Tables) },
            register = { navController.navigate(WorshipHubRoutes.Register) },
            button3 = { navController.navigate(WorshipHubRoutes.Button3) },
            button4 = { navController.navigate(WorshipHubRoutes.Button4) },
            button5 = { navController.navigate(WorshipHubRoutes.Button5) },
            button6 = { navController.navigate(WorshipHubRoutes.Button6) },
            button7 = { navController.navigate(WorshipHubRoutes.Button7) },
            button8 = { navController.navigate(WorshipHubRoutes.Button8) },
            back = { popOrFinish() }
        )

        composable(WorshipHubRoutes.Hub) {
            WorshipHubView(
                nav = nav
            )
        }

        composable(WorshipHubRoutes.Tables) {
            WorshipSongsTableView(
                onBackClick = { popOrFinish() },
                viewModel = viewModel
            )
        }

        composable(WorshipHubRoutes.Register) { MusicRegistrationView( viewModel = viewModel,onBack = ::popOrFinish) }
        composable(WorshipHubRoutes.Button3) { WorshipHubPlaceholderScreen(title = "Area 1", onBack = ::popOrFinish) }
        composable(WorshipHubRoutes.Button4) { WorshipHubPlaceholderScreen(title = "Area 2", onBack = ::popOrFinish) }
        composable(WorshipHubRoutes.Button5) { WorshipHubPlaceholderScreen(title = "Area 3", onBack = ::popOrFinish) }
        composable(WorshipHubRoutes.Button6) { WorshipHubPlaceholderScreen(title = "Area 4", onBack = ::popOrFinish) }
        composable(WorshipHubRoutes.Button7) { WorshipHubPlaceholderScreen(title = "Area 5", onBack = ::popOrFinish) }
        composable(WorshipHubRoutes.Button8) { WorshipHubPlaceholderScreen(title = "Area 6", onBack = ::popOrFinish) }
    }
}

@Composable
private fun WorshipHubPlaceholderScreen(
    title: String,
    onBack: () -> Unit
) {
    BaseScreen(
        tabName = title,
        logoRes = R.drawable.ic_in_development,
        showBackArrow = true,
        onBackClick = onBack,
        showAccountAction = true
    ) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // aplica o padding do Scaffold do BaseScreen
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
                Spacer(modifier = Modifier.height(24.dp))

                androidx.compose.material3.Icon(
                    painter = painterResource(id = R.drawable.ic_in_development),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Em desenvolvimento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))
                Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
            }
        }
    }
}