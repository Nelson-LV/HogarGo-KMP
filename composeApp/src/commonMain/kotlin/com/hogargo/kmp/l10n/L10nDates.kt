package com.hogargo.kmp.l10n

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

// Nombres de meses y dias, y formatos de fecha localizados (sin depender de la plataforma).

val L10n.monthNames: List<String>
    get() = if (isEnglish) {
        listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December",
        )
    } else {
        listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
        )
    }

private val L10n.monthShortNames: List<String>
    get() = if (isEnglish) {
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    } else {
        listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    }

/** Domingo primero: Dom..Sab / Sun..Sat. */
val L10n.daysOfWeekShort: List<String>
    get() = if (isEnglish) {
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    } else {
        listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
    }

private fun L10n.weekdayLong(day: DayOfWeek): String {
    val es = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
    val en = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    return (if (isEnglish) en else es)[day.ordinal]
}

/** "5 ene" / "5 Jan". */
fun L10n.shortDate(date: LocalDate): String = "${date.dayOfMonth} ${monthShortNames[date.monthNumber - 1]}"

/** "Lunes 5 de enero" / "Monday, January 5" (inicial en mayuscula). */
fun L10n.longDate(date: LocalDate): String {
    val month = monthNames[date.monthNumber - 1]
    val text = if (isEnglish) {
        "${weekdayLong(date.dayOfWeek)}, $month ${date.dayOfMonth}"
    } else {
        "${weekdayLong(date.dayOfWeek)} ${date.dayOfMonth} de ${month.lowercase()}"
    }
    return text.replaceFirstChar { it.uppercase() }
}
