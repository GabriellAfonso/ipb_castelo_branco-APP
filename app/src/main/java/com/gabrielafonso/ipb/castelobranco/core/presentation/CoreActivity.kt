package com.gabrielafonso.ipb.castelobranco.core.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.gabrielafonso.ipb.castelobranco.core.presentation.navigation.AppNavHost
import com.gabrielafonso.ipb.castelobranco.core.presentation.theme.IPBCasteloBrancoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CoreActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IPBCasteloBrancoTheme(dynamicColor = false) {
                val navController = rememberNavController()
                AppNavHost(navController)
            }
        }
    }
}
