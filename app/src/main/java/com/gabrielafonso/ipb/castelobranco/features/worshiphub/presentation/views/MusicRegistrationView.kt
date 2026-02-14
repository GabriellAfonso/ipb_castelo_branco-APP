package com.gabrielafonso.ipb.castelobranco.features.worshiphub.presentation.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.presentation.register.MusicRegistrationEvent
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.presentation.register.MusicRegistrationViewModel
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.presentation.register.RegistrationType
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.presentation.register.components.RegistrationTypeSelect
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.presentation.register.components.SundayDatePickerDialog
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.presentation.register.components.SundayRegistrationForm

@Composable
fun MusicRegistrationView(
    onBack: () -> Unit,
    viewModel: MusicRegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onEvent(MusicRegistrationEvent.Init)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.onEvent(MusicRegistrationEvent.SnackbarShown)
    }

    if (uiState.showDatePicker && uiState.registrationType == RegistrationType.SUNDAY) {
        SundayDatePickerDialog(
            initialDate = uiState.selectedDate,
            onDismiss = { viewModel.onEvent(MusicRegistrationEvent.DismissDatePicker) },
            onConfirm = { picked -> viewModel.onEvent(MusicRegistrationEvent.DatePicked(picked)) }
        )
    }

    BaseScreen(
        tabName = "Registrar",
        logoRes = R.drawable.ic_register,
        showBackArrow = true,
        onBackClick = onBack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "REGISTRAR",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                RegistrationTypeSelect(
                    value = uiState.registrationType,
                    onChange = { viewModel.onEvent(MusicRegistrationEvent.RegistrationTypeChanged(it)) },
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.registrationType == RegistrationType.SUNDAY) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.dateBr,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Data") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            TextButton(onClick = { viewModel.onEvent(MusicRegistrationEvent.OpenDatePicker) }) {
                                Text("Selecionar")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                when (uiState.registrationType) {
                    RegistrationType.SUNDAY -> SundayRegistrationForm(
                        availableSongs = uiState.availableSongs,
                        rows = uiState.sundayRows,
                        onSongQueryChange = { pos, q -> viewModel.onEvent(MusicRegistrationEvent.SundaySongQueryChanged(pos, q)) },
                        onSongSelect = { pos, song -> viewModel.onEvent(MusicRegistrationEvent.SundaySongSelected(pos, song)) },
                        onToneChange = { pos, tone -> viewModel.onEvent(MusicRegistrationEvent.SundayToneChanged(pos, tone)) },
                        onAddMoreClick = { viewModel.onEvent(MusicRegistrationEvent.AddSundayRow) },
                        onRemoveRowClick = { pos -> viewModel.onEvent(MusicRegistrationEvent.RemoveSundayRow(pos)) }
                    )

                    RegistrationType.MUSIC -> {
                        Text(
                            text = "Ainda não implementado.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.onEvent(MusicRegistrationEvent.Submit) },
                    enabled = uiState.canSubmit && !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (uiState.isSubmitting) "A enviar..." else "Enviar ao servidor")
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }
    }
}