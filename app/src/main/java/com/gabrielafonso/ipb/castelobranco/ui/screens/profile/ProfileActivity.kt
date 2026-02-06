package com.gabrielafonso.ipb.castelobranco.ui.screens.profile

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.gabrielafonso.ipb.castelobranco.ui.screens.base.BaseActivity
import com.gabrielafonso.ipb.castelobranco.ui.screens.main.MainView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : BaseActivity() {

    override fun onPreCreate(savedInstanceState: Bundle?) {
        super.onPreCreate(savedInstanceState)

    }
    @Composable
    override fun ScreenContent() {
        ProfileView(
            onBackClick = { finish() }
        )
    }
}

