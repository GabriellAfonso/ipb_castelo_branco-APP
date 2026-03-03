package com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.features.admin.panel.presentation.navigation.AdminNav
import com.gabrielafonso.ipb.castelobranco.core.ui.components.DateFieldWithPicker
import com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.components.MusicSongRegistrationForm
import com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.components.RegistrationTypeSelect
import com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.components.SundayDatePickerDialog
import com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.components.SundayRegistrationForm
import com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.state.MusicRegistrationEvent
import com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.state.MusicRegistrationUiState
import com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.state.RegistrationType
import com.gabrielafonso.ipb.castelobranco.features.admin.register.presentation.viewmodel.MusicRegistrationViewModel

private val Green = Color(0xFF0F6B5C)
private val Orange = Color(0xFFF2A300)

// ── Actions por domínio ───────────────────────────────────────────────────────

data class MusicRegistrationScreenActions(
    val onRegistrationTypeChange: (RegistrationType) -> Unit,
    val onSubmit: () -> Unit,
)

data class SundayRegistrationActions(
    val onOpenDatePicker: () -> Unit,
    val onSongQueryChange: (position: Int, query: String) -> Unit,
    val onSongSelect: (position: Int, song: com.gabrielafonso.ipb.castelobranco.features.worshiphub.tables.model.Song) -> Unit,
    val onToneChange: (position: Int, tone: String) -> Unit,
    val onAddRow: () -> Unit,
    val onRemoveRow: (position: Int) -> Unit,
)

data class MusicSongRegistrationActions(
    val onTitleChange: (String) -> Unit,
    val onArtistChange: (String) -> Unit,
)

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun MusicRegistrationScreen(
    nav: AdminNav,
    viewModel: MusicRegistrationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.onEvent(MusicRegistrationEvent.Init) }

    LaunchedEffect(state.snackbarMessage) {
        val msg = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.onEvent(MusicRegistrationEvent.SnackbarShown)
    }

    if (state.showDatePicker && state.registrationType == RegistrationType.SUNDAY) {
        SundayDatePickerDialog(
            initialDate = state.selectedDate,
            onDismiss = { viewModel.onEvent(MusicRegistrationEvent.DismissDatePicker) },
            onConfirm = { viewModel.onEvent(MusicRegistrationEvent.DatePicked(it)) }
        )
    }

    val screenActions = MusicRegistrationScreenActions(
        onRegistrationTypeChange = { viewModel.onEvent(MusicRegistrationEvent.RegistrationTypeChanged(it)) },
        onSubmit = { viewModel.onEvent(MusicRegistrationEvent.Submit) },
    )

    val sundayActions = SundayRegistrationActions(
        onOpenDatePicker = { viewModel.onEvent(MusicRegistrationEvent.OpenDatePicker) },
        onSongQueryChange = { pos, q -> viewModel.onEvent(MusicRegistrationEvent.SundaySongQueryChanged(pos, q)) },
        onSongSelect = { pos, song -> viewModel.onEvent(MusicRegistrationEvent.SundaySongSelected(pos, song)) },
        onToneChange = { pos, tone -> viewModel.onEvent(MusicRegistrationEvent.SundayToneChanged(pos, tone)) },
        onAddRow = { viewModel.onEvent(MusicRegistrationEvent.AddSundayRow) },
        onRemoveRow = { viewModel.onEvent(MusicRegistrationEvent.RemoveSundayRow(it)) },
    )

    val musicActions = MusicSongRegistrationActions(
        onTitleChange = { viewModel.onEvent(MusicRegistrationEvent.MusicTitleChanged(it)) },
        onArtistChange = { viewModel.onEvent(MusicRegistrationEvent.MusicArtistChanged(it)) },
    )

    MusicRegistrationContent(
        state = state,
        screenActions = screenActions,
        sundayActions = sundayActions,
        musicActions = musicActions,
        snackbarHostState = snackbarHostState,
        onBackClick = nav.back
    )
}

// ── Stateless screen ──────────────────────────────────────────────────────────

@Composable
fun MusicRegistrationContent(
    state: MusicRegistrationUiState,
    screenActions: MusicRegistrationScreenActions,
    sundayActions: SundayRegistrationActions,
    musicActions: MusicSongRegistrationActions,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit
) {
    BaseScreen(
        tabName = "Registrar",
        logoRes = R.drawable.ic_register,
        showBackArrow = true,
        onBackClick = onBackClick
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
                ScreenTitle()

                Spacer(modifier = Modifier.height(20.dp))

                RegistrationTypeSelect(
                    value = state.registrationType,
                    onChange = screenActions.onRegistrationTypeChange,
                    modifier = Modifier.fillMaxWidth()
                )

                if (state.registrationType == RegistrationType.SUNDAY) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DateFieldWithPicker(
                        dateBr = state.dateBr,
                        onOpenPicker = sundayActions.onOpenDatePicker,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(20.dp))

                when (state.registrationType) {
                    RegistrationType.SUNDAY -> SundayRegistrationForm(
                        availableSongs = state.availableSongs,
                        rows = state.sundayRows,
                        onSongQueryChange = sundayActions.onSongQueryChange,
                        onSongSelect = sundayActions.onSongSelect,
                        onToneChange = sundayActions.onToneChange,
                        onAddMoreClick = sundayActions.onAddRow,
                        onRemoveRowClick = sundayActions.onRemoveRow
                    )
                    RegistrationType.MUSIC -> MusicSongRegistrationForm(
                        state = state.musicForm,
                        actions = musicActions,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = screenActions.onSubmit,
                    enabled = state.canSubmit && !state.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        disabledContainerColor = Green.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (state.isSubmitting) "A enviar..." else "Enviar ao servidor",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
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

@Composable
private fun ScreenTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .height(3.dp)
                .fillMaxWidth(0.12f)
                .background(Orange, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Registrar",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Green
        )
    }
}