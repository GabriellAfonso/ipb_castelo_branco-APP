package com.gabrielafonso.ipb.castelobranco.features.hymnal.entry

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HymnalActivity : BaseActivity() {

    override fun onPreCreate(savedInstanceState: Bundle?) {
        super.onPreCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

    @Composable
    override fun ScreenContent() {
        val navController = rememberNavController()
        _root_ide_package_.com.gabrielafonso.ipb.castelobranco.features.hymnal.presentation.navigation.HymnalNavGraph(
            navController = navController,
            onFinish = { finish() }
        )
    }
}