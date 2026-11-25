package com.hogargo.kmp.calendar

import com.hogargo.kmp.db.Bill
import com.hogargo.kmp.db.CalendarEvent
import com.hogargo.kmp.db.HouseholdTask
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus

enum class CalendarError { EMPTY_TITLE, INVALID_AMOUNT, INVALID_TIME }

class CalendarException(val error: CalendarError) : Exception(error.name)

/** Punto de color bajo un dia del mes. */
enum class DayDot { BILL, BILL_OVERDUE, EVENT, TASK }

/** Reglas puras del calendario (sin base de datos). */
object CalendarRules {
    /** Primer dia del mes mostrado: el mes actual desplazado [offset] meses. */
    fun monthStart(today: LocalDate, offset: Int): LocalDate =
        LocalDate(today.year, today.monthNumber, 1).plus(offset, DateTimeUnit.MONTH)

    fun daysInMonth(firstOfMonth: LocalDate): Int =
        firstOfMonth.plus(1, DateTimeUnit.MONTH).let { next -> (next.toEpochDays() - firstOfMonth.toEpochDays()).toInt() }

    /**
     * Celdas del mes con la semana empezando en domingo: huecos (null) antes del dia 1,
     * los dias del mes y huecos hasta completar filas de 7.
     */
    fun monthCells(firstOfMonth: LocalDate): List<LocalDate?> {
        val offset = firstOfMonth.dayOfWeek.isoDayNumber % 7 // domingo = 0
        val days = daysInMonth(firstOfMonth)
        val total = ((offset + days + 6) / 7) * 7
        return List(total) { index ->
            val day = index - offset + 1
            if (day in 1..days) firstOfMonth.plus(day - 1, DateTimeUnit.DAY) else null
        }
    }

    /** Hasta 3 puntos en este orden: factura (roja si vencida), evento y tarea completada. */
    fun dotsFor(
        date: LocalDate,
        events: List<CalendarEvent>,
        bills: List<Bill>,
        tasks: List<HouseholdTask>,
        today: LocalDate,
    ): List<DayDot> {
        val iso = date.toString()
        val dots = mutableListOf<DayDot>()
        val dayBills = bills.filter { it.dueDate == iso && !it.paid }
        if (dayBills.isNotEmpty()) {
            dots += if (dayBills.any { LocalDate.parse(it.dueDate) <= today }) DayDot.BILL_OVERDUE else DayDot.BILL
        }
        if (events.any { it.date == iso }) dots += DayDot.EVENT
        if (tasks.any { it.completedDate == iso }) dots += DayDot.TASK
        return dots
    }

    /** Eventos de hoy en adelante, por fecha y luego por hora. */
    fun upcomingEvents(events: List<CalendarEvent>, today: LocalDate): List<CalendarEvent> =
        events.filter { it.date >= today.toString() }
            .sortedWith(compareBy({ it.date }, { it.timeLabel.orEmpty() }, { it.createdAt }))

    /** El comportamiento de Android: sin pagar y con fecha de hoy o anterior muestra "Vence Hoy". */
    fun isDueNow(bill: Bill, today: LocalDate): Boolean = !bill.paid && LocalDate.parse(bill.dueDate) <= today

    /** Hora 24 h: acepta "9:30" o "09:30"; vacio es valido (evento sin hora). Devuelve "HH:mm" o null. */
    fun normalizeTime(text: String): String? {
        val trimmed = text.trim()
        val parts = trimmed.split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (parts[1].length != 2 || h !in 0..23 || m !in 0..59) return null
        return "${h.toString().padStart(2, '0')}:${parts[1]}"
    }
}
