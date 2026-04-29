package com.mingeek.sudokusage.ui.screens.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mingeek.sudokusage.R
import com.mingeek.sudokusage.domain.board.Difficulty
import com.mingeek.sudokusage.domain.board.VariantId
import com.mingeek.sudokusage.domain.daily.DailyOutcome

// Killer is intentionally excluded from this list pending a uniqueness-guaranteed
// cage-layout generator (the empty-board flood-fill bank produces multi-solution
// puzzles for cage size ≥ 3 in reasonable time, which would let the engine flag a
// user's legit alternate solution as wrong). All Killer infra (RuleSet, generator,
// codec, board renderer) ships ready; flip the row back on once the bank is fixed.
private val VARIANT_PICKER = listOf(
    VariantId.Classic to "9×9 Classic",
    VariantId.Mini6 to "6×6 미니",
    VariantId.Mini4 to "4×4 미니",
    VariantId.XSudoku to "X 스도쿠",
    VariantId.Hyper to "하이퍼",
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPickDifficulty: (VariantId, Difficulty) -> Unit,
    onContinue: () -> Unit,
    onOpenDaily: () -> Unit,
    onOpenTrainer: () -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top-right action icons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            CircleIcon(icon = Icons.Default.EmojiEvents, contentDescription = "업적", onClick = onOpenAchievements)
            Spacer(Modifier.size(8.dp))
            CircleIcon(icon = Icons.Default.BarChart, contentDescription = stringResource(R.string.action_stats), onClick = onOpenStats)
            Spacer(Modifier.size(8.dp))
            CircleIcon(icon = Icons.Default.Settings, contentDescription = stringResource(R.string.action_settings), onClick = onOpenSettings)
        }
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))

        state.savedGame?.let { saved ->
            ContinueCard(
                difficulty = saved.difficulty,
                elapsedMs = saved.elapsedMs,
                onClick = onContinue,
            )
            Spacer(Modifier.height(12.dp))
        }

        DailyEntryCard(
            outcome = state.dailyOutcomeToday,
            currentStreak = state.currentStreak,
            onClick = onOpenDaily,
        )
        Spacer(Modifier.height(12.dp))

        TrainerEntryCard(onClick = onOpenTrainer)
        Spacer(Modifier.height(24.dp))

        Text(
            text = if (state.savedGame == null) "난이도 선택" else "새 게임",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
        )
        Spacer(Modifier.height(8.dp))

        var selectedVariant by remember { mutableStateOf(VariantId.Classic) }
        val selectedIndex = VARIANT_PICKER.indexOfFirst { it.first == selectedVariant }
        ScrollableTabRow(
            selectedTabIndex = selectedIndex.coerceAtLeast(0),
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            VARIANT_PICKER.forEach { (variant, label) ->
                Tab(
                    selected = variant == selectedVariant,
                    onClick = { selectedVariant = variant },
                    text = { Text(label) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            for (d in Difficulty.values()) {
                DifficultyCard(difficulty = d, onClick = { onPickDifficulty(selectedVariant, d) })
            }
        }
    }
}

@Composable
private fun CircleIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DailyEntryCard(
    outcome: DailyOutcome,
    currentStreak: Int,
    onClick: () -> Unit,
) {
    val statusText = when (outcome) {
        DailyOutcome.Won -> "오늘 완료 ✓"
        DailyOutcome.Failed -> "오늘 실패"
        DailyOutcome.NotPlayed -> "오늘 도전 시작"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "오늘의 도전",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        if (currentStreak > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = "${currentStreak}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun TrainerEntryCard(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.School,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "기법 학습",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Text(
                text = "현자의 4가지 핵심 기법",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun ContinueCard(difficulty: Difficulty, elapsedMs: Long, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.action_continue),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = "${stringResource(difficultyLabelRes(difficulty))} • ${formatTime(elapsedMs)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun DifficultyCard(difficulty: Difficulty, onClick: () -> Unit) {
    val (labelRes, dotCount) = when (difficulty) {
        Difficulty.Easy -> R.string.difficulty_easy to 1
        Difficulty.Medium -> R.string.difficulty_medium to 2
        Difficulty.Hard -> R.string.difficulty_hard to 3
        Difficulty.Expert -> R.string.difficulty_expert to 4
        Difficulty.Master -> R.string.difficulty_master to 5
        Difficulty.Extreme -> R.string.difficulty_extreme to 6
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 1..6) {
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (i <= dotCount) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )
                        .padding(4.dp)
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val total = ms / 1000
    val m = total / 60
    val s = total % 60
    return "%d:%02d".format(m, s)
}

private fun difficultyLabelRes(d: Difficulty): Int = when (d) {
    Difficulty.Easy -> R.string.difficulty_easy
    Difficulty.Medium -> R.string.difficulty_medium
    Difficulty.Hard -> R.string.difficulty_hard
    Difficulty.Expert -> R.string.difficulty_expert
    Difficulty.Master -> R.string.difficulty_master
    Difficulty.Extreme -> R.string.difficulty_extreme
}
