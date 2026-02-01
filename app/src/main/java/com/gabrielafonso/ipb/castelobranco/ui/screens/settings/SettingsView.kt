package com.gabrielafonso.ipb.castelobranco.ui.screens.settings

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.ui.components.ThemeToggle
import com.gabrielafonso.ipb.castelobranco.ui.screens.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.ui.screens.main.MainActivity
import com.gabrielafonso.ipb.castelobranco.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.collect

@Composable
fun SettingsView(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val resolvedDark = uiState.darkMode ?: run {
        when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> isSystemInDarkTheme()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect {
            (context as? Activity)?.apply {

                val mainIntent = Intent(this, MainActivity::class.java)
                val settingsIntent = Intent(this, SettingsActivity::class.java)

                val options = ActivityOptions.makeCustomAnimation(this, 0, 0)

                startActivities(arrayOf(mainIntent, settingsIntent), options.toBundle())

                finishAffinity()
            }
        }
    }

    SettingsScreen(
        onBackClick = onBackClick,
        darkMode = resolvedDark,
        onToggleDark = {
            // apenas solicita toggle; o recreate será acionado quando o ViewModel emitir o evento
            viewModel.toggleDarkMode()
        }
    )
}

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    darkMode: Boolean,
    onToggleDark: () -> Unit
) {
    BaseScreen(
        tabName = "Configurações",
        logoRes =  R.drawable.sarca_ipb,
        showBackArrow = true,
        onBackClick = onBackClick
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ThemeToggle(
                checked = darkMode,
                onCheckedChange = { onToggleDark() }
            )
        }
    }
}
