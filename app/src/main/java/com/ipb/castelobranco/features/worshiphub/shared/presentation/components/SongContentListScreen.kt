package com.ipb.castelobranco.features.worshiphub.shared.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ipb.castelobranco.R
import com.ipb.castelobranco.core.presentation.base.BaseScreen
import com.ipb.castelobranco.core.presentation.components.ElasticPullToRefresh
import com.ipb.castelobranco.core.presentation.theme.BrandColors

private val Orange = BrandColors.Orange
private val Green = BrandColors.Green

/**
 * List screen shared by the chord charts and lyrics sub-features. Both render the same layout;
 * only labels, accent color, leading icon and the optional metadata chips differ.
 */
@Composable
fun SongContentListScreen(
    tabName: String,
    searchPlaceholder: String,
    addItemLabel: String,
    accentColor: Color,
    leadingIcon: ImageVector,
    rows: List<SongContentRow>,
    query: String,
    isLoading: Boolean,
    error: String?,
    isAdmin: Boolean,
    isRefreshing: Boolean,
    onQueryChange: (String) -> Unit,
    onItemClick: (id: Int) -> Unit,
    onTogglePin: (songId: Int) -> Unit,
    onRefresh: () -> Unit,
    onCreateClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    BaseScreen(
        tabName       = tabName,
        logoRes       = R.drawable.ic_sarca_ipb,
        showBackArrow = true,
        onBackClick   = onBackClick,
        extraActions  = {
            if (isAdmin) {
                AdminOverflowMenu(addItemLabel = addItemLabel, onCreateClick = onCreateClick)
            }
        },
    ) { innerPadding ->
        ElasticPullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh    = onRefresh,
            modifier     = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Spacer(modifier = Modifier.height(6.dp))
                SearchCard(
                    query         = query,
                    onQueryChange = onQueryChange,
                    placeholder   = searchPlaceholder,
                    resultsCount  = rows.size,
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (rows.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(rows) { item ->
                            SongContentCard(
                                item        = item,
                                accentColor = accentColor,
                                leadingIcon = leadingIcon,
                                onClick     = { onItemClick(item.id) },
                                onTogglePin = { onTogglePin(item.songId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchCard(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    resultsCount: Int,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Pesquisar",
                tint = Green,
                modifier = Modifier.size(22.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(Green),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            MetadataChip(text = "$resultsCount", color = Green)

            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@Composable
private fun SongContentCard(
    item: SongContentRow,
    accentColor: Color,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = item.songName,
                    color      = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    style      = MaterialTheme.typography.titleMedium,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )

                if (item.chips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item.chips.forEach { chip ->
                            MetadataChip(text = chip.text, color = chip.color)
                        }
                    }
                }
            }

            IconButton(onClick = onTogglePin) {
                if (item.isPinned) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Orange),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text       = text,
            color      = color,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text  = "No results",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun AdminOverflowMenu(addItemLabel: String, onCreateClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector        = Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text    = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector        = Icons.Default.Add,
                            contentDescription = null,
                            modifier           = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(addItemLabel)
                    }
                },
                onClick = { expanded = false; onCreateClick() },
            )
        }
    }
}
