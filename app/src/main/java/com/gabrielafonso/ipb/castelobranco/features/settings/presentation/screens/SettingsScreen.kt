package com.gabrielafonso.ipb.castelobranco.features.settings.presentation.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.ui.components.ThemeToggle
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.features.settings.entry.SettingsActivity
import com.gabrielafonso.ipb.castelobranco.features.main.entry.MainActivity
import com.gabrielafonso.ipb.castelobranco.features.settings.presentation.viewmodel.SettingsViewModel


@Composable
fun SettingsScreen(
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

    SettingsContent(
        onBackClick = onBackClick,
        darkMode = resolvedDark,
        onToggleDark = { viewModel.toggleDarkMode() },
        onClearGallery = { viewModel.clearGallery() }
    )
}

@Composable
fun SettingsContent(
    onBackClick: () -> Unit,
    darkMode: Boolean,
    onToggleDark: () -> Unit,
    onClearGallery: () -> Unit,
) {
    BaseScreen(
        tabName = "Configurações",
        logoRes =  R.drawable.ic_sarca_ipb,
        showBackArrow = true,
        onBackClick = onBackClick,
        showAccountAction = false
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
           Button(
                    onClick = onClearGallery,
                ) {
                    Text("Apagar galeria")
                }
        }
    }
}
