package com.mingeek.sudokusage.ui.screens.daily

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.mingeek.sudokusage.domain.daily.DailyOutcome
import com.mingeek.sudokusage.domain.daily.DailyStatus
import com.mingeek.sudokusage.ui.screens.daily.components.CalendarGrid
import java.time.LocalDate

@Composable
fun DailyScreen(
    viewModel: DailyViewModel,
    onPlay: (LocalDate) -> Unit,
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
                text = "오늘의 도전",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        when (val s = state) {
            null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
            }
            else -> ReadyContent(status = s, onPlay = onPlay)
        }
    }
}

@Composable
private fun ReadyContent(status: DailyStatus, onPlay: (LocalDate) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TodayCard(status = status, onPlay = { onPlay(status.today) })
        StreakCard(current = status.currentStreak, best = status.bestStreak)
        Spacer(Modifier.height(8.dp))
        CalendarGrid(today = status.today, days = status.recentDays)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TodayCard(status: DailyStatus, onPlay: () -> Unit) {
    val (statusText, statusColor) = when (status.todayInfo.outcome) {
        DailyOutcome.Won -> "✅ 클리어" to MaterialTheme.colorScheme.onSecondaryContainer
        DailyOutcome.Failed -> "❌ 실패" to MaterialTheme.colorScheme.error
        DailyOutcome.NotPlayed -> "오늘 도전이 기다려요" to MaterialTheme.colorScheme.onPrimaryContainer
    }
    val bg = when (status.todayInfo.outcome) {
        DailyOutcome.Won -> MaterialTheme.colorScheme.secondaryContainer
        DailyOutcome.Failed -> MaterialTheme.colorScheme.errorContainer
        DailyOutcome.NotPlayed -> MaterialTheme.colorScheme.primaryContainer
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(20.dp),
    ) {
        Text(
            text = "${status.today}",
            style = MaterialTheme.typography.titleLarge,
            color = statusColor,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.headlineMedium,
            color = statusColor,
        )
        val elapsed = status.todayInfo.elapsedMs
        if (status.todayInfo.outcome != DailyOutcome.NotPlayed && elapsed != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "시간: ${formatTime(elapsed)}",
                style = MaterialTheme.typography.bodyLarge,
                color = statusColor,
            )
        }
        if (status.todayInfo.outcome == DailyOutcome.NotPlayed) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("도전 시작", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun StreakCard(current: Int, best: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(20.dp),
    ) {
        StreakStat(label = "현재 연속", value = current, modifier = Modifier.weight(1f))
        StreakStat(label = "최고 연속", value = best, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StreakStat(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "${value}일",
            style = MaterialTheme.typography.displayLarge,
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
