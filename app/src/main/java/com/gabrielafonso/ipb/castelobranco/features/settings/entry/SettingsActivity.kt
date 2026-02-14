package com.gabrielafonso.ipb.castelobranco.features.settings.entry

import android.os.Bundle
import androidx.compose.runtime.Composable
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseActivity
import com.gabrielafonso.ipb.castelobranco.features.settings.presentation.views.SettingsView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {

    override fun onPreCreate(savedInstanceState: Bundle?) {
        super.onPreCreate(savedInstanceState)

    }
    @Composable
    override fun ScreenContent() {
        SettingsView(
            onBackClick = { finish() }
        )
    }
}