package com.mingeek.sudokusage.ui.screens.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun GameActionRow(
    canUndo: Boolean,
    canRedo: Boolean,
    noteMode: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onErase: () -> Unit,
    onToggleNote: () -> Unit,
    onHint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionButton(icon = Icons.AutoMirrored.Default.Undo, label = "되돌리기", enabled = canUndo, onClick = onUndo)
        ActionButton(icon = Icons.AutoMirrored.Default.Redo, label = "다시 실행", enabled = canRedo, onClick = onRedo)
        ActionButton(icon = Icons.AutoMirrored.Default.Backspace, label = "지우기", onClick = onErase)
        ActionButton(
            icon = Icons.Default.EditNote,
            label = if (noteMode) "노트 ON" else "노트",
            highlighted = noteMode,
            onClick = onToggleNote,
        )
        ActionButton(icon = Icons.Default.Lightbulb, label = "힌트", onClick = onHint)
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    highlighted: Boolean = false,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (highlighted) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
        ) {
            IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (highlighted) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
