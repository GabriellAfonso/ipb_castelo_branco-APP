package com.gabrielafonso.ipb.castelobranco.core.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
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

    companion object {
        private const val EXTRA_APP_MESSAGE = "extra_app_message"

        fun newRootIntent(context: Context, message: String? = null): Intent {
            return Intent(context, CoreActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                if (!message.isNullOrBlank()) putExtra(EXTRA_APP_MESSAGE, message)
            }
        }
    }

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

fun Activity.restartApp(message: String? = null) {
    startActivity(CoreActivity.newRootIntent(this, message))
    finish()
}