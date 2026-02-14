package com.gabrielafonso.ipb.castelobranco.features.auth.entry

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : BaseActivity() {


    override fun onPreCreate(savedInstanceState: Bundle?) {
        super.onPreCreate(savedInstanceState)

    }

    @Composable
    override fun ScreenContent() {
        val navController = rememberNavController()
        _root_ide_package_.com.gabrielafonso.ipb.castelobranco.features.auth.presentation.navigation.AuthNavGraph(
            navController = navController,
            onFinish = { finish() }
        )
    }
}