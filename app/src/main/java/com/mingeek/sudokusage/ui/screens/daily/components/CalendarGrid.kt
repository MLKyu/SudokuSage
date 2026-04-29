package com.mingeek.sudokusage.ui.screens.daily.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mingeek.sudokusage.domain.daily.DailyDayInfo
import com.mingeek.sudokusage.domain.daily.DailyOutcome
import java.time.LocalDate

private val DOW_LABELS = listOf("월", "화", "수", "목", "금", "토", "일")

/**
 * Renders [days] as a 5×7 calendar grid aligned to weekdays (Monday-first).
 * Days outside the data window or in the future appear as blanks.
 */
@Composable
fun CalendarGrid(
    today: LocalDate,
    days: List<DailyDayInfo>,
    modifier: Modifier = Modifier,
) {
    val byDate = remember(days) { days.associateBy { it.date } }
    val cells = remember(today) { calendarCells(today) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dow in DOW_LABELS) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = dow,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        for (week in cells.chunked(7)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (cellDate in week) {
                    DayCell(
                        date = cellDate,
                        info = cellDate?.let(byDate::get),
                        isToday = cellDate == today,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    info: DailyDayInfo?,
    isToday: Boolean,
    modifier: Modifier = Modifier,
) {
    val outcome = info?.outcome ?: DailyOutcome.NotPlayed
    val colors = MaterialTheme.colorScheme
    val (bg, fg) = when {
        date == null -> Color.Transparent to colors.onSurfaceVariant.copy(alpha = 0.3f)
        outcome == DailyOutcome.Won -> colors.secondary to colors.onSecondary
        outcome == DailyOutcome.Failed -> colors.error.copy(alpha = 0.6f) to colors.onError
        else -> colors.surfaceVariant to colors.onSurfaceVariant
    }
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(shape)
            .background(bg)
            .then(if (isToday) Modifier.border(2.dp, colors.primary, shape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (date != null) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = fg,
            )
        }
    }
}

/**
 * 35 cells (5 weeks × 7 days), Mon-Sun, ending in the week containing [today].
 * Cells in the future of [today] are null.
 */
private fun calendarCells(today: LocalDate): List<LocalDate?> {
    val todayDow = today.dayOfWeek.value
    val mondayOfThisWeek = today.minusDays((todayDow - 1).toLong())
    val firstCell = mondayOfThisWeek.minusWeeks(4L)
    return (0 until 35).map { i ->
        val d = firstCell.plusDays(i.toLong())
        if (d.isAfter(today)) null else d
    }
}
