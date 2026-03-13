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
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CoreActivity : ComponentActivity() {

    private lateinit var appUpdateManager: AppUpdateManager

    companion object {
        private const val EXTRA_APP_MESSAGE = "extra_app_message"
        private const val UPDATE_REQUEST_CODE = 500

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

        appUpdateManager = AppUpdateManagerFactory.create(this)

        setContent {
            IPBCasteloBrancoTheme(dynamicColor = false) {
                val navController = rememberNavController()
                AppNavHost(navController)
            }
        }

        checkForImmediateUpdate()
    }

    override fun onResume() {
        super.onResume()
        // If the user somehow dismissed the update screen, re-trigger it.
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                @Suppress("DEPRECATION")
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    AppUpdateType.IMMEDIATE,
                    this,
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }

    private fun checkForImmediateUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                @Suppress("DEPRECATION")
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    AppUpdateType.IMMEDIATE,
                    this,
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE && resultCode != RESULT_OK) {
            // User cancelled or update failed — finish so they can't use an outdated version.
            finish()
        }
    }
}

fun Activity.restartApp(message: String? = null) {
    startActivity(CoreActivity.newRootIntent(this, message))
    finish()
}
