package com.ipb.castelobranco.features.worshiphub.shared.presentation.components

import androidx.compose.ui.graphics.Color

data class SongContentRow(
    val id: Int,
    val songId: Int,
    val songName: String,
    val isPinned: Boolean,
    val chips: List<SongContentChip> = emptyList(),
)

data class SongContentChip(val text: String, val color: Color)
