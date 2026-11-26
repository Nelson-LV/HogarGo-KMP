package com.hogargo.kmp.household.notifications

import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

actual class HouseholdNotifier {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    // El centro guarda el delegate como referencia debil: lo retenemos aqui.
    private val delegate = ForegroundPresenter()

    init {
        center.delegate = delegate
    }

    actual fun requestPermission() {
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
        ) { _, _ -> }
    }

    actual fun show(id: String, title: String, body: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
            setThreadIdentifier(THREAD_ID)
        }
        center.addNotificationRequest(
            UNNotificationRequest.requestWithIdentifier(id, content, null),
            withCompletionHandler = null,
        )
    }

    actual fun scheduleStreakReminder(id: String, title: String, body: String, hour: Int, minute: Int) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())
            setThreadIdentifier(THREAD_ID)
        }
        val dateComponents = platform.Foundation.NSDateComponents().apply {
            setHour(hour.toLong())
            setMinute(minute.toLong())
        }
        val trigger = platform.UserNotifications.UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents,
            repeats = true,
        )
        val request = UNNotificationRequest.requestWithIdentifier(id, content, trigger)
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    private companion object {
        const val THREAD_ID = "household_updates"
    }
}

/** Muestra el aviso tambien con la app abierta. Tocarlo solo abre la app. */
private class ForegroundPresenter : NSObject(), UNUserNotificationCenterDelegateProtocol {
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit,
    ) {
        withCompletionHandler(
            UNNotificationPresentationOptionBanner or UNNotificationPresentationOptionList or
                UNNotificationPresentationOptionSound,
        )
    }
}
