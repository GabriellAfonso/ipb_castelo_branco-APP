package com.gabrielafonso.ipb.castelobranco.ui.screens.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    companion object {
        private const val EXTRA_APP_MESSAGE = "extra_app_message"

        fun newRootIntent(context: Context, message: String? = null): Intent {
            return Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                if (!message.isNullOrBlank()) putExtra(EXTRA_APP_MESSAGE, message)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.getStringExtra(EXTRA_APP_MESSAGE)?.let { msg ->
            showAppMessage(msg)
            intent.removeExtra(EXTRA_APP_MESSAGE)
        }
    }

    private fun showAppMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Composable
    override fun ScreenContent() {
        MainView()
    }
}

fun Activity.goToMainAsRoot(message: String? = null) {
    startActivity(MainActivity.newRootIntent(this, message))
    finish()
}