package com.gabrielafonso.ipb.castelobranco.core.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.presentation.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.core.presentation.components.CustomButton
import com.gabrielafonso.ipb.castelobranco.core.presentation.components.Highlight
import com.gabrielafonso.ipb.castelobranco.core.presentation.components.HighlightBirthdays
import com.gabrielafonso.ipb.castelobranco.core.presentation.components.HighlightEvents
import com.gabrielafonso.ipb.castelobranco.core.presentation.components.HighlightScheduleUnavailable
import com.gabrielafonso.ipb.castelobranco.core.presentation.components.HighlightSundaySchedule
import com.gabrielafonso.ipb.castelobranco.core.presentation.viewmodel.CoreViewModel
import com.gabrielafonso.ipb.castelobranco.features.profile.presentation.viewmodel.ProfileViewModel
import com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.components.ScheduleSectionUi
import com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.viewmodel.ScheduleViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Agrupador de permissões e estado de autenticação
 */
data class UserAuthState(
    val isLoggedIn: Boolean = false,
    val isAdmin: Boolean = false,
)

@Composable
fun CoreView(
    onNavigateToAuth: () -> Unit,
    onNavigateToWorshipHub: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToHymnal: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: CoreViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    scheduleViewModel: ScheduleViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.initialize() }
    LaunchedEffect(Unit) { profileViewModel.initialize() }

    val isLoggedIn      by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val profileUiState  by profileViewModel.uiState.collectAsStateWithLifecycle()
    val nextSection     by scheduleViewModel.nextSection.collectAsStateWithLifecycle()

    val authState = UserAuthState(
        isLoggedIn = isLoggedIn,
        isAdmin    = profileUiState.isAdmin ?: false,
    )

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CoreViewModel.CoreEvent.LogoutSuccess -> onLogoutSuccess()
            }
        }
    }

    CoreScreen(
        onNavigateToAuth       = onNavigateToAuth,
        onNavigateToWorshipHub = onNavigateToWorshipHub,
        onNavigateToSchedule   = onNavigateToSchedule,
        onNavigateToGallery    = onNavigateToGallery,
        onNavigateToHymnal     = onNavigateToHymnal,
        onNavigateToSettings   = onNavigateToSettings,
        onNavigateToAdmin      = onNavigateToAdmin,
        authState              = authState,
        nextSection            = nextSection,
        onLogout               = viewModel::logout,
    )
}

@Composable
fun CoreScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToWorshipHub: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToHymnal: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    authState: UserAuthState,
    nextSection: ScheduleSectionUi?,
    onLogout: () -> Unit,
) {
    NavigationDrawer(
        onNavigateToAuth     = onNavigateToAuth,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToAdmin    = onNavigateToAdmin,
        authState            = authState,
        onLogout             = onLogout,
    ) { openDrawer ->
        BaseScreen(
            tabName     = stringResource(R.string.app_name),
            onMenuClick = openDrawer,
        ) { innerPadding ->
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                Highlight(pages = buildHighlightPages(nextSection))

                Spacer(modifier = Modifier.height(60.dp))
                ButtonGrid(
                    onNavigateToWorshipHub = onNavigateToWorshipHub,
                    onNavigateToSchedule   = onNavigateToSchedule,
                    onNavigateToGallery    = onNavigateToGallery,
                    onNavigateToHymnal     = onNavigateToHymnal,
                )
            }
        }
    }
}

private fun buildHighlightPages(
    nextSection: ScheduleSectionUi?,
): List<@Composable () -> Unit> = buildList {
    add { HighlightBirthdays() }

    if (nextSection != null) {
        add { HighlightSundaySchedule(section = nextSection) }
    } else {
        add { HighlightScheduleUnavailable() }
    }

    add { HighlightEvents() }
}

@Composable
fun NavigationDrawer(
    onNavigateToAuth: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    authState: UserAuthState,
    onLogout: () -> Unit,
    content: @Composable (openDrawer: () -> Unit) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope       = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState   = drawerState,
        drawerContent = {
            // Força recomposição se o status de login mudar
            key(authState.isLoggedIn) {
                ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.8f)) {
                    DrawerContent(
                        onNavigateToAuth     = onNavigateToAuth,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToAdmin    = onNavigateToAdmin,
                        authState            = authState,
                        onLogout             = onLogout,
                    ) { action ->
                        scope.launch {
                            drawerState.close()
                            action()
                        }
                    }
                }
            }
        },
        content = { content { scope.launch { drawerState.open() } } },
    )
}

@Composable
fun DrawerContent(
    onNavigateToAuth: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    authState: UserAuthState,
    onLogout: () -> Unit,
    onItemClick: (action: () -> Unit) -> Unit,
) {
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.padding(10.dp)) {
        if (!authState.isLoggedIn) {
            DrawerMenuItem(
                iconRes   = R.drawable.ic_login,
                label     = "Entrar",
                textColor = textColor,
            ) {
                onItemClick { onNavigateToAuth() }
            }
        }

        if (authState.isLoggedIn && authState.isAdmin) {
            DrawerMenuItem(
                iconRes   = R.drawable.ic_admin_panel,
                label     = "Painel Admin",
                textColor = textColor,
            ) {
                onItemClick { onNavigateToAdmin() }
            }
        }

        DrawerMenuItem(
            iconRes   = R.drawable.ic_settings,
            label     = "Configurações",
            textColor = textColor,
        ) {
            onItemClick { onNavigateToSettings() }
        }

        if (authState.isLoggedIn) {
            DrawerMenuItem(
                iconRes   = R.drawable.ic_logout,
                label     = "Logout",
                textColor = textColor,
            ) {
                onItemClick { onLogout() }
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    iconRes: Int,
    label: String,
    textColor: Color,
    onClick: () -> Unit,
) {
    TextButton(
        onClick  = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = textColor),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter            = painterResource(id = iconRes),
                contentDescription = null,
                tint               = textColor,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text      = label,
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color     = textColor,
            )
        }
    }
}

@Composable
fun ButtonGrid(
    onNavigateToWorshipHub: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToHymnal: () -> Unit,
) {
    val iconColor = MaterialTheme.colorScheme.primaryContainer
    val buttons = listOf(
        ButtonInfo(R.drawable.ic_worshiphub,     "Min. Louvor", iconColor, onNavigateToWorshipHub),
        ButtonInfo(R.drawable.ic_schedule,       "Escala",      iconColor, onNavigateToSchedule),
        ButtonInfo(R.drawable.ic_galery,         "Galeria",     iconColor, onNavigateToGallery),
        ButtonInfo(R.drawable.ic_sarca_ipb,      "Hinário",     iconColor, onNavigateToHymnal),
        ButtonInfo(R.drawable.ic_in_development, "In Dev",      iconColor) { },
        ButtonInfo(R.drawable.ic_in_development, "In Dev",      iconColor) { },
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ButtonRow(buttons.subList(0, 3))
        ButtonRow(buttons.subList(3, 6))
    }
}

@Composable
private fun ButtonRow(rowButtons: List<ButtonInfo>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(25.dp),
        modifier              = Modifier.padding(vertical = 16.dp),
    ) {
        rowButtons.forEach { button ->
            CustomButton(
                image           = painterResource(id = button.iconRes),
                text            = button.label,
                backgroundColor = button.color,
                onClick         = button.onClick,
            )
        }
    }
}

data class ButtonInfo(
    val iconRes: Int,
    val label: String,
    val color: Color,
    val onClick: () -> Unit,
)
