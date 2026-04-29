package com.mingeek.sudokusage.ui.screens.achievements

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mingeek.sudokusage.domain.achievement.AchievementProgress

@Composable
fun AchievementsScreen(
    viewModel: AchievementsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "업적",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.weight(1f))
            val unlocked = state.count { it.unlocked }
            Text(
                text = "$unlocked / ${state.size}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 12.dp),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            for (progress in state) {
                AchievementRow(progress)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AchievementRow(progress: AchievementProgress) {
    val colors = MaterialTheme.colorScheme
    val bg = if (progress.unlocked) colors.secondaryContainer else colors.surfaceVariant
    val fg = if (progress.unlocked) colors.onSecondaryContainer else colors.onSurfaceVariant.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (progress.unlocked) colors.secondary else colors.outline.copy(alpha = 0.25f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (progress.unlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                contentDescription = null,
                tint = if (progress.unlocked) colors.onSecondary else colors.onSurfaceVariant,
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = progress.achievement.name,
                style = MaterialTheme.typography.titleLarge,
                color = fg,
            )
            Text(
                text = progress.achievement.description,
                style = MaterialTheme.typography.bodyLarge,
                color = fg,
            )
        }
    }
}
