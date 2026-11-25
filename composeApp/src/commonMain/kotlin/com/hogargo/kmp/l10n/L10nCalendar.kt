package com.hogargo.kmp.l10n

import com.hogargo.kmp.calendar.CalendarError

// Textos del Calendario (ES/EN).

val L10n.calendarTitle get() = tr("Calendario", "Calendar")
val L10n.calendarSubtitle get() = tr("Un resumen cálido de los ritmos de tu mes.", "A warm summary of your month's rhythm.")
fun L10n.calendarMonthYear(month: Int, year: Int) = "${monthNames[month - 1]} $year"
val L10n.calendarPrevMonth get() = tr("Mes anterior", "Previous month")
val L10n.calendarNextMonth get() = tr("Mes siguiente", "Next month")
val L10n.calendarLegendEvents get() = tr("Eventos", "Events")
val L10n.calendarLegendBills get() = tr("Facturas", "Bills")
val L10n.calendarLegendTasks get() = tr("Tareas", "Tasks")
val L10n.calendarDayEvents get() = tr("Eventos", "Events")
val L10n.calendarDayBills get() = tr("Facturas que vencen", "Bills due")
val L10n.calendarDayTasks get() = tr("Tareas completadas", "Completed tasks")
val L10n.calendarDayEmpty get() = tr("No hay nada asignado a este día.", "Nothing is scheduled for this day.")
val L10n.calendarUpcoming get() = tr("Próximos Eventos", "Upcoming Events")
val L10n.calendarEventsEmpty get() = tr("No hay eventos programados.", "No events scheduled.")
val L10n.calendarAddEvent get() = tr("Nuevo Evento", "New Event")
val L10n.calendarEventNameLabel get() = tr("Nombre del evento", "Event name")
val L10n.calendarEventNameHint get() = tr("Ej. Cumpleaños de Leo, Cita médica", "E.g. Leo's birthday, Doctor appointment")
val L10n.calendarEventDateLabel get() = tr("Fecha", "Date")
val L10n.calendarEventTimeLabel get() = tr("Hora (24 h, HH:mm)", "Time (24 h, HH:mm)")
val L10n.calendarEventCreate get() = tr("Crear", "Create")
val L10n.calendarDeleteEvent get() = tr("Eliminar evento", "Delete event")
val L10n.calendarBillsDue get() = tr("Vencimiento de Facturas", "Bills Due")
val L10n.calendarDueToday get() = tr("¡Vence Hoy!", "Due Today!")
val L10n.calendarAddBill get() = tr("Nueva Factura", "New Bill")
val L10n.calendarBillNameLabel get() = tr("Nombre", "Name")
val L10n.calendarBillNameHint get() = tr("Ej: Internet", "E.g.: Internet")
val L10n.calendarBillDueDateLabel get() = tr("Fecha de vencimiento", "Due date")
val L10n.calendarBillCreate get() = tr("Agregar", "Add")
val L10n.calendarBillsEmpty
    get() = tr(
        "Aún no tienes facturas registradas. Toca + para añadir la primera.",
        "No bills yet. Tap + to add the first one.",
    )
val L10n.calendarDeleteBill get() = tr("Eliminar factura", "Delete bill")

fun L10n.calendarErrorMessage(error: CalendarError): String = when (error) {
    CalendarError.EMPTY_TITLE -> tr("Escribe un nombre", "Write a name")
    CalendarError.INVALID_AMOUNT -> tr("Ingresa un monto válido mayor a 0", "Enter a valid amount greater than 0")
    CalendarError.INVALID_TIME -> tr("Hora inválida. Usa HH:mm, por ejemplo 18:30", "Invalid time. Use HH:mm, e.g. 18:30")
}
