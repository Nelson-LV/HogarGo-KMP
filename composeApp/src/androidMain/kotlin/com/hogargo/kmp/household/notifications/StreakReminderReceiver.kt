package com.hogargo.kmp.household.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hogargo.kmp.MainActivity
import com.hogargo.kmp.R

class StreakReminderReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "household_updates"
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_BODY = "extra_body"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.getStringExtra(EXTRA_ID) ?: "streak_reminder"
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "🔥 ¡Mantén tu racha en HogarGo!"
        val body = intent?.getStringExtra(EXTRA_BODY) ?: "Tienes tareas pendientes por completar antes de las 8:00 pm."

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(CHANNEL_ID, "Novedades del hogar", NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        manager.notify(id.hashCode(), notification)
    }
}
