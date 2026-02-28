package com.gabrielafonso.ipb.castelobranco.features.admin.presentation.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gabrielafonso.ipb.castelobranco.R
import com.gabrielafonso.ipb.castelobranco.core.ui.base.BaseScreen
import com.gabrielafonso.ipb.castelobranco.features.admin.presentation.navigation.AdminNav

data class AdminAction(
    val label: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val onClick: () -> Unit
)

@Composable
fun AdminView(
    nav: AdminNav
) {
    AdminScreen(nav = nav)
}

@Composable
fun AdminScreen(
    nav: AdminNav
) {
    val green = Color(0xFF0F6B5C)
    val orange = Color(0xFFF2A300)
    val teal = Color(0xFF1A9E8A)

    val actions = listOf(
        AdminAction(
            label = "Registrar Música",
            description = "Adicionar nova música ao hinário",
            icon = Icons.Filled.Edit,
            accentColor = orange,
            onClick = nav.register
        ),
        AdminAction(
            label = "Marcar Presença",
            description = "Registrar presença dos membros",
            icon = Icons.Filled.Person,
            accentColor = teal,
            onClick = { /* TODO */ }
        ),
        AdminAction(
            label = "Gerar Escala",
            description = "Criar escala mensal de atividades",
            icon = Icons.Filled.DateRange,
            accentColor = green,
            onClick = { /* TODO */ }
        ),
    )

    BaseScreen(
        tabName = "Administração",
        logoRes = R.drawable.ic_sarca_ipb,
        showBackArrow = true,
        onBackClick = nav.back
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            AdminHeaderCard(green = green, orange = orange)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Funcionalidades",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(actions) { action ->
                    AdminActionCard(action = action)
                }
            }
        }
    }
}

@Composable
private fun AdminHeaderCard(green: Color, orange: Color) {
    val cardBg = MaterialTheme.colorScheme.surfaceContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 48.dp)
                    .background(orange, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Painel Admin",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = green
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Gerencie os recursos da igreja",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun AdminActionCard(action: AdminAction) {
    val cardBg = MaterialTheme.colorScheme.surfaceContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = action.onClick),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = action.accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.label,
                    tint = action.accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = action.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = action.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    lineHeight = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        color = action.accentColor.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}