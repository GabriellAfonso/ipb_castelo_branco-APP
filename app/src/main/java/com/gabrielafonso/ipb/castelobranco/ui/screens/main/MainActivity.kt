package com.gabrielafonso.ipb.castelobranco.ui.screens.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielafonso.ipb.castelobranco.ui.theme.IPBCasteloBrancoTheme
import com.gabrielafonso.ipb.castelobranco.ui.screens.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.launch
import com.gabrielafonso.ipb.castelobranco.util.ActivityEvents
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    override fun onPreCreate(savedInstanceState: Bundle?) {
        super.onPreCreate(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // escuta evento global e recria a própria MainActivity quando recebido
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                ActivityEvents.recreateMain.collect {
                    recreate()
                }
            }
        }
    }

    @Composable
    override fun ScreenContent() {
        MainView()
    }
}
