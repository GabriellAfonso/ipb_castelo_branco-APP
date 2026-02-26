package com.gabrielafonso.ipb.castelobranco.features.worshiphub.hub.entry

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.gabrielafonso.ipb.castelobranco.core.ui.theme.IPBCasteloBrancoTheme
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseActivity
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.hub.presentation.navigation.WorshipHubNavGraph

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorshipHubActivity : BaseActivity() {

    override fun onPreCreate(savedInstanceState: Bundle?) {
        super.onPreCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Ações que precisam ocorrer após setContent (não dentro do Composable)
    }

    @Composable
    override fun ScreenContent() {
        val navController = rememberNavController()
        WorshipHubNavGraph(
            navController = navController,
            onFinish = { finish() }
        )
//        WorshipHubScreen()
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewWorshipHub() {
//    IPBCasteloBrancoTheme {
//        WorshipHubView(
//            nav = WorshipHubNav(
//                tables = {},
//                register = {},
//                button3 = {},
//                button4 = {},
//                button5 = {},
//                button6 = {},
//                button7 = {},
//                button8 = {},
//                back = {}
//            )
//        )
//    }
//}
