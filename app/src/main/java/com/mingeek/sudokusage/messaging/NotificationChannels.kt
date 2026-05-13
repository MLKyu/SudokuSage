package com.mingeek.sudokusage.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.mingeek.sudokusage.R

object NotificationChannels {

    const val DEFAULT_ID = "default"

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(DEFAULT_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                DEFAULT_ID,
                context.getString(R.string.notif_channel_default_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notif_channel_default_description)
            }
        )
    }
}
