package com.hogargo.kmp.household.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.hogargo.kmp.MainActivity
import com.hogargo.kmp.R
import com.hogargo.kmp.l10n.L10n

/**
 * @param askPermission lo aporta la Activity: lanza la solicitud de POST_NOTIFICATIONS (Android 13+).
 */
actual class HouseholdNotifier(
    private val context: Context,
    private val askPermission: () -> Unit,
) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    init {
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, L10n.channelName, NotificationManager.IMPORTANCE_DEFAULT),
        )
    }

    actual fun requestPermission() {
        if (!hasPermission()) askPermission()
    }

    actual fun show(id: String, title: String, body: String) {
        if (!hasPermission()) return
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

    private fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val CHANNEL_ID = "household_updates"
    }
}
