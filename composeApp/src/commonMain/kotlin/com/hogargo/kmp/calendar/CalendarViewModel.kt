package com.hogargo.kmp.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hogargo.kmp.core.todayLocal
import com.hogargo.kmp.db.Bill
import com.hogargo.kmp.db.CalendarEvent
import com.hogargo.kmp.db.HouseholdTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

/** Una celda del mes: null = hueco. */
data class DayCell(val date: LocalDate, val dots: List<DayDot>, val isToday: Boolean, val isSelected: Boolean)

data class CalendarUiState(
    val today: LocalDate,
    val monthStart: LocalDate,
    val cells: List<DayCell?> = emptyList(),
    val selected: LocalDate = today,
    val selectedEvents: List<CalendarEvent> = emptyList(),
    val selectedBills: List<Bill> = emptyList(),
    val selectedTasks: List<HouseholdTask> = emptyList(),
    val upcoming: List<CalendarEvent> = emptyList(),
    val bills: List<Bill> = emptyList(),
)

class CalendarViewModel(
    private val repository: CalendarRepository,
    private val householdId: String,
    private val today: () -> LocalDate = ::todayLocal,
) : ViewModel() {

    // El mes mostrado se guarda como desplazamiento respecto al mes actual.
    private val monthOffset = MutableStateFlow(0)
    private val selected = MutableStateFlow(today())

    val state: StateFlow<CalendarUiState> = combine(
        repository.observeEvents(householdId),
        repository.observeBills(householdId),
        repository.observeCompletedTasks(householdId),
        monthOffset,
        selected,
    ) { events, bills, tasks, offset, sel -> build(events, bills, tasks, offset, sel) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialState())

    fun previousMonth() = monthOffset.update { it - 1 }

    fun nextMonth() = monthOffset.update { it + 1 }

    fun select(date: LocalDate) {
        selected.value = date
    }

    /** Crea el evento y salta a su fecha. Devuelve el error o null. */
    fun addEvent(title: String, date: LocalDate, time: String): CalendarError? = attempt {
        repository.addEvent(householdId, title, date, time)
        selected.value = date
        monthOffset.value = monthsBetween(today(), date)
    }

    fun addBill(title: String, amount: String, due: LocalDate): CalendarError? =
        attempt { repository.addBill(householdId, title, amount, due) }

    fun setEventDone(id: String, done: Boolean) = repository.setEventDone(householdId, id, done)

    fun setBillPaid(id: String, paid: Boolean) = repository.setBillPaid(householdId, id, paid)

    fun deleteEvent(id: String) = repository.deleteEvent(householdId, id)

    fun deleteBill(id: String) = repository.deleteBill(householdId, id)

    private fun build(
        events: List<CalendarEvent>,
        bills: List<Bill>,
        tasks: List<HouseholdTask>,
        offset: Int,
        sel: LocalDate,
    ): CalendarUiState {
        val now = today()
        val start = CalendarRules.monthStart(now, offset)
        val cells = CalendarRules.monthCells(start).map { date ->
            date?.let { DayCell(it, CalendarRules.dotsFor(it, events, bills, tasks, now), it == now, it == sel) }
        }
        val iso = sel.toString()
        return CalendarUiState(
            today = now,
            monthStart = start,
            cells = cells,
            selected = sel,
            selectedEvents = events.filter { it.date == iso }.sortedBy { it.timeLabel.orEmpty() },
            selectedBills = bills.filter { it.dueDate == iso },
            selectedTasks = tasks.filter { it.completedDate == iso },
            upcoming = CalendarRules.upcomingEvents(events, now),
            bills = bills,
        )
    }

    private fun initialState(): CalendarUiState {
        val now = today()
        return CalendarUiState(today = now, monthStart = CalendarRules.monthStart(now, 0), selected = now)
    }

    private fun monthsBetween(from: LocalDate, to: LocalDate): Int =
        (to.year - from.year) * 12 + (to.monthNumber - from.monthNumber)

    private fun attempt(block: () -> Unit): CalendarError? = try {
        block()
        null
    } catch (e: CalendarException) {
        e.error
    }
}
