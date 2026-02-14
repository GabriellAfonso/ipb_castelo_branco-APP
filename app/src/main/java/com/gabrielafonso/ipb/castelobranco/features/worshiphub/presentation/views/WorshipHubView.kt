package com.gabrielafonso.ipb.castelobranco.features.worshiphub.presentation.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.ui.components.CustomButton
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseScreen

@Stable
data class WorshipHubNav(
    val tables: () -> Unit,
    val register: () -> Unit,
    val button3: () -> Unit,
    val button4: () -> Unit,
    val button5: () -> Unit,
    val button6: () -> Unit,
    val button7: () -> Unit,
    val button8: () -> Unit,
    val back: () -> Unit
)

private data class WorshipHubButtonInfo(
    val iconRes: Int,
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun WorshipHubView(
    nav: WorshipHubNav
) {
    val remembered = remember(nav) { nav }
    WorshipHubScreen(actions = remembered)
}

@Composable
private fun WorshipHubScreen(
    actions: WorshipHubNav
) {
    BaseScreen(
        tabName = "Min. Louvor",
        logoRes = R.drawable.ic_worshiphub,
        showBackArrow = true,
        onBackClick = actions.back
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WorshipHubButtonGrid(
                modifier = Modifier.fillMaxHeight(),
                actions = actions
            )
        }
    }
}

@Composable
private fun WorshipHubButtonGrid(
    modifier: Modifier = Modifier,
    actions: WorshipHubNav
) {
    val buttons = remember(actions) {
        listOf(
            WorshipHubButtonInfo(R.drawable.ic_table, "Tabelas", actions.tables),
            WorshipHubButtonInfo(R.drawable.ic_register, "Registrar", actions.register),
            WorshipHubButtonInfo(R.drawable.ic_in_development, "In Dev", actions.button3),
            WorshipHubButtonInfo(R.drawable.ic_in_development, "In Dev", actions.button4),
            WorshipHubButtonInfo(R.drawable.ic_in_development, "In Dev", actions.button5),
            WorshipHubButtonInfo(R.drawable.ic_in_development, "In Dev", actions.button6),
        )
    }

    val gap = 50.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(gap, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) { rowIndex ->
            val left = buttons[rowIndex * 2]
            val right = buttons[rowIndex * 2 + 1]

            Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomButton(
                    image = painterResource(id = left.iconRes),
                    text = left.label,
                    onClick = left.onClick,
                    size = 150.dp,
                )
                CustomButton(
                    image = painterResource(id = right.iconRes),
                    text = right.label,
                    onClick = right.onClick,
                    size = 150.dp,
                )
            }
        }
    }
}
