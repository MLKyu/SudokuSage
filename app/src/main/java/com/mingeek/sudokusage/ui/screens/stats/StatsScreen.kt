package com.mingeek.sudokusage.ui.screens.stats

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mingeek.sudokusage.R
import com.mingeek.sudokusage.data.repo.StatsSummary
import com.mingeek.sudokusage.domain.board.Difficulty

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
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
                text = stringResource(R.string.action_stats),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            WinsBarChart(state = state)
            for (d in Difficulty.values()) {
                StatsCard(difficulty = d, summary = state[d])
            }
        }
    }
}

@Composable
private fun WinsBarChart(state: Map<Difficulty, StatsSummary>) {
    val difficulties = Difficulty.values()
    val maxWins = difficulties.maxOf { (state[it]?.gamesWon ?: 0) }.coerceAtLeast(1)
    val colors = MaterialTheme.colorScheme
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = colors.onSurfaceVariant, fontSize = 11.sp)
    val countStyle = TextStyle(color = colors.onSurface, fontSize = 12.sp)
    val barColor = colors.secondary
    val emptyColor = colors.outline.copy(alpha = 0.18f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = "난이도별 승리",
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val n = difficulties.size
                val gap = size.width * 0.04f
                val barWidth = (size.width - gap * (n + 1)) / n
                val baseY = size.height - 28f  // leave room for label
                for ((i, d) in difficulties.withIndex()) {
                    val wins = state[d]?.gamesWon ?: 0
                    val ratio = wins.toFloat() / maxWins
                    val barHeight = (baseY - 24f) * ratio
                    val x = gap + i * (barWidth + gap)
                    val color: Color = if (wins == 0) emptyColor else barColor
                    drawRect(
                        color = color,
                        topLeft = Offset(x, baseY - barHeight),
                        size = Size(barWidth, barHeight.coerceAtLeast(2f)),
                    )
                    val countLayout = measurer.measure(AnnotatedString(wins.toString()), countStyle)
                    drawText(
                        textLayoutResult = countLayout,
                        topLeft = Offset(
                            x + barWidth / 2f - countLayout.size.width / 2f,
                            (baseY - barHeight - countLayout.size.height - 4f).coerceAtLeast(0f),
                        ),
                    )
                    val label = shortLabel(d)
                    val labelLayout = measurer.measure(AnnotatedString(label), labelStyle)
                    drawText(
                        textLayoutResult = labelLayout,
                        topLeft = Offset(
                            x + barWidth / 2f - labelLayout.size.width / 2f,
                            baseY + 6f,
                        ),
                    )
                }
            }
        }
    }
}

private fun shortLabel(d: Difficulty): String = when (d) {
    Difficulty.Easy -> "쉬움"
    Difficulty.Medium -> "보통"
    Difficulty.Hard -> "어려움"
    Difficulty.Expert -> "전문가"
    Difficulty.Master -> "마스터"
    Difficulty.Extreme -> "익스트림"
}

@Composable
private fun StatsCard(difficulty: Difficulty, summary: StatsSummary?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Text(
            text = stringResource(difficultyLabelRes(difficulty)),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        if (summary == null) {
            Text(
                text = "아직 기록 없음",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            StatsRow("승리", "${summary.gamesWon} / ${summary.gamesStarted}")
            StatsRow("최고 기록", summary.bestTimeMs?.let(::formatTime) ?: "-")
            StatsRow("평균 시간", summary.avgTimeMs?.let(::formatTime) ?: "-")
            StatsRow("연속 승리", "${summary.currentStreak} (최고 ${summary.bestStreak})")
        }
    }
}

@Composable
private fun StatsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
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
