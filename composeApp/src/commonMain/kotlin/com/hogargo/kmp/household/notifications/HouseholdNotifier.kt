package com.hogargo.kmp.household.notifications

import com.hogargo.kmp.db.HouseholdNotice
import com.hogargo.kmp.household.data.NoticeType
import com.hogargo.kmp.l10n.L10n

/**
 * Muestra los avisos del hogar como notificaciones locales ("Novedades del hogar").
 * Android: NotificationManager con canal propio. iOS: UNUserNotificationCenter.
 */
expect class HouseholdNotifier {
    /** Pide permiso de notificaciones (solo pregunta si aun no se ha decidido). */
    fun requestPermission()

    /** Si no hay permiso no se muestra nada; el aviso igual queda como entregado. */
    fun show(id: String, title: String, body: String)

    /** Programa un recordatorio recurrente para la racha diaria a la hora especificada (por defecto 8:00 PM). */
    fun scheduleStreakReminder(id: String, title: String, body: String, hour: Int = 20, minute: Int = 0)
}

/** Titulo y cuerpo del aviso, o null si no se debe notificar (REMOVED o tipo desconocido). */
fun noticeText(notice: HouseholdNotice): Pair<String, String>? {
    val a = notice.arg1
    val b = notice.arg2
    return when (NoticeType.entries.firstOrNull { it.name == notice.type }) {
        NoticeType.RENAMED -> L10n.tr("Cambió el nombre del hogar", "The home was renamed") to
            L10n.tr("«$a» ahora se llama «$b»", "“$a” is now called “$b”")
        NoticeType.JOIN_REQUEST -> L10n.tr("Nueva solicitud de ingreso", "New join request") to
            L10n.tr("$a quiere unirse a $b. Acéptalo desde tu perfil.", "$a wants to join $b. Accept them from your profile.")
        NoticeType.JOINED -> L10n.tr("Nuevo integrante", "New member") to
            L10n.tr("$a se unió al hogar.", "$a joined the home.")
        NoticeType.LEFT -> L10n.tr("Alguien salió del hogar", "Someone left the home") to
            L10n.tr("$a salió del hogar.", "$a left the home.")
        NoticeType.MEMBER_REMOVED -> L10n.tr("Cambios en el hogar", "Home changes") to
            L10n.tr("$a fue eliminado del hogar.", "$a was removed from the home.")
        NoticeType.ADMIN -> L10n.tr("Ahora eres administrador", "You're now the admin") to
            L10n.tr(
                "Quien administraba $a salió, y ahora tú eres el admin.",
                "The person who managed $a left, and now you're the admin.",
            )
        NoticeType.REMOVED, null -> null
    }
}
