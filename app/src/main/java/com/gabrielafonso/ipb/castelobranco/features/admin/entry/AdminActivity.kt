package com.gabrielafonso.ipb.castelobranco.features.admin.entry

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseActivity
import com.gabrielafonso.ipb.castelobranco.features.admin.presentation.navigation.AdminNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminActivity : BaseActivity() {

    override fun onPreCreate(savedInstanceState: Bundle?) {
        super.onPreCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }

    @Composable
    override fun ScreenContent() {
        val navController = rememberNavController()
        AdminNavGraph(
            navController = navController,
            onFinish = { finish() }
        )
    }
}