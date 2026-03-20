package com.ipb.castelobranco.features.settings.presentation.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ipb.castelobranco.R
import com.ipb.castelobranco.core.presentation.base.BaseScreen
import com.ipb.castelobranco.core.presentation.base.findActivity
import com.ipb.castelobranco.core.presentation.components.ThemeToggle
import com.ipb.castelobranco.features.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val resolvedDark = uiState.darkMode ?: run {
        when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO  -> false
            else                             -> isSystemInDarkTheme()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect {
            context.findActivity()?.recreate()
        }
    }

    SettingsContent(
        onBackClick    = onBackClick,
        darkMode       = resolvedDark,
        onToggleDark   = { viewModel.toggleDarkMode() },
        onClearGallery = { viewModel.clearGallery() },
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
        tabName           = "Configurações",
        logoRes           = R.drawable.ic_sarca_ipb,
        showBackArrow     = true,
        onBackClick       = onBackClick,
        showAccountAction = false,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            ThemeToggle(
                checked         = darkMode,
                onCheckedChange = { onToggleDark() },
            )
            Button(onClick = onClearGallery) {
                Text("Apagar galeria")
            }
        }
    }
}
