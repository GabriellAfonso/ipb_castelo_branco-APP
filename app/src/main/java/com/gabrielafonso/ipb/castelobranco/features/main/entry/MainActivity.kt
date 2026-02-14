package com.gabrielafonso.ipb.castelobranco.features.main.entry

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.compose.runtime.Composable
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseActivity
import com.gabrielafonso.ipb.castelobranco.features.main.presentation.views.MainView
import dagger.hilt.android.AndroidEntryPoint

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