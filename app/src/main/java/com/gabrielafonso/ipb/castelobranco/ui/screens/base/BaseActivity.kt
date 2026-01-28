package com.gabrielafonso.ipb.castelobranco.ui.screens.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.gabrielafonso.ipb.castelobranco.ui.theme.IPBCasteloBrancoTheme


abstract class BaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hook executado antes de aplicar edge-to-edge e setContent
        onPreCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            IPBCasteloBrancoTheme(dynamicColor = false) {
                ScreenContent()
            }
        }

        // Hook executado logo após setContent
        onPostCreate(savedInstanceState)
    }

    // Hooks protegidos que Activities filhas podem sobrescrever
    protected open fun onPreCreate(savedInstanceState: Bundle?) {}

    @Composable
    protected abstract fun ScreenContent()
}