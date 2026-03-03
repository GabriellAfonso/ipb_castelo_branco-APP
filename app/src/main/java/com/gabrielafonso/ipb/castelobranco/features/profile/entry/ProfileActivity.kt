package com.gabrielafonso.ipb.castelobranco.features.profile.entry

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseActivity
import com.gabrielafonso.ipb.castelobranco.features.profile.presentation.screens.ProfileScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileActivity : BaseActivity() {

    override fun onPreCreate(savedInstanceState: Bundle?) {
        super.onPreCreate(savedInstanceState)

    }
    @Composable
    override fun ScreenContent() {
        ProfileScreen(
            onBackClick = { finish() }
        )
    }
}