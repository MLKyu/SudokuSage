package com.mingeek.sudokusage.ui.screens.game.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mingeek.sudokusage.domain.board.GameStatus

@Composable
fun GameTopBar(
    difficultyLabel: String,
    mistakes: Int,
    mistakeLimit: Int?,
    elapsedMs: Long,
    status: GameStatus,
    onBack: () -> Unit,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mistakeText = if (mistakeLimit != null) "$mistakes/$mistakeLimit" else "$mistakes"
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = difficultyLabel,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "❌ $mistakeText",
            style = MaterialTheme.typography.bodyLarge,
            color = if (mistakeLimit != null && mistakes >= mistakeLimit)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = if (status == GameStatus.Paused) "—:—" else formatElapsed(elapsedMs),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        IconButton(
            onClick = onTogglePause,
            enabled = status == GameStatus.Playing || status == GameStatus.Paused,
        ) {
            if (status == GameStatus.Paused) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
            } else {
                Icon(Icons.Default.Pause, contentDescription = "Pause")
            }
        }
    }
}

private fun formatElapsed(ms: Long): String {
    val total = ms / 1000
    val m = total / 60
    val s = total % 60
    return "%d:%02d".format(m, s)
}
