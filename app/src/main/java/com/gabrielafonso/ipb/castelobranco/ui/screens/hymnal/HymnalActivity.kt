package com.gabrielafonso.ipb.castelobranco.ui.screens.hymnal

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.gabrielafonso.ipb.castelobranco.ui.screens.base.BaseActivity
import com.gabrielafonso.ipb.castelobranco.ui.screens.base.BaseViewModelProvider

class HymnalActivity : BaseActivity() {

    private val viewModel: HymnalViewModel by viewModels { BaseViewModelProvider.Factory }

    override fun onPreCreate(savedInstanceState: Bundle?) {
        super.onPreCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

    @Composable
    override fun ScreenContent() {
        val navController = rememberNavController()
        HymnalNavGraph(
            navController = navController,
            viewModel = viewModel
        )
    }
}
//@Preview(showBackground = true)
//@Composable
//fun PreviewWorshipHub() {
//    IPBCasteloBrancoTheme {
//        WorshipHubScreen(onTablesClick = {})
//    }
//}

