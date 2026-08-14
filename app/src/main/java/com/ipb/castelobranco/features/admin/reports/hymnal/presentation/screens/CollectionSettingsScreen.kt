package com.ipb.castelobranco.features.admin.reports.hymnal.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ipb.castelobranco.R
import com.ipb.castelobranco.core.presentation.base.BaseScreen
import com.ipb.castelobranco.features.admin.reports.hymnal.domain.model.SettingField
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.components.SettingNumberField
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.state.CollectionSettingsEvent
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.state.CollectionSettingsUiState
import com.ipb.castelobranco.features.admin.reports.hymnal.presentation.viewmodel.CollectionSettingsViewModel

/**
 * The six numbers that govern collection.
 *
 * Every range is checked locally before anything is sent, and a refusal — local or from the
 * service — is shown on the field it belongs to.
 */
@Composable
fun CollectionSettingsScreen(
    viewModel: CollectionSettingsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CollectionSettingsEvent.ShowMessage ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    BaseScreen(
        tabName = "Parâmetros de coleta",
        logoRes = R.drawable.ic_sarca_ipb,
        showBackArrow = true,
        onBackClick = onBack,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            CollectionSettingsContent(
                state = state,
                innerPadding = innerPadding,
                onValueChange = viewModel::onValueChanged,
                onSave = viewModel::onSave,
                onRetry = viewModel::load,
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
fun CollectionSettingsContent(
    state: CollectionSettingsUiState,
    innerPadding: PaddingValues,
    onValueChange: (SettingField, String) -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
) {
    when {
        state.isLoading -> Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }

        state.error != null -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = state.error, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRetry) { Text("Tentar novamente") }
        }

        else -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LocalEffectNotice()

            SettingField.entries.forEach { field ->
                SettingNumberField(
                    field = field,
                    value = state.values[field].orEmpty(),
                    error = state.fieldErrors[field],
                    onValueChange = { onValueChange(field, it) },
                )
            }

            Button(
                onClick = onSave,
                enabled = state.isDirty && !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.isSaving) "Salvando…" else "Salvar")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * The one limitation worth stating up front: the collection running on *this* device keeps its
 * cached values until the app is restarted. That cache lives in the hymnal feature, which the
 * administration area may not import.
 */
@Composable
private fun LocalEffectNotice() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
    ) {
        Text(
            text = "Vale para todos os aparelhos",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "As mudanças passam a valer conforme cada aparelho busca os parâmetros. " +
                "Neste aparelho, a coleta continua usando os valores atuais até o aplicativo " +
                "ser reaberto.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CollectionSettingsContentPreview() {
    CollectionSettingsContent(
        state = CollectionSettingsUiState(
            isLoading = false,
            values = mapOf(
                SettingField.MIN_SECONDS_TO_COUNT to "30",
                SettingField.COLLAPSE_WINDOW_MINUTES to "10",
                SettingField.MAX_BATCH_SIZE to "200",
                SettingField.MAX_PAST_DAYS to "90",
                SettingField.FUTURE_TOLERANCE_MINUTES to "5",
                SettingField.WINDOW_GRACE_MINUTES to "30",
            ),
            fieldErrors = mapOf(
                SettingField.MIN_SECONDS_TO_COUNT to "Informe um número entre 1 e 3600.",
            ),
            isDirty = true,
        ),
        innerPadding = PaddingValues(0.dp),
        onValueChange = { _, _ -> },
        onSave = {},
        onRetry = {},
    )
}
