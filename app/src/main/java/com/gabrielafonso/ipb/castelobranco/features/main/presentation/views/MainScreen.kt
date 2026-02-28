package com.gabrielafonso.ipb.castelobranco.features.main.presentation.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.core.ui.components.CustomButton
import com.gabrielafonso.ipb.castelobranco.core.ui.components.Highlight
import com.gabrielafonso.ipb.castelobranco.core.ui.components.HighlightAniversariantes
import com.gabrielafonso.ipb.castelobranco.core.ui.components.HighlightEscalaIndisponivel
import com.gabrielafonso.ipb.castelobranco.core.ui.components.HighlightEventos
import com.gabrielafonso.ipb.castelobranco.features.auth.entry.AuthActivity
import com.gabrielafonso.ipb.castelobranco.features.gallery.entry.GalleryActivity
import com.gabrielafonso.ipb.castelobranco.features.hymnal.entry.HymnalActivity
import com.gabrielafonso.ipb.castelobranco.features.main.entry.goToMainAsRoot
import com.gabrielafonso.ipb.castelobranco.features.main.presentation.viewmodel.MainViewModel
import com.gabrielafonso.ipb.castelobranco.features.profile.presentation.viewmodel.ProfileViewModel
import com.gabrielafonso.ipb.castelobranco.features.schedule.entry.ScheduleActivity
import com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.components.ScheduleSectionUi
import com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.components.SectionCard
import com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.viewmodel.ScheduleUiState
import com.gabrielafonso.ipb.castelobranco.features.schedule.presentation.viewmodel.ScheduleViewModel
import com.gabrielafonso.ipb.castelobranco.features.settings.entry.SettingsActivity
import com.gabrielafonso.ipb.castelobranco.features.worshiphub.hub.entry.WorshipHubActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow


/**
 * Agrupador de permissões e estado de autenticação
 */
data class UserAuthState(
    val isLoggedIn: Boolean = false,
    val isAdmin: Boolean = false
)

@Composable
fun MainView(
    viewModel: MainViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    scheduleViewModel: ScheduleViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val actions = remember(context) { MainActions(context) }

    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isAdmin by profileViewModel.isAdmin.collectAsStateWithLifecycle()
    val nextSection by scheduleViewModel.nextSection.collectAsStateWithLifecycle()

    val authState = UserAuthState(
        isLoggedIn = isLoggedIn,
        isAdmin = isAdmin ?: false
    )

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MainViewModel.MainEvent.LogoutSuccess -> {
                    (context as? Activity)?.goToMainAsRoot(message = "Você saiu da conta")
                }
            }
        }
    }

    MainScreen(
        actions = actions,
        authState = authState,
        nextSection = nextSection,
        onLogout = viewModel::logout
    )
}

@Composable
fun MainScreen(
    actions: MainActions,
    authState: UserAuthState,
    nextSection: ScheduleSectionUi?,
    onLogout: () -> Unit
) {
    NavigationDrawer(
        actions = actions,
        authState = authState,
        onLogout = onLogout
    ) { openDrawer ->
        BaseScreen(
            tabName = "IPB Castelo Branco",
            onMenuClick = openDrawer
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                Highlight(
                    pages = buildHighlightPages(nextSection)
                )

                Spacer(modifier = Modifier.height(60.dp))
                ButtonGrid(actions = actions)
            }
        }
    }
}

@Composable
private fun buildHighlightPages(
    nextSection: ScheduleSectionUi?
): List<@Composable () -> Unit> = buildList {
    add { HighlightAniversariantes() }
    if (nextSection != null) {
        //TODO fazer um schedule section só pro highlight pra tirar essa gambiarra
        add {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header com título e horário
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = nextSection.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (nextSection.time.isNotBlank()) {
                            Text(
                                text = nextSection.time,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    // Header da tabela
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceDim)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Dia",
                            modifier = Modifier.weight(0.25f),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = "Responsável",
                            modifier = Modifier.weight(0.75f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // Linhas da escala — roláveis para não cortar nenhum nome
                    val rows = nextSection.rows.sortedBy { it.day }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(rows) { index, row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.getDefault(), "%02d", row.day),
                                    modifier = Modifier.weight(0.25f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = row.member,
                                    modifier = Modifier.weight(0.75f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (index != rows.lastIndex) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        add { HighlightEscalaIndisponivel() }
    }

    add { HighlightEventos() }
}
@Composable
fun NavigationDrawer(
    actions: MainActions,
    authState: UserAuthState,
    onLogout: () -> Unit,
    content: @Composable (openDrawer: () -> Unit) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Força recomposição se o status de login mudar
            key(authState.isLoggedIn) {
                ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.8f)) {
                    DrawerContent(
                        actions = actions,
                        authState = authState,
                        onLogout = onLogout
                    ) { action ->
                        scope.launch {
                            drawerState.close()
                            action()
                        }
                    }
                }
            }
        },
        content = { content { scope.launch { drawerState.open() } } }
    )
}

@Composable
fun DrawerContent(
    actions: MainActions,
    authState: UserAuthState,
    onLogout: () -> Unit,
    onItemClick: (action: () -> Unit) -> Unit
) {
    val textColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = Modifier.padding(10.dp)) {
        if (!authState.isLoggedIn) {
            DrawerMenuItem(
                iconRes = R.drawable.ic_login,
                label = "Entrar",
                textColor = textColor
            ) {
                onItemClick { actions.openAuth() }
            }
        }

        // Exemplo de uso do isAdmin: Mostrar menu restrito
        if (authState.isLoggedIn && authState.isAdmin) {
            DrawerMenuItem(
                iconRes = R.drawable.ic_admin_panel, // Troque por ic_admin se tiver
                label = "Painel Admin",
                textColor = textColor // Destaque para admin
            ) {
                onItemClick { /* ações de admin */ }
            }
        }

        DrawerMenuItem(
            iconRes = R.drawable.ic_settings,
            label = "Configurações",
            textColor = textColor
        ) {
            onItemClick { actions.openSettings() }
        }

        if (authState.isLoggedIn) {
            DrawerMenuItem(
                iconRes = R.drawable.ic_logout,
                label = "Logout",
                textColor = textColor
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
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = textColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = textColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = textColor
            )
        }
    }
}

@Composable
fun ButtonGrid(actions: MainActions) {
    val iconColor = MaterialTheme.colorScheme.primaryContainer
    val buttons = remember {
        listOf(
            ButtonInfo(R.drawable.ic_worshiphub, "Min. Louvor", iconColor, actions::openWorshipHub),
            ButtonInfo(R.drawable.ic_schedule, "Escala", iconColor, actions::openSchedule),
            ButtonInfo(R.drawable.ic_galery, "Galeria", iconColor, actions::openGallery),
            ButtonInfo(R.drawable.ic_sarca_ipb, "Hinário", iconColor, actions::openHymnal),
            ButtonInfo(R.drawable.ic_in_development, "In Dev", iconColor) { },
            ButtonInfo(R.drawable.ic_in_development, "In Dev", iconColor) { }
        )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ButtonRow(buttons.subList(0, 3))
        ButtonRow(buttons.subList(3, 6))
    }
}

@Composable
private fun ButtonRow(rowButtons: List<ButtonInfo>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(25.dp),
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        rowButtons.forEach { button ->
            CustomButton(
                image = painterResource(id = button.iconRes),
                text = button.label,
                backgroundColor = button.color,
                onClick = button.onClick
            )
        }
    }
}

class MainActions(private val context: Context) {
    fun openWorshipHub() = openActivity(WorshipHubActivity::class.java)
    fun openSchedule() = openActivity(ScheduleActivity::class.java)
    fun openHymnal() = openActivity(HymnalActivity::class.java)
    fun openAuth() = openActivity(AuthActivity::class.java)
    fun openSettings() = openActivity(SettingsActivity::class.java)
    fun openGallery() = openActivity(GalleryActivity::class.java)

    private fun <T> openActivity(activity: Class<T>) {
        context.startActivity(Intent(context, activity))
    }
}

data class ButtonInfo(
    val iconRes: Int,
    val label: String,
    val color: Color,
    val onClick: () -> Unit
)