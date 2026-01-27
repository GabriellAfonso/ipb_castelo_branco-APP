package com.gabrielafonso.ipb.castelobranco.ui.components

import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
//import com.gabrielafonso.ipb.castelobranco.PraiseActivity
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.ui.screens.hymnal.HymnalActivity
import com.gabrielafonso.ipb.castelobranco.ui.screens.monthschedule.MonthScheduleActivity
import com.gabrielafonso.ipb.castelobranco.ui.screens.worshiphub.WorshipHubActivity

data class ButtonInfo(
    val drawable: Int,
    val label: String,
    val color: Color,
    val onClick: () -> Unit
)
@Composable
fun ButtonGrid() {
    val context = LocalContext.current
    val iconColor = Color(0xFF157C53)

    // Lista preguiçosa de botões
    val buttons = listOf(
        ButtonInfo(
            drawable = R.drawable.louvor_icon,
            label = "Louvor",
            color = iconColor,
            onClick = {
                val intent = Intent(context, WorshipHubActivity::class.java)
                context.startActivity(intent)
            }
        ),
        ButtonInfo(
            drawable = R.drawable.calendar_icon,
            label = "Escala",
            color = iconColor,
            onClick = {
                val intent = Intent(context, MonthScheduleActivity::class.java)
                context.startActivity(intent)
            }
        ),
        ButtonInfo(
            drawable = R.drawable.gallery_icon,
            label = "Galeria",
            color = iconColor,
            onClick = { println("Galeria clicked") }
        ),
        ButtonInfo(
            drawable = R.drawable.sarca_ipb,
            label = "Hinário",
            color = iconColor,
            onClick = {
                val intent = Intent(context, HymnalActivity::class.java)
                context.startActivity(intent)
            }
        ),
        ButtonInfo(
            drawable = R.drawable.sarca_ipb,
            label = "Exemplo",
            color = iconColor,
            onClick = { println("Sample 1 clicked") }
        ),
        ButtonInfo(
            drawable = R.drawable.sarca_ipb,
            label = "Exemplo",
            color = iconColor,
            onClick = { println("Sample 2 clicked") }
        )
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Primeira linha
        Row(horizontalArrangement = Arrangement.spacedBy(25.dp), modifier = Modifier.padding(vertical = 16.dp)) {
            buttons.take(3).forEachIndexed { index, (drawable, label, color,onClick) ->
                CustomButton(
                    image = painterResource(id = drawable),
                    text = label,
                    backgroundColor = color,
                    onClick = onClick
                )
            }
        }

        // Segunda linha
        Row(horizontalArrangement = Arrangement.spacedBy(25.dp), modifier = Modifier.padding(vertical = 16.dp)) {
            buttons.drop(3).take(3).forEachIndexed { index, (drawable, label, color,onClick) ->
                CustomButton(
                    image = painterResource(id = drawable),
                    text = label,
                    backgroundColor = color,
                    onClick = onClick
                )
            }
        }
    }
}