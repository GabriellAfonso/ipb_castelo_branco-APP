package com.ipb.castelobranco.features.settings.presentation.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ipb.castelobranco.BuildConfig
import com.ipb.castelobranco.R
import com.ipb.castelobranco.core.presentation.base.BaseScreen
import com.ipb.castelobranco.core.presentation.base.findActivity
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
        onResetGallery = { viewModel.clearGallery() },
        onResetBible   = { viewModel.clearAndRedownloadBible() },
    )
}

@Composable
fun SettingsContent(
    onBackClick: () -> Unit,
    darkMode: Boolean,
    onToggleDark: () -> Unit,
    onResetGallery: () -> Unit,
    onResetBible: () -> Unit,
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
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSectionHeader("Aparência")
            SettingsSwitchRow(
                icon            = Icons.Filled.DarkMode,
                title           = "Modo escuro",
                description     = if (darkMode) "Ativado" else "Desativado",
                checked         = darkMode,
                onCheckedChange = { onToggleDark() },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsSectionHeader("Dados")
            SettingsActionRow(
                icon        = Icons.Filled.Image,
                title       = "Resetar galeria",
                description = "Apaga as fotos em cache",
                tint        = MaterialTheme.colorScheme.error,
                onClick     = onResetGallery,
            )
            SettingsActionRow(
                icon        = Icons.Filled.AutoStories,
                title       = "Resetar Bíblia",
                description = "Apaga e baixa novamente",
                tint        = MaterialTheme.colorScheme.error,
                onClick     = onResetBible,
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsSectionHeader("Sobre")
            SettingsInfoRow(
                icon  = Icons.Filled.Info,
                title = "Versão",
                value = BuildConfig.VERSION_NAME,
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text       = title,
        style      = MaterialTheme.typography.labelMedium,
        color      = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier   = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent   = { Text(title) },
        supportingContent = { Text(description) },
        leadingContent    = {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
    )
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    ListItem(
        modifier          = Modifier.clickable(onClick = onClick),
        headlineContent   = { Text(title, color = tint) },
        supportingContent = { Text(description) },
        leadingContent    = {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = tint,
            )
        },
    )
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    value: String,
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent  = {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}
