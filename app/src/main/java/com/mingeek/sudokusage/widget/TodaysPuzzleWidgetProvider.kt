package com.mingeek.sudokusage.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.mingeek.sudokusage.MainActivity
import com.mingeek.sudokusage.R
import com.mingeek.sudokusage.data.repo.DailyChallengeRepository
import com.mingeek.sudokusage.di.DiQualifiers
import com.mingeek.sudokusage.domain.daily.DailyOutcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import java.time.LocalDate

/**
 * Home-screen widget showing today's daily-challenge status + current streak.
 * Tapping it deep-links into the daily challenge for today.
 *
 * Refresh model:
 *  - on enable / system update broadcast
 *  - on demand via [ACTION_REFRESH] (e.g. after a daily completion)
 *  - no periodic schedule — battery friendly
 *
 * Coroutine lifecycle: we use the standard `goAsync()` pattern to keep the
 * receiver alive across the suspend, and dispatch on the app-scope singleton
 * so cancellation is tied to process lifetime (not a per-call orphan scope).
 */
class TodaysPuzzleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val pending = goAsync()
        try {
            val koin: Koin? = GlobalContext.getOrNull()
            if (koin == null) {
                // Cold-start corner case: receiver fired before Application.onCreate
                // initialized Koin. Render a placeholder so the widget isn't blank.
                for (id in appWidgetIds) {
                    appWidgetManager.updateAppWidget(id, placeholder(context))
                }
                pending.finish()
                return
            }

            val repo: DailyChallengeRepository = koin.get()
            val appScope: CoroutineScope = koin.get(DiQualifiers.ApplicationScope)
            appScope.launch {
                try {
                    val today = LocalDate.now()
                    val info = repo.get(today)
                    val outcome = when (info?.result) {
                        "Won" -> DailyOutcome.Won
                        "Failed" -> DailyOutcome.Failed
                        else -> DailyOutcome.NotPlayed
                    }
                    val streak = repo.currentStreakAsOf(today)
                    for (id in appWidgetIds) {
                        appWidgetManager.updateAppWidget(id, render(context, outcome, streak))
                    }
                } finally {
                    pending.finish()
                }
            }
        } catch (t: Throwable) {
            // Either Koin failed to resolve, or launch threw before scheduling.
            // Always release the BroadcastReceiver to avoid wakelock leak.
            pending.finish()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(
                ComponentName(context, TodaysPuzzleWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) onUpdate(context, mgr, ids)
        }
    }

    private fun render(context: Context, outcome: DailyOutcome, streak: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_todays_puzzle)
        val statusText = when (outcome) {
            DailyOutcome.Won -> context.getString(R.string.widget_status_won)
            DailyOutcome.Failed -> context.getString(R.string.widget_status_failed)
            DailyOutcome.NotPlayed -> context.getString(R.string.widget_status_not_played)
        }
        views.setTextViewText(R.id.widget_status, statusText)
        views.setTextViewText(
            R.id.widget_streak,
            context.getString(R.string.widget_streak_format, streak),
        )
        views.setOnClickPendingIntent(R.id.widget_root, launchIntent(context))
        return views
    }

    private fun placeholder(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_todays_puzzle)
        views.setTextViewText(R.id.widget_status, "—")
        views.setTextViewText(R.id.widget_streak, "—")
        views.setOnClickPendingIntent(R.id.widget_root, launchIntent(context))
        return views
    }

    private fun launchIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data = android.net.Uri.parse("sudokusage://daily")
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    companion object {
        const val ACTION_REFRESH = "com.mingeek.sudokusage.widget.REFRESH"
    }
}
