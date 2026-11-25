package com.hogargo.kmp.calendar

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.hogargo.kmp.core.newId
import com.hogargo.kmp.core.nowMillis
import com.hogargo.kmp.db.Bill
import com.hogargo.kmp.db.CalendarEvent
import com.hogargo.kmp.db.HogarGoDatabase
import com.hogargo.kmp.db.HouseholdTask
import com.hogargo.kmp.finance.FinanceRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Facturas y eventos del hogar. Las tareas completadas solo se leen:
 * // TODO(integracion tareas): cuando exista el modulo de tareas, crear aqui el enlace con "proxima tarea".
 */
class CalendarRepository(
    db: HogarGoDatabase,
    private val now: () -> Long = ::nowMillis,
) {
    private val q = db.calendarQueries
    private val tasks = db.taskQueries

    fun observeBills(householdId: String): Flow<List<Bill>> =
        q.billsByHousehold(householdId).asFlow().mapToList(Dispatchers.Default)

    fun observeEvents(householdId: String): Flow<List<CalendarEvent>> =
        q.eventsByHousehold(householdId).asFlow().mapToList(Dispatchers.Default)

    fun observeCompletedTasks(householdId: String): Flow<List<HouseholdTask>> =
        tasks.completedTasks(householdId).asFlow().mapToList(Dispatchers.Default)

    fun bills(householdId: String): List<Bill> = q.billsByHousehold(householdId).executeAsList()

    fun events(householdId: String): List<CalendarEvent> = q.eventsByHousehold(householdId).executeAsList()

    /** @throws CalendarException nombre vacio o monto no mayor a 0. */
    fun addBill(householdId: String, title: String, amountText: String, dueDate: LocalDate) {
        if (title.trim().isEmpty()) throw CalendarException(CalendarError.EMPTY_TITLE)
        val amount = FinanceRules.validAmount(amountText) ?: throw CalendarException(CalendarError.INVALID_AMOUNT)
        q.insertBill(newId(), householdId, title.trim(), amount, dueDate.toString(), now())
    }

    /** [timeText] vacio guarda el evento sin hora. @throws CalendarException titulo vacio u hora invalida. */
    fun addEvent(householdId: String, title: String, date: LocalDate, timeText: String) {
        if (title.trim().isEmpty()) throw CalendarException(CalendarError.EMPTY_TITLE)
        val time = if (timeText.isBlank()) null else CalendarRules.normalizeTime(timeText)
            ?: throw CalendarException(CalendarError.INVALID_TIME)
        q.insertEvent(newId(), householdId, title.trim(), date.toString(), time, now())
    }

    fun setBillPaid(householdId: String, billId: String, paid: Boolean) = q.setBillPaid(paid, billId, householdId)

    fun setEventDone(householdId: String, eventId: String, done: Boolean) = q.setEventDone(done, eventId, householdId)

    fun deleteBill(householdId: String, billId: String) = q.deleteBill(billId, householdId)

    fun deleteEvent(householdId: String, eventId: String) = q.deleteEvent(eventId, householdId)

    fun deleteAll(householdId: String) {
        q.deleteBillsByHousehold(householdId)
        q.deleteEventsByHousehold(householdId)
    }
}
